package net.thaumcraft.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.registry.TCEntities;

/**
 * A rajada do pech: o {@code EntityPechBlast} da 4.2.3.5, que sai da varinha do pech mago e do foco dos pechs. Uma
 * bolinha de fogos-fátuos que cai de leve e, onde bate, fere tudo a dois blocos (menos pechs) e deixa veneno,
 * lentidão ou fraqueza — as três juntas com a sombra-da-noite.
 */
public class PechBlastEntity extends ThrowableProjectile {
    private int strength;
    private int duration;
    private boolean nightshade;

    public PechBlastEntity(EntityType<? extends PechBlastEntity> type, Level level) {
        super(type, level);
    }

    public PechBlastEntity(Level level, LivingEntity thrower, int strength, int duration, boolean nightshade) {
        super(TCEntities.PECH_BLAST, level);
        this.setOwner(thrower);
        Throw.from(this, thrower, 1.5f, 1.0f);
        this.strength = strength;
        this.duration = duration;
        this.nightshade = nightshade;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.025;
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) net.thaumcraft.client.NodeClient.pechBlastTrail(this);
        super.tick();
        if (this.tickCount > 500) this.discard();
    }

    /** O {@code onImpact}: um estouro de fogos-fátuos e o dano a dois blocos. */
    @Override
    protected void onHit(HitResult hit) {
        if (this.level().isClientSide()) {
            net.thaumcraft.client.NodeClient.pechBlastBurst(this);
            return;
        }
        if (!(this.level() instanceof ServerLevel level)) return;
        Entity owner = this.getOwner();
        for (Entity entity : level.getEntities(owner, this.getBoundingBox().inflate(2.0))) {
            if (entity instanceof PechEntity || !(entity instanceof LivingEntity living)) continue;
            living.hurtServer(level, this.damageSources().thrown(this, owner), this.strength + 2);
            int time = 100 + this.duration * 40;
            if (this.nightshade) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, time, this.strength));
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, time, this.strength + 1));
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, time, this.strength));
            } else {
                switch (this.random.nextInt(3)) {
                    case 0 -> living.addEffect(new MobEffectInstance(MobEffects.POISON, time, this.strength));
                    case 1 -> living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, time, this.strength + 1));
                    default -> living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, time, this.strength));
                }
            }
        }
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("strength", this.strength);
        output.putInt("duration", this.duration);
        output.putBoolean("nightshade", this.nightshade);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.strength = input.getIntOr("strength", 0);
        this.duration = input.getIntOr("duration", 0);
        this.nightshade = input.getBooleanOr("nightshade", false);
    }
}
