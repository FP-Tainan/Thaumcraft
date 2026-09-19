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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.EnergizedNodeBlockEntity;
import net.thaumcraft.block.entity.NodeConverterBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O nó energizado: o metadado 5 do {@code BlockAiry} da 4.2.3.5. Só existe entre um estabilizador (sem redstone)
 * embaixo e um transdutor em cima; faltando um dos dois, estoura.
 */
public class EnergizedNodeBlock extends BaseEntityBlock {
    public static final MapCodec<EnergizedNodeBlock> CODEC = simpleCodec(EnergizedNodeBlock::new);
    private static final VoxelShape CORE = Block.box(4.8, 4.8, 4.8, 11.2, 11.2, 11.2);

    public EnergizedNodeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CORE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean moved) {
        super.neighborChanged(state, level, pos, block, orientation, moved);
        if (level.isClientSide()) return;
        BlockPos below = pos.below();
        boolean held = !level.hasNeighborSignal(below) && level.getBlockState(below).getBlock() instanceof NodeStabilizerBlock
                && level.getBlockEntity(pos.above()) instanceof NodeConverterBlockEntity;
        if (!held) explodify(level, pos);
    }

    /**
     * O {@code BlockAiry.explodify}: o nó some numa explosão de força três, sem fogo. (No original, gosma e gás de fluxo
     * se espalhavam em volta; o fluxo ainda não existe aqui.)
     */
    public static void explodify(Level level, BlockPos pos) {
        if (level.isClientSide()) return;
        level.removeBlock(pos, false);
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0f, false, Level.ExplosionInteraction.BLOCK);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergizedNodeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.ENERGIZED_NODE, EnergizedNodeBlockEntity::tick);
    }
}
