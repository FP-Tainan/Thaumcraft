package net.thaumcraft.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.registry.TCEntities;

/**
 * A bola de fogo do foco de fogo com a melhoria de mesmo nome: o {@code EntityExplosiveOrb} da 4.2.3.5. Voa quase reta
 * (cai um centésimo por tique) e, batendo, estoura sem quebrar blocos — mais forte com a potência e, com o fogo
 * alquímico, pondo fogo em volta; quem ela acerta em cheio leva uma vez e meia a força em dano de fogo. Batida por
 * alguém, sai na direção em que esse alguém olha.
 */
public class ExplosiveOrbEntity extends ThrowableProjectile {
    /** A força do estouro: 1, mais 0,4 por potência. */
    public float strength = 1.0f;
    /** O {@code onFire}: com fogo alquímico, o estouro acende em volta. */
    public boolean onFire;

    /** As faíscas que ela vai soltando, do lado de quem vê. */
    public static java.util.function.Consumer<ExplosiveOrbEntity> clientTrail = orb -> {
    };

    public ExplosiveOrbEntity(EntityType<? extends ExplosiveOrbEntity> type, Level level) {
        super(type, level);
    }

    public ExplosiveOrbEntity(Level level, LivingEntity thrower) {
        super(TCEntities.EXPLOSIVE_ORB, level);
        this.setOwner(thrower);
        Throw.once(this, thrower, 1.5f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.01;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) clientTrail.accept(this);
        if (this.tickCount > 500) this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!(this.level() instanceof ServerLevel server)) return;
        Entity owner = this.getOwner();
        DamageSource source = owner == null
                ? new DamageSource(server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.ON_FIRE), this, this)
                : new DamageSource(server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.FIREBALL), this, owner);
        hit.getEntity().hurtServer(server, source, this.strength * 1.5f);
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (this.level() instanceof ServerLevel server) {
            server.explode(null, this.getX(), this.getY(), this.getZ(), this.strength, this.onFire, Level.ExplosionInteraction.NONE);
        }
        this.discard();
    }

    /** O {@code attackEntityFrom}: batida, sai na direção em que o atacante olha. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInvulnerableToBase(source)) return false;
        this.markHurt();
        Entity by = source.getEntity();
        if (by == null) return false;
        Vec3 look = by.getLookAngle();
        this.setDeltaMovement(look.scale(0.9));
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

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("strength", this.strength);
        output.putBoolean("onFire", this.onFire);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.strength = input.getFloatOr("strength", 1.0f);
        this.onFire = input.getBooleanOr("onFire", false);
    }
}
