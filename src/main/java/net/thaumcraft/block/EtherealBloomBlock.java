package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.registry.TCBlocks;

/**
 * A Flor Etérea: a única coisa que faz a mácula recuar.
 *
 * <p>É a flor do original, e a resposta dele para quem deixou a mácula crescer demais. Plantada, ela
 * limpa o que está maculado à volta dela, um pedaço de cada vez — a crosta volta a ser terra, o solo
 * maculado volta a ser grama, e as fibras somem.
 */
public class EtherealBloomBlock extends VegetationBlock {
    public static final MapCodec<EtherealBloomBlock> CODEC = simpleCodec(EtherealBloomBlock::new);
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 14.0, 12.0);
    /** Até onde ela limpa. */
    private static final int REACH = 8;

    public EtherealBloomBlock(Properties properties) {
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
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // ela procura mácula à volta e desfaz um pedaço por vez
        for (int tries = 0; tries < 16; tries++) {
            BlockPos at = pos.offset(
                    random.nextInt(REACH * 2 + 1) - REACH,
                    random.nextInt(REACH + 1) - REACH / 2,
                    random.nextInt(REACH * 2 + 1) - REACH);
            if (!level.isLoaded(at)) continue;
            BlockState there = level.getBlockState(at);
            if (!TaintBlock.isTaint(there)) continue;

            if (there.is(TCBlocks.TAINT_FIBRES)) {
                level.removeBlock(at, false);
            } else if (there.is(TCBlocks.TAINT_CRUST)) {
                level.setBlockAndUpdate(at, Blocks.DIRT.defaultBlockState());
            } else {
                level.setBlockAndUpdate(at, Blocks.GRASS_BLOCK.defaultBlockState());
            }
            level.sendParticles(ParticleTypes.END_ROD,
                    at.getX() + 0.5, at.getY() + 0.8, at.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.02);
            return;
        }
    }

    @Override
    public void animateTick(BlockState state, net.minecraft.world.level.Level level, BlockPos pos,
                            RandomSource random) {
        if (random.nextInt(4) != 0) return;
        level.addParticle(ParticleTypes.END_ROD,
                pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                pos.getY() + 0.7 + random.nextDouble() * 0.3,
                pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                0.0, 0.01, 0.0);
    }
}
