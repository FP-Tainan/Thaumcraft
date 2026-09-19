package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.thaumcraft.block.entity.ArcaneSpaBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O spa arcano: o número 12 do {@code BlockStoneDevice} da 4.2.3.5 — um bloco de pedra com a bacia em cima. Um balde
 * (ou outro recipiente) na mão despeja no tanque; a mão vazia abre a tela.
 */
public class ArcaneSpaBlock extends BaseEntityBlock {
    public static final MapCodec<ArcaneSpaBlock> CODEC = simpleCodec(ArcaneSpaBlock::new);

    public ArcaneSpaBlock(Properties properties) {
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
        return new ArcaneSpaBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, TCBlockEntities.ARCANE_SPA, ArcaneSpaBlockEntity::tick);
    }

    /** O {@code onBlockActivated} do número 12: o que o recipiente da mão tiver vai para o tanque, se couber. */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        if (player.isShiftKeyDown() || !(level.getBlockEntity(pos) instanceof ArcaneSpaBlockEntity spa)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        Storage<FluidVariant> held = ContainerItemContext.forPlayerInteraction(player, hand).find(FluidStorage.ITEM);
        if (held == null) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        long moved = StorageUtil.move(held, spa.tank, variant -> true, Long.MAX_VALUE, null);
        if (moved > 0) {
            level.playSound(null, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.33f,
                    1.0f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ArcaneSpaBlockEntity spa) player.openMenu(spa);
        return InteractionResult.SUCCESS;
    }
}
