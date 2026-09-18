package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.block.entity.NodeBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O nó do pinheiro-de-prata: o terceiro tipo do {@code BlockMagicalLog} da 4.2.3.5.
 *
 * <p>É um pedaço do tronco com um nó de aura puro dentro — o original põe um {@code TileNode} no próprio
 * tronco. O nó é desenhado por cima da madeira e a varinha bebe dele como de qualquer outro. Quebrado, o
 * tronco volta a ser tora comum de pinheiro-de-prata e o nó se perde.
 */
public class SilverwoodKnotBlock extends BaseEntityBlock {
    public static final MapCodec<SilverwoodKnotBlock> CODEC = simpleCodec(SilverwoodKnotBlock::new);

    public SilverwoodKnotBlock(Properties properties) {
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NodeBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.NODE, NodeBlockEntity::tick);
    }
}
