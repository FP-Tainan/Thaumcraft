package net.thaumcraft.maleficium.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.entity.Throw;

/**
 * A bola de matéria escura: o {@code EntityDarkMatter} do Tainted Magic 8.1.1.
 *
 * <p>Voa reto, sem peso, e ao bater fere tudo o que estiver a um bloco e meio (mais meio por cada aumento) e deixa
 * todo mundo fraco por oito segundos — e definhando, se o foco tiver a melhoria corrosiva.
 */
public class DarkMatterEntity extends ThrowableProjectile {
    /** O estouro, do lado de quem vê. */
    public static java.util.function.Consumer<DarkMatterEntity> clientBurst = orb -> {
    };

    private float damage;
    private int enlarge;
    private boolean corrosive;

    public DarkMatterEntity(EntityType<? extends DarkMatterEntity> type, Level level) {
        super(type, level);
    }

    public DarkMatterEntity(Level level, LivingEntity thrower, float damage, int enlarge, boolean corrosive) {
        super(MaleficiumEntities.DARK_MATTER, level);
        this.setOwner(thrower);
        this.damage = damage;
        this.enlarge = enlarge;
        this.corrosive = corrosive;
        Throw.fromThrower(this, thrower, 1.5f);
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
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(this.level() instanceof ServerLevel server) || !(this.getOwner() instanceof LivingEntity owner)) return;
        double expand = 1.5 + this.enlarge * 0.5;
        for (Entity e : server.getEntities(owner, this.getBoundingBox().inflate(expand))) {
            if (!(e instanceof LivingEntity living)) continue;
            living.hurtServer(server, this.damageSources().thrown(this, owner), this.damage);
            if (this.corrosive) living.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 1));
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
        }
        server.playSound(null, this, SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 0.5f,
                2.6f + (this.random.nextFloat() - this.random.nextFloat()) * 0.8f);
        this.tickCount = 100;
        server.broadcastEntityEvent(this, (byte) 16);
        this.discard();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) clientBurst.accept(this);
        else super.handleEntityEvent(id);
    }
}
