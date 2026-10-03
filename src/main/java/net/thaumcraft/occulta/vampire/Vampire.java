package net.thaumcraft.occulta.vampire;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.wolf.Werewolf;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>vampirice do jogador</b>: o {@code ExtendedPlayer} do Witchery, na parte que é do vampiro.
 *
 * <p>É o irmão da {@linkplain Werewolf licantropia}, e é interessante como eles são <b>opostos</b>. Um
 * lobisomem não escolhe quando muda e ganha força por isso; um vampiro escolhe tudo, e o preço dele é
 * <b>constante</b>: ele tem de <b>beber</b>, todos os dias, para sempre.
 *
 * <h2>O sangue é o relógio dele</h2>
 *
 * <p>Um vampiro não tem fome — tem <b>sede</b>. O {@code poder de sangue} é a única coisa que o sustenta, e
 * ele faz três trabalhos ao mesmo tempo:
 *
 * <ul>
 *   <li>é a <b>comida</b>: cinco de sangue viram um de comida, e é o único jeito de um vampiro comer;</li>
 *   <li>é o <b>combustível</b> dos poderes, que gastam dele;</li>
 *   <li>e é o <b>guarda-sol</b>: enquanto ele tiver sangue, o sol só o castiga. Zerado o sangue, o sol o
 *       <b>mata</b>.</li>
 * </ul>
 *
 * <p>O teto do sangue cresce com o grau — <b>quinhentos mais duzentos e cinquenta por grau</b> —, e cresce
 * pela <b>metade</b> em quem também é lobisomem do segundo grau para cima. Ser as duas coisas custa.
 *
 * <h2>E a escada dele não tem estátua</h2>
 *
 * <p>Onde o lobisomem tem um altar que lhe diz o que fazer, o vampiro tem um <b>teto de grau</b> que sobe
 * sozinho à medida que ele <b>lê</b> — e dez feitos que ninguém lhe explica. É de propósito: um lobisomem é
 * servo de alguém, e um vampiro não é servo de ninguém.
 *
 * <p>Esta classe guarda o que ele <b>é</b>; o que ele <b>bebe</b> está em {@link Blood}, o que o <b>sol e a
 * sede</b> lhe fazem está em {@link VampireTick}, e o que ele <b>faz</b> está em {@link VampirePowers}.
 */
public record Vampire(int grau, int sangue, int teto, int conta) {
    /** O maior grau que há, como no lobisomem. */
    public static final int TETO = 10;

    /** O sangue que um vampiro novo recebe, e o que o teto dele vale por grau. */
    public static final int SANGUE_DO_PRIMEIRO = 125;
    public static final int SANGUE_BASE = 500;
    public static final int SANGUE_POR_GRAU = 250;

    /** O menor teto de grau que ler alguma coisa dá. */
    public static final int MENOR_TETO = 3;

    /** Do segundo grau de lobisomem em diante, o sangue de um vampiro cresce pela metade. */
    public static final int HÍBRIDO_AOS = 2;

    /** Quem não é nada. */
    public static final Vampire NINGUÉM = new Vampire(0, 0, 0, 0);

