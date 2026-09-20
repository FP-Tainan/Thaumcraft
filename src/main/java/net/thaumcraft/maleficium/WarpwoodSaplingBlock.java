package net.thaumcraft.maleficium;

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
 * A muda da árvore distorcida: o {@code BlockWarpwoodSapling} do Tainted Magic.
 *
 * <p>Como a do jogo, ela espera a luz e o tempo; quando cresce, tira-se a muda e tenta-se a árvore — se não couber,
 * a muda volta para o lugar. A farinha de osso vale como numa muda comum, que é o que o Thaumcraft já faz com as
 * mudas mágicas dele.
 */
public class WarpwoodSaplingBlock extends VegetationBlock implements BonemealableBlock {
    public static final MapCodec<WarpwoodSaplingBlock> CODEC = simpleCodec(WarpwoodSaplingBlock::new);
    private static final VoxelShape SHAPE = Block.box(1.6, 0.0, 1.6, 14.4, 12.8, 14.4);

    public WarpwoodSaplingBlock(Properties properties) {
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
        if (random.nextInt(25) != 0) return;
        this.grow(level, pos, state, random);
    }

    /** Tira a muda e tenta a árvore; se não der, a devolve. */
    public boolean grow(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        level.removeBlock(pos, false);
        boolean grew = WarpwoodTree.generate(level, random, pos, 7, 5);
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
