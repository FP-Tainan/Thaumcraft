package net.thaumcraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.registry.TCBlocks;

/**
 * O pinheiro-de-prata: o {@code WorldGenSilverwoodTrees} da 4.2.3.5, descompilado e traduzido conta por conta.
 *
 * <p>Um tronco em cruz, com raízes deitadas nos quatro lados e uma copa redonda de folhas prateadas. Pelo
 * tronco, de vez em quando, um pedaço vira nó — um tronco com um nó de aura puro dentro. Quando é o mundo
 * nascendo, a folha-cintilante brota em volta do pé.
 */
public final class SilverwoodTree {
    private SilverwoodTree() {
    }

    /**
     * @param worldgen se é o mundo nascendo (flores em volta, sem avisar vizinhos) ou uma muda crescendo
     */
    public static boolean generate(LevelAccessor level, RandomSource random, BlockPos pos, int minHeight,
                                   int randomHeight, boolean worldgen) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        int flags = worldgen ? Block.UPDATE_CLIENTS : Block.UPDATE_ALL;
        int height = random.nextInt(randomHeight) + minHeight;
        if (y < level.getMinY() + 1 || y + height + 1 > level.getMaxY()) return false;

        for (int yy = y; yy <= y + 1 + height; yy++) {
            int spread = 1;
            if (yy == y) spread = 0;
            if (yy >= y + 1 + height - 2) spread = 3;
            for (int xx = x - spread; xx <= x + spread; xx++) {
                for (int zz = z - spread; zz <= z + spread; zz++) {
                    BlockState state = level.getBlockState(new BlockPos(xx, yy, zz));
                    if (!state.isAir() && !state.is(BlockTags.LEAVES) && !state.canBeReplaced() && yy > y) return false;
                }
            }
        }

