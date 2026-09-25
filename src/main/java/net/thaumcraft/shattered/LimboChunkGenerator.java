package net.thaumcraft.shattered;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * O chão do Limbo: o {@code ChunkGeneratorLimbo} das Portas Dimensionais.
 *
 * <p>É uma terra de tecido desfiado, de relevo baixo e sem céu, assente num chão de tecido eterno — o único que o
 * Limbo não come, e a única saída que ele tem.
 *
 * <p><b>Diferença declarada:</b> o original faz o relevo com o ruído de oitavas do jogo de 2014, que hoje não
 * existe do mesmo jeito. Aqui o relevo sai do ruído de Perlin do jogo de agora, com o mesmo feitio: ondas largas e
 * baixas. O que se vê é o mesmo; os números por baixo, não.
 */
public class LimboChunkGenerator extends ChunkGenerator {
    public static final MapCodec<LimboChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource)).apply(i, LimboChunkGenerator::new));

    /** O chão de tecido eterno, e o tecido desfiado por cima. */
    public static final int FLOOR = 8;
    /** A altura do meio do relevo. */
    public static final int BASE = 40;
    /** E o quanto ele sobe e desce. */
    public static final int SWING = 14;

    public LimboChunkGenerator(BiomeSource biomes) {
        super(biomes);
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
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState random, ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public int getGenDepth() {
        return 256;
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
        BlockState desfiado = FabricBlocks.UNRAVELLED.defaultBlockState();
        BlockState eterno = FabricBlocks.ETERNAL.defaultBlockState();
        PerlinSimplexNoise ruído = noise(random);
        ChunkPos onde = chunk.getPos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int alto = height(ruído, onde.getMinBlockX() + x, onde.getMinBlockZ() + z);
                for (int y = 0; y <= alto; y++) {
                    cursor.set(onde.getMinBlockX() + x, y, onde.getMinBlockZ() + z);
                    chunk.setBlockState(cursor, y <= FLOOR ? eterno : desfiado);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    /** O relevo daquela casa. */
    private static int height(PerlinSimplexNoise ruído, int x, int z) {
        double largo = ruído.getValue(x / 90.0, z / 90.0, false);
        double miúdo = ruído.getValue(x / 17.0, z / 17.0, false);
        return Mth.clamp((int) (BASE + largo * SWING + miúdo * 3.0), FLOOR + 1, 200);
    }

    /**
     * O ruído do Limbo é sempre o mesmo, em todo mundo: o Limbo não é um lugar de cada um, é o mesmo lugar
     * nenhum para toda a gente. E assim não se monta ruído novo a cada pedaço.
     */
    private static final PerlinSimplexNoise NOISE = new PerlinSimplexNoise(
            new WorldgenRandom(new LegacyRandomSource(0x11B0L)), List.of(-4, -3, -2, -1, 0));

    private static PerlinSimplexNoise noise(RandomState random) {
        return NOISE;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return height(noise(random), x, z) + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        BlockState desfiado = FabricBlocks.UNRAVELLED.defaultBlockState();
        BlockState eterno = FabricBlocks.ETERNAL.defaultBlockState();
        int alto = height(noise(random), x, z);
        BlockState[] coluna = new BlockState[level.getHeight()];
        for (int y = 0; y < coluna.length; y++) {
            int mundo = level.getMinY() + y;
            coluna[y] = mundo > alto ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
                    : (mundo <= FLOOR ? eterno : desfiado);
        }
        return new NoiseColumn(level.getMinY(), coluna);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
    }
}
