package net.thaumcraft.occulta.enslave;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.mixin.MobGoalAccessor;
import net.thaumcraft.occulta.OccultaEffects;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * O <b>Escravizado</b>: o {@code PotionEnslaved} do Witchery.
 *
 * <p>Não é domar. Um bicho escravizado continua o bicho que era — o zumbi continua zumbi, e morde quem
 * encontrar. O que muda são duas coisas, e só essas duas:
 *
 * <ol>
 *   <li><b>ele nunca mais escolhe o escravizador por alvo</b> — e se já o tinha, larga;</li>
 *   <li><b>ele briga as brigas do escravizador</b>: quem bater em quem o escravizou passa a ser alvo dele.</li>
 * </ol>
 *
 * <p>É por isso que ele não tem prazo: o efeito do original é <b>infinito</b>, e está lá só para ser
 * perguntado. Não dá velocidade, não dá dano, não aparece como coisa boa nem como coisa má — é um laço, e um
 * laço não acaba sozinho.
 *
 * <p>Isto é o que faltava ao <b>Cozimento da Ressurreição</b>: no original, quem levanta os mortos fica dono
 * deles, e sem este laço o frasco era uma arma que mordia quem a atirava.
 *
 * <p><b>Quem não se escraviza</b>, e são os do original: os <b>chefes</b>, os <b>golens</b>, as <b>bruxas</b>,
 * os <b>entes</b> — e, no original, também o demônio e o diabrete, que este porte ainda não tem.
 *
 * <p><b>Mudança declarada:</b> o original guarda o <b>nome</b> de quem escravizou, e acha a pessoa pelo nome.
 * Aqui se guarda o <b>UUID</b>. É estritamente melhor: um nome muda, e no original um bicho escravizado por
 * alguém que trocasse de nome ficava preso a um nome que não existia mais. Está no {@code PORTE.md}.
 */
public final class Enslavement {
    /**
     * De quanto em quanto se olha se a vontade já está posta: as vinte batidas do original.
     *
     * <p>O original faz isso no {@code onLivingUpdate} do efeito, com {@code getTotalWorldTime() % 20 == 3}.
     * Aqui é a batida do próprio efeito, que é o mesmo relógio visto do outro lado.
     */
    public static final int DE_QUANTO_EM_QUANTO = 20;

    /** Quem escravizou este bicho. */
    public static final AttachmentType<UUID> DONO = AttachmentRegistry.<UUID>builder()
            .initializer(() -> null)
            .persistent(UUIDUtil.CODEC)
            .buildAndRegister(Thaumcraft.id("enslaver"));

    private Enslavement() {
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o apego. */
    public static void init() {
    }

    /**
     * Põe o laço: o {@code setEnslaverForMob}.
     *
     * <p>Devolve <b>falso</b> se não houve mudança — ou porque não há bicho nem gente, ou porque <b>já era
     * desta mesma pessoa</b>. Escravizar de novo o que já é seu não é um laço novo, e o original devolve falso
     * de propósito: é assim que o Cozimento da Ressurreição não gasta duas vezes com o mesmo morto.
     */
    public static boolean escraviza(@Nullable Mob bicho, @Nullable Player quem) {
        if (bicho == null || quem == null) return false;

        UUID jáEra = bicho.getAttached(DONO);
        if (quem.getUUID().equals(jáEra)) return false;

        bicho.setAttached(DONO, quem.getUUID());
        bicho.addEffect(new MobEffectInstance(OccultaEffects.ENSLAVED, MobEffectInstance.INFINITE_DURATION));
        // o dropAttackTarget: ele larga o que tivesse em mira
        bicho.setTarget(null);
        bicho.setLastHurtByMob(null);
        return true;
    }

    /** Se este bicho tem dono. */
    public static boolean escravizado(@Nullable Entity bicho) {
        return bicho != null && bicho.hasAttached(DONO);
    }

    /** E quem é esse dono, se há algum. */
    @Nullable
    public static UUID dono(@Nullable Entity bicho) {
        return bicho == null ? null : bicho.getAttached(DONO);
    }

    /** Se este bicho é escravo desta pessoa: o {@code isMobEnslavedBy}. */
    public static boolean escravoDe(@Nullable Entity bicho, @Nullable Player quem) {
        return bicho != null && quem != null && quem.getUUID().equals(bicho.getAttached(DONO));
    }

    /** Quem escravizou este bicho, se ainda está no mundo. */
    @Nullable
    public static Player donoDe(ServerLevel level, Entity bicho) {
        UUID quem = dono(bicho);
        return quem == null ? null : level.getPlayerByUUID(quem);
    }

    /**
     * Se este bicho se pode escravizar: o {@code canCreatureBeEnslaved}.
     *
     * <p><b>Mudança declarada:</b> o original pergunta {@code instanceof IBossDisplayData}, que é a interface
     * da barra de chefe de 2014. Hoje não há interface nem etiqueta de chefe: o que há são os dois chefes do
     * jogo, e é por eles que se pergunta. Está no {@code PORTE.md}.
     */
    public static boolean podeSerEscravizado(@Nullable LivingEntity bicho) {
        if (!(bicho instanceof Mob)) return false;
        if (bicho instanceof EnderDragon || bicho instanceof WitherBoss) return false;
        if (bicho instanceof net.minecraft.world.entity.animal.golem.AbstractGolem) return false;
        if (bicho instanceof Witch) return false;
        if (bicho instanceof net.thaumcraft.occulta.coven.CovenWitchEntity) return false;
        return !(bicho instanceof net.thaumcraft.occulta.EntEntity);
    }

    /**
     * A batida do efeito: põe a vontade de brigar as brigas do dono, se ela ainda não está posta.
     *
     * <p>O original varre a lista de alvos à procura dela a cada vinte batidas, e é o que se faz aqui — porque
     * um bicho pode nascer, ser escravizado, e só depois ter a lista de alvos trocada por outra coisa (uma
     * invocação, um domador), e nesse caso a vontade tem de voltar.
     */
    public static void tick(ServerLevel level, LivingEntity bicho) {
        if (!(bicho instanceof PathfinderMob andarilho)) return;
        if (!escravizado(andarilho)) return;

        var alvos = ((MobGoalAccessor) andarilho).thaumcraft$targetSelector();
        boolean jáTem = alvos.getAvailableGoals().stream()
                .anyMatch(g -> g.getGoal() instanceof EnslaverHurtByTargetGoal);
        if (jáTem) return;

        alvos.addGoal(1, new EnslaverHurtByTargetGoal(andarilho));
    }
}
