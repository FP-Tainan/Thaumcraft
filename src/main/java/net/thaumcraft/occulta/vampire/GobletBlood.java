package net.thaumcraft.occulta.vampire;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * O que está dentro do <b>Cálice</b>: o {@code BloodSource} e o {@code WITCBloodUUID} do Witchery.
 *
 * <p>Um cálice cheio guarda <b>de quem</b> é o sangue, e isso importa por três razões, cada uma de um degrau
 * diferente da história:
 *
 * <ul>
 *   <li>o sangue de <b>galinha</b> não vira ninguém em vampiro — serve para chamar;</li>
 *   <li>o sangue de <b>Lilith</b> vira, e é o único que vira. É a <b>porta de entrada</b>;</li>
 *   <li>e o sangue de uma <b>pessoa</b> guarda o nome dela, porque o nono degrau da escada pede que o
 *       vampiro beba o <b>seu próprio</b>.</li>
 * </ul>
 *
 * <p>No original isto é uma chave de texto no NBT — {@code __chicken}, {@code __lilith} ou um UUID —, e aqui
 * é a mesma coisa dita com tipos: ou é uma das duas <b>fontes</b>, ou é o dono, com nome e tudo.
 */
public record GobletBlood(Fonte fonte, Optional<UUID> dono, String nome) {
    /** De onde o sangue veio, quando não veio de ninguém em particular. */
    public enum Fonte implements net.minecraft.util.StringRepresentable {
        /** De uma <b>galinha</b>, sacrificada com a Boline sobre o rito. */
        GALINHA("galinha"),
        /** De <b>Lilith</b>, que é a única coisa que faz um vampiro. */
        LILITH("lilith"),
        /** Ou de <b>alguém</b>, e então o nome dessa pessoa está guardado ao lado. */
        ALGUÉM("alguem");

        private final String nome;

        Fonte(String nome) {
            this.nome = nome;
        }

        @Override
        public String getSerializedName() {
            return this.nome;
        }
    }

    public static final Codec<GobletBlood> CODEC = RecordCodecBuilder.create(i -> i.group(
                    net.minecraft.util.StringRepresentable.fromEnum(Fonte::values)
                            .fieldOf("fonte").forGetter(GobletBlood::fonte),
                    net.minecraft.core.UUIDUtil.CODEC.optionalFieldOf("dono").forGetter(GobletBlood::dono),
                    Codec.STRING.optionalFieldOf("nome", "").forGetter(GobletBlood::nome))
            .apply(i, GobletBlood::new));

    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, GobletBlood> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.idMapper(i -> Fonte.values()[i], Fonte::ordinal).cast(),
                    GobletBlood::fonte,
                    net.minecraft.core.UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional),
                    GobletBlood::dono,
                    ByteBufCodecs.STRING_UTF8, GobletBlood::nome,
                    GobletBlood::new);

    /** O sangue de uma galinha. */
    public static final GobletBlood GALINHA = new GobletBlood(Fonte.GALINHA, Optional.empty(), "");

    /** E o de Lilith, que é o que faz um vampiro. */
    public static final GobletBlood LILITH = new GobletBlood(Fonte.LILITH, Optional.empty(), "");

    /** O sangue desta pessoa, com o nome dela. */
    public static GobletBlood de(Player quem) {
        return new GobletBlood(Fonte.ALGUÉM, Optional.of(quem.getUUID()), quem.getGameProfile().name());
    }

    /** Se este sangue é desta pessoa: o que o nono degrau da escada pergunta. */
    public boolean éDe(Player quem) {
        return this.dono.isPresent() && this.dono.get().equals(quem.getUUID());
    }

    /** O nome do que está dentro, para o cálice o dizer na mão. */
    public net.minecraft.network.chat.Component diz() {
        if (this.fonte == Fonte.ALGUÉM) {
            return net.minecraft.network.chat.Component.literal(this.nome);
        }
        return net.minecraft.network.chat.Component.translatable(
                "tc.goblet." + this.fonte.getSerializedName());
    }
}
