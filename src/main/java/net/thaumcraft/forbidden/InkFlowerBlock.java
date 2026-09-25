package net.thaumcraft.forbidden;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;

/**
 * A Flor de Tinta: o {@code BlockBlackFlower} do Forbidden Magic 0.575.
 *
 * <p>Uma flor preta que se espalha sozinha pelos arredores, mas só até dez delas num pedaço de cinco por três
 * por cinco — dela sai a tinta preta.
 */
public class InkFlowerBlock extends net.minecraft.world.level.block.VegetationBlock {
    public static final MapCodec<InkFlowerBlock> CODEC = simpleCodec(InkFlowerBlock::new);

    public InkFlowerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends net.minecraft.world.level.block.VegetationBlock> codec() {
        return CODEC;
    }

    /**
     * O {@code spreadFlowers}: com dez flores já perto, ela para; senão procura um lugar vago em volta, oito
     * tentativas, e nasce lá.
     */
    public void spread(LevelAccessor level, BlockPos pos, RandomSource random) {
        int cabem = 10;
        for (BlockPos perto : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 1, 2))) {
            if (level.getBlockState(perto).is(this) && --cabem <= 0) return;
        }
        BlockPos onde = pos;
        BlockPos tentativa = sorteia(pos, random);
        for (int volta = 0; volta < 4; volta++) {
            if (level.isEmptyBlock(tentativa) && this.defaultBlockState().canSurvive(level, tentativa)) {
                onde = tentativa;
            }
            tentativa = sorteia(onde, random);
        }
        if (level.isEmptyBlock(tentativa) && this.defaultBlockState().canSurvive(level, tentativa)) {
            level.setBlock(tentativa, this.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static BlockPos sorteia(BlockPos pos, RandomSource random) {
        return pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
    }
}
