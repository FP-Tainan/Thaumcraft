package net.thaumcraft.occulta.vampire;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.wolf.Lycanthropy;

/**
 * O que um vampiro <b>faz</b>: os {@code VampirePower} do {@code ExtendedPlayer}.
 *
 * <p>Um vampiro não tem botões: tem um <b>poder escolhido</b>, e o que ele faz com o mundo depende de qual
 * seja. Enquanto houver um poder escolhido, o clique direito deixa de abrir baús e passa a <b>ser</b> esse
 * poder — e é por isso que escolher <i>nenhum</i> é uma escolha legítima e a que ele passa mais tempo a
 * fazer.
 *
 * <p>Os quatro são: <b>beber</b>, a <b>visão</b>, a <b>velocidade</b> e a <b>forma de morcego</b>.
 *
 * <h2>Beber, que é o que o sustenta</h2>
 *
 * <p>Com o poder de beber escolhido, tocar num vivo a <b>um bloco e três décimos</b> — <b>dois e um</b>, se
 * ele estiver paralisado, porque a presa não foge — lhe tira sangue. E o que se tira depende de quem é:
 *
 * <ul>
 *   <li><b>aldeão</b> ou <b>gente</b>: dez de sangue, e é disto que ele vive;</li>
 *   <li><b>bicho</b>: dois, e <b>nunca acima de um quarto do teto</b> — sangue de bicho mantém vivo e não
 *       faz forte;</li>
 *   <li>e <b>lobisomem</b>: nada. Pior: <b>quatro de dor</b> em quem mordeu, e uma labareda. O sangue de um
 *       lobisomem é veneno para um vampiro, e é a única regra do mod em que as duas maldições se encontram.
 *       </li>
 * </ul>
 *
 * <p>Em <b>forma de morcego</b> sai só <b>dois</b>, seja de quem for: um morcego não tem boca para isso.
 *
 * <p>E morder um aldeão <b>tem testemunhas</b> — os guardas que virem passam a caçar quem mordeu.
 */
public final class VampirePowers {
    /** O que escolher. */
    public enum Poder implements net.minecraft.util.StringRepresentable {
        /** Nenhum, que é o estado normal e deixa o mundo funcionar. */
        NENHUM("nenhum"),
        /** <b>Beber</b>, que é o que o sustenta. */
        BEBER("beber"),
        /** A <b>visão</b>, que se liga e desliga. */
        VISÃO("visao"),
        /** A <b>velocidade</b>. */
        VELOCIDADE("velocidade"),
        /** E a <b>forma de morcego</b>. */
        MORCEGO("morcego");

        private final String nome;

        Poder(String nome) {
            this.nome = nome;
        }

        @Override
        public String getSerializedName() {
            return this.nome;
        }
    }

    /** A que distância ele alcança, e a que distância alcança quem não pode fugir. */
    public static final double ALCANCE = 1.3;
    public static final double ALCANCE_DE_PRESA_PRESA = 2.1;

    /** Quanto ele tira de cada um. */
    public static final int GOLE = 10;
    public static final int GOLE_DE_MORCEGO = 2;
    public static final int GOLE_DE_BICHO = 2;

    /** E o que o sangue de lobisomem lhe faz. */
    public static final float SANGUE_DE_LOBO = 4.0f;

    /** A que distância um guarda vê uma mordida. */
    public static final double TESTEMUNHAS = 16.0;

    /** O poder escolhido, que o cliente precisa de saber para desenhar e para mandar o clique. */
    public static final AttachmentType<Poder> ESCOLHIDO = AttachmentRegistry.<Poder>builder()
            .initializer(() -> Poder.NENHUM)
            .persistent(net.minecraft.util.StringRepresentable.fromEnum(Poder::values))
            .copyOnDeath()
            .syncWith(ByteBufCodecs.idMapper(i -> Poder.values()[i], Poder::ordinal).cast(),
                    AttachmentSyncPredicate.all())
            .buildAndRegister(Thaumcraft.id("vampire_power"));

    /** E se a visão está ligada, que é um interruptor e não um poder a usar. */
    public static final AttachmentType<Boolean> VÊ_NO_ESCURO = AttachmentRegistry.<Boolean>builder()
            .initializer(() -> false)
            .persistent(com.mojang.serialization.Codec.BOOL)
            .copyOnDeath()
            .syncWith(ByteBufCodecs.BOOL.cast(), AttachmentSyncPredicate.all())
            .buildAndRegister(Thaumcraft.id("vampire_vision"));

