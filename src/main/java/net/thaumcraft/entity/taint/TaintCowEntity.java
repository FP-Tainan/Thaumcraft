package net.thaumcraft.entity.taint;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * A vaca maculada: o {@code EntityTaintCow} da 4.2.3.5. 40 de vida, 6 de dano, lenta (0,27), foge da água; caça
 * jogadores e aldeões (e bichos, por último). Muge baixo (volume 0,4).
 */
public class TaintCowEntity extends TaintedMonster {
    public TaintCowEntity(EntityType<? extends TaintCowEntity> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, -1.0f);
    }

    public static AttributeSupplier.Builder attributes() {
        return TaintedMonster.attributes(40.0, 6.0, 0.27);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(8, new NearestAttackableTargetGoal<>(this, Animal.class, false));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return vanilla("entity.cow.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return vanilla("entity.cow.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return vanilla("entity.cow.hurt");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(vanilla("entity.cow.step"), 0.15f, 1.0f);
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    /** O {@code dropFewItems}. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        TaintDrops.either(level, this);
    }
}
