package net.thaumcraft.forbidden;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A muda maculada: o {@code BlockSaplingTainted} do Forbidden Magic 0.575.
 *
 * <p>Como a muda comum, ela espera a luz e o tempo; quando cresce, tira-se a muda e tenta-se a árvore — se não
 * couber, a muda volta para o lugar.
 */
public class TaintedSaplingBlock extends VegetationBlock implements BonemealableBlock {
    public static final MapCodec<TaintedSaplingBlock> CODEC = simpleCodec(TaintedSaplingBlock::new);
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);

    public TaintedSaplingBlock(Properties properties) {
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
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) < 9) return;
        if (random.nextInt(7) != 0) return;
        this.grow(level, pos, state, random);
    }

    /** Tira a muda e tenta a árvore; se não der, a devolve. */
    public boolean grow(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        level.removeBlock(pos, false);
        boolean grew = TaintedTree.generate(level, random, pos, 4);
        if (!grew) level.setBlock(pos, state, Block.UPDATE_NONE);
        return grew;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < 0.45f;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        this.grow(level, pos, state, random);
    }
}
