package net.thaumcraft.block.eldritch;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.thaumcraft.block.entity.eldritch.StrangeCrystalBlockEntity;

/**
 * Os cristais estranhos: o número 7 do {@code BlockCrystal} da 4.2.3.5 (o {@code TileEldritchCrystal}), que crescem da
 * pedra incrustada luminosa e do teto dos ninhos das Terras de Fora. Apontam para o lado {@link #FACING}; sem o bloco de
 * trás, caem. Quebrados, deixam um fragmento equilibrado.
 */
public class StrangeCrystalBlock extends BaseEntityBlock {
    public static final MapCodec<StrangeCrystalBlock> CODEC = simpleCodec(StrangeCrystalBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public StrangeCrystalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
                                     BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (direction == state.getValue(FACING).getOpposite() && neighborState.isAir()) return Blocks.AIR.defaultBlockState();
        return state;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StrangeCrystalBlockEntity(pos, state);
    }
}
