package net.thaumcraft.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCBlocks;

import java.util.List;

/**
 * O {@code generateOres} do {@code ThaumcraftWorldGenerator} da 4.2.3.5, uma vez por pedaço de mundo:
 * <ul>
 *     <li>cinábrio: 18 tentativas de um bloco só, na pedra, da altura zero até 51 (um quinto do mundo de então);</li>
 *     <li>âmbar: 20 tentativas de um bloco só, na pedra, até 24 abaixo da superfície;</li>
 *     <li>pedra infundida: 8 veios de até seis blocos, na pedra, da altura zero até cinco abaixo da superfície; o
 *     aspecto é sorteado, e uma vez em três sai o aspecto do bioma.</li>
 * </ul>
 * As alturas são as do mundo de 1.7.10, que começava no zero; abaixo disso é ardósia, onde o original não chegava.
 */
public class ThaumOresFeature extends Feature<NoneFeatureConfiguration> {
    private static final TagMatchTest STONE = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
    private static final List<String> PRIMALS = List.of("air", "fire", "water", "earth", "order", "entropy");

    public ThaumOresFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int cx = origin.getX() & ~15, cz = origin.getZ() & ~15;

        for (int i = 0; i < 18; i++) {
            BlockPos at = new BlockPos(cx + random.nextInt(16), random.nextInt(256 / 5), cz + random.nextInt(16));
            if (STONE.test(level.getBlockState(at), random)) level.setBlock(at, TCBlocks.CINNABAR_ORE.defaultBlockState(), 2);
        }
        for (int i = 0; i < 20; i++) {
            int x = cx + random.nextInt(16), z = cz + random.nextInt(16);
            BlockPos at = new BlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - random.nextInt(25), z);
            if (STONE.test(level.getBlockState(at), random)) level.setBlock(at, TCBlocks.AMBER_ORE.defaultBlockState(), 2);
        }
        for (int i = 0; i < 8; i++) {
            int x = cx + random.nextInt(16), z = cz + random.nextInt(16);
            int y = random.nextInt(Math.max(5, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 5));
            int kind = random.nextInt(6);
            if (random.nextInt(3) == 0) {
                BlockPos here = new BlockPos(x, y, z);
                Aspect tag = BiomeAura.aspectOf(level.getBiome(here), random);
                int index = tag == null ? -1 : List.of(Aspects.AIR, Aspects.FIRE, Aspects.WATER, Aspects.EARTH, Aspects.ORDER, Aspects.ENTROPY).indexOf(tag);
                kind = index >= 0 ? index : random.nextInt(6);
            }
            BlockState vein = TCBlocks.INFUSED_STONE.get(PRIMALS.get(kind)).defaultBlockState();
            Feature.ORE.place(new OreConfiguration(List.of(OreConfiguration.target(STONE, vein)), 6), level, context.chunkGenerator(), random,
                    new BlockPos(x, y, z));
        }
        return true;
    }
}
