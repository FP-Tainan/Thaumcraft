package net.thaumcraft.occulta.torment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * O chão do Tormento: o {@code WorldChunkManagerTorment} do Witchery.
 *
 * <p>O mundo é <b>vazio</b>. Nada de relevo, nada de minério, nada de caverna: ar do fundo ao topo, e no meio
 * dele seis lajes flutuando, uma por andar do labirinto, com oito blocos de escuro entre uma e a seguinte.
 *
 * <p>O original escreve o labirinto no instante em que o pedaço (0,0) se povoa, com um bloco de cada vez, a
 * atravessar pedaços que ainda não nasceram. Hoje isso não se faz: cada pedaço se gera por si. Então o
 * labirinto é desenhado numa planta — o {@link TormentMaze} — e aqui cada pedaço copia dela o que lhe toca.
 * O desenho é o mesmo; o que muda é que ele passou a ser uma conta em vez de uma escrita.
 *
 * <p>A planta é feita <b>uma vez</b>, na primeira vez que alguém a pede, a partir da semente do mundo — e é
 * por isso que o labirinto deste mundo é sempre este labirinto, e não um novo a cada vez que o pedaço se
 * recarrega.
 */
public class TormentChunkGenerator extends ChunkGenerator {
    public static final MapCodec<TormentChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource),
            com.mojang.serialization.Codec.LONG.fieldOf("seed").forGetter(g -> g.semente))
            .apply(i, TormentChunkGenerator::new));

    /** Quanto o mundo tem de alto. O topo do último andar fica no noventa, e sobra. */
    public static final int HEIGHT = 128;

    private final long semente;
    private TormentMaze planta;

    public TormentChunkGenerator(BiomeSource biomes, long semente) {
        super(biomes);
        this.semente = semente;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    /** A planta deste mundo, desenhando-a na primeira vez que alguém a pede. */
    public synchronized TormentMaze maze() {
        if (this.planta == null) this.planta = new TormentMaze(this.semente);
        return this.planta;
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState random, BiomeManager biomes,
                             StructureManager structures, ChunkAccess chunk) {
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState random,
                             ChunkAccess chunk) {
    }

    /** No Tormento não nasce nada sozinho: o {@code getPossibleCreatures} do original devolve nulo. */
    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public int getGenDepth() {
        return HEIGHT;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState random,
                                                        StructureManager structures, ChunkAccess chunk) {
        TormentMaze maze = maze();
        ChunkPos onde = chunk.getPos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int deBaixo = TormentMaze.floorOf(0) - TormentMaze.FLOOR_DOWN;
        int deCima = TormentMaze.floorOf(TormentMaze.LEVELS - 1) + TormentMaze.WALL;

        for (int y = deBaixo; y <= deCima && y < HEIGHT; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int mx = onde.getMinBlockX() + x;
                    int mz = onde.getMinBlockZ() + z;
                    byte marca = maze.at(mx, y, mz);
                    if (marca == TormentMaze.NADA || marca == TormentMaze.AR) continue;
                    BlockState posto = TormentMaze.blockOf(marca);
                    chunk.setBlockState(cursor.set(mx, y, mz), posto);
                    /*
                     * E o baú precisa de alma. Um pedaço em geração não a faz sozinho pelo feitio do
                     * bloco — quem a faz é o mundo quando o pedaço entra nele —, de modo que ela se põe
                     * aqui à mão, como qualquer estrutura do jogo faz com os baús dela.
                     */
                    if (marca == TormentMaze.BAÚ) {
                        chunk.setBlockEntity(new RefillingChestBlockEntity(cursor.immutable(), posto));
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    /** O chão de pisar daquela casa: o topo do andar mais alto que tiver chão ali. */
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level,
                             RandomState random) {
        TormentMaze maze = maze();
        for (int andar = TormentMaze.LEVELS - 1; andar >= 0; andar--) {
            int chão = TormentMaze.floorOf(andar);
            byte marca = maze.at(x, chão, z);
            if (marca == TormentMaze.PEDRA || marca == TormentMaze.MICÉLIO) return chão + 1;
        }
        return getMinY();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        TormentMaze maze = maze();
        BlockState[] coluna = new BlockState[level.getHeight()];
        for (int k = 0; k < coluna.length; k++) {
            byte marca = maze.at(x, level.getMinY() + k, z);
            coluna[k] = marca == TormentMaze.NADA || marca == TormentMaze.AR
                    ? Blocks.AIR.defaultBlockState() : TormentMaze.blockOf(marca);
        }
        return new NoiseColumn(level.getMinY(), coluna);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
    }
}
