package net.thaumcraft.occulta.wolf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.OccultaEffects;
import net.thaumcraft.occulta.OccultaItems;
import org.jetbrains.annotations.Nullable;

/**
 * A <b>licantropia do jogador</b>: o {@code ExtendedPlayer} e o {@code Shapeshift} do Witchery, na parte que é
 * do lobo.
 *
 * <p>É a maior coisa do mod, e o que a torna grande não são os poderes — é o <b>preço</b>. Um lobisomem não
 * escolhe quando se transforma: a <b>lua cheia</b> escolhe por ele, e quando ela o apanha ele <b>larga tudo o
 * que veste e tudo o que leva na mão</b>. Nada de armadura, nada de espada, nada de mochila à mão. O que
 * sobra é o que ele é.
 *
 * <p><b>Três coisas o seguram</b>, e só três:
 *
 * <ul>
 *   <li>o <b>Amuleto da Lua</b>, que fica na mochila e trava a forma onde ela estiver — e é a única coisa que
 *       não cai quando ele muda;</li>
 *   <li>o <b>acônito</b>, que não deixa a transformação acontecer de todo;</li>
 *   <li>e o <b>grau</b>: do <b>segundo</b> em diante ele manda na mudança, e do <b>quinto</b> em diante pode
 *       escolher a forma de <b>lobisomem</b> em vez da de lobo.</li>
 * </ul>
 *
 * <p>Abaixo do segundo grau, a lua manda e ele obedece.
 *
 * <p>Quem a <b>pega</b> é quem é mordido por um lobisomem, e é o que o {@link WolfmanEntity} faz a quem
 * derruba.
 *
 * <p>Esta classe guarda o que ele <b>é</b>; o que ele <b>faz</b> está em {@link WerewolfPowers}, e o que a lua
 * lhe faz está em {@link WerewolfTick}.
 */
public record Werewolf(int grau, Forma forma) {
    /** O maior grau que há. */
    public static final int TETO = 10;

    /** Do segundo em diante ele manda na mudança. */
    public static final int MANDA_NA_MUDANÇA = 2;

    /** E do quinto em diante pode ser lobisomem e não só lobo. */
    public static final int LOBISOMEM_AOS = 5;

    /** Quem não é nada. */
    public static final Werewolf NINGUÉM = new Werewolf(0, Forma.GENTE);

    /** A forma em que ele anda. */
    public enum Forma implements net.minecraft.util.StringRepresentable {
        /** Gente, que é como ele anda de dia. */
        GENTE("gente"),
        /** <b>Lobo</b>: quatro patas, depressa, e sem mãos. */
        LOBO("lobo"),
        /** E <b>lobisomem</b>: de pé, e com mãos. */
        LOBISOMEM("lobisomem");

        private final String nome;

        Forma(String nome) {
            this.nome = nome;
        }

        @Override
        public String getSerializedName() {
            return this.nome;
        }

        /** Se esta forma é bicho — qualquer das duas. */
        public boolean éBicho() {
            return this != GENTE;
        }
    }

