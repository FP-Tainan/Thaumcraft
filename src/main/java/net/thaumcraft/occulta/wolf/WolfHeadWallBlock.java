package net.thaumcraft.occulta.wolf;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A <b>Cabeça de Lobo</b> pregada numa parede: o outro ramo do {@code BlockWolfHead} do Witchery.
 *
 * <p>As quatro caixas são as do original, e cada uma encosta na parede de que a cabeça sai — meio bloco de
 * fundo, a meia altura. Ela olha <b>para fora</b> da parede, e o giro dela não se escolhe: é o lado.
 */
public class WolfHeadWallBlock extends BaseEntityBlock {
    public static final MapCodec<WolfHeadWallBlock> CODEC = simpleCodec(WolfHeadWallBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** As quatro caixas do original, uma por parede. */
    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.NORTH, Block.box(4.0, 4.0, 8.0, 12.0, 12.0, 16.0),
            Direction.SOUTH, Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 8.0),
            Direction.WEST, Block.box(8.0, 4.0, 4.0, 16.0, 12.0, 12.0),
            Direction.EAST, Block.box(0.0, 4.0, 4.0, 8.0, 12.0, 12.0));

    public WolfHeadWallBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        for (Direction lado : context.getNearestLookingDirections()) {
            if (!lado.getAxis().isHorizontal()) continue;
            BlockState feitio = this.defaultBlockState().setValue(FACING, lado.getOpposite());
            if (feitio.canSurvive(context.getLevel(), context.getClickedPos())) return feitio;
        }
        return null;
    }

    @Override
    protected RenderShape getRenderShape(BlockState feitio) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                  CollisionContext context) {
        return SHAPES.get(feitio.getValue(FACING));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos onde, BlockState feitio) {
        return new WolfHeadBlockEntity(onde, feitio);
    }
}
