package net.thaumcraft.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.level.Level;

/**
 * O zumbi furioso: o {@code EntityGiantBrainyZombie} da 4.2.3.5, o que nasce em volta dos nós sombrios. Grande, com 60
 * de vida, pula em cima de quem ataca, e fica mais bravo a cada golpe que leva: a raiva (de um a dois) aumenta o
 * tamanho e o dano, e passa devagar.
 */
public class GiantBrainyZombieEntity extends BrainyZombieEntity {
    private static final EntityDataAccessor<Float> ANGER = SynchedEntityData.defineId(GiantBrainyZombieEntity.class, EntityDataSerializers.FLOAT);

    public GiantBrainyZombieEntity(EntityType<? extends GiantBrainyZombieEntity> type, Level level) {
        super(type, level);
        this.xpReward = 15;
    }

    public static AttributeSupplier.Builder attributes() {
        return BrainyZombieEntity.attributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANGER, 1.0f);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4f));
    }

    public float anger() {
        return this.entityData.get(ANGER);
    }

    public void setAnger(float anger) {
        this.entityData.set(ANGER, anger);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (ANGER.equals(accessor)) this.refreshDimensions();
    }

    /** O tamanho do zumbi de então, 0,6 por 1,8, vezes 1,2 mais a raiva. */
    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        float scale = 1.2f + this.anger();
        return EntityDimensions.scalable(0.6f * scale, 1.8f * scale).withEyeHeight(1.8f * scale * 0.85f);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.anger() > 1.0f) this.setAnger(this.anger() - 0.002f);
        var damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) damage.setBaseValue(7.0f + (this.anger() - 1.0f) * 5.0f);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        this.setAnger(Math.min(2.0f, this.anger() + 0.1f));
        return super.hurtServer(level, source, amount);
    }
}