    private VampirePowers() {
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela os apegos. */
    public static void init() {
    }

    // ------------------------------------------------------------------ o que ele escolheu

    public static Poder escolhido(Player quem) {
        if (!Vampire.é(quem)) return Poder.NENHUM;
        return quem.getAttachedOrCreate(ESCOLHIDO);
    }

    public static void escolhe(Player quem, Poder qual) {
        quem.setAttached(ESCOLHIDO, qual);
    }

    /** Passa ao poder seguinte, que é o que a tecla faz. */
    public static void seguinte(Player quem) {
        if (!Vampire.é(quem)) return;
        Poder[] todos = Poder.values();
        escolhe(quem, todos[(escolhido(quem).ordinal() + 1) % todos.length]);
    }

    public static boolean vêNoEscuro(Player quem) {
        return Vampire.é(quem) && quem.getAttachedOrCreate(VÊ_NO_ESCURO);
    }

    /** Liga e desliga a visão. */
    public static void viraAVisão(Player quem) {
        boolean vai = !vêNoEscuro(quem);
        quem.setAttached(VÊ_NO_ESCURO, vai);
        if (!vai) quem.removeEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
    }

    // ------------------------------------------------------------------ e o que ele faz com isso

    /**
     * <b>Beber</b>: o ramo do {@code onEntityInteract} que é do vampiro.
     *
     * @return se ele bebeu, e então o jogo não tem mais nada a fazer com esse toque
     */
    public static boolean bebe(ServerLevel level, Player quem, LivingEntity deQuem) {
        if (escolhido(quem) != Poder.BEBER) return false;
        double alcance = Blood.desacordado(deQuem) ? ALCANCE_DE_PRESA_PRESA : ALCANCE;
        if (deQuem.distanceToSqr(quem.getX(), deQuem.getY(), quem.getZ()) > alcance * alcance) return false;

        // o sangue de um lobisomem é veneno
        if (Lycanthropy.éMesmoDeGente(deQuem)) {
            quem.hurtServer(level, level.damageSources().indirectMagic(quem, quem), SANGUE_DE_LOBO);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                    deQuem.getX(), deQuem.getY() + deQuem.getBbHeight() * 0.8, deQuem.getZ(),
                    16, 0.5, 0.2, 0.5, 0.0);
            barulho(level, deQuem);
            return true;
        }

        int gole = emMorcego(quem) ? GOLE_DE_MORCEGO : GOLE;
        if (deQuem instanceof Villager || deQuem instanceof Player) {
            Vampire.bebe(quem, Blood.tira(level, deQuem, gole, quem));
            pó(level, deQuem);
            barulho(level, deQuem);
            if (deQuem instanceof Villager aldeão) testemunhas(level, quem, aldeão);
            return true;
        }
        if (deQuem instanceof Animal) {
            Vampire.bebeDeBicho(quem, GOLE_DE_BICHO);
            pó(level, deQuem);
            barulho(level, deQuem);
            return true;
        }
        return false;
    }

    /**
     * Se ele está em <b>forma de morcego</b>, que é a forma em que o gole é pequeno.
     *
     * <p><b>Hoje responde sempre que não</b>, e é de propósito: a forma de morcego é a fatia seguinte. Esta
     * pergunta fica escrita aqui para que a conta do gole já esteja certa quando ela vier — é a mesma costura
     * que o {@link net.thaumcraft.occulta.wolf.Lycanthropy} foi antes de a licantropia existir, e que fechou
     * com uma linha.
     */
    public static boolean emMorcego(Player quem) {
        return false;
    }

    /**
     * <b>Quem viu, conta</b>: o {@code checkForBloodDrinkingWitnesses}.
     *
     * <p>Todo guarda de aldeia a <b>dezesseis blocos</b> que esteja de olhos abertos — e não paralisado — vem
     * atrás de quem mordeu. Morder dentro de uma aldeia não é um crime sem vítima: é um crime com guardas.
     */
    public static void testemunhas(ServerLevel level, Player quem, LivingEntity vítima) {
        var roda = vítima.getBoundingBox().inflate(TESTEMUNHAS, TESTEMUNHAS / 2.0, TESTEMUNHAS);
        for (var guarda : level.getEntitiesOfClass(
                net.thaumcraft.occulta.village.VillageGuardEntity.class, roda)) {
            if (guarda.hasEffect(net.thaumcraft.occulta.OccultaEffects.PARALYSIS)) continue;
            if (!guarda.getSensing().hasLineOfSight(vítima)) continue;
            guarda.setTarget(quem);
        }
    }

    private static void pó(ServerLevel level, LivingEntity deQuem) {
        level.sendParticles(DustParticleOptions.REDSTONE,
                deQuem.getX(), deQuem.getY() + deQuem.getBbHeight() * 0.8, deQuem.getZ(),
                16, 0.5, 0.2, 0.5, 0.0);
    }

    private static void barulho(ServerLevel level, LivingEntity deQuem) {
        level.playSound(null, deQuem.blockPosition(), SoundEvents.GENERIC_DRINK.value(),
                SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
