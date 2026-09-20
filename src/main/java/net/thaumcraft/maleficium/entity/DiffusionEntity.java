package net.thaumcraft.maleficium.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.entity.Throw;

/**
 * A névoa de matéria escura: o {@code EntityDiffusion} do Tainted Magic 8.1.1.
 *
 * <p>É o que a melhoria de difusão faz sair do foco em vez do tiro concentrado: um punhado de bolinhas espalhadas
 * que, ao encostar em alguém, ferem e deixam fraco por dois segundos — e definhando, se for corrosiva.
 */
public class DiffusionEntity extends ThrowableProjectile {
    /** A poeira que fica, do lado de quem vê. */
    public static java.util.function.Consumer<DiffusionEntity> clientBurst = cloud -> {
    };

    private float damage;
    private boolean corrosive;

    public DiffusionEntity(EntityType<? extends DiffusionEntity> type, Level level) {
        super(type, level);
    }

    public DiffusionEntity(Level level, LivingEntity thrower, float scatter, float damage, boolean corrosive) {
        super(MaleficiumEntities.DIFFUSION, level);
        this.setOwner(thrower);
        this.damage = damage;
        this.corrosive = corrosive;
        Throw.fromThrower(this, thrower, 1.0f, scatter);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

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
        if (this.tickCount > 100) this.discard();
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(this.level() instanceof ServerLevel server) || !(this.getOwner() instanceof LivingEntity owner)) return;
        // só fere quando a bolinha bate em alguém, como no original
        if (hit instanceof EntityHitResult) {
            for (Entity e : server.getEntities(owner, this.getBoundingBox().inflate(1.0))) {
                if (!(e instanceof LivingEntity living)) continue;
                living.hurtServer(server, this.damageSources().thrown(this, owner), this.damage);
                if (this.corrosive) living.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 1));
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 1));
            }
        }
        server.broadcastEntityEvent(this, (byte) 16);
        this.discard();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) clientBurst.accept(this);
        else super.handleEntityEvent(id);
    }
}
