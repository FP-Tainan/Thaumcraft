package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.thaumcraft.block.entity.NodeConverterBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O transdutor de nó: o metadado 11 do {@code BlockStoneDevice} da 4.2.3.5. Vai em cima de um nó que tenha um
 * estabilizador embaixo; com redstone, energiza o nó. Qualquer mudança em volta faz ele reavaliar. Desenhado pelo
 * {@link net.thaumcraft.client.render.NodeConverterRenderer}.
 */
public class NodeConverterBlock extends BaseEntityBlock {
    public static final MapCodec<NodeConverterBlock> CODEC = simpleCodec(NodeConverterBlock::new);

    public NodeConverterBlock(Properties properties) {
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
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean moved) {
        super.neighborChanged(state, level, pos, block, orientation, moved);
        if (level.getBlockEntity(pos) instanceof NodeConverterBlockEntity converter) converter.checkStatus(level, pos);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NodeConverterBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.NODE_CONVERTER, NodeConverterBlockEntity::tick);
    }
}
