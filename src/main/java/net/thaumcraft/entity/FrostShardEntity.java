package net.thaumcraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;

/**
 * A lasca de gelo que o foco de gelo atira.
 *
 * <p>No original ela sai da varinha, voa reto, machuca quem acerta e congela a água em que bate. O dano é
 * o mesmo do original sem melhorias: três de dano. A lentidão que ela deixa é acréscimo daqui, e está
 * anotada em {@code docs/PORTE.md} — o original entrega esse efeito pela melhoria do gelo alquímico, que
 * ainda não existe neste porte.
 */
public class FrostShardEntity extends ThrowableItemProjectile {
    /** O dano do original, {@code setDamage(3.0F + potency * 1.5D)} sem potência nenhuma. */
    private static final float DAMAGE = 3.0f;
    /** Até onde a lasca vai antes de sumir sozinha, em tiques. */
    private static final int LIFETIME = 100;

    public FrostShardEntity(EntityType<? extends FrostShardEntity> type, Level level) {
        super(type, level);
    }

    public FrostShardEntity(Level level, LivingEntity thrower) {
        super(TCEntities.FROST_SHARD, thrower, level, new ItemStack(TCItems.FROST_SHARD));
    }

    @Override
    protected Item getDefaultItem() {
        return TCItems.FROST_SHARD;
    }

    @Override
    protected double getDefaultGravity() {
        // a lasca vai quase reta; o original dá a ela uma queda bem de leve
        return 0.03;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY(), this.getZ(), 2, 0.05, 0.05, 0.05, 0.01);
            if (this.tickCount > LIFETIME) this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        Entity target = hit.getEntity();
        Entity shooter = this.getOwner();
        target.hurt(this.damageSources().thrown(this, shooter), DAMAGE);
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!(this.level() instanceof ServerLevel server)) return;

        // água vira gelo, como no original; e onde há chão livre fica uma camada de neve
        BlockPos pos = hit.getBlockPos();
        BlockState state = server.getBlockState(pos);
        if (state.getFluidState().getType() == Fluids.WATER && state.getFluidState().isSource()) {
            server.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
        } else {
            BlockPos above = hit.getBlockPos().relative(hit.getDirection());
            if (server.getBlockState(above).isAir()
                    && Blocks.SNOW.defaultBlockState().canSurvive(server, above)) {
                server.setBlockAndUpdate(above, Blocks.SNOW.defaultBlockState());
            }
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY(), this.getZ(), 20, 0.2, 0.2, 0.2, 0.08);
            server.playSound(null, this.blockPosition(), net.thaumcraft.registry.TCSounds.ICE.value(),
                    SoundSource.PLAYERS, 0.6f, 1.2f);
            this.discard();
        }
    }
}
