package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * O bloco de barreira: a {@code BlockBarrier} do Witchery.
 *
 * <p>Ele não se monta nem se apanha — os ritos de proteção põem-no e ele <b>some sozinho</b> quando a conta
 * acaba. É de vidro e não se vê de longe; o que ele faz é <b>não deixar passar</b>.
 *
 * <p>E sabe de quem é: <b>quem ergueu a barreira a atravessa</b>, e quem está em criativo agachado também. Uma
 * barreira que não trava gente trava só o que não é gente.
 */
public class BarrierBlock extends BaseEntityBlock {
    public static final MapCodec<BarrierBlock> CODEC = simpleCodec(BarrierBlock::new);

    /** Quanto tempo uma casa de barreira dura sem que o rito a renove: o {@code 30} do original. */
    public static final int TICKS_TO_LIVE = 30;

    /** A casa é um pouco menor do que o bloco, como no original. */
    private static final double INSET = 0.0625;
    private static final VoxelShape SHAPE =
            Block.box(INSET * 16, INSET * 16, INSET * 16, 16 - INSET * 16, 16 - INSET * 16, 16 - INSET * 16);

    public BarrierBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BarrierBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                           BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, OccultaBlocks.BARRIER_ENTITY, (mundo, onde, feitio, alma) -> alma.tick());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Quem ela deixa passar, passa: é aqui que a barreira sabe de quem é. */
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        if (!(context instanceof EntityCollisionContext quem)) return SHAPE;
        if (!(quem.getEntity() instanceof Player gente)) return SHAPE;
        if (level.getBlockEntity(pos) instanceof BarrierBlockEntity alma && alma.lets(gente)) {
            return Shapes.empty();
        }
        return SHAPE;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState vizinho, net.minecraft.core.Direction lado) {
        return vizinho.is(this) || super.skipRendering(state, vizinho, lado);
    }

    /**
     * Põe uma casa de barreira naquele lugar, ou renova a que lá estiver.
     *
     * @param blocksPlayers se ela trava gente também, e não só o que não é gente
     * @param owner         quem a ergueu, que passa sempre
     */
    public static void put(ServerLevel level, BlockPos onde, int ticks, boolean blocksPlayers,
                           @Nullable UUID owner) {
        BlockState estava = level.getBlockState(onde);
        boolean já = estava.is(OccultaBlocks.BARRIER);
        if (!já) {
            if (!estava.isAir() && !estava.canBeReplaced()) return;
            level.setBlock(onde, OccultaBlocks.BARRIER.defaultBlockState(), Block.UPDATE_ALL);
        }
        if (level.getBlockEntity(onde) instanceof BarrierBlockEntity alma) {
            alma.renew(ticks, blocksPlayers, owner);
        }
    }
}
