package net.thaumcraft.occulta.vampire;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaEffects;
import org.jetbrains.annotations.Nullable;

/**
 * <b>O sangue que os vivos têm</b>: o {@code humanBlood} do {@code ExtendedPlayer} e o {@code blood} do
 * {@code ExtendedVillager}.
 *
 * <p>É o outro lado da vampirice, e é o que a torna um sistema em vez de um poder: toda pessoa e todo aldeão
 * carrega <b>quinhentos de sangue</b>, e esse sangue <b>é uma coisa que acaba</b>.
 *
 * <h2>Beber de quem está acordado dá menos</h2>
 *
 * <p>De um vivo <b>desacordado</b> — adormecido, ou <b>paralisado</b> ao quinto grau da poção — sai tudo o
 * que se pede. De um vivo <b>acordado</b> sai <b>dois terços</b>: ele se debate, e a conta do original é
 * exatamente essa.
 *
 * <p>É daí que vem todo o jeito de jogar de um vampiro: não se morde quem está de pé, <b>adormece-se</b>
 * primeiro. A poção da Paralisia e a Maçã do Sono deixam de ser truques e passam a ser ferramentas de caça.
 *
 * <h2>E beber demais mata</h2>
 *
 * <p>Enquanto a vítima tiver <b>metade ou mais</b> do sangue, a mordida quase não dói — um décimo de dano, e
 * nada se ela estiver desacordada. <b>Abaixo da metade</b>, cada gole <b>fere</b>: um e três décimos num
 * aldeão, um numa pessoa.
 *
 * <p>É isso que faz do segundo degrau da escada — <b>beber de um aldeão e deixá-lo entre duzentos e
 * cinquenta e duzentos e oitenta</b>, seis vezes — uma coisa de pulso firme: um gole a mais e o aldeão
 * começa a morrer, e a conta volta ao princípio.
 */
public final class Blood {
    /** Quanto sangue um vivo tem, cheio. */
    public static final int TETO = 500;

    /** O que sai de quem está acordado: dois terços. */
    public static final float ACORDADO = 0.66f;

    /** Abaixo de metade do sangue, a mordida fere. */
    public static final int METADE = 250;

    /** O que ela fere: num aldeão, numa pessoa, e o arranhão de quem está acordado. */
    public static final float DÓI_ALDEÃO = 1.3f;
    public static final float DÓI_GENTE = 1.0f;
    public static final float ARRANHÃO = 0.1f;

    /** Do quinto grau em diante, a Paralisia conta como estar desacordado. */
    public static final int PARALISIA_QUE_DESACORDA = 4;

    /** Quanto um vivo recupera a cada volta do relógio. */
    public static final int RECUPERA = 2;

    /**
     * O sangue de um vivo.
     *
     * <p><b>Vai sincronizado</b> porque um vampiro precisa de o ver antes de morder — é o que lhe diz se
     * aquele aldeão ainda aguenta um gole.
     */
    public static final AttachmentType<Integer> DATA = AttachmentRegistry.<Integer>builder()
            .initializer(() -> TETO)
            .persistent(com.mojang.serialization.Codec.INT)
            .copyOnDeath()
            .syncWith(ByteBufCodecs.VAR_INT.cast(), AttachmentSyncPredicate.all())
            .buildAndRegister(Thaumcraft.id("blood"));

    private Blood() {
    }

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o apego. */
    public static void init() {
    }

    /** Quanto sangue este vivo tem. */
    public static int de(@Nullable LivingEntity quem) {
        if (quem == null) return 0;
        return quem.getAttachedOrCreate(DATA);
    }

    /** Põe o sangue, preso entre zero e o teto. */
    public static void põe(LivingEntity quem, int quanto) {
        quem.setAttached(DATA, Math.clamp(quanto, 0, TETO));
    }

    /** Dá sangue, até o teto: é o que o corpo repõe sozinho, e o que dormir repõe de uma vez. */
    public static void dá(LivingEntity quem, int quanto) {
        int tem = de(quem);
        if (tem >= TETO) return;
        põe(quem, tem + quanto);
    }

    /** Se este vivo está desacordado, e por isso dá tudo o que se lhe pede. */
    public static boolean desacordado(LivingEntity quem) {
        if (quem.isSleeping()) return true;
        var presa = quem.getEffect(OccultaEffects.PARALYSIS);
        return presa != null && presa.getAmplifier() >= PARALISIA_QUE_DESACORDA;
    }

    /**
     * <b>Tira sangue</b>: o {@code takeBlood} e o {@code takeHumanBlood}, que são a mesma conta.
     *
     * @param quanto o que se pede; de quem está acordado sai menos
     * @param quem   quem morde, para a dor ter dono
     * @return o que saiu de verdade
     */
    public static int tira(ServerLevel level, LivingEntity deQuem, int quanto, LivingEntity quem) {
        boolean dorme = desacordado(deQuem);
        if (!dorme) quanto = (int) Math.ceil(ACORDADO * quanto);

        int tinha = de(deQuem);
        int saiu = Math.min(tinha, Math.max(quanto, 0));
        põe(deQuem, tinha - saiu);

        float dói = deQuem instanceof Villager ? DÓI_ALDEÃO : DÓI_GENTE;
        if (de(deQuem) < METADE) {
            deQuem.hurtServer(level, dor(level, quem), dói);
        } else if (!dorme) {
            deQuem.hurtServer(level, dor(level, quem), ARRANHÃO);
        }
        return saiu;
    }

    /**
     * A dor da mordida, que é <b>mágica</b> e não de arma.
     *
     * <p>O original usa o {@code DamageSource.magic} com dono, e isso importa: armadura não protege de uma
     * mordida, e a dela não acorda quem está desacordado por poção.
     */
    private static net.minecraft.world.damagesource.DamageSource dor(ServerLevel level, LivingEntity quem) {
        return level.damageSources().indirectMagic(quem, quem);
    }
}