    public static final Codec<Werewolf> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.INT.fieldOf("grau").forGetter(Werewolf::grau),
                    net.minecraft.util.StringRepresentable.fromEnum(Forma::values)
                            .optionalFieldOf("forma", Forma.GENTE).forGetter(Werewolf::forma))
            .apply(i, Werewolf::new));

    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, Werewolf> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, Werewolf::grau,
                    ByteBufCodecs.idMapper(i -> Forma.values()[i], Forma::ordinal), Werewolf::forma,
                    Werewolf::new);

    /**
     * O que o jogador é, e em que forma anda.
     *
     * <p><b>Vai sincronizado</b>, e tem de ir: o grau e a forma mandam no que se desenha, e quem desenha é o
     * cliente. É a mesma razão por que a cor do Colorido viaja num apego.
     *
     * <p>E <b>atravessa a morte</b>: morrer não cura a licantropia. É o original, e é o que faz dela uma
     * coisa que se carrega.
     */
    public static final AttachmentType<Werewolf> DATA = AttachmentRegistry.<Werewolf>builder()
            .initializer(() -> NINGUÉM)
            .persistent(CODEC)
            .copyOnDeath()
            .syncWith(STREAM_CODEC, AttachmentSyncPredicate.all())
            .buildAndRegister(Thaumcraft.id("werewolf"));

    /** Sem uso fora do porte: obriga a classe a carregar, e com ela o apego. */
    public static void init() {
    }

    // ------------------------------------------------------------------ o que ele é

    /** O que esta pessoa é. */
    public static Werewolf de(@Nullable LivingEntity quem) {
        if (!(quem instanceof Player gente)) return NINGUÉM;
        return gente.getAttachedOrCreate(DATA);
    }

    /** O grau desta pessoa, ou zero se ela não for nada. */
    public static int grauDe(@Nullable LivingEntity quem) {
        return de(quem).grau();
    }

    /** A forma em que ela anda. */
    public static Forma formaDe(@Nullable LivingEntity quem) {
        return de(quem).forma();
    }

    /** Se ela está em forma de bicho — lobo ou lobisomem. */
    public static boolean emBicho(@Nullable LivingEntity quem) {
        return formaDe(quem).éBicho();
    }

    /**
     * Põe o grau, e com ele o que o grau manda.
     *
     * <p>Baixar o grau a <b>zero</b> devolve a pessoa à forma de gente, mesmo que ela estivesse de lobo: é o
     * {@code setWerewolfLevel} do original, e é o que a cura faz.
     */
    public static void grau(Player quem, int grau) {
        int novo = Math.clamp(grau, 0, TETO);
        Werewolf era = de(quem);
        if (era.grau() == novo) return;

        Forma forma = novo == 0 ? Forma.GENTE : era.forma();
        quem.setAttached(DATA, new Werewolf(novo, forma));
        if (quem.level() instanceof ServerLevel level) WerewolfTick.arruma(level, quem);
    }

    /** Sobe um grau, até ao teto. */
    public static void sobeUmGrau(Player quem) {
        grau(quem, de(quem).grau() + 1);
    }

    /** Muda de forma, e arruma o corpo para ela. */
    public static void forma(Player quem, Forma qual) {
        Werewolf era = de(quem);
        if (era.forma() == qual) return;
        quem.setAttached(DATA, new Werewolf(era.grau(), qual));
        if (quem.level() instanceof ServerLevel level) WerewolfTick.arruma(level, quem);
    }

    // ------------------------------------------------------------------ o que o segura

    /** Se ele manda na própria mudança: do segundo grau em diante. */
    public static boolean mandaNaMudança(@Nullable LivingEntity quem) {
        return grauDe(quem) >= MANDA_NA_MUDANÇA;
    }

    /** E se pode ser lobisomem e não só lobo: do quinto em diante. */
    public static boolean podeSerLobisomem(@Nullable LivingEntity quem) {
        return grauDe(quem) >= LOBISOMEM_AOS;
    }

    /**
     * Se ele traz o <b>Amuleto da Lua</b> na mochila.
     *
     * <p>O original olha a mochila inteira, e não a mão: o amuleto não se usa, <b>tem-se</b>. Quem o tem fica
     * na forma em que estiver, e a lua passa por cima dele sem lhe tocar.
     */
    public static boolean temAmuleto(Player quem) {
        for (int casa = 0; casa < quem.getInventory().getContainerSize(); casa++) {
            if (quem.getInventory().getItem(casa).is(OccultaItems.MOON_CHARM)) return true;
        }
        return false;
    }

    /** E se o acônito o está a segurar: o {@code isWolfsbaneActive}. */
    public static boolean temAcônito(LivingEntity quem) {
        return quem.hasEffect(OccultaEffects.WOLFSBANE);
    }

    /** Se alguma coisa impede a lua de lhe tocar: o amuleto ou o acônito. */
    public static boolean aLuaNãoLhePega(Player quem) {
        return temAmuleto(quem) || temAcônito(quem);
    }

    // ------------------------------------------------------------------ o preço

    /**
     * <b>O que ele larga ao virar bicho</b>: o {@code updateWerewolfEffects} do original.
     *
     * <p>Cai tudo o que ele veste — e, sendo <b>lobo</b>, também o que ele tem na mão, porque um lobo não tem
     * mãos. O <b>Amuleto da Lua</b> é a única coisa que fica, e é de propósito: sem isso, transformar-se
     * largava no chão a única coisa que podia desfazer a transformação.
     *
     * <p>E vem com <b>visão noturna</b> e com o veneno limpo, que são as duas coisas boas de ser bicho.
     */
    public static void vira(ServerLevel level, Player quem, Forma qual) {
        quem.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false));
        quem.removeEffect(MobEffects.POISON);

        // o lobisomem guarda as mãos; o lobo não
        boolean caiAMão = qual == Forma.LOBO;
        for (EquipmentSlot casa : EquipmentSlot.values()) {
            boolean armadura = casa.getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
            boolean naMão = casa == EquipmentSlot.MAINHAND;
            if (!armadura && !(naMão && caiAMão)) continue;

            ItemStack tinha = quem.getItemBySlot(casa);
            if (tinha.isEmpty() || tinha.is(OccultaItems.MOON_CHARM)) continue;
            quem.setItemSlot(casa, ItemStack.EMPTY);
            quem.drop(tinha, false);
        }
    }
}
