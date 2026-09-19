package net.thaumcraft.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCSounds;

/**
 * O orbe que persegue o alvo: o {@code EntityGolemOrb} da 4.2.3.5, que o clérigo, o pretor e o golem eldritch atiram.
 * Sem gravidade, vai puxando para o meio do alvo; batendo em alguém, dá o dano de quem atirou (o vermelho inteiro, o
 * azul seis décimos), estala e estoura. Some sozinho em oito segundos (o vermelho, doze). Batido, sai para onde o
 * atacante olha.
 */
public class GolemOrbEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(GolemOrbEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> RED = SynchedEntityData.defineId(GolemOrbEntity.class, EntityDataSerializers.BOOLEAN);

    /** O estouro, do lado de quem vê. */
    public static java.util.function.Consumer<GolemOrbEntity> clientBurst = orb -> {
    };

    public GolemOrbEntity(EntityType<? extends GolemOrbEntity> type, Level level) {
        super(type, level);
    }

    public GolemOrbEntity(Level level, LivingEntity thrower, LivingEntity target, boolean red) {
        super(TCEntities.GOLEM_ORB, level);
        this.setOwner(thrower);
        Throw.fromThrower(this, thrower, 1.5f);
        this.entityData.set(TARGET, target == null ? -1 : target.getId());
        this.entityData.set(RED, red);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TARGET, -1);
        builder.define(RED, false);
    }

    public boolean isRed() {
        return this.entityData.get(RED);
    }

    /** O {@code setThrowableHeading}, com o espalhamento do jogo de então. */
    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Throw.shoot(this, x, y, z, velocity, inaccuracy);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > (this.isRed() ? 240 : 160)) this.discard();
        Entity t = this.level().getEntity(this.entityData.get(TARGET));
        if (t instanceof LivingEntity target) {
            double d = this.distanceToSqr(target);
            double dx = (target.getX() - this.getX()) / d;
            double dy = (target.getBoundingBox().minY + target.getBbHeight() * 0.6 - this.getY()) / d;
            double dz = (target.getZ() - this.getZ()) / d;
            var m = this.getDeltaMovement().add(dx * 0.2, dy * 0.2, dz * 0.2);
            this.setDeltaMovement(Mth.clamp(m.x, -0.25, 0.25), Mth.clamp(m.y, -0.25, 0.25), Mth.clamp(m.z, -0.25, 0.25));
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (this.level() instanceof ServerLevel server) {
            if (this.getOwner() instanceof LivingEntity owner && hit instanceof EntityHitResult entityHit) {
                var attack = owner.getAttribute(Attributes.ATTACK_DAMAGE);
                float damage = (float) (attack == null ? 0.0 : attack.getValue()) * (this.isRed() ? 1.0f : 0.6f);
                entityHit.getEntity().hurtServer(server, this.damageSources().thrown(this, owner), damage);
            }
            server.playSound(null, this.getX(), this.getY(), this.getZ(), TCSounds.SHOCK.value(), SoundSource.HOSTILE, 1.0f,
                    1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
            server.broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) clientBurst.accept(this);
        else super.handleEntityEvent(id);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInvulnerableToBase(source)) return false;
        this.markHurt();
        Entity by = source.getEntity();
        if (by == null) return false;
        this.setDeltaMovement(by.getLookAngle().scale(0.9));
        level.playSound(null, this, TCSounds.ZAP.value(), SoundSource.HOSTILE, 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 0.1f;
    }
}
