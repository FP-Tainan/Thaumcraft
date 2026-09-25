package net.thaumcraft.shattered;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * O comer da fenda: o {@code RiftDecay} das Portas Dimensionais.
 *
 * <p>Uma fenda solta vai desfazendo o mundo em volta dela, um bloco de cada vez, e do que ela come sai às vezes um
 * Fio do Mundo — que é de onde vem todo o resto do ramo. Não come o que é do ramo: o tecido, as portas e as
 * outras fendas ficam.
 *
 * <p><b>Diferença declarada:</b> no original a fome da fenda cresce com o tamanho dela, e as fendas crescem e
 * encolhem. Aqui a fenda ainda não tem tamanho — come com a mesma fome sempre, e a fatia do tamanho fica para o
 * registro de fendas, quando ele chegar.
 */
public final class RiftDecay {
    /** O alcance em que a fenda pega o que come. */
    public static final int RANGE = 3;
    /** E de quantas em quantas mordidas sai um fio. */
    public static final int THREAD_CHANCE = 4;

    private RiftDecay() {
    }

    /** Se a fenda pode comer aquele bloco. */
    public static boolean canDecay(BlockState estado) {
        if (estado.isAir()) return false;
        Block bloco = estado.getBlock();
        if (bloco == FabricBlocks.UNRAVELLED || bloco == FabricBlocks.ETERNAL) return false;
        if (FabricBlocks.isFabric(bloco)) return false;
        if (bloco == ShatteredBlocks.RIFT) return false;
        if (ShatteredBlocks.doors().contains(bloco)) return false;
        if (estado.getDestroySpeed(null, BlockPos.ZERO) < 0.0f) return false;
        return !estado.hasBlockEntity();
    }

    /** Uma mordida: some com um bloco perto e, de vez em quando, deixa um fio no lugar. */
    public static void bite(ServerLevel level, BlockPos fenda, RandomSource random) {
        BlockPos onde = fenda.offset(
                random.nextInt(RANGE * 2 + 1) - RANGE,
                random.nextInt(RANGE * 2 + 1) - RANGE,
                random.nextInt(RANGE * 2 + 1) - RANGE);
        BlockState estado = level.getBlockState(onde);
        if (!canDecay(estado)) return;
        level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
        if (random.nextInt(THREAD_CHANCE) != 0) return;
        Block.popResource(level, onde, new ItemStack(ShatteredItems.WORLD_THREAD));
    }
}
