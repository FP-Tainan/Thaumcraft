package net.thaumcraft.forbidden;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * As folhas maculadas: o {@code BlockLeavesTainted} do Forbidden Magic 0.575.
 *
 * <p>Caem a muda, como as folhas comuns, e uma vez em vinte deixam cair um Fruto Maculado quando apodrecem — é o
 * {@code dropBlockAsItem} do {@code removeLeaves} do original.
 */
public class TaintedLeavesBlock extends LeavesBlock {
    public static final MapCodec<TaintedLeavesBlock> CODEC = simpleCodec(TaintedLeavesBlock::new);

    public TaintedLeavesBlock(Properties properties) {
        super(0.0f, properties);
    }

    @Override
    public MapCodec<? extends LeavesBlock> codec() {
        return CODEC;
    }

    /** As folhas do ramo não soltam folhinha ao vento. */
    @Override
    protected void spawnFallingLeavesParticle(net.minecraft.world.level.Level level, BlockPos pos, RandomSource random) {
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean apodrece = state.getValue(DISTANCE) == DECAY_DISTANCE && !state.getValue(PERSISTENT);
        super.randomTick(state, level, pos, random);
        if (apodrece && random.nextInt(20) == 0) {
            Block.popResource(level, pos, new ItemStack(ForbiddenItems.TAINT_FRUIT));
        }
    }
}
