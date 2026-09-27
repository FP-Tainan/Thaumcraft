package net.thaumcraft.occulta;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Um glifo de giz no chão: o {@code BlockCircleGlyph} do Witchery.
 *
 * <p>São três gizes — o de <b>Ritual</b>, o do <b>Alhures</b> e o <b>Infernal</b> —, e cada um risca <b>doze
 * desenhos</b> diferentes, sorteados a cada risco. No original os doze são os feitios do bloco; aqui são a marca
 * {@code shape}, que é o mesmo por outro nome.
 *
 * <p>Um glifo é um tapete rente ao chão: não se pisa nele, não se esbarra nele, e só se aguenta sobre coisa
 * firme.
 */
public class GlyphBlock extends Block {
    public static final MapCodec<GlyphBlock> CODEC = simpleCodec(GlyphBlock::new);

    /** Qual dos doze desenhos este glifo tem. */
    public static final IntegerProperty SHAPE = IntegerProperty.create("shape", 0, 11);

    /** Quantos desenhos cada giz tem. */
    public static final int SHAPES = 12;

    /** A altura de um risco de giz: meio dezesseis avos, como no original. */
    private static final VoxelShape SHAPE_BOX = Block.column(16.0, 0.0, 0.25);

    public GlyphBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BOX;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return net.minecraft.world.phys.shapes.Shapes.empty();
    }

    /** Giz não se risca no ar: precisa de chão firme por baixo. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, net.minecraft.world.level.ScheduledTickAccess ticks,
                                     BlockPos pos, net.minecraft.core.Direction direction, BlockPos neighbour,
                                     BlockState neighbourState, net.minecraft.util.RandomSource random) {
        if (direction == net.minecraft.core.Direction.DOWN && !this.canSurvive(state, level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return state;
    }
}
