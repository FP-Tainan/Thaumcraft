package net.thaumcraft.occulta;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.arcana.Mana;

/**
 * As poções que mexem no <b>dano</b>: o {@code IHandleLivingHurt} do Witchery, que era um punhado de ganchos
 * no {@code LivingHurtEvent} do Forge.
 *
 * <p>Elas estão todas aqui e não cada uma na sua classe porque todas se perguntam no <b>mesmo instante</b> —
 * entre o golpe e a vida —, e porque a ordem entre elas importa: o que uma tira, a seguinte já não vê.
 *
 * <p>A ordem é a do original, que é a de registro das poções:
 *
 * <ol>
 *   <li><b>Enregelado</b> e <b>Enrolado em Vinha</b> mexem no que o <b>fogo</b> faz — um a menos, ou até
 *       quatro vezes mais;</li>
 *   <li><b>Absorver Magia</b> come um quinto por grau do dano <b>mágico</b> e o vira mana;</li>
 *   <li><b>Refletir Dano</b> manda uma parte de volta a quem bateu;</li>
 *   <li><b>Repelir Agressor</b> empurra quem bateu de perto;</li>
 *   <li>e <b>Não Sentir Dor</b> paga o resto com <b>fome</b> em vez de vida.</li>
 * </ol>
 */
public final class OccultaHurt {
    /** O quanto Absorver Magia come por grau: um quinto. */
    public static final float ABSORVE = 0.2f;

    /** E o quanto Refletir Dano manda de volta por grau: um décimo. */
    public static final float REFLETE = 0.1f;

    /** A que distância o Repelir Agressor alcança, ao quadrado: os três blocos do original. */
    public static final double ALCANCE_DO_EMPURRÃO = 9.0;

    /** Quanto de mana vale cada ponto de dano mágico comido. */
    public static final float MANA_POR_DANO = 10.0f;

    private OccultaHurt() {
    }

    /** O que sobra do golpe depois de as poções do ofício lhe mexerem. */
    public static float hurt(LivingEntity quem, DamageSource fonte, float dano) {
        if (!(quem.level() instanceof ServerLevel level)) return dano;
        if (dano <= 0.0f) return dano;

        dano = fogo(quem, fonte, dano);
        dano = absorveMagia(level, quem, fonte, dano);
        dano = reflete(level, quem, fonte, dano);
        empurra(level, quem, fonte);
        dano = fome(quem, fonte, dano);
        // e a pancada de um lobisomem em forma de bicho, que corria no mesmo gancho do original
        dano = net.thaumcraft.occulta.wolf.WerewolfHooks.pancada(fonte, dano);
        return dano;
    }

    /**
     * O <b>Enregelado</b> e o <b>Enrolado em Vinha</b>, que puxam o fogo para lados opostos.
     *
     * <p>O gelo tira um por grau, e do terceiro em diante não deixa passar nada. A vinha <b>multiplica</b>,
     * até quatro vezes — porque uma vinha em volta do corpo é o pior lugar para se estar quando algo pega
     * fogo.
     */
    private static float fogo(LivingEntity quem, DamageSource fonte, float dano) {
        if (!fonte.is(DamageTypeTags.IS_FIRE)) return dano;

        var vinha = quem.getEffect(OccultaEffects.WRAPPED_IN_VINE);
        if (vinha != null) dano *= Math.min(vinha.getAmplifier() + 1, 4);

        var gelo = quem.getEffect(OccultaEffects.CHILLED);
        if (gelo != null) {
            int grau = gelo.getAmplifier();
            float piso = grau >= 2 ? 0.0f : Math.min(dano, 1.0f);
            dano = Math.max(dano - (1 + grau), piso);
        }
        return dano;
    }

