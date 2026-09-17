package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * O Nitor: a chama fria que não queima nada e não se apaga.
 *
 * <p>No original ele é a luz de todo taumaturgo — posta-se no ar ou na parede e fica lá, acesa para
 * sempre, sem precisar de tocha nem de suporte. É o {@code blockAiry} do mod: um bloco que não segura
 * ninguém, não tem face para quebrar contra, e acende o lugar inteiro.
 */
public class NitorBlock extends Block {
    public static final MapCodec<NitorBlock> CODEC = simpleCodec(NitorBlock::new);
    /** Ele é só a chama: um miolinho no meio do bloco, para a mira ter onde pegar. */
    private static final VoxelShape SHAPE = Block.box(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);

    public NitorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // a chama respira e solta faísca, como no original
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        level.addParticle(ParticleTypes.SMALL_FLAME,
                x + (random.nextDouble() - 0.5) * 0.25,
                y + (random.nextDouble() - 0.5) * 0.25,
                z + (random.nextDouble() - 0.5) * 0.25,
                0.0, 0.005, 0.0);
        if (random.nextInt(4) != 0) return;
        level.addParticle(ParticleTypes.END_ROD,
                x + (random.nextDouble() - 0.5) * 0.4,
                y + (random.nextDouble() - 0.5) * 0.4,
                z + (random.nextDouble() - 0.5) * 0.4,
                0.0, 0.01, 0.0);
    }
}
