package net.thaumcraft.forbidden;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * A Gaiola da Ira: o {@code BlockWrathCage} do Forbidden Magic 0.575.
 *
 * <p>Com um cristal marcado na mão, ela se afina com aquele bicho — e devolve à mão o cristal do bicho que
 * estava nela. Com o Garfo do Diabolista, ela troca de modo: a essência do bicho, Ira ou Desídia.
 */
public class WrathCageBlock extends BaseEntityBlock {
    public static final MapCodec<WrathCageBlock> CODEC = simpleCodec(WrathCageBlock::new);

    public WrathCageBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WrathCageBlockEntity(pos, state);
    }

    @Override
    protected net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ForbiddenBlocks.WRATH_CAGE_ENTITY, (mundo, pos, estado, cage) -> {
            WrathCageBlockEntity.tick(mundo, pos, estado, cage);
            cage.drawEssentia();
        });
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof WrathCageBlockEntity cage)) return InteractionResult.PASS;

        if (held.getItem() instanceof MobCrystalItem) {
            Identifier bicho = MobCrystalItem.mob(held);
            if (bicho == null) return InteractionResult.PASS;
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            Identifier antes = cage.attune(bicho);
            // o cristal de quem afinou volta com o bicho que estava na gaiola, como no original
            ItemStack devolve = antes == null ? ItemStack.EMPTY : MobCrystalItem.of(antes);
            held.shrink(1);
            if (!devolve.isEmpty() && !player.getInventory().add(devolve)) player.drop(devolve, false);
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }

        if (held.is(ForbiddenItems.DIABOLIST_FORK)) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            cage.cycleMode();
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

}
