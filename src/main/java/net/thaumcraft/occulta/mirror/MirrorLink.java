package net.thaumcraft.occulta.mirror;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * A ligação de um espelho ao outro: o {@code DimCoords} mais o {@code Dimension} do original.
 *
 * <p>Um espelho feito na bancada ainda não tem nenhuma. Ele ganha a sua na primeira vez que alguém lhe passa à
 * frente: abre-se então uma cela no {@linkplain MirrorWorld Mundo do Espelho}, e cada um dos dois passa a saber
 * onde o outro está.
 *
 * @param level o mundo do outro lado
 * @param pos   e a metade de cima do espelho que lá está
 */
public record MirrorLink(ResourceKey<Level> level, BlockPos pos) {
    public static final Codec<MirrorLink> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceKey.codec(net.minecraft.core.registries.Registries.DIMENSION).fieldOf("level")
                    .forGetter(MirrorLink::level),
            BlockPos.CODEC.fieldOf("pos").forGetter(MirrorLink::pos)).apply(i, MirrorLink::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MirrorLink> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(net.minecraft.core.registries.Registries.DIMENSION), MirrorLink::level,
            BlockPos.STREAM_CODEC, MirrorLink::pos, MirrorLink::new);

    /**
     * O que um espelho em item carrega: a ligação, se já tiver uma, e se ele está <b>vazado</b>.
     *
     * <p>É o mesmo que o original guarda no NBT do item — {@code DimCoords} e {@code DemonSlain} —, e é o que faz
     * um espelho assentado outra vez continuar a ser o mesmo espelho.
     *
     * @param link   o outro lado, se já houver
     * @param hollow se o Reflexo dele já morreu
     */
    public record Held(Optional<MirrorLink> link, boolean hollow) {
        public static final Held EMPTY = new Held(Optional.empty(), false);

        public static final Codec<Held> CODEC = RecordCodecBuilder.create(i -> i.group(
                MirrorLink.CODEC.optionalFieldOf("link").forGetter(Held::link),
                Codec.BOOL.optionalFieldOf("hollow", false).forGetter(Held::hollow)).apply(i, Held::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Held> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(MirrorLink.STREAM_CODEC), Held::link,
                ByteBufCodecs.BOOL, Held::hollow, Held::new);
    }
}
