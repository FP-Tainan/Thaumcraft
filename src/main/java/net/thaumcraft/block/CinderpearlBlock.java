package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A pérola de cinzas: o quarto tipo do {@code BlockCustomPlant} da 4.2.3.5.
 *
 * <p>A flor do deserto, planta de terra seca: pega na areia e na terracota, brilha com luz oito e, uma vez em
 * duas, solta fumaça e uma chaminha. Moída na mesa, vira pó de blaze.
 */
public class CinderpearlBlock extends VegetationBlock {
    public static final MapCodec<CinderpearlBlock> CODEC = simpleCodec(CinderpearlBlock::new);
    /** A caixa do {@code BlockCustomPlant}: quatro décimos para cada lado do meio, oito décimos de altura. */
    private static final VoxelShape SHAPE = Block.box(1.6, 0.0, 1.6, 14.4, 12.8, 14.4);

    public CinderpearlBlock(Properties properties) {
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

    /** O {@code EnumPlantType.Desert} do Forge: areia e terracota; e a grama, onde o gerador também a põe. */
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.SAND) || state.is(BlockTags.TERRACOTTA) || super.mayPlaceOn(state, level, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!random.nextBoolean()) return;
        RandomSource r = level.getRandom();
        double x = pos.getX() + 0.5f + (r.nextFloat() - r.nextFloat()) * 0.1f;
        double y = pos.getY() + 0.6f + (r.nextFloat() - r.nextFloat()) * 0.1f;
        double z = pos.getZ() + 0.5f + (r.nextFloat() - r.nextFloat()) * 0.1f;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
    }
}
