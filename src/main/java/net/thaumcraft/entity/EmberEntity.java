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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.registry.TCEntities;

/**
 * A brasa do foco de fogo: o {@code EntityEmber} da 4.2.3.5, descompilado.
 *
 * <p>Voa a um bloco por tique, sem cair, perdendo cinco por cento da pressa a cada tique, e some em vinte
 * tiques. Quem ela acerta leva dois de dano de bola de fogo e fica em chamas por três segundos — a não ser que
 * não pegue fogo. Ela não é alvo de nada: atravessa sem ser atingida.
 */
public class EmberEntity extends ThrowableProjectile {
    /** Quantos tiques ela vive. */
    public static final int DURATION = 20;
    /** O {@code damage} do original sem potência: {@code 2 + potency}. */
    private static final float DAMAGE = 2.0f;

    public EmberEntity(EntityType<? extends EmberEntity> type, Level level) {
        super(type, level);
    }

    /** O {@code EntityEmber(world, thrower, scatter)}: sai do olho de quem lança, na direção da mira. */
    public EmberEntity(Level level, LivingEntity thrower, float scatter) {
        super(TCEntities.EMBER, level);
        this.setOwner(thrower);
        Throw.from(this, thrower, 1.0f, scatter);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        if (this.tickCount > DURATION) {
            this.discard();
            return;
        }
        this.setDeltaMovement(this.getDeltaMovement().scale(0.95));
        if (this.onGround()) this.setDeltaMovement(this.getDeltaMovement().scale(0.66));
        super.tick();
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!(this.level() instanceof ServerLevel server)) return;
        Entity target = hit.getEntity();
        if (target.fireImmune()) return;
        DamageSource source = new DamageSource(server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(DamageTypes.FIREBALL), this, this.getOwner());
        if (target.hurtServer(server, source, DAMAGE)) target.igniteForSeconds(3.0f);
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!this.level().isClientSide()) this.discard();
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }
}
