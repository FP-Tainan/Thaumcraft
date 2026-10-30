package net.thaumcraft.occulta.brew;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * O <b>cipó de bruxa</b>: o {@code BlockWitchVine} do Witchery.
 *
 * <p>É o cipó do jogo pela cara — mesma textura, mesma tinta da folhagem do lugar — e <b>outra coisa</b> por
 * dentro. Quem o põe é o cozimento de <b>brotação</b>, que o corre de cima a baixo pela parede em que o
 * frasco bateu, e ele <b>não se alastra</b>: fica exatamente onde foi posto.
 *
 * <p>E há uma diferença que se sente com os pés: <b>esbarra-se nele</b>. O cipó do jogo não tem caixa de
 * choque nenhuma — passa-se através dele. Este tem: são duas casas de dezesseis coladas à parede, e dá para
 * ficar de pé em cima delas. É do original, e muda o que ele é — uma parede coberta de cipó de bruxa é uma
 * parede <b>mais grossa</b>.
 *
 * <p>Sobe-se por ele, como pelo do jogo. Ele não larga nada, e não se apanha.
 */
public class WitchVineBlock extends Block {
    public static final MapCodec<WitchVineBlock> CODEC = simpleCodec(WitchVineBlock::new);

    /** Para que lado está a parede em que ele se agarra: o metadata dois a cinco do original. */
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** O quanto ele avança da parede: duas casas de dezesseis, que é o {@code 0.125F} do original. */
    private static final int GROSSURA = 2;

    private static final Map<Direction, VoxelShape> FORMAS = Map.of(
            Direction.NORTH, Block.box(0.0, 0.0, 0.0, 16.0, 16.0, GROSSURA),
            Direction.SOUTH, Block.box(0.0, 0.0, 16 - GROSSURA, 16.0, 16.0, 16.0),
            Direction.WEST, Block.box(0.0, 0.0, 0.0, GROSSURA, 16.0, 16.0),
            Direction.EAST, Block.box(16 - GROSSURA, 0.0, 0.0, 16.0, 16.0, 16.0));

    public WitchVineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /**
     * A forma dele, que é também a de esbarrar: o original devolve a caixa do metadata nos dois casos, e não
     * devolve nada como o cipó do jogo faz.
     */
    @Override
    protected VoxelShape getShape(BlockState feitio, BlockGetter level, BlockPos onde,
                                  CollisionContext contexto) {
        return FORMAS.get(feitio.getValue(FACING));
    }
}
