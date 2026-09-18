package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.world.GreatwoodTree;
import net.thaumcraft.world.SilverwoodTree;

/**
 * As mudas das árvores mágicas: os dois primeiros tipos do {@code BlockCustomPlant} da 4.2.3.5.
 *
 * <p>Com luz nove ou mais em cima, a muda de grande-madeira vira árvore uma vez em vinte e cinco tiques ao
 * acaso, e a de pinheiro-de-prata uma vez em cinquenta.
 *
 * <p><b>Diferença do original, pedida:</b> o {@code BlockCustomPlant} não aceitava farinha de osso. Aqui ela vale
 * como numa muda comum: 45% de chance de tentar a árvore a cada uso.
 * Se a árvore não couber, a muda volta para o lugar.
 */
public class MagicalSaplingBlock extends VegetationBlock implements BonemealableBlock {
    public static final MapCodec<MagicalSaplingBlock> CODEC = simpleCodec(properties -> new MagicalSaplingBlock(false, properties));
    public static final MapCodec<MagicalSaplingBlock> SILVER_CODEC = simpleCodec(properties -> new MagicalSaplingBlock(true, properties));
    /** A caixa do {@code BlockCustomPlant}: quatro décimos para cada lado do meio, oito décimos de altura. */
    private static final VoxelShape SHAPE = Block.box(1.6, 0.0, 1.6, 14.4, 12.8, 14.4);

    private final boolean silverwood;

    public MagicalSaplingBlock(boolean silverwood, Properties properties) {
        super(properties);
        this.silverwood = silverwood;
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return this.silverwood ? SILVER_CODEC : CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) < 9) return;
        if (random.nextInt(this.silverwood ? 50 : 25) != 0) return;
        this.grow(level, pos, state, random);
    }

    /** O {@code growGreatTree} e o {@code growSilverTree}: tira a muda e tenta a árvore; se não der, a devolve. */
    public boolean grow(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        level.removeBlock(pos, false);
        boolean grew = this.silverwood
                ? SilverwoodTree.generate(level, random, pos, 7, 5, false)
                : GreatwoodTree.generate(level, random, pos, false, false);
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
