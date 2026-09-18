package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * A mesa: o {@code BlockTable} de metadado zero e um da 4.2.3.5.
 *
 * <p>É uma mesa de madeira comum, de tampo grosso e duas pernas com travessa, virada de acordo com quem a
 * põe. Não faz nada sozinha: a varinha a transforma em bancada arcana, e as ferramentas de escrita, sobre
 * duas mesas encostadas, as transformam na mesa de pesquisa.
 */
public class TableBlock extends Block {
    public static final MapCodec<TableBlock> CODEC = simpleCodec(TableBlock::new);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    public TableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    /** O original escolhe entre dois giros pelo rumo de quem põe: norte e sul dão um, leste e oeste o outro. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        return this.defaultBlockState().setValue(AXIS,
                facing.getAxis() == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z);
    }

    public static boolean isTable(BlockState state) {
        return state.getBlock() instanceof TableBlock;
    }
}
