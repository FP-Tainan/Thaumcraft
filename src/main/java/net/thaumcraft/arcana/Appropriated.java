package net.thaumcraft.arcana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * O que a <b>Apropriação</b> guardou dentro de um feitiço: o {@code stored_data} do Ars Magica 2.
 *
 * <p>Ela é a única coisa deste ramo que <b>tira uma coisa do mundo e a leva consigo</b> — um bloco com tudo o
 * que ele tem dentro, ou um bicho com tudo o que ele é. Enquanto estiver guardada, aquilo <b>não existe</b> em
 * lugar nenhum senão aqui, e lançar o feitiço outra vez põe de volta.
 *
 * <p>É por isso que ela guarda o <b>dado do bloco</b> e não só o nome dele: um baú apropriado volta com o que
 * tinha dentro. No original isto é o NBT do {@code TileEntity} copiado inteiro, e aqui é o mesmo.
 *
 * @param bloco   o feitio do bloco guardado, se for um bloco
 * @param dentro  o que o bloco tinha dentro, se tinha
 * @param bicho   e o bicho guardado, se for um bicho
 */
public record Appropriated(Optional<BlockState> bloco, Optional<CompoundTag> dentro,
                           Optional<CompoundTag> bicho) {
    public static final Codec<Appropriated> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockState.CODEC.optionalFieldOf("bloco").forGetter(Appropriated::bloco),
            CompoundTag.CODEC.optionalFieldOf("dentro").forGetter(Appropriated::dentro),
            CompoundTag.CODEC.optionalFieldOf("bicho").forGetter(Appropriated::bicho))
            .apply(i, Appropriated::new));

    public static final net.minecraft.network.codec.StreamCodec<
            net.minecraft.network.RegistryFriendlyByteBuf, Appropriated> STREAM_CODEC =
            net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static Appropriated of(BlockState feitio, Optional<CompoundTag> dentro) {
        return new Appropriated(Optional.of(feitio), dentro, Optional.empty());
    }

    public static Appropriated of(CompoundTag bicho) {
        return new Appropriated(Optional.empty(), Optional.empty(), Optional.of(bicho));
    }
}
