package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.thaumcraft.block.entity.LevitatorBlockEntity;
import net.thaumcraft.registry.TCBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * O levitador arcano: o {@code BlockLifter} da 4.2.3.5. Um bloco de madeira com frestas por onde se vê o brilho de
 * dentro — verde em cima, roxo dos lados —, que acende forte enquanto ele está ligado.
 */
public class LevitatorBlock extends BaseEntityBlock {
    public static final MapCodec<LevitatorBlock> CODEC = simpleCodec(LevitatorBlock::new);
    /** Desligado pela redstone: o {@code gettingPower} do original, nele ou no bloco de cima. */
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    /** A faísca que sobe de um levitador ligado; o cliente pendura aqui o desenho dela. */
    public interface ClientEffects {
        void sparkle(double x, double y, double z, RandomSource random);
    }

    public static ClientEffects clientEffects = (x, y, z, random) -> {
    };

    public LevitatorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWERED, LevitatorBlockEntity.gettingPower(context.getLevel(), context.getClickedPos()));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean moved) {
        boolean powered = LevitatorBlockEntity.gettingPower(level, pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
            updateLifterStack(level, pos);
        }
        super.neighborChanged(state, level, pos, block, orientation, moved);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        updateLifterStack(level, pos);
        super.onPlace(state, level, pos, old, moved);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        updateLifterStack(level, pos);
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    /** O {@code updateLifterStack}: os levitadores empilhados embaixo refazem a conta do alcance. */
    private static void updateLifterStack(Level level, BlockPos pos) {
        for (int count = 1; level.getBlockEntity(pos.below(count)) instanceof LevitatorBlockEntity below; count++) {
            below.requiresUpdate = true;
        }
        if (level.getBlockEntity(pos) instanceof LevitatorBlockEntity self) self.requiresUpdate = true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) return;
        if (level.getBlockEntity(pos) instanceof LevitatorBlockEntity lifter && lifter.rangeAbove > 0) {
            clientEffects.sparkle(pos.getX() + 0.2 + random.nextFloat() * 0.6, pos.getY() + 1, pos.getZ() + 0.2 + random.nextFloat() * 0.6, random);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LevitatorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.LEVITATOR, LevitatorBlockEntity::tick);
    }
}
