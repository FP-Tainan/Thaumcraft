package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.registry.TCBlocks;

/**
 * As fibras da mácula: o mato roxo que nasce em cima do que ela já tomou.
 *
 * <p>É o {@code BlockTaintFibres} do original. Ela cresce no ar colado a um bloco sólido, e é ela que faz
 * a mácula parecer viva — a crosta é a doença, as fibras são o mofo que ela cria por cima.
 */
public class TaintFibreBlock extends VegetationBlock {
    public static final MapCodec<TaintFibreBlock> CODEC = simpleCodec(TaintFibreBlock::new);
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

    public TaintFibreBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        // ela se agarra em qualquer coisa firme, e não só em terra como a flor comum
        return state.isFaceSturdy(level, pos, Direction.UP) || TaintBlock.isTaint(state);
    }

    /**
     * Tenta pôr fibra neste lugar.
     *
     * <p>As condições são as do {@code spreadFibres} do original: precisa estar colado a alguma coisa
     * firme, não pode estar cercado só de mácula (senão ela se enterra em si mesma), e o que estiver ali
     * tem de ser ar ou coisa que se substitui.
     *
     * @return se nasceu fibra ali
     */
    public static boolean spread(Level level, BlockPos pos, RandomSource random) {
        if (!(level instanceof ServerLevel server)) return false;
        BlockState there = server.getBlockState(pos);
        if (!there.isAir() && !there.canBeReplaced()) return false;
        if (!there.getFluidState().isEmpty()) return false;
        if (!nextToSolid(server, pos)) return false;
        if (onlyNextToTaint(server, pos)) return false;

        // só uma em dez vira mato de pé; o resto é a crosta rasteira
        if (random.nextInt(10) != 0) return false;
        if (!server.getBlockState(pos.below()).isFaceSturdy(server, pos.below(), Direction.UP)) return false;
        if (!server.getBlockState(pos.above()).isAir()) return false;

        server.setBlockAndUpdate(pos, TCBlocks.TAINT_FIBRES.defaultBlockState());
        return true;
    }

    private static boolean nextToSolid(LevelReader level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos at = pos.relative(dir);
            if (level.getBlockState(at).isFaceSturdy(level, at, dir.getOpposite())) return true;
        }
        return false;
    }

    private static boolean onlyNextToTaint(LevelReader level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockState state = level.getBlockState(pos.relative(dir));
            if (state.isAir()) continue;
            if (!TaintBlock.isTaint(state)) return false;
        }
        return true;
    }
}
