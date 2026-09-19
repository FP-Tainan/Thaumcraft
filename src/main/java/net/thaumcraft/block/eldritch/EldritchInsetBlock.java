package net.thaumcraft.block.eldritch;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Map;

/**
 * As pedras engastadas do eldritch: os números 4, 5 e 6 do {@code BlockEldritch} da 4.2.3.5 (a pedra incrustada
 * luminosa, a pedra de glifos e o enfeite). O {@code BlockEldritchRenderer} as desenha dois pixels para dentro em cada
 * face que não encosta num bloco sólido, como uma pedra encaixada na parede; o estado guarda quais faces estão soltas
 * para o modelo. A caixa de colisão continua o bloco inteiro.
 */
public class EldritchInsetBlock extends Block {
    public static final MapCodec<EldritchInsetBlock> CODEC = simpleCodec(EldritchInsetBlock::new);
    public static final Map<Direction, BooleanProperty> OPEN = Map.of(
            Direction.DOWN, BooleanProperty.create("open_down"), Direction.UP, BooleanProperty.create("open_up"),
            Direction.NORTH, BooleanProperty.create("open_north"), Direction.SOUTH, BooleanProperty.create("open_south"),
            Direction.WEST, BooleanProperty.create("open_west"), Direction.EAST, BooleanProperty.create("open_east"));

    public EldritchInsetBlock(Properties properties) {
        super(properties);
        BlockState state = this.stateDefinition.any();
        for (BooleanProperty open : OPEN.values()) state = state.setValue(open, true);
        this.registerDefaultState(state);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        for (Direction dir : Direction.values()) builder.add(OPEN.get(dir));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return shape(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        return state.setValue(OPEN.get(direction), !neighbour.isFaceSturdy(level, neighbourPos, direction.getOpposite()));
    }

    /** Quais faces não encostam em nada sólido ({@code isSideSolid} do vizinho, na face voltada para cá). */
    public static BlockState shape(BlockState state, BlockGetter level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos at = pos.relative(dir);
            state = state.setValue(OPEN.get(dir), !level.getBlockState(at).isFaceSturdy(level, at, dir.getOpposite()));
        }
        return state;
    }
}
