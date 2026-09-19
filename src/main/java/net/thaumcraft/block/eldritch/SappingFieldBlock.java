package net.thaumcraft.block.eldritch;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.thaumcraft.api.EldritchMob;
import net.thaumcraft.registry.TCSounds;

/**
 * O campo sugador: o número 11 do {@code BlockAiry} da 4.2.3.5, que o guardião-mor deixa por onde anda e espalha em
 * anéis. Não se vê: é só faísca roxa e o zumbido da escada de Jacó. Quem não é eldritch e passa anda devagar, cansa e
 * fica fraco, e uma vez em cem murcha; some sozinho.
 */
public class SappingFieldBlock extends Block {
    public static final MapCodec<SappingFieldBlock> CODEC = simpleCodec(SappingFieldBlock::new);

    public interface ClientEffects {
        void spark(Level level, BlockPos pos, RandomSource random);
    }

    public static ClientEffects clientEffects = (level, pos, random) -> {
    };

    public SappingFieldBlock(Properties properties) {
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
        if (entity instanceof EldritchMob) return;
        if (level instanceof ServerLevel server && level.getRandom().nextInt(100) == 0) {
            entity.hurtServer(server, level.damageSources().wither(), 1.0f);
        }
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.66, 1.0, 0.66));
        if (entity instanceof Player player) player.causeFoodExhaustion(0.05f);
        if (entity instanceof LivingEntity living && !level.isClientSide()) {
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, true, true));
        }
    }

    /** O {@code updateTick}: some — no tique marcado pelo guardião ou num ao acaso. */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
        clientEffects.spark(level, pos, r);
        if (r.nextInt(50) == 0) {
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), TCSounds.JACOBS.value(), SoundSource.BLOCKS, 0.5f,
                    1.0f + (r.nextFloat() - r.nextFloat()) * 0.2f, false);
        }
    }
}
