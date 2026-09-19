package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

/**
 * A algema de golem: o número 9 do {@code BlockCosmeticSolid} da 4.2.3.5 (e o 10, que é ela energizada). Com sinal de
 * redstone ela acende, e o golem que estiver em cima dela para de pensar até o sinal sumir.
 */
public class GolemFetterBlock extends Block {
    public static final MapCodec<GolemFetterBlock> CODEC = simpleCodec(GolemFetterBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public GolemFetterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    /** O {@code onNeighborBlockChange}: 9 vira 10 com sinal, e volta sem ele. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, @Nullable Orientation orientation, boolean moved) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) level.setBlock(pos, state.setValue(POWERED, powered), 3);
        super.neighborChanged(state, level, pos, neighbor, orientation, moved);
    }
}
