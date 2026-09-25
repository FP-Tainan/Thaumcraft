package net.thaumcraft.forbidden;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * O Arbusto Umbrio: o {@code BlockRoseBush} do Forbidden Magic 0.575.
 *
 * <p>Uma roseira preta de dois blocos de altura que espalha Flores de Tinta à volta — sozinha, três vezes em
 * dez a cada batida do acaso, e de uma vez só quando alguém lhe joga farinha de osso.
 */
public class UmbralBushBlock extends DoublePlantBlock implements net.minecraft.world.level.block.BonemealableBlock {
    public static final MapCodec<UmbralBushBlock> CODEC = simpleCodec(UmbralBushBlock::new);

    public UmbralBushBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends DoublePlantBlock> codec() {
        return CODEC;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) <= 2) this.spread(level, pos, random);
    }

    /** O {@code spreadFlowers} dele: uma Flor de Tinta num lugar vago dos arredores. */
    private void spread(ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(ForbiddenBlocks.INK_FLOWER instanceof InkFlowerBlock flor)) return;
        BlockPos onde = pos;
        BlockPos tentativa = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
        for (int volta = 0; volta < 8; volta++) {
            if (level.isEmptyBlock(tentativa) && flor.defaultBlockState().canSurvive(level, tentativa)) {
                onde = tentativa;
            }
            tentativa = onde.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
        }
        if (level.isEmptyBlock(tentativa) && flor.defaultBlockState().canSurvive(level, tentativa)) {
            level.setBlock(tentativa, flor.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(net.minecraft.world.level.Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        this.spread(level, pos, random);
    }
}
