package net.thaumcraft.entity.eldritch;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.entity.Throw;
import net.thaumcraft.registry.TCEntities;

/**
 * O orbe eldritch: o {@code EntityEldritchOrb} da 4.2.3.5, que o guardião atira. Sem gravidade; batendo, dá dois terços
 * do dano de quem atirou e fraqueza a tudo o que é vivo a até dois blocos (menos os mortos-vivos), chia e se desfaz em
 * fogos-fátuos.
 */
public class EldritchOrbEntity extends ThrowableProjectile {
    /** O estouro, do lado de quem vê. */
    public static java.util.function.Consumer<EldritchOrbEntity> clientBurst = orb -> {
    };

    public EldritchOrbEntity(EntityType<? extends EldritchOrbEntity> type, Level level) {
        super(type, level);
    }

    public EldritchOrbEntity(Level level, LivingEntity thrower) {
        super(TCEntities.ELDRITCH_ORB, level);
        this.setOwner(thrower);
        Throw.fromThrower(this, thrower, 1.5f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
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
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(this.level() instanceof ServerLevel server) || !(this.getOwner() instanceof LivingEntity owner)) return;
        var attack = owner.getAttribute(Attributes.ATTACK_DAMAGE);
        float damage = (float) (attack == null ? 0.0 : attack.getValue()) * 0.666f;
        for (Entity e : server.getEntities(owner, this.getBoundingBox().inflate(2.0))) {
            if (e instanceof LivingEntity living && !living.isInvertedHealAndHarm()) {
                living.hurtServer(server, this.damageSources().thrown(this, owner), damage);
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));
            }
        }
        server.playSound(null, this, SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 0.5f,
                2.6f + (this.random.nextFloat() - this.random.nextFloat()) * 0.8f);
        this.tickCount = 100;
        server.broadcastEntityEvent(this, (byte) 16);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) clientBurst.accept(this);
        else super.handleEntityEvent(id);
    }
}
