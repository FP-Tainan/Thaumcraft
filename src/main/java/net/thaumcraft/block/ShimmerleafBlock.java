package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A folha-cintilante: a flor branca que brilha de leve no escuro.
 *
 * <p>É dela que se faz a Flor Etérea, a única coisa que faz a mácula recuar. No original ela nasce
 * debaixo dos pinheiros-de-prata; esses ainda não chegaram por aqui, e por isso ela nasce sozinha e rara
 * pelas florestas. Está anotado em {@code docs/PORTE.md}.
 */
public class ShimmerleafBlock extends VegetationBlock {
    public static final MapCodec<ShimmerleafBlock> CODEC = simpleCodec(ShimmerleafBlock::new);
    private static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 11.0, 11.0);

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
        if (random.nextInt(8) != 0) return;
        level.addParticle(ParticleTypes.END_ROD,
                pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.3,
                pos.getY() + 0.5 + random.nextDouble() * 0.3,
                pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.3,
                0.0, 0.005, 0.0);
    }
}
