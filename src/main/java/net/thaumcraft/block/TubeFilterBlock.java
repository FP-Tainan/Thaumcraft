package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.entity.TubeBlockEntity;
import net.thaumcraft.block.entity.TubeFilterBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCResources;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * O tubo filtro, o metadado três do {@code BlockTube}: um rótulo marcado na mão prende o aspecto nele; agachado,
 * o rótulo sai, caindo do lado em que se clicou.
 */
public class TubeFilterBlock extends TubeBlock {
    public static final MapCodec<TubeFilterBlock> CODEC = simpleCodec(TubeFilterBlock::new);

    public TubeFilterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TubeFilterBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.TUBE_FILTER, TubeBlockEntity::tick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof TubeFilterBlockEntity filter)) return InteractionResult.PASS;
        if (player.isShiftKeyDown() && filter.aspectFilter() != null) {
            if (!level.isClientSide()) {
                Direction side = hit.getDirection();
                level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5 + side.getStepX() / 3.0, pos.getY() + 0.5,
                        pos.getZ() + 0.5 + side.getStepZ() / 3.0, TubeFilterBlockEntity.label(filter.aspectFilter())));
                filter.setAspectFilter(null);
            }
            level.playSound(player, pos, TCSounds.PAGE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        String marked = stack.get(TCComponents.LABEL_ASPECT);
        if (filter.aspectFilter() == null && stack.is(TCResources.get("jar_label")) && marked != null) {
            Aspect aspect = Aspect.of(marked);
            if (aspect != null) {
                if (!level.isClientSide()) {
                    filter.setAspectFilter(aspect);
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                }
                level.playSound(player, pos, TCSounds.PAGE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }
}
