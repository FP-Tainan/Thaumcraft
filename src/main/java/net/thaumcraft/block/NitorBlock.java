package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
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
public class NitorBlock extends BaseEntityBlock {
    public static final MapCodec<NitorBlock> CODEC = simpleCodec(NitorBlock::new);
    /** Ele é só a chama: um miolinho no meio do bloco, para a mira ter onde pegar. */
    private static final VoxelShape SHAPE = Block.box(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);

    public NitorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // o Nitor não tem modelo: ele é um brilho virado para quem olha, e quem o desenha é o
        // NitorRenderer. Um modelo de bloco chapado ficaria com cara de adesivo colado no ar.
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new net.thaumcraft.block.entity.NitorBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // uma faísca de vez em quando, de leve: o brilho em si quem desenha é o NitorRenderer
        if (random.nextInt(6) != 0) return;
        level.addParticle(ParticleTypes.END_ROD,
                pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.35,
                pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.35,
                pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.35,
                0.0, 0.012, 0.0);
    }
}
