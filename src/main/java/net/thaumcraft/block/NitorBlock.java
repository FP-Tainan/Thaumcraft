package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
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
 *
 * <p>Ele não solta partícula nenhuma de propósito. É chama mágica, e não fogueira: as faíscas do jogo
 * são bolotas brancas grandes demais, e chegavam a cobrir o próprio brilho. O que se vê dele é só o que
 * o {@link net.thaumcraft.client.render.NitorRenderer} desenha.
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

}
