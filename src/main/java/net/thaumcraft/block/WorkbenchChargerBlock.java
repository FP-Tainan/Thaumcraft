package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.VisRelayBlockEntity;
import net.thaumcraft.block.entity.WorkbenchChargerBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O carregador da bancada arcana: o metadado 2 do {@code BlockMetalDevice} da 4.2.3.5. Vai em cima da bancada e enche
 * a varinha dela com o vis da rede. Desenhado pelo {@link net.thaumcraft.client.render.VisRelayRenderer}.
 */
public class WorkbenchChargerBlock extends BaseEntityBlock {
    public static final MapCodec<WorkbenchChargerBlock> CODEC = simpleCodec(WorkbenchChargerBlock::new);
    private static final VoxelShape SHAPE = Block.box(5.0, 8.0, 5.0, 11.0, 16.0, 11.0);

    public WorkbenchChargerBlock(Properties properties) {
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
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        byte shard = VisRelayBlock.shardColour(stack);
        if (shard < 0 || player.isShiftKeyDown()) return super.useItemOn(stack, state, level, pos, player, hand, hit);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof VisRelayBlockEntity relay) relay.attune(shard);
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WorkbenchChargerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.WORKBENCH_CHARGER, WorkbenchChargerBlockEntity::tick);
    }
}
