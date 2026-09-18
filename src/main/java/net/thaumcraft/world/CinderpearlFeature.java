package net.thaumcraft.world;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.thaumcraft.registry.TCBlocks;

/**
 * As pérolas de cinzas pelo deserto: o fim do {@code generateVegetation} da 4.2.3.5.
 *
 * <p>Uma chance em trinta por pedaço de mundo, só em terra quente (mais de um grau) de chão de areia — o
 * deserto e as terras áridas —, com o {@code WorldGenCustomFlowers}: dezoito tentativas em volta, em cima de
 * grama ou areia.
 */
public class CinderpearlFeature extends Feature<NoneFeatureConfiguration> {
    public CinderpearlFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        Holder<Biome> biome = level.getBiome(pos);
        if (!(biome.is(ConventionalBiomeTags.IS_DESERT) || biome.is(ConventionalBiomeTags.IS_BADLANDS))) return false;
        if (!(biome.value().getBaseTemperature() > 1.0f)) return false;
        boolean placed = false;
        for (int i = 0; i < 18; i++) {
            BlockPos at = pos.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4),
                    random.nextInt(8) - random.nextInt(8));
            BlockState below = level.getBlockState(at.below());
            if (level.isEmptyBlock(at) && (below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.SAND) || below.is(Blocks.RED_SAND))) {
                level.setBlock(at, TCBlocks.CINDERPEARL.defaultBlockState(), Block.UPDATE_CLIENTS);
                placed = true;
            }
        }
        return placed;
    }
}