        if (!TreeLeaves.isSoil(level.getBlockState(pos.below())) || y >= level.getMaxY() - height - 1) return false;
        // o onPlantGrow do original: a grama debaixo vira terra
        if (level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)) level.setBlock(pos.below(), Blocks.DIRT.defaultBlockState(), flags);

        TreeLeaves leaves = new TreeLeaves();
        BlockState leaf = TCBlocks.SILVERWOOD_LEAVES.defaultBlockState();
        int start = y + height - 5;
        int end = y + height + 3 + random.nextInt(3);
        for (int yy = start; yy <= end; yy++) {
            int cty = Mth.clamp(yy, y + height - 3, y + height);
            for (int xx = x - 5; xx <= x + 5; xx++) {
                for (int zz = z - 5; zz <= z + 5; zz++) {
                    double dx = xx - x, dy = yy - cty, dz = zz - z;
                    double dist = dx * dx + dy * dy + dz * dz;
                    BlockPos at = new BlockPos(xx, yy, zz);
                    if (dist < 10 + random.nextInt(8) && replaceableByLeaves(level.getBlockState(at))) {
                        leaves.place(level, at, leaf, flags);
                    }
                }
            }
        }

        int chance = (int) (height * 1.5);
        boolean lastKnot = false;
        int k;
        for (k = 0; k < height; k++) {
            BlockPos at = new BlockPos(x, y + k, z);
            BlockState state = level.getBlockState(at);
            if (state.isAir() || state.is(BlockTags.LEAVES) || state.canBeReplaced()) {
                if (k > 0 && !lastKnot && random.nextInt(chance) == 0) {
                    level.setBlock(at, TCBlocks.SILVERWOOD_KNOT.defaultBlockState(), flags);
                    NodeFeature.setupNode(level, at, random, true);
                    chance += height;
                    lastKnot = true;
                } else {
                    log(level, leaves, at, Direction.Axis.Y, flags);
                    lastKnot = false;
                }
                log(level, leaves, at.west(), Direction.Axis.Y, flags);
                log(level, leaves, at.east(), Direction.Axis.Y, flags);
                log(level, leaves, at.north(), Direction.Axis.Y, flags);
                log(level, leaves, at.south(), Direction.Axis.Y, flags);
            }
        }

        log(level, leaves, new BlockPos(x, y + k, z), Direction.Axis.Y, flags);
        log(level, leaves, new BlockPos(x - 1, y, z - 1), Direction.Axis.Y, flags);
        log(level, leaves, new BlockPos(x + 1, y, z + 1), Direction.Axis.Y, flags);
        log(level, leaves, new BlockPos(x - 1, y, z + 1), Direction.Axis.Y, flags);
        log(level, leaves, new BlockPos(x + 1, y, z - 1), Direction.Axis.Y, flags);
        if (random.nextInt(3) != 0) log(level, leaves, new BlockPos(x - 1, y + 1, z - 1), Direction.Axis.Y, flags);
        if (random.nextInt(3) != 0) log(level, leaves, new BlockPos(x + 1, y + 1, z + 1), Direction.Axis.Y, flags);
        if (random.nextInt(3) != 0) log(level, leaves, new BlockPos(x - 1, y + 1, z + 1), Direction.Axis.Y, flags);
        if (random.nextInt(3) != 0) log(level, leaves, new BlockPos(x + 1, y + 1, z - 1), Direction.Axis.Y, flags);
        // as raízes deitadas
        log(level, leaves, new BlockPos(x - 2, y, z), Direction.Axis.X, flags);
        log(level, leaves, new BlockPos(x + 2, y, z), Direction.Axis.X, flags);
        log(level, leaves, new BlockPos(x, y, z - 2), Direction.Axis.Z, flags);
        log(level, leaves, new BlockPos(x, y, z + 2), Direction.Axis.Z, flags);
        log(level, leaves, new BlockPos(x - 2, y - 1, z), Direction.Axis.Y, flags);
        log(level, leaves, new BlockPos(x + 2, y - 1, z), Direction.Axis.Y, flags);
        log(level, leaves, new BlockPos(x, y - 1, z - 2), Direction.Axis.Y, flags);
        log(level, leaves, new BlockPos(x, y - 1, z + 2), Direction.Axis.Y, flags);
        // os galhos debaixo da copa
        int top = y + (height - 4);
        log(level, leaves, new BlockPos(x - 1, top, z - 1), Direction.Axis.Y, flags);
        log(level, leaves, new BlockPos(x + 1, top, z + 1), Direction.Axis.Y, flags);
        log(level, leaves, new BlockPos(x - 1, top, z + 1), Direction.Axis.Y, flags);
        log(level, leaves, new BlockPos(x + 1, top, z - 1), Direction.Axis.Y, flags);
        if (random.nextInt(3) == 0) log(level, leaves, new BlockPos(x - 1, top - 1, z - 1), Direction.Axis.Y, flags);
        if (random.nextInt(3) == 0) log(level, leaves, new BlockPos(x + 1, top - 1, z + 1), Direction.Axis.Y, flags);
        if (random.nextInt(3) == 0) log(level, leaves, new BlockPos(x - 1, top - 1, z + 1), Direction.Axis.Y, flags);
        if (random.nextInt(3) == 0) log(level, leaves, new BlockPos(x + 1, top - 1, z - 1), Direction.Axis.Y, flags);
        log(level, leaves, new BlockPos(x - 2, top, z), Direction.Axis.X, flags);
        log(level, leaves, new BlockPos(x + 2, top, z), Direction.Axis.X, flags);
        log(level, leaves, new BlockPos(x, top, z - 2), Direction.Axis.Z, flags);
        log(level, leaves, new BlockPos(x, top, z + 2), Direction.Axis.Z, flags);
        leaves.settle(level, flags);

        if (worldgen) shimmerleaves(level, random, pos);
        return true;
    }

    private static void log(LevelAccessor level, TreeLeaves leaves, BlockPos pos, Direction.Axis axis, int flags) {
        level.setBlock(pos, TCBlocks.SILVERWOOD_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis), flags);
        leaves.forget(pos);
    }

    /** O {@code canBeReplacedByLeaves} do Forge: tudo o que não é bloco cheio de verdade. */
    private static boolean replaceableByLeaves(BlockState state) {
        return state.isAir() || state.is(BlockTags.LEAVES) || state.canBeReplaced();
    }

    /** O {@code WorldGenCustomFlowers} com a folha-cintilante: dezoito tentativas em volta do pé. */
    private static void shimmerleaves(LevelAccessor level, RandomSource random, BlockPos pos) {
        for (int i = 0; i < 18; i++) {
            BlockPos at = pos.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4),
                    random.nextInt(8) - random.nextInt(8));
            BlockState below = level.getBlockState(at.below());
            if (level.isEmptyBlock(at) && (below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.SAND))) {
                level.setBlock(at, TCBlocks.SHIMMERLEAF.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }
}
