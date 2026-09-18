package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.block.entity.TubeBlockEntity;
import net.thaumcraft.block.entity.TubeOnewayBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O tubo de mão única, o metadado seis do {@code BlockTube}. Nasce apontando para o lado do bloco em que
 * foi posto, e a varinha no miolo o gira; três anéis azulados mostram o sentido.
 */
public class TubeOnewayBlock extends TubeBlock {
    public static final MapCodec<TubeOnewayBlock> CODEC = simpleCodec(TubeOnewayBlock::new);

    public TubeOnewayBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TubeOnewayBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.TUBE_ONEWAY, TubeBlockEntity::tick);
    }
}
