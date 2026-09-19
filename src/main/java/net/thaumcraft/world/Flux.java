package net.thaumcraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.block.FluxBlock;
import net.thaumcraft.registry.TCBlocks;

/**
 * O fluxo que a magia derrama no mundo: a gosma ({@code blockFluxGoo}) que escorre para baixo e o gás
 * ({@code blockFluxGas}) que sobe.
 */
public final class Flux {
    private Flux() {
    }

    /** A gosma com tantos quanta (1 a 8). */
    public static BlockState goo(int quanta) {
        return TCBlocks.FLUX_GOO.defaultBlockState().setValue(FluxBlock.LEVEL, Math.clamp(quanta - 1, 0, 7));
    }

    /** O gás com tantos quanta (1 a 8). */
    public static BlockState gas(int quanta) {
        return TCBlocks.FLUX_GAS.defaultBlockState().setValue(FluxBlock.LEVEL, Math.clamp(quanta - 1, 0, 7));
    }

    /**
     * O derrame do reservatório quebrado ({@code BlockEssentiaReservoir.breakBlock}): cinquenta sorteios a até quatro
     * blocos; no ar abaixo dele, gosma cheia, e no resto, gás cheio — até {@code amount} blocos (e mais um).
     */
    public static void spill(ServerLevel level, BlockPos pos, int amount) {
        scatter(level, pos, 5, amount);
    }

    /**
     * O {@code BlockAiry.explodify}: o nó energizado some numa explosão de força três e cinquenta sorteios a até sete
     * blocos viram gosma (abaixo dele) ou gás (acima), sem limite.
     */
    public static void explodify(ServerLevel level, BlockPos pos) {
        scatter(level, pos, 8, Integer.MAX_VALUE);
    }

    private static void scatter(ServerLevel level, BlockPos pos, int range, int amount) {
        RandomSource random = level.getRandom();
        int q = 0;
        for (int a = 0; a < 50; a++) {
            BlockPos at = pos.offset(random.nextInt(range) - random.nextInt(range), random.nextInt(range) - random.nextInt(range),
                    random.nextInt(range) - random.nextInt(range));
            if (!level.isEmptyBlock(at)) continue;
            level.setBlock(at, at.getY() < pos.getY() ? goo(8) : gas(8), Block.UPDATE_ALL);
            if (q++ >= amount) break;
        }
    }

    /**
     * O {@code TileCrucible.spill}: uma vez em quatro, um quantum de fluxo sai do crisol — gás ou gosma no ar de cima,
     * mais um quantum no fluxo que já está lá, ou, com o lugar ocupado, gás ou gosma num vizinho de ar sorteado.
     */
    public static void crucibleSpill(ServerLevel level, BlockPos pos) {
        RandomSource random = level.getRandom();
        if (random.nextInt(4) != 0) return;
        BlockPos up = pos.above();
        if (level.isEmptyBlock(up)) {
            level.setBlock(up, random.nextBoolean() ? gas(1) : goo(1), Block.UPDATE_ALL);
            return;
        }
        BlockState there = level.getBlockState(up);
        if ((there.is(TCBlocks.FLUX_GOO) || there.is(TCBlocks.FLUX_GAS)) && there.getValue(FluxBlock.LEVEL) < 7) {
            level.setBlock(up, there.setValue(FluxBlock.LEVEL, there.getValue(FluxBlock.LEVEL) + 1), Block.UPDATE_ALL);
            return;
        }
        BlockPos at = pos.offset(-1 + random.nextInt(3), -1 + random.nextInt(3), -1 + random.nextInt(3));
        if (level.isEmptyBlock(at)) {
            level.setBlock(at, random.nextBoolean() ? gas(1) : goo(1), Block.UPDATE_ALL);
        }
    }
}
