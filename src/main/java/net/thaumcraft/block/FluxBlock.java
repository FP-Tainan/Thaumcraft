package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O fluxo que escorre ou sobe: o {@code BlockFluidFinite} do Forge que a gosma ({@code BlockFluxGoo}) e o gás
 * ({@code BlockFluxGas}) de fluxo da 4.2.3.5 usam. Cada bloco tem de um a oito "quanta" (o nível 0 a 7); o fluido
 * finito não se cria nem some andando — cai (ou sobe) inteiro para onde houver lugar e, parado, se reparte por igual
 * com os lados. Qualquer bloco posto nele o substitui (o material dele é substituível).
 */
public abstract class FluxBlock extends Block {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 7);

    protected FluxBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 7));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    /** Para onde este fluxo anda: a gosma para baixo, o gás para cima. */
    protected abstract Direction flow();

    /** De quantos em quantos tiques ele anda (a viscosidade do original dividida por 200). */
    protected abstract int tickRate();

    public static int quanta(BlockState state) {
        return state.getValue(LEVEL) + 1;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        double h = quanta(state) * 14.0 / 8.0;
        return this.flow() == Direction.DOWN ? Block.box(0, 0, 0, 16, h, 16) : Block.box(0, 16 - h, 0, 16, 16, 16);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        level.scheduleTick(pos, this, this.tickRate());
    }

    @Override
    protected BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        ticks.scheduleTick(pos, this, this.tickRate());
        return state;
    }

    /** O lugar aceita este fluxo? Ar, coisa que se substitui (que não seja fluido) ou o próprio fluxo. */
    protected boolean displaceable(LevelAccessor level, BlockPos pos) {
        BlockState there = level.getBlockState(pos);
        if (there.is(this)) return false;
        return there.isAir() || there.canBeReplaced() && there.getFluidState().isEmpty();
    }

    /** O {@code updateTick} do fluido finito: primeiro cai (ou sobe), depois se reparte com os lados. */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int quanta = quanta(state);
        BlockPos next = pos.relative(this.flow());
        if (this.displaceable(level, next)) {
            level.setBlock(next, state, Block.UPDATE_ALL);
            level.removeBlock(pos, false);
            return;
        }
        BlockState nextState = level.getBlockState(next);
        if (nextState.is(this) && quanta(nextState) < 8) {
            int moved = Math.min(quanta, 8 - quanta(nextState));
            level.setBlock(next, nextState.setValue(LEVEL, quanta(nextState) + moved - 1), Block.UPDATE_ALL);
            if (quanta - moved <= 0) {
                level.removeBlock(pos, false);
                return;
            }
            quanta -= moved;
            level.setBlock(pos, state.setValue(LEVEL, quanta - 1), Block.UPDATE_ALL);
            state = level.getBlockState(pos);
        }
        if (quanta <= 1) return;
        List<BlockPos> cells = new ArrayList<>();
        int total = quanta;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos side = pos.relative(d);
            BlockState there = level.getBlockState(side);
            if (there.is(this)) {
                if (quanta(there) < quanta) {
                    cells.add(side);
                    total += quanta(there);
                }
            } else if (this.displaceable(level, side)) {
                cells.add(side);
            }
        }
        if (cells.isEmpty()) return;
        int each = total / (cells.size() + 1);
        int rest = total % (cells.size() + 1);
        if (each <= 0) return;
        level.setBlock(pos, this.defaultBlockState().setValue(LEVEL, Math.min(7, each + (rest-- > 0 ? 1 : 0) - 1)), Block.UPDATE_ALL);
        for (BlockPos side : cells) {
            int q = each + (rest-- > 0 ? 1 : 0);
            if (q > 0) level.setBlock(side, this.defaultBlockState().setValue(LEVEL, Math.min(7, q - 1)), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, @Nullable Orientation orientation, boolean moved) {
        level.scheduleTick(pos, this, this.tickRate());
    }

    @Override
    protected abstract MapCodec<? extends Block> codec();

    /** Quão cheio este bloco está, de zero a um (o {@code getQuantaPercentage}). */
    public static float fullness(BlockState state) {
        return quanta(state) / 8.0f;
    }

    @Override
    protected abstract void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean past);
}
