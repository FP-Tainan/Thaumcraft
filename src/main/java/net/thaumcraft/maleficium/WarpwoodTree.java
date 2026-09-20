package net.thaumcraft.maleficium;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;

/**
 * A árvore distorcida: o {@code WorldGenWarpwoodTree} do Tainted Magic 8.1.1, conta por conta.
 *
 * <p>Um tronco em cruz de cinco por cinco na base, subindo entre sete e onze blocos, com uma copa de folhas em
 * bola achatada no alto; aqui e ali o tronco cria um <b>nó</b>, que é duro de quebrar e guarda sementes do vazio.
 * Ao pé dela nasce um pé de beladona.
 */
public final class WarpwoodTree {
    private WarpwoodTree() {
    }

    /** O {@code generate} do original: a muda chama com sete e cinco. */
    public static boolean generate(LevelAccessor level, RandomSource random, BlockPos origin, int minHeight, int randomHeight) {
        int x = origin.getX(), y = origin.getY(), z = origin.getZ();
        int height = random.nextInt(randomHeight) + minHeight;
        int bottom = level.getMinY();
        int top = level.getMaxY();
        if (y < bottom + 1 || y + height + 1 > top) return false;

        // tem espaço? a base pede uma coluna livre, e o alto, três blocos para cada lado
        for (int iy = y; iy <= y + 1 + height; iy++) {
            int spread = iy == y ? 0 : iy >= y + 1 + height - 2 ? 3 : 1;
            for (int ix = x - spread; ix <= x + spread; ix++) {
                for (int iz = z - spread; iz <= z + spread; iz++) {
                    if (iy < bottom || iy > top) return false;
                    BlockPos pos = new BlockPos(ix, iy, iz);
                    BlockState state = level.getBlockState(pos);
                    if (iy > y && !state.isAir() && !state.is(net.minecraft.tags.BlockTags.LEAVES)
                            && !state.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }

        // o chão que segura planta: é o que o {@code canSustainPlant} do original pergunta
        BlockPos ground = new BlockPos(x, y - 1, z);
        BlockState below = level.getBlockState(ground);
        if (!below.is(net.minecraft.tags.BlockTags.SUPPORTS_VEGETATION) && !below.is(net.minecraft.world.level.block.Blocks.FARMLAND)) {
            return false;
        }
        if (y + height + 1 >= top) return false;

        // a copa: uma bola achatada de folhas em volta do alto do tronco
        int start = y + height - 5;
        int end = y + height + 3 + random.nextInt(3);
        for (int iy = start; iy <= end; iy++) {
            int centre = Mth.clamp(iy, y + height - 3, y + height);
            for (int ix = x - 5; ix <= x + 5; ix++) {
                for (int iz = z - 5; iz <= z + 5; iz++) {
                    double dx = ix - x, dy = iy - centre, dz = iz - z;
                    double distance = dx * dx + dy * dy + dz * dz;
                    BlockPos pos = new BlockPos(ix, iy, iz);
                    if (distance < 10 + random.nextInt(8) && level.getBlockState(pos).canBeReplaced()) {
                        leaves(level, pos);
                    }
                }
            }
        }

        // o tronco, que de vez em quando cria um nó — nunca dois seguidos
        int chance = height;
        boolean lastWasKnot = false;
        int up = 0;
        for (; up < height; up++) {
            BlockPos pos = new BlockPos(x, y + up, z);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && !state.is(net.minecraft.tags.BlockTags.LEAVES) && !state.canBeReplaced()) continue;
            if (up > 0 && !lastWasKnot && random.nextInt(chance) == 0) {
                knot(level, pos);
                lastWasKnot = true;
            } else {
                log(level, pos, Direction.Axis.Y);
                lastWasKnot = false;
                chance--;
            }
            log(level, pos.west(), Direction.Axis.Y);
            log(level, pos.east(), Direction.Axis.Y);
            log(level, pos.north(), Direction.Axis.Y);
            log(level, pos.south(), Direction.Axis.Y);
        }
        log(level, new BlockPos(x, y + up, z), Direction.Axis.Y);

        // os quatro pés das quinas, e os braços deitados que saem da base
        corners(level, random, x, y, z, 0);
        corners(level, random, x, y + height - 5, z, height - 4);
        arms(level, x, y, z);
        arms(level, x, y + height - 4, z);
        for (BlockPos pos : new BlockPos[]{new BlockPos(x - 2, y - 1, z), new BlockPos(x + 2, y - 1, z),
                new BlockPos(x, y - 1, z - 2), new BlockPos(x, y - 1, z + 2)}) {
            log(level, pos, Direction.Axis.Y);
        }

        // e a beladona ao pé da árvore
        BlockPos bush = new BlockPos(x + random.nextInt(5) - 2, y, z + random.nextInt(5) - 2);
        if (level.getBlockState(bush).isAir()
                && MaleficiumBlocks.NIGHTSHADE_BUSH.defaultBlockState().canSurvive(level, bush)) {
            level.setBlock(bush, MaleficiumBlocks.NIGHTSHADE_BUSH.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    /** As quatro quinas da base e as do alto: as de cima só duas em cada três vezes. */
    private static void corners(LevelAccessor level, RandomSource random, int x, int y, int z, int offset) {
        boolean base = offset == 0;
        for (int[] corner : new int[][]{{-1, -1}, {1, 1}, {-1, 1}, {1, -1}}) {
            BlockPos pos = new BlockPos(x + corner[0], y, z + corner[1]);
            if (base) {
                log(level, pos, Direction.Axis.Y);
                if (random.nextInt(3) != 0) log(level, pos.above(), Direction.Axis.Y);
            } else {
                log(level, pos.above(), Direction.Axis.Y);
                if (random.nextInt(3) == 0) log(level, pos, Direction.Axis.Y);
            }
        }
    }

    /** Os quatro braços deitados, dois no eixo X e dois no Z. */
    private static void arms(LevelAccessor level, int x, int y, int z) {
        log(level, new BlockPos(x - 2, y, z), Direction.Axis.X);
        log(level, new BlockPos(x + 2, y, z), Direction.Axis.X);
        log(level, new BlockPos(x, y, z - 2), Direction.Axis.Z);
        log(level, new BlockPos(x, y, z + 2), Direction.Axis.Z);
    }

    private static void log(LevelAccessor level, BlockPos pos, Direction.Axis axis) {
        BlockState state = level.getBlockState(pos);
        if (!state.isAir() && !state.is(net.minecraft.tags.BlockTags.LEAVES) && !state.canBeReplaced()) return;
        level.setBlock(pos, MaleficiumBlocks.WARPWOOD_LOG.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, axis), Block.UPDATE_CLIENTS);
    }

    private static void knot(LevelAccessor level, BlockPos pos) {
        level.setBlock(pos, MaleficiumBlocks.WARPWOOD_KNOT.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    private static void leaves(LevelAccessor level, BlockPos pos) {
        level.setBlock(pos, MaleficiumBlocks.WARPWOOD_LEAVES.defaultBlockState()
                .setValue(BlockStateProperties.PERSISTENT, false), Block.UPDATE_CLIENTS);
    }
}
