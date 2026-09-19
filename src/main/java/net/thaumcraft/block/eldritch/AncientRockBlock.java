package net.thaumcraft.block.eldritch;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * A rocha antiga: o {@code BlockCosmeticSolid} 12 da 4.2.3.5. As quatro figuras ({@code er_1} a {@code er_4}) formam um
 * ladrilho de dois por dois que corre pelo mundo: cada face escolhe a sua pela paridade das duas coordenadas do plano
 * dela. As paridades ficam guardadas no estado, acertadas quando o bloco é posto.
 */
public class AncientRockBlock extends Block {
    public static final MapCodec<AncientRockBlock> CODEC = simpleCodec(AncientRockBlock::new);
    public static final BooleanProperty ODD_X = BooleanProperty.create("odd_x");
    public static final BooleanProperty ODD_Y = BooleanProperty.create("odd_y");
    public static final BooleanProperty ODD_Z = BooleanProperty.create("odd_z");

    public AncientRockBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(ODD_X, false).setValue(ODD_Y, false).setValue(ODD_Z, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ODD_X, ODD_Y, ODD_Z);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return at(this.defaultBlockState(), context.getClickedPos());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        BlockState right = at(state, pos);
        if (right != state) level.setBlock(pos, right, Block.UPDATE_CLIENTS);
    }

    /** O estado com as paridades daquele lugar ({@code Math.abs(x % 2)} do original). */
    public static BlockState at(BlockState state, BlockPos pos) {
        return state.setValue(ODD_X, (pos.getX() & 1) != 0).setValue(ODD_Y, (pos.getY() & 1) != 0).setValue(ODD_Z, (pos.getZ() & 1) != 0);
    }
}
