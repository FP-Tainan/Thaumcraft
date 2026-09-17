package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.InfusionMatrixBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * A matriz rúnica: uma pedra entalhada que fica pairando sobre a sala de infusão.
 *
 * <p>Ela não faz nada sozinha. Quem a acorda é a varinha, e ela só acorda se a construção em volta
 * estiver certa — o pedestal dois blocos abaixo e os quatro pilares nos cantos dele.
 */
public class InfusionMatrixBlock extends BaseEntityBlock {
    public static final MapCodec<InfusionMatrixBlock> CODEC = simpleCodec(InfusionMatrixBlock::new);
    private static final VoxelShape SHAPE = Block.box(1.0, 3.0, 1.0, 15.0, 13.0, 15.0);

    public InfusionMatrixBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfusionMatrixBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.INFUSION_MATRIX, InfusionMatrixBlockEntity::tick);
    }
}