    public static final Codec<Vampire> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.INT.fieldOf("grau").forGetter(Vampire::grau),
                    Codec.INT.optionalFieldOf("sangue", 0).forGetter(Vampire::sangue),
                    Codec.INT.optionalFieldOf("teto", 0).forGetter(Vampire::teto),
                    Codec.INT.optionalFieldOf("conta", 0).forGetter(Vampire::conta))
            .apply(i, Vampire::new));

    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Vampire> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, Vampire::grau,
                    ByteBufCodecs.VAR_INT, Vampire::sangue,
                    ByteBufCodecs.VAR_INT, Vampire::teto,
                    ByteBufCodecs.VAR_INT, Vampire::conta,
                    Vampire::new);

    /**
     * O que o jogador é, e quanto sangue tem.
     *
     * <p><b>Vai sincronizado</b>, e tem de ir: a barra de sangue é a única coisa que um vampiro olha o tempo
     * todo, e quem a desenha é o cliente.
     *
     * <p>E <b>atravessa a morte</b>: morrer não cura a vampirice. É o original, e é o que faz dela uma coisa
     * que se carrega.
     */
    public static final AttachmentType<Vampire> DATA = AttachmentRegistry.<Vampire>builder()
            .initializer(() -> NINGUÉM)
            .persistent(CODEC)
            .copyOnDeath()
            .syncWith(STREAM_CODEC, AttachmentSyncPredicate.all())
            .buildAndRegister(Thaumcraft.id("vampire"));

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o apego. */
    public static void init() {
    }

    // ------------------------------------------------------------------ o que ele é

    public static Vampire de(@Nullable LivingEntity quem) {
        if (!(quem instanceof Player gente)) return NINGUÉM;
        return gente.getAttachedOrCreate(DATA);
    }

    /** O grau desta pessoa, ou zero se ela não for nada. */
    public static int grauDe(@Nullable LivingEntity quem) {
        return de(quem).grau();
    }

    /** Se ela é vampira de todo. */
    public static boolean é(@Nullable LivingEntity quem) {
        return grauDe(quem) > 0;
    }

    /** O sangue que ela tem. */
    public static int sangueDe(@Nullable LivingEntity quem) {
        return de(quem).sangue();
    }

    /**
     * E o teto do sangue dela: <b>quinhentos mais duzentos e cinquenta por grau</b>.
     *
     * <p>Quem também é <b>lobisomem do segundo grau</b> para cima cresce pela <b>metade</b> — a conta
     * {@code floor(grau * 0.5)} do original. Ser as duas coisas é possível e custa, e esta é a única linha do
     * mod em que as duas se olham.
     */
    public static int tetoDoSangue(@Nullable LivingEntity quem) {
        int grau = grauDe(quem);
        int conta = Werewolf.grauDe(quem) >= HÍBRIDO_AOS ? (int) Math.floor(grau * 0.5) : grau;
        return SANGUE_BASE + conta * SANGUE_POR_GRAU;
    }

    // ------------------------------------------------------------------ o que o muda

    /**
     * Põe o grau, e com ele o que o grau manda.
     *
     * <p>Virar vampiro ao <b>primeiro grau</b> dá cento e vinte e cinco de sangue — o bastante para a
     * primeira noite e não mais. Baixar a <b>zero</b> apaga o sangue e devolve a pessoa ao sangue de gente,
     * que é o que a cura faz.
     *
     * <p>E, como na licantropia, <b>mudar de grau apaga a conta</b>: o que ele fez para subir não serve para
     * o degrau seguinte.
     */
    public static void grau(Player quem, int grau) {
        int novo = Math.clamp(grau, 0, TETO);
        Vampire era = de(quem);
        if (era.grau() == novo) return;

        int sangue = switch (novo) {
            case 0 -> 0;
            case 1 -> SANGUE_DO_PRIMEIRO;
            default -> era.sangue();
        };
        quem.setAttached(DATA, new Vampire(novo, sangue, era.teto(), 0));

        if (novo == 0) {
            Blood.dá(quem, Blood.TETO / 10);
        } else {
            Blood.põe(quem, 0);
        }
        if (quem.level() instanceof ServerLevel level) VampireTick.arruma(level, quem);
    }

    /**
     * Sobe um grau, se o <b>teto</b> deixar.
     *
     * <p>O teto é o que o original chama de {@code vampireLevelCap}, e é o que faz da escada do vampiro uma
     * coisa que <b>se lê</b> antes de se fazer: sem ter lido, o feito não vale.
     */
    public static void sobeUmGrau(Player quem) {
        Vampire era = de(quem);
        if (era.grau() >= TETO || era.grau() >= era.teto()) return;
        grau(quem, era.grau() + 1);
        quem.sendSystemMessage(Component.translatable("tc.vampire.thirstgrows")
                .withStyle(ChatFormatting.GOLD));
    }

    /** Se o teto deixa ele subir agora. */
    public static boolean podeSubir(Player quem) {
        Vampire é = de(quem);
        return é.grau() < TETO && é.grau() < é.teto();
    }

    /** Levanta o teto do grau: o {@code increaseVampireLevelCap}, que nunca o põe abaixo de três. */
    public static void levantaOTeto(Player quem, int até) {
        Vampire era = de(quem);
        if (até <= era.teto()) return;
        quem.setAttached(DATA, new Vampire(era.grau(), era.sangue(),
                Math.max(até, MENOR_TETO), era.conta()));
    }

    // ------------------------------------------------------------------ o sangue

    /** Põe o sangue, preso entre zero e o teto. */
    public static void sangue(Player quem, int quanto) {
        Vampire era = de(quem);
        int novo = Math.clamp(quanto, 0, tetoDoSangue(quem));
        if (era.sangue() == novo) return;
        quem.setAttached(DATA, new Vampire(era.grau(), novo, era.teto(), era.conta()));
    }

    /**
     * Bebeu: o sangue sobe.
     *
     * <p>E há uma coisa que acontece aqui e em mais lado nenhum: <b>encher o sangue até ao teto no primeiro
     * grau sobe o vampiro ao segundo</b>. É o único degrau da escada que não se procura — ele vem de beber o
     * bastante, e é como o mod diz ao jogador que a sede é o caminho.
     */
    public static void bebe(Player quem, int quanto) {
        if (quanto <= 0) return;
        int teto = tetoDoSangue(quem);
        Vampire era = de(quem);
        if (era.sangue() >= teto) return;

        sangue(quem, era.sangue() + quanto);
        if (de(quem).grau() == 1 && de(quem).sangue() >= teto) sobeUmGrau(quem);
    }

    /**
     * Bebeu de um bicho: o sangue sobe, mas <b>nunca passa de um quarto do teto</b>.
     *
     * <p>É a regra mais elegante do vampiro: sangue de bicho <b>mantém vivo e não faz forte</b>. Quem não
     * quiser morder gente sobrevive — e fica preso no primeiro grau para sempre, porque o segundo pede o
     * sangue cheio.
     */
    public static void bebeDeBicho(Player quem, int quanto) {
        int limite = (int) Math.ceil(tetoDoSangue(quem) * 0.25f);
        Vampire era = de(quem);
        if (era.sangue() >= limite) return;
        sangue(quem, Math.min(era.sangue() + quanto, limite));
    }

    /**
     * Gastou: o sangue desce, e diz se deu.
     *
     * <p>No <b>criativo</b> tudo dá e nada se gasta, como no original.
     *
     * @param exato se é preciso ter tudo o que se pede; não sendo, basta ter alguma coisa
     */
    public static boolean gasta(Player quem, int quanto, boolean exato) {
        if (quem.getAbilities().instabuild) return true;
        Vampire era = de(quem);
        if (era.sangue() < (exato ? quanto : 1)) return false;
        sangue(quem, era.sangue() - quanto);
        return true;
    }

    // ------------------------------------------------------------------ e a conta dos feitos

    public static int contaDe(Player quem) {
        return de(quem).conta();
    }

    public static void conta(Player quem) {
        Vampire era = de(quem);
        quem.setAttached(DATA, new Vampire(era.grau(), era.sangue(), era.teto(), era.conta() + 1));
    }

    /** E a conta volta a zero, que é o que uma mordida mal dada faz. */
    public static void apagaAConta(Player quem) {
        Vampire era = de(quem);
        if (era.conta() == 0) return;
        quem.setAttached(DATA, new Vampire(era.grau(), era.sangue(), era.teto(), 0));
    }
}
