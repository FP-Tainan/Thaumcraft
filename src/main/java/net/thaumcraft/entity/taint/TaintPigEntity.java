package net.thaumcraft.entity.taint;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
 * O porco maculado: o {@code EntityTaintPig} da 4.2.3.5. 20 de vida, 4 de dano, 2 de armadura (0,275), foge da água,
 * não some com a distância; caça jogadores e aldeões (e bichos, por último).
 */
public class TaintPigEntity extends TaintedMonster {
    public TaintPigEntity(EntityType<? extends TaintPigEntity> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, -1.0f);
    }

    public static AttributeSupplier.Builder attributes() {
        return TaintedMonster.attributes(20.0, 4.0, 0.275).add(Attributes.ARMOR, 2.0);
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
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return vanilla("entity.pig.ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return vanilla("entity.pig.ambient");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return vanilla("entity.pig.death");
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(vanilla("entity.pig.step"), 0.15f, 1.0f);
    }

    /** O {@code dropFewItems}. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (level.getRandom().nextInt(3) == 0) TaintDrops.either(level, this);
    }
}
