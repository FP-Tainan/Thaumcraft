package net.thaumcraft.forbidden;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A árvore maculada: o {@code WorldGenTaintedTree} do Forbidden Magic 0.575, conta por conta.
 *
 * <p>É a árvore de carvalho do jogo antigo, com tronco de um bloco e copa de quatro camadas, feita de tronco e
 * folhas maculados. Só cresce em chão que segura planta, e só onde couber inteira.
 */
public final class TaintedTree {
    private TaintedTree() {
    }

    /** O {@code generate} do original: a muda chama com quatro (o {@code minTreeHeight} dela). */
    public static boolean generate(LevelAccessor level, RandomSource random, BlockPos origin, int minHeight) {
        int x = origin.getX(), y = origin.getY(), z = origin.getZ();
        int height = random.nextInt(3) + minHeight;
        int bottom = level.getMinY();
        int top = level.getMaxY();
        if (y < bottom + 1 || y + height + 1 > top) return false;

        // tem espaço? a base pede a própria coluna, o meio um bloco para cada lado e o alto dois
        for (int iy = y; iy <= y + 1 + height; iy++) {
            int spread = iy == y ? 0 : iy >= y + 1 + height - 2 ? 2 : 1;
            for (int ix = x - spread; ix <= x + spread; ix++) {
                for (int iz = z - spread; iz <= z + spread; iz++) {
                    if (iy < bottom || iy > top) return false;
                    BlockState state = level.getBlockState(new BlockPos(ix, iy, iz));
                    if (iy > y && !state.isAir() && !state.is(net.minecraft.tags.BlockTags.LEAVES)
                            && !state.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }

        // o chão que segura planta: é o que o {@code canSustainPlant} do original pergunta
        BlockState below = level.getBlockState(new BlockPos(x, y - 1, z));
        if (!below.is(net.minecraft.tags.BlockTags.SUPPORTS_VEGETATION)
                && !below.is(net.minecraft.world.level.block.Blocks.FARMLAND)) {
            return false;
        }
        if (y + height + 1 >= top) return false;

        // a copa: quatro camadas, mais larga embaixo, com os cantos sorteados
        for (int iy = y - 3 + height; iy <= y + height; iy++) {
            int degrau = iy - (y + height);
            int raio = 1 - degrau / 2;
            for (int ix = x - raio; ix <= x + raio; ix++) {
                int dx = ix - x;
                for (int iz = z - raio; iz <= z + raio; iz++) {
                    int dz = iz - z;
                    boolean canto = Math.abs(dx) == raio && Math.abs(dz) == raio;
                    if (canto && (random.nextInt(2) == 0 || degrau == 0)) continue;
                    BlockPos pos = new BlockPos(ix, iy, iz);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || state.is(net.minecraft.tags.BlockTags.LEAVES)) {
                        level.setBlock(pos, ForbiddenBlocks.TAINT_LEAVES.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }

        // e o tronco
        for (int iy = 0; iy < height; iy++) {
            BlockPos pos = new BlockPos(x, y + iy, z);
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.is(net.minecraft.tags.BlockTags.LEAVES)) {
                level.setBlock(pos, ForbiddenBlocks.TAINT_LOG.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        return true;
    }
}
