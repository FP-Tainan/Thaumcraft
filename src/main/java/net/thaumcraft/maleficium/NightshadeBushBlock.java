package net.thaumcraft.maleficium;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A beladona (<i>Atropa Belladonna</i>): o {@code BlockNightshadeBush} do Tainted Magic.
 *
 * <p>Quem passa por dentro dela se fere e sai envenenado por sete segundos. As bagas — de uma a três por pé — são
 * o veneno de que o mod precisa; com tesoura, o pé sai inteiro.
 */
public class NightshadeBushBlock extends VegetationBlock {
    public static final MapCodec<NightshadeBushBlock> CODEC = simpleCodec(NightshadeBushBlock::new);
    /** A caixa do arbusto do jogo antigo: um pouco menor que o bloco. */
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

    public NightshadeBushBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier applier, boolean intersects) {
        if (!(entity instanceof LivingEntity living)) return;
        entity.hurt(net.thaumcraft.registry.TCDamageTypes.nightshade(level), 1.0f);
        living.addEffect(new MobEffectInstance(MobEffects.POISON, 140, 0));
    }

}
