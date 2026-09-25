package net.thaumcraft.shattered;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * O desfiar do Limbo: o {@code LimboDecay} das Portas Dimensionais.
 *
 * <p>Nada do que cai lá dentro fica como era. Cada bloco desce um degrau de cada vez — pedra, pedregulho,
 * cascalho, tecido desfiado — e o que não é cheio some. Do tecido desfiado o desfiar espalha-se para os seis
 * vizinhos, metade das vezes.
 *
 * <p>Não se desfaz o tecido desfiado nem o eterno, nem as portas, nem as fendas: são o que fica de pé no Limbo.
 */
public final class LimboDecay {
    /** A escada do {@code decaySequence}, do mais desfeito ao mais inteiro. */
    public static List<BlockState> sequence() {
        return List.of(
                FabricBlocks.UNRAVELLED.defaultBlockState(),
                Blocks.GRAVEL.defaultBlockState(),
                Blocks.COBBLESTONE.defaultBlockState(),
                Blocks.STONE.defaultBlockState());
    }

    /** Uma em duas, como no original. */
    public static final int SPREAD_CHANCE = 2;

    private LimboDecay() {
    }

    /** Se aquele bloco pode desfiar-se. */
    public static boolean canDecay(BlockState estado) {
        if (estado.isAir()) return false;
        Block bloco = estado.getBlock();
        if (bloco == FabricBlocks.UNRAVELLED || bloco == FabricBlocks.ETERNAL) return false;
        if (bloco == ShatteredBlocks.RIFT) return false;
        if (ShatteredBlocks.doors().contains(bloco)) return false;
        return !estado.hasBlockEntity();
    }

    /** Desfia aquele bloco um degrau. */
    public static void decay(ServerLevel level, BlockPos onde) {
        BlockState estado = level.getBlockState(onde);
        if (!canDecay(estado)) return;
        if (!estado.isSolidRender()) {
            level.setBlockAndUpdate(onde, Blocks.AIR.defaultBlockState());
            return;
        }
        List<BlockState> escada = sequence();
        int degrau = 0;
        while (degrau < escada.size() && !escada.get(degrau).getBlock().equals(estado.getBlock())) degrau++;
        // o que não está na escada entra por baixo dela, como no original
        BlockState seguinte = escada.get(Math.max(0, Math.min(degrau, escada.size()) - 1));
        level.setBlockAndUpdate(onde, seguinte);
    }

    /** O {@code applySpreadDecay}: do tecido desfiado, o desfiar passa aos seis vizinhos. */
    public static void spread(ServerLevel level, BlockPos onde, RandomSource random) {
        if (random.nextInt(SPREAD_CHANCE) != 0) return;
        for (Direction lado : Direction.values()) decay(level, onde.relative(lado));
    }
}
