package net.thaumcraft.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.thaumcraft.block.ManaPodBlock;
import net.thaumcraft.block.entity.ManaPodBlockEntity;
import net.thaumcraft.registry.TCBlocks;

/**
 * O que o {@code decorate} da Floresta Mágica e da Terra Maculada da 4.2.3.5 põe além do que o jogo já sabe fazer:
 * as vagens de mana, os cogumelos-vis junto das toras, o pinheiro-de-prata mais alto da floresta e as fibras de mácula.
 */
public final class MagicalForestFeatures {
    private MagicalForestFeatures() {
    }

    /** O {@code WorldGenManaPods}: da altura 64 para cima, a primeira tora com dois vãos embaixo ganha uma vagem. */
    public static class ManaPods extends Feature<NoneFeatureConfiguration> {
        public ManaPods(Codec<NoneFeatureConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            WorldGenLevel level = context.level();
            RandomSource random = context.random();
            int x0 = context.origin().getX(), z0 = context.origin().getZ();
            int x = x0, z = z0;
            int top = Math.min(128, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z));
            for (int y = 64; y < top; y++) {
                BlockPos pos = new BlockPos(x, y, z);
                if (level.isEmptyBlock(pos) && level.isEmptyBlock(pos.below())) {
                    if (ManaPodBlock.canHangAt(level, pos)) {
                        level.setBlock(pos, TCBlocks.MANA_POD.defaultBlockState().setValue(ManaPodBlock.AGE, 2 + random.nextInt(5)), 2);
                        if (level.getBlockEntity(pos) instanceof ManaPodBlockEntity pod) pod.checkGrowth();
                        return true;
                    }
                } else {
                    x = x0 + random.nextInt(4) - random.nextInt(4);
                    z = z0 + random.nextInt(4) - random.nextInt(4);
                }
            }
            return false;
        }
    }

    /** O cogumelo-vis: na grama, descendo até a altura 50, ao lado de alguma tora. */
    public static class Vishrooms extends Feature<NoneFeatureConfiguration> {
        public Vishrooms(Codec<NoneFeatureConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            WorldGenLevel level = context.level();
            int x = context.origin().getX(), z = context.origin().getZ();
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            while (y > 50 && !level.getBlockState(new BlockPos(x, y, z)).is(Blocks.GRASS_BLOCK)) y--;
            BlockPos grass = new BlockPos(x, y, z);
            if (!level.getBlockState(grass).is(Blocks.GRASS_BLOCK)) return false;
            BlockPos above = grass.above();
            if (!level.getBlockState(above).canBeReplaced() || !nextToWood(level, above)) return false;
            level.setBlock(above, TCBlocks.VISHROOM.defaultBlockState(), 2);
            return true;
        }

        private static boolean nextToWood(WorldGenLevel level, BlockPos pos) {
            for (BlockPos near : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                if (!near.equals(pos) && level.getBlockState(near).is(BlockTags.LOGS)) return true;
            }
            return false;
        }
    }

    /** O {@code WorldGenSilverwoodTrees(false, 8, 5)} da Floresta Mágica: um tanto mais alto que o de fora. */
    public static class TallSilverwood extends Feature<NoneFeatureConfiguration> {
        public TallSilverwood(Codec<NoneFeatureConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            return SilverwoodTree.generate(context.level(), context.random(), context.origin(), 8, 5, true);
        }
    }

    /** A grande-madeira da Floresta Mágica, sem tirar a sorte do bioma. */
    public static class ForestGreatwood extends Feature<NoneFeatureConfiguration> {
        public ForestGreatwood(Codec<NoneFeatureConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            return GreatwoodTree.generate(context.level(), context.random(), context.origin(), true, context.random().nextInt(8) == 0);
        }
    }

    /**
     * O {@code decorateSpecial} da Terra Maculada: dez tentativas de fibras em cima da grama (ou no lugar do mato em
     * cima dela) e oito de fibras em qualquer vão encostado em algo firme.
     */
    public static class TaintFibres extends Feature<NoneFeatureConfiguration> {
        public TaintFibres(Codec<NoneFeatureConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            WorldGenLevel level = context.level();
            RandomSource random = context.random();
            int cx = context.origin().getX() & ~15, cz = context.origin().getZ() & ~15;
            BlockState fibres = TCBlocks.TAINT_FIBRES.defaultBlockState();
            for (int a = 0; a < 10; a++) {
                int x = cx + random.nextInt(16), z = cz + random.nextInt(16);
                BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1, z);
                BlockState state = level.getBlockState(pos);
                if (state.isAir()) continue;
                if (state.is(Blocks.GRASS_BLOCK)) {
                    if (level.isEmptyBlock(pos.above())) level.setBlock(pos.above(), fibres, 2);
                } else if (state.canBeReplaced() && level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) {
                    level.setBlock(pos, fibres, 2);
                }
            }
            for (int a = 0; a < 8; a++) {
                int x = cx + random.nextInt(16), z = cz + random.nextInt(16);
                BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
                if (level.isEmptyBlock(pos) && nextToSolid(level, pos)) level.setBlock(pos, fibres, 2);
            }
            return true;
        }

        private static boolean nextToSolid(WorldGenLevel level, BlockPos pos) {
            for (Direction dir : Direction.values()) {
                if (level.getBlockState(pos.relative(dir)).isFaceSturdy(level, pos.relative(dir), dir.getOpposite())) return true;
            }
            return false;
        }
    }
}
