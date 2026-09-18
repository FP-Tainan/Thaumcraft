package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.TubeBufferBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O tampão de essência, o metadado quatro do {@code BlockTube}: uma caixa de oito por oito no meio, com os
 * braços de cano saindo para quem se liga a ela.
 */
public class TubeBufferBlock extends TubeBlock {
    public static final MapCodec<TubeBufferBlock> CODEC = simpleCodec(TubeBufferBlock::new);
    private static final VoxelShape BOX = Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);
    private static final VoxelShape[] ARMS = {
            Block.box(7.0, 0.0, 7.0, 9.0, 4.0, 9.0),
            Block.box(7.0, 12.0, 7.0, 9.0, 16.0, 9.0),
            Block.box(7.0, 7.0, 0.0, 9.0, 9.0, 4.0),
            Block.box(7.0, 7.0, 12.0, 9.0, 9.0, 16.0),
            Block.box(0.0, 7.0, 7.0, 4.0, 9.0, 9.0),
            Block.box(12.0, 7.0, 7.0, 16.0, 9.0, 9.0),
    };

    public TubeBufferBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = BOX;
        for (Direction dir : Direction.values()) {
            if (state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(dir))) shape = Shapes.or(shape, ARMS[dir.get3DDataValue()]);
        }
        return shape;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TubeBufferBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.TUBE_BUFFER, TubeBufferBlockEntity::tick);
    }
}
