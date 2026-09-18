package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A folha-cintilante: o terceiro tipo do {@code BlockCustomPlant} da 4.2.3.5.
 *
 * <p>É dela que se faz a Flor Etérea, a única coisa que faz a mácula recuar. Nasce em volta do pé dos
 * pinheiros-de-prata, brilha com luz oito e, uma vez em três, solta um fogo-fátuo pequeno, entre o ciano e o
 * branco.
 */
public class ShimmerleafBlock extends VegetationBlock {
    /** Os efeitos do lado de quem vê, que o cliente pendura aqui ao abrir. */
    public interface ClientEffects {
        void wisp(double x, double y, double z, float size, float red, float green, float blue);
    }

    public static ClientEffects clientEffects = (x, y, z, size, red, green, blue) -> {
    };

    public static final MapCodec<ShimmerleafBlock> CODEC = simpleCodec(ShimmerleafBlock::new);
    /** A caixa do {@code BlockCustomPlant}: quatro décimos para cada lado do meio, oito décimos de altura. */
    private static final VoxelShape SHAPE = Block.box(1.6, 0.0, 1.6, 14.4, 12.8, 14.4);

    public ShimmerleafBlock(Properties properties) {
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
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0) return;
        RandomSource r = level.getRandom();
        float red = 0.3f + r.nextFloat() * 0.3f;
        float green = 0.7f + r.nextFloat() * 0.3f;
        float blue = 0.7f + r.nextFloat() * 0.3f;
        clientEffects.wisp(pos.getX() + 0.5f + (r.nextFloat() - r.nextFloat()) * 0.1f,
                pos.getY() + 0.5f + (r.nextFloat() - r.nextFloat()) * 0.15f,
                pos.getZ() + 0.5f + (r.nextFloat() - r.nextFloat()) * 0.1f, 0.2f, red, green, blue);
    }
}
