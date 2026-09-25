package net.thaumcraft.crimson;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.research.ResearchManager;
import org.jetbrains.annotations.Nullable;

/**
 * O Altar Antigo: o {@code BossActivator} do Crimson Warfare — o pedestal de pedra que está no meio das cruzes de
 * pedra arcana espalhadas pelo mundo.
 *
 * <p>Não se quebra e não se pega. Quem não souber o rito não consegue fazer nada com ele; quem souber põe nele uma
 * Semente do Vazio, e quinze segundos depois vem buscá-la um dos três.
 */
public class AncientAltarBlock extends BaseEntityBlock {
    public static final MapCodec<AncientAltarBlock> CODEC = simpleCodec(AncientAltarBlock::new);

    /** As três caixas do original: o pé, o fuste e a mesa. */
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0),
            Block.box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0));

    public AncientAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AncientAltarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, CrimsonBlocks.ANCIENT_ALTAR_ENTITY, AncientAltarBlockEntity::tick);
    }

    @Override
    protected net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return use(level, pos, player, ItemStack.EMPTY);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        return use(level, pos, player, stack);
    }

    /** O {@code onBlockActivated}: sem a pesquisa do rito, o altar não responde a ninguém. */
    private static InteractionResult use(Level level, BlockPos pos, Player player, ItemStack held) {
        if (!ResearchManager.knows(player, "CW_WARFARE")) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof AncientAltarBlockEntity altar)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (altar.hasSeed()) {
            // devolve a semente a quem a quiser de volta antes da hora
            ItemStack devolvida = altar.take();
            if (!player.getInventory().add(devolvida)) player.drop(devolvida, false);
            return InteractionResult.SUCCESS;
        }
        if (!AncientAltarBlockEntity.isSeed(held)) return InteractionResult.PASS;
        altar.put(held);
        if (!player.getAbilities().instabuild) held.shrink(1);
        return InteractionResult.SUCCESS;
    }
}
