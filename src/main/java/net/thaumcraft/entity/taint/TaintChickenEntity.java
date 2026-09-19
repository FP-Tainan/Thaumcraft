package net.thaumcraft.entity.taint;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A galinha maculada: o {@code EntityTaintChicken} da 4.2.3.5. 8 de vida, 3 de dano, 2 de armadura, rápida (0,4); pula
 * em quem ataca, bate asas no ar, cai devagar e não se machuca na queda. Caça jogadores, aldeões e bichos.
 */
public class TaintChickenEntity extends TaintedMonster {
    public float flap, flapSpeed, oFlap, oFlapSpeed;
    public float flapping = 1.0f;

    public TaintChickenEntity(EntityType<? extends TaintChickenEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return TaintedMonster.attributes(8.0, 3.0, 0.4).add(Attributes.ARMOR, 2.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.3f));
        this.goalSelector.addGoal(3, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Animal.class, false));
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    /** O {@code onLivingUpdate}: as asas e a queda lenta. */
    @Override
    public void aiStep() {
        super.aiStep();
        this.oFlap = this.flap;
        this.oFlapSpeed = this.flapSpeed;
        this.flapSpeed += (this.onGround() ? -1 : 4) * 0.3f;
        this.flapSpeed = Math.clamp(this.flapSpeed, 0.0f, 1.0f);
        if (!this.onGround() && this.flapping < 1.0f) this.flapping = 1.0f;
        this.flapping *= 0.9f;
        Vec3 m = this.getDeltaMovement();
        if (!this.onGround() && m.y < 0.0) this.setDeltaMovement(m.multiply(1.0, 0.9, 1.0));
        this.flap += this.flapping * 2.0f;
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return vanilla("entity.chicken.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return vanilla("entity.chicken.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return vanilla("entity.chicken.hurt");
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        return 5;
    }

    /** O {@code dropFewItems}. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (level.getRandom().nextInt(4) == 0) TaintDrops.goo(level, this);
        else TaintDrops.tendril(level, this);
    }
}