    /**
     * <b>Absorver Magia</b>: um quinto por grau do dano mágico vira <b>mana</b>.
     *
     * <p>No original o que ele enche é a energia de infusão; aqui é a mana do Ars Arcana, que é o mesmo poço
     * — como no virote anulador, e pela mesma razão. <b>Declarado no {@code PORTE.md}.</b>
     */
    private static float absorveMagia(ServerLevel level, LivingEntity quem, DamageSource fonte, float dano) {
        var come = quem.getEffect(OccultaEffects.ABSORB_MAGIC);
        if (come == null || !éMágico(fonte)) return dano;

        float comido = dano * ABSORVE * (come.getAmplifier() + 1);
        dano -= comido;

        if (quem instanceof Player gente && comido > 1.0f) {
            Mana conta = Mana.of(gente);
            if (conta.maxMana() > 0.0f) {
                Mana.set(gente, conta.withMana(
                        Math.min(conta.mana() + comido * MANA_POR_DANO, conta.maxMana())));
            }
        }
        return dano;
    }

    /** O que o original chama de dano mágico. */
    private static boolean éMágico(DamageSource fonte) {
        return fonte.is(net.minecraft.world.damagesource.DamageTypes.MAGIC)
                || fonte.is(net.minecraft.world.damagesource.DamageTypes.INDIRECT_MAGIC);
    }

    /**
     * <b>Refletir Dano</b>: um décimo por grau volta para quem bateu, e sai do que chegou.
     *
     * <p>De <b>perto</b> conta um grau a mais; de longe, só do terceiro grau em diante vale alguma coisa — um
     * espelho não devolve uma flecha sem um bom motivo.
     */
    private static float reflete(ServerLevel level, LivingEntity quem, DamageSource fonte, float dano) {
        var espelho = quem.getEffect(OccultaEffects.REFLECT_DAMAGE);
        if (espelho == null) return dano;
        if (!(fonte.getEntity() instanceof LivingEntity quemBateu) || quemBateu == quem) return dano;

        boolean deLonge = !fonte.isDirect();
        int grau = espelho.getAmplifier();
        if (deLonge && grau < 2) return dano;

        float volta = (float) Math.ceil(dano * REFLETE * (grau + (deLonge ? 0 : 1)));
        if (volta <= 0.0f) return dano;
        quemBateu.hurtServer(level, fonte, volta);
        return dano - volta;
    }

    /** <b>Repelir Agressor</b>: quem bate de perto sai de perto. */
    private static void empurra(ServerLevel level, LivingEntity quem, DamageSource fonte) {
        var empurrão = quem.getEffect(OccultaEffects.REPELL_ATTACKER);
        if (empurrão == null) return;
        if (!(fonte.getEntity() instanceof LivingEntity quemBateu) || quemBateu == quem) return;
        if (!fonte.isDirect()) return;
        if (quemBateu.distanceToSqr(quem) >= ALCANCE_DO_EMPURRÃO) return;

        int grau = empurrão.getAmplifier();
        Vec3 para = quemBateu.position().subtract(quem.position());
        double plano = Math.sqrt(para.x * para.x + para.z * para.z);
        if (plano < 1.0E-4) return;

        double força = 1.0 + grau * 0.75;
        double alto = 0.5 + grau * 0.2;
        quemBateu.push(para.x / plano * força, alto, para.z / plano * força);
        quemBateu.hurtMarked = true;
    }

    /**
     * <b>Não Sentir Dor</b>: o golpe de bicho ou de gente sai da <b>fome</b> antes de sair da vida.
     *
     * <p>Só de gente, e só de golpe vivo — fogo, queda e afogamento doem na mesma. É a poção de quem vai para
     * a briga sabendo que vai sair dela com fome em vez de sangue.
     */
    private static float fome(LivingEntity quem, DamageSource fonte, float dano) {
        var sem = quem.getEffect(OccultaEffects.FEEL_NO_PAIN);
        if (sem == null || !(quem instanceof Player gente)) return dano;
        if (!(fonte.getEntity() instanceof LivingEntity)) return dano;

        var fome = gente.getFoodData();
        int tem = fome.getFoodLevel();
        if (tem <= 0) return dano;

        float paga = Math.min(dano, tem);
        fome.setFoodLevel(tem - (int) Math.ceil(paga));
        return dano - paga;
    }
}
