package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.block.entity.PedestalBlockEntity;
import net.thaumcraft.registry.TCSounds;

/**
 * O pedestal arcano: uma coluna baixa de pedra com um prato em cima.
 *
 * <p>Um toque põe o que está na mão; outro toque tira. Ele segura uma coisa só, como no original.
 */
public class PedestalBlock extends BaseEntityBlock {
    public static final MapCodec<PedestalBlock> CODEC = simpleCodec(PedestalBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2.0, 0.0, 2.0, 14.0, 2.0, 14.0),     // a base
            Block.box(5.0, 2.0, 5.0, 11.0, 11.0, 11.0),    // a coluna
            Block.box(3.0, 11.0, 3.0, 13.0, 13.0, 13.0));  // o prato

    public PedestalBlock(Properties properties) {
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
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, net.minecraft.world.InteractionHand hand,
                                          BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack held = pedestal.held();
        if (held.isEmpty()) {
            if (stack.isEmpty()) return InteractionResult.PASS;
            pedestal.hold(stack.split(1));
            level.playSound(null, pos, TCSounds.JAR.value(), SoundSource.BLOCKS, 0.5f, 1.2f);
        } else {
            pedestal.hold(ItemStack.EMPTY);
            if (!player.getInventory().add(held)) player.drop(held, false);
            level.playSound(null, pos, TCSounds.JAR.value(), SoundSource.BLOCKS, 0.5f, 0.9f);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level,
                                               BlockPos pos, boolean moved) {
        // quebrando o pedestal, o que estava em cima cai no chão
        if (level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal) {
            net.minecraft.world.Containers.dropContents(level, pos, pedestal);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalBlockEntity(pos, state);
    }

    /** As faíscas do {@code receiveClientEvent} do {@code TilePedestal}, desenhadas por quem vê. */
    public interface ClientEffects {
        void sparkle(BlockPos pos, int colour, int count);
    }

    public static ClientEffects clientEffects;

    /**
     * Os eventos do {@code TilePedestal}: 11 é o ingrediente perdido na infusão (faíscas magenta) e 12 é a infusão pronta
     * (faíscas de toda cor), um bloco acima do pedestal.
     */
    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        if (id != 11 && id != 12) return super.triggerEvent(state, level, pos, id, param);
        if (level.isClientSide() && clientEffects != null) {
            // o particleCount(5) e o particleCount(10) do original, com as partículas no máximo
            int times = id == 11 ? 10 : 20;
            for (int a = 0; a < times; a++) clientEffects.sparkle(pos.above(), id == 11 ? 0xC000C0 : -9999, 2);
        }
        return true;
    }
}
