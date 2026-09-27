package net.thaumcraft.occulta.mirror;

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
 * O chão do Mundo do Espelho: o {@code WorldChunkManagerMirror} do Witchery.
 *
 * <p>Não tem relevo nem nada que se pareça com terra. É uma <b>colmeia</b>: cada pedaço de dezesseis por
 * dezesseis por dezesseis é uma <b>cela</b> de nove de lado, forrada de superfície de espelho, e as celas não se
 * ligam umas às outras. Quem vai para lá aparece numa delas, e só sai pelo espelho por onde entrou.
 *
 * <p>O desenho é o do original, casa por casa: as duas tabelas dele dizem quais casas de cada pedaço são parede.
 * Em <b>x</b> e em <b>z</b>, as quatro primeiras e as três últimas; em <b>y</b>, as seis de baixo e a última. O
 * que sobra — de quatro a doze em x e z, de seis a catorze em y — é o vão da cela.
 */
public class MirrorChunkGenerator extends ChunkGenerator {
    public static final MapCodec<MirrorChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource))
            .apply(i, MirrorChunkGenerator::new));

    /** Quais casas de {@code x} e de {@code z} são parede: o {@code wallPointsXZ} do original. */
    private static final boolean[] WALL_XZ = flags(1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1);
    /** E quais casas de {@code y}: o {@code wallPointsY}. */
    private static final boolean[] WALL_Y = flags(1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1);

    /** Quanto o mundo tem de alto, em pedaços de dezesseis. */
    public static final int HEIGHT = 256;

    public MirrorChunkGenerator(BiomeSource biomes) {
        super(biomes);
    }

    private static boolean[] flags(int... quais) {
        boolean[] feito = new boolean[quais.length];
        for (int i = 0; i < quais.length; i++) feito[i] = quais[i] == 1;
        return feito;
    }

    /** Se aquela casa de um pedaço é parede. As contas são em coordenada de mundo; o resto é resto. */
    public static boolean isWall(int x, int y, int z) {
        if (y < 0 || y >= HEIGHT) return true;
        return WALL_Y[y & 15] || WALL_XZ[x & 15] || WALL_XZ[z & 15];
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState random, BiomeManager biomes,
                             StructureManager structures, ChunkAccess chunk) {
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState random,
                             ChunkAccess chunk) {
    }

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
        BlockState parede = net.thaumcraft.occulta.OccultaBlocks.MIRROR_WALL.defaultBlockState();
        ChunkPos onde = chunk.getPos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int mx = onde.getMinBlockX() + x;
                    int mz = onde.getMinBlockZ() + z;
                    if (!isWall(mx, y, mz)) continue;
                    chunk.setBlockState(cursor.set(mx, y, mz), parede);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        // o chão da cela daquela casa: a primeira parede que há debaixo do vão
        for (int y = HEIGHT - 1; y >= 0; y--) {
            if (isWall(x, y, z)) return y + 1;
        }
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        BlockState parede = net.thaumcraft.occulta.OccultaBlocks.MIRROR_WALL.defaultBlockState();
        BlockState ar = Blocks.AIR.defaultBlockState();
        BlockState[] coluna = new BlockState[level.getHeight()];
        for (int y = 0; y < coluna.length; y++) {
            coluna[y] = isWall(x, level.getMinY() + y, z) ? parede : ar;
        }
        return new NoiseColumn(level.getMinY(), coluna);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
    }
}
