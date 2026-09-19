package net.thaumcraft.world.outer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.thaumcraft.Thaumcraft;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * O labirinto das Terras de Fora: o {@code MazeHandler} e o {@code MazeThread} da 4.2.3.5. Uma tabela só para o mundo
 * todo, de chunk para casa, preenchida quando um anel eldritch nasce no mundo de cima ou quando alguém põe um olho no
 * altar — em outra linha de execução, como no original, para não travar o jogo. As casas são os chunks das Terras de Fora
 * nas mesmas coordenadas do anel, e é lá que o chunk é construído quando alguém chega.
 *
 * <p>No original ia num arquivo à parte ({@code labyrinth.dat}); aqui é um dado salvo do mundo.
 */
public final class Labyrinth extends SavedData {
    private record Entry(int x, int z, short cell) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("x").forGetter(Entry::x),
                Codec.INT.fieldOf("z").forGetter(Entry::z),
                Codec.SHORT.fieldOf("cell").forGetter(Entry::cell)).apply(i, Entry::new));
    }

    private static final Codec<Labyrinth> CODEC = Entry.CODEC.listOf().fieldOf("cells").codec().xmap(Labyrinth::new, Labyrinth::entries);
    private static final SavedDataType<Labyrinth> TYPE = new SavedDataType<>(Thaumcraft.id("labyrinth"), Labyrinth::new, CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    /** O do mundo aberto agora (o original também guardava numa tabela estática). */
    @Nullable
    private static volatile Labyrinth current;

    private final Map<Long, Short> cells = new ConcurrentHashMap<>();

    private Labyrinth() {
    }

    private Labyrinth(List<Entry> entries) {
        for (Entry e : entries) this.cells.put(key(e.x, e.z), e.cell);
    }

    private List<Entry> entries() {
        List<Entry> out = new ArrayList<>();
        this.cells.forEach((k, v) -> {
            if (v > 0) out.add(new Entry((int) (k >> 32), (int) (long) k, v));
        });
        return out;
    }

    private static long key(int x, int z) {
        return (long) x << 32 | z & 0xFFFFFFFFL;
    }

    public static void init() {
        ServerLevelEvents.LOAD.register((server, level) -> {
            if (level.dimension() == Level.OVERWORLD) current = server.getDataStorage().computeIfAbsent(TYPE);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> current = null);
    }

    /** O labirinto do mundo, carregando se preciso. */
    @Nullable
    public static Labyrinth get(@Nullable MinecraftServer server) {
        Labyrinth l = current;
        if (l == null && server != null) current = l = server.getDataStorage().computeIfAbsent(TYPE);
        return l;
    }

    @Nullable
    public Cell cell(int chunkX, int chunkZ) {
        Short raw = this.cells.get(key(chunkX, chunkZ));
        return raw == null ? null : new Cell(raw);
    }

    private void putRaw(int x, int z, short cell) {
        this.cells.put(key(x, z), cell);
        this.setDirty();
    }

    private void remove(int x, int z) {
        this.cells.remove(key(x, z));
        this.setDirty();
    }

    private short raw(int x, int z) {
        Short raw = this.cells.get(key(x, z));
        return raw == null ? 0 : raw;
    }

    /** O {@code mazesInRange}: há alguma casa já reservada no retângulo em volta deste chunk? */
    public boolean mazesInRange(int chunkX, int chunkZ, int w, int h) {
        for (int x = -w; x <= w; x++) {
            for (int z = -h; z <= h; z++) {
                if (this.cells.containsKey(key(chunkX + x, chunkZ + z))) return true;
            }
        }
        return false;
    }

    /** O {@code MazeThread}: reserva os cantos na hora e traça o labirinto em outra linha de execução. */
    public void reserve(int x, int z, int w, int h, long seed) {
        this.putRaw(x, z, (short) 0);
        this.putRaw(x - w, z - h, (short) 0);
        this.putRaw(x + w, z + h, (short) 0);
        this.putRaw(x - w, z + h, (short) 0);
        this.putRaw(x + w, z - h, (short) 0);
        Thread thread = new Thread(() -> this.build(x, z, w, h, seed), "Thaumcraft labirinto");
        thread.setDaemon(true);
        thread.start();
    }

    /** O corpo do {@code MazeThread}, à parte para os testes poderem esperar por ele. */
    public void build(int x, int z, int w, int h, long seed) {
        MazeGenerator gen = new MazeGenerator(w, h, seed++);
        while (!gen.generate()) gen = new MazeGenerator(w, h, seed++);
        int col = x - (1 + w / 2);
        int row = z - (1 + h / 2);
        for (int a = 0; a < w; a++) {
            for (int b = 0; b < h; b++) {
                if (gen.grid[b][a] > 0) this.putRaw(a + col, b + row, (short) gen.grid[b][a]);
            }
        }
        for (int[] corner : new int[][]{{x, z}, {x - w, z - h}, {x + w, z + h}, {x - w, z + h}, {x + w, z - h}}) {
            if (this.raw(corner[0], corner[1]) == 0) this.remove(corner[0], corner[1]);
        }
    }
}
