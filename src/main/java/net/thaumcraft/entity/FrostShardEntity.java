package net.thaumcraft.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;

/**
 * A esfera de gelo do foco de gelo: o {@code EntityFrostShard} da 4.2.3.5, descompilado.
 *
 * <p>Sai a um bloco e meio por tique, cai de leve e quica: em parede, chão ou criatura, ela volta com metade
 * da pressa, e na quarta batida se parte num estalo de vidro, espalhando lascas. Quem ela acerta leva o dano
 * dela — três, sem potência. Ela não congela água nem deixa neve: isso é da melhoria do gelo alquímico, que
 * no original também só deixa lentidão.
 */
public class FrostShardEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> DAMAGE =
            SynchedEntityData.defineId(FrostShardEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> FROSTY =
            SynchedEntityData.defineId(FrostShardEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FRAGILE =
            SynchedEntityData.defineId(FrostShardEntity.class, EntityDataSerializers.BOOLEAN);

    /** O quanto volta de cada batida: meio, ou 0,8 no pedregulho de gelo. */
    public double bounce = 0.5;
    /** Quantas batidas aguenta: três, ou seis no pedregulho. */
    public int bounceLimit = 3;

    /** As faíscas do gelo alquímico, do lado de quem vê. */
    public static java.util.function.BiConsumer<FrostShardEntity, Integer> clientSparkle = (shard, frosty) -> {
    };

    public FrostShardEntity(EntityType<? extends FrostShardEntity> type, Level level) {
        super(type, level);
    }

    /** O {@code EntityFrostShard(world, thrower, scatter)}. */
    public FrostShardEntity(Level level, LivingEntity thrower, float scatter) {
        super(TCEntities.FROST_SHARD, level);
        this.setOwner(thrower);
        Throw.from(this, thrower, 1.5f, scatter);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, 0.0f);
        builder.define(FROSTY, 0);
        builder.define(FRAGILE, false);
    }

    public void setDamage(float damage) {
        this.entityData.set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.entityData.get(DAMAGE);
    }

    /** O {@code frosty}: os níveis de gelo alquímico, que deixam lentidão em quem é atingido. */
    public void setFrosty(int frosty) {
        this.entityData.set(FROSTY, frosty);
    }

    public int getFrosty() {
        return this.entityData.get(FROSTY);
    }

    /** O {@code fragile}: os estilhaços do tiro espalhado, que caem menos e se partem no primeiro alvo. */
    public void setFragile(boolean fragile) {
        this.entityData.set(FRAGILE, fragile);
    }

    public boolean isFragile() {
        return this.entityData.get(FRAGILE);
    }

    @Override
    protected double getDefaultGravity() {
        return this.isFragile() ? 0.015 : 0.05;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() && this.getFrosty() > 0) clientSparkle.accept(this, this.getFrosty());
        // a esfera vira para onde voa, devagar: um quinto do caminho por tique
        Vec3 motion = this.getDeltaMovement();
        double horizontal = motion.horizontalDistance();
        float yaw = (float) (Math.atan2(motion.x, motion.z) * 180.0 / Math.PI);
        float pitch = (float) (Math.atan2(motion.y, horizontal) * 180.0 / Math.PI);
        while (pitch - this.xRotO < -180.0f) this.xRotO -= 360.0f;
        while (pitch - this.xRotO >= 180.0f) this.xRotO += 360.0f;
        while (yaw - this.yRotO < -180.0f) this.yRotO -= 360.0f;
        while (yaw - this.yRotO >= 180.0f) this.yRotO += 360.0f;
        this.setXRot(this.xRotO + (pitch - this.xRotO) * 0.2f);
        this.setYRot(this.yRotO + (yaw - this.yRotO) * 0.2f);
    }

    @Override
    protected void onHit(HitResult hit) {
        Vec3 m = this.getDeltaMovement();
        double mx = m.x, my = m.y, mz = m.z;
        Level level = this.level();
        if (hit instanceof EntityHitResult entityHit) {
            Entity target = entityHit.getEntity();
            // volta pelo lado em que bateu
            if (this.getBlockZ() != target.getBlockZ()) mz *= -1.0;
            if (this.getBlockX() != target.getBlockX()) mx *= -1.0;
            if (this.getBlockY() != target.getBlockY()) my *= -0.9;
            mx *= 0.66;
            my *= 0.66;
            mz *= 0.66;
            this.shatter(level, frost(), (int) this.getDamage());
            if (level instanceof ServerLevel server) {
                Vec3 before = target.getDeltaMovement();
                target.hurtServer(server, this.damageSources().thrown(this, this.getOwner()), this.getDamage());
                if (target instanceof LivingEntity living && this.getFrosty() > 0) {
                    living.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOWNESS, 200, this.getFrosty() - 1));
                }
                if (this.isFragile()) {
                    // o estilhaço se parte, e o alvo nem fica invulnerável nem é empurrado de verdade
                    target.invulnerableTime = 0;
                    this.discard();
                    level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 0.3f,
                            1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
                    Vec3 after = target.getDeltaMovement();
                    target.setDeltaMovement(before.add(after.subtract(before).scale(0.1)));
                }
            }
        } else if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            var face = blockHit.getDirection();
            if (face.getStepZ() != 0) mz *= -1.0;
            if (face.getStepX() != 0) mx *= -1.0;
            if (face.getStepY() != 0) my *= -0.9;
            BlockState state = level.getBlockState(blockHit.getBlockPos());
            if (!level.isClientSide()) {
                level.playSound(null, this.getX(), this.getY(), this.getZ(), state.getSoundType().getBreakSound(),
                        SoundSource.NEUTRAL, 0.3f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
            }
            this.shatter(level, new BlockParticleOption(ParticleTypes.BLOCK, state), (int) this.getDamage());
        }

        mx *= this.bounce;
        my *= this.bounce;
        mz *= this.bounce;
        double speed = Math.sqrt(mx * mx + my * my + mz * mz);
        if (speed > 0.0) {
            this.setPos(this.getX() - mx / speed * 0.05, this.getY() - my / speed * 0.05, this.getZ() - mz / speed * 0.05);
        }
        this.setDeltaMovement(mx, my, mz);

        if (this.bounceLimit-- <= 0) {
            if (!level.isClientSide()) {
                level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GLASS_BREAK,
                        SoundSource.NEUTRAL, 0.3f, 1.2f / (this.random.nextFloat() * 0.2f + 0.9f));
                this.discard();
            }
            this.shatter(level, frost(), (int) (8.0f * this.getDamage()));
        }
    }

    private static ParticleOptions frost() {
        return new ItemParticleOption(ParticleTypes.ITEM, TCItems.FROST_SHARD);
    }

    /** As lascas do {@code blockcrack}: só do lado de quem vê. */
    private void shatter(Level level, ParticleOptions particle, int count) {
        if (!level.isClientSide()) return;
        for (int a = 0; a < count; a++) {
            level.addParticle(particle, this.getX(), this.getY(), this.getZ(),
                    4.0 * (this.random.nextFloat() - 0.5) * 0.1, 0.5 * 0.1, (this.random.nextFloat() - 0.5) * 4.0 * 0.1);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
