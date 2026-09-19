package net.thaumcraft.block;

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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A matriz mnemônica: o aparelho de metal 12 (o {@code TileBrainbox}) da 4.2.3.5. Uma caixa de 10/16 com o cérebro,
 * presa pelo pino ao bloco em que foi posta; virada para um taumatório, dá a ele duas receitas a mais. Cai se o bloco
 * a que está presa sumir.
 */
public class MnemonicMatrixBlock extends Block {
    public static final MapCodec<MnemonicMatrixBlock> CODEC = simpleCodec(MnemonicMatrixBlock::new);
    /** O {@code facing}: para onde vai o pino, o lado do bloco em que ela se apoia. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    private static final VoxelShape BOX = Block.box(3, 3, 3, 13, 13, 13);
    private static final VoxelShape[] SHAPES = new VoxelShape[6];

    static {
        for (Direction d : Direction.values()) {
            VoxelShape pin = switch (d) {
                case UP -> Block.box(6, 13, 6, 10, 16, 10);
                case DOWN -> Block.box(6, 0, 6, 10, 3, 10);
                case EAST -> Block.box(13, 6, 6, 16, 10, 10);
                case WEST -> Block.box(0, 6, 6, 3, 10, 10);
                case SOUTH -> Block.box(6, 6, 13, 10, 10, 16);
                case NORTH -> Block.box(6, 6, 0, 10, 10, 3);
            };
            SHAPES[d.get3DDataValue()] = Shapes.or(BOX, pin);
        }
    }

    public MnemonicMatrixBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** O {@code placeBlockAt} do item: o pino vai para o bloco em que se clicou. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).get3DDataValue()];
    }

    /** O {@code onNeighborBlockChange}: sem nada no lado do pino, cai. */
    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        if (direction == state.getValue(FACING) && neighbour.isAir()) ticks.scheduleTick(pos, this, 1);
        return state;
    }

    @Override
    protected void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockState(pos.relative(state.getValue(FACING))).isAir()) level.destroyBlock(pos, true);
    }
}
