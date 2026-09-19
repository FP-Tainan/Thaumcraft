package net.thaumcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.registry.TCSounds;

/**
 * O campo de faísca: o número 10 do {@code BlockAiry} da 4.2.3.5, que o choque de terra espalha pelo chão. Não se vê
 * nem se pisa: é só faísca azul e o zumbido da escada de Jacó. Quem passa leva um ou dois de dano mágico e anda
 * devagar; some sozinho, num tique ao acaso ou, a cada toque, uma vez em cem.
 */
public class SparkFieldBlock extends Block {
    public static final MapCodec<SparkFieldBlock> CODEC = simpleCodec(SparkFieldBlock::new);

    public SparkFieldBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getEntityInsideCollisionShape(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return Shapes.block();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean past) {
        if (level instanceof ServerLevel server) {
            entity.hurtServer(server, level.damageSources().magic(), 1 + level.getRandom().nextInt(2));
            if (level.getRandom().nextInt(100) == 0) level.removeBlock(pos, false);
        }
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.8, 1.0, 0.8));
    }

    /** O {@code updateTick}: some. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }

    /** O {@code randomDisplayTick}: uma faísca azul-clara por tique e, de vez em quando, o zumbido. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
        clientEffects.spark(level, pos, r);
        if (r.nextInt(50) == 0) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), TCSounds.JACOBS.value(), SoundSource.BLOCKS, 0.5f,
                    1.0f + (r.nextFloat() - r.nextFloat()) * 0.2f, false);
        }
    }

    public interface ClientEffects {
        void spark(Level level, BlockPos pos, RandomSource random);
    }

    public static ClientEffects clientEffects = (level, pos, random) -> {
    };
}
