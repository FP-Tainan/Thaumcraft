package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * O foco composto de recarga: o número 8 do {@code BlockStoneDevice} da 4.2.3.5, a cruz de pedra que se põe sobre o
 * pedestal de recarga para ele quebrar os aspectos compostos dos nós em primários. Tocá-lo é tocar o pedestal de baixo.
 */
public class RechargeFocusBlock extends Block {
    public static final MapCodec<RechargeFocusBlock> CODEC = simpleCodec(RechargeFocusBlock::new);
    /** A caixa do original: de 1 a 15, sete dezesseis avos de altura. */
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 7.0, 15.0);

    public RechargeFocusBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        return WandPedestalBlock.use(level, pos.below(), player, hand) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return WandPedestalBlock.use(level, pos.below(), player, InteractionHand.MAIN_HAND) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}
