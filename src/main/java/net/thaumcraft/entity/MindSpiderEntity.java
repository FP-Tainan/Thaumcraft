package net.thaumcraft.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A aranha da mente: o {@code EntityMindSpider} da 4.2.3.5, o susto da distorção. Uma aranha miúda (0,3), quase
 * transparente, que caça o jogador a doze blocos. As falsas só quem as chamou vê ({@code viewer}), não mordem, não dão
 * experiência e somem em um minuto; as de verdade mordem (um de dano, um de vida).
 */
public class MindSpiderEntity extends Spider {
    private static final EntityDataAccessor<Boolean> HARMLESS = SynchedEntityData.defineId(MindSpiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> VIEWER = SynchedEntityData.defineId(MindSpiderEntity.class, EntityDataSerializers.STRING);
    private int lifeSpan = Integer.MAX_VALUE;

    public MindSpiderEntity(EntityType<? extends MindSpiderEntity> type, Level level) {
        super(type, level);
        this.xpReward = 1;
    }

    public static AttributeSupplier.Builder attributes() {
        return Spider.createAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.ATTACK_DAMAGE, 1.0).add(Attributes.FOLLOW_RANGE, 12.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HARMLESS, false);
        builder.define(VIEWER, "");
    }

    /** A aranha de antes caçava o jogador mais perto a doze blocos, de dia ou de noite; a falsa só finge. */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public String getViewer() {
        return this.entityData.get(VIEWER);
    }

    public void setViewer(String player) {
        this.entityData.set(VIEWER, player);
    }

    public boolean isHarmless() {
        return this.entityData.get(HARMLESS);
    }

    public void setHarmless(boolean harmless) {
        if (harmless) this.lifeSpan = 1200;
        this.entityData.set(HARMLESS, harmless);
    }

    /** A falsa encosta, mas não morde. */
    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        return !this.isHarmless() && super.doHurtTarget(level, target);
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        return this.isHarmless() ? 0 : super.getBaseExperienceReward(level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount > this.lifeSpan) this.discard();
    }

    @Override
    public float getVoicePitch() {
        return 0.7f;
    }

    /** Não pisa em placa de pressão nem faz barulho ao andar. */
    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, boolean killedByPlayer) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("harmless", this.isHarmless());
        output.putString("viewer", this.getViewer());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(HARMLESS, input.getBooleanOr("harmless", false));
        this.setViewer(input.getStringOr("viewer", ""));
    }
}
