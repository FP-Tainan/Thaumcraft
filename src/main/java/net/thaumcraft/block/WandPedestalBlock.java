package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.thaumcraft.block.entity.WandPedestalBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import net.thaumcraft.registry.TCItems;
import org.jetbrains.annotations.Nullable;

/**
 * O pedestal de recarga: o número 5 do {@code BlockStoneDevice} da 4.2.3.5 — três degraus de pedra (a base de um
 * bloco inteiro, o meio e a coluna). A mão com uma varinha ou um amuleto de vis o põe em cima; qualquer toque com
 * alguma coisa já em cima a devolve. O comparador lê o quanto a varinha está cheia.
 */
public class WandPedestalBlock extends BaseEntityBlock {
    public static final MapCodec<WandPedestalBlock> CODEC = simpleCodec(WandPedestalBlock::new);
    /** A caixa do {@code setBlockBoundsBasedOnState}: a coluna de 4 a 12, da altura inteira. */
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

    public WandPedestalBlock(Properties properties) {
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
        return new WandPedestalBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.WAND_PEDESTAL, WandPedestalBlockEntity::tick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult hit) {
        // com o foco composto na mão, o toque é para pô-lo em cima
        if (stack.is(TCItems.RECHARGE_FOCUS)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        return use(level, pos, player, hand) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return use(level, pos, player, InteractionHand.MAIN_HAND) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    /** O {@code onBlockActivated} do número 5: tira o que está em cima, ou põe a varinha (ou o amuleto) da mão. */
    static boolean use(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (!(level.getBlockEntity(pos) instanceof WandPedestalBlockEntity ped)) return false;
        if (!ped.held().isEmpty()) {
            if (!level.isClientSide()) {
                ItemStack out = ped.removeItemNoUpdate(0);
                level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY(), player.getZ(), out));
                ped.setChanged();
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2f,
                        ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7f + 1.0f) * 1.5f);
            }
            return true;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!WandPedestalBlockEntity.accepts(stack)) return false;
        if (!level.isClientSide()) {
            ped.setItem(0, stack.copyWithCount(1));
            stack.shrink(1);
            ped.setChanged();
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2f,
                    ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7f + 1.0f) * 1.6f);
        }
        return true;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof WandPedestalBlockEntity ped ? ped.comparator() : 0;
    }
}
