package net.thaumcraft.entity.golem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

/**
 * Uma marca do sino: o {@code Marker} da 4.2.3.5 — um bloco, a face tocada e a cor (−1 é "qualquer cor").
 *
 * <p>A dimensão de então era um número; aqui é o nome do mundo. A comparação "frouxa" do original
 * ({@code equalsFuzzy}) aceita qualquer cor quando a marca pedida é de cor −1.
 */
public record Marker(int x, int y, int z, Identifier dim, byte side, byte color) {
    public static final Codec<Marker> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("x").forGetter(Marker::x),
            Codec.INT.fieldOf("y").forGetter(Marker::y),
            Codec.INT.fieldOf("z").forGetter(Marker::z),
            Identifier.CODEC.fieldOf("dim").forGetter(Marker::dim),
            Codec.BYTE.fieldOf("side").forGetter(Marker::side),
            Codec.BYTE.fieldOf("color").forGetter(Marker::color)).apply(i, Marker::new));
    public static final StreamCodec<ByteBuf, Marker> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Marker::x, ByteBufCodecs.VAR_INT, Marker::y, ByteBufCodecs.VAR_INT, Marker::z,
            Identifier.STREAM_CODEC, Marker::dim, ByteBufCodecs.BYTE, Marker::side, ByteBufCodecs.BYTE, Marker::color,
            Marker::new);

    public Marker(BlockPos pos, Level level, int side, int color) {
        this(pos.getX(), pos.getY(), pos.getZ(), level.dimension().identifier(), (byte) side, (byte) color);
    }

    public BlockPos pos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public Direction direction() {
        return Direction.from3DDataValue(this.side);
    }

    public boolean in(Level level) {
        return this.dim.equals(level.dimension().identifier());
    }

    /** O {@code equalsFuzzy}: tudo igual, e a cor igual ou −1 nesta marca. */
    public boolean equalsFuzzy(Marker other) {
        return this.x == other.x && this.y == other.y && this.z == other.z && this.dim.equals(other.dim)
                && this.side == other.side && (this.color == other.color || this.color == -1);
    }

    public Marker withColor(int color) {
        return new Marker(this.x, this.y, this.z, this.dim, this.side, (byte) color);
    }
}
