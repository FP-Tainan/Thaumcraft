package net.thaumcraft.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.thaumcraft.api.aspects.Aspect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * O que está escrito numa nota de pesquisa: o {@code ResearchNoteData} da 4.2.3.5.
 *
 * <p>A nota sabe de que pesquisa ela é, a cor do aspecto principal (que tinge a fita do pergaminho), se já
 * foi resolvida, quantas cópias já se tiraram dela e o tabuleiro de hexágonos. Cada casa tem um tipo: zero
 * é casa vazia, um é aspecto que a pesquisa pede (fixo, não se apaga) e dois é aspecto que quem pesquisa
 * escreveu.
 *
 * <p>Ela é imutável; quem escreve nela faz uma nova e põe de volta no item.
 */
public record ResearchNote(String key, int color, boolean complete, int copies, List<Cell> cells) {
    /** Uma casa do tabuleiro. {@code aspect} é a etiqueta do aspecto, ou vazio. */
    public record Cell(int q, int r, int type, String aspect) {
        public static final Codec<Cell> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("q").forGetter(Cell::q),
                Codec.INT.fieldOf("r").forGetter(Cell::r),
                Codec.INT.fieldOf("type").forGetter(Cell::type),
                Codec.STRING.optionalFieldOf("aspect", "").forGetter(Cell::aspect)
        ).apply(instance, Cell::new));

        public static final StreamCodec<ByteBuf, Cell> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Cell::q,
                ByteBufCodecs.VAR_INT, Cell::r,
                ByteBufCodecs.VAR_INT, Cell::type,
                ByteBufCodecs.STRING_UTF8, Cell::aspect,
                Cell::new);

        public Hex hex() {
            return new Hex(this.q, this.r);
        }

        public Aspect aspectOrNull() {
            return this.aspect.isEmpty() ? null : Aspect.of(this.aspect);
        }
    }

    public static final Codec<ResearchNote> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("key").forGetter(ResearchNote::key),
            Codec.INT.fieldOf("color").forGetter(ResearchNote::color),
            Codec.BOOL.fieldOf("complete").forGetter(ResearchNote::complete),
            Codec.INT.fieldOf("copies").forGetter(ResearchNote::copies),
            Cell.CODEC.listOf().fieldOf("hexgrid").forGetter(ResearchNote::cells)
    ).apply(instance, ResearchNote::new));

    public static final StreamCodec<ByteBuf, ResearchNote> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ResearchNote::key,
            ByteBufCodecs.INT, ResearchNote::color,
            ByteBufCodecs.BOOL, ResearchNote::complete,
            ByteBufCodecs.VAR_INT, ResearchNote::copies,
            Cell.STREAM_CODEC.apply(ByteBufCodecs.list()), ResearchNote::cells,
            ResearchNote::new);

    /** As casas pela chave {@code "q:r"}, na ordem em que estão guardadas. */
    public Map<String, Cell> byKey() {
        Map<String, Cell> map = new LinkedHashMap<>();
        for (Cell cell : this.cells) map.put(cell.hex().key(), cell);
        return map;
    }

    public ResearchNote with(Map<String, Cell> cells, boolean complete, int copies) {
        return new ResearchNote(this.key, this.color, complete, copies, new ArrayList<>(cells.values()));
    }
}
