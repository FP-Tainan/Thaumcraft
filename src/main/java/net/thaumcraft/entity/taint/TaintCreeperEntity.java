package net.thaumcraft.entity.taint;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.TaintedMob;
import net.thaumcraft.registry.TCEffects;

import java.util.EnumSet;

/**
 * O creeper maculado: o {@code EntityTaintCreeper} da 4.2.3.5. 30 de vida (0,25); o pavio é de 30 tiques e o estouro,
 * de força 1,5, dá o fluxo da mácula (5 s) a quem estiver a seis blocos e macula o chão em volta — dez sorteios a até
 * cinco blocos, metade deles pintando a coluna de Terra Maculada e pondo fibra no chão firme.
 */
public class TaintCreeperEntity extends TaintedMonster {
    private static final EntityDataAccessor<Integer> SWELL_DIR = SynchedEntityData.defineId(TaintCreeperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> POWERED = SynchedEntityData.defineId(TaintCreeperEntity.class, EntityDataSerializers.BOOLEAN);
    private int oldSwell;
    private int swell;
    private int maxSwell = 30;
    private int explosionRadius = 3;

    public TaintCreeperEntity(EntityType<? extends TaintCreeperEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return TaintedMonster.attributes(30.0, 2.0, 0.25);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SWELL_DIR, -1);
        builder.define(POWERED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SwellGoal(this));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Ocelot.class, 6.0f, 1.0, 1.2));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Cat.class, 6.0f, 1.0, 1.2));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    /** O {@code getMaxSafePointTries}: com alvo, cai de mais alto quanto mais vida tem. */
    @Override
    public int getMaxFallDistance() {
        return this.getTarget() == null ? 3 : 3 + (int) (this.getHealth() - 1.0f);
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        boolean hurt = super.causeFallDamage(distance, multiplier, source);
        this.swell = (int) (this.swell + distance * 1.5);
        if (this.swell > this.maxSwell - 5) this.swell = this.maxSwell - 5;
        return hurt;
    }

    public int getSwellDir() {
        return this.entityData.get(SWELL_DIR);
    }

    public void setSwellDir(int dir) {
        this.entityData.set(SWELL_DIR, dir);
    }

    /** O {@code getCreeperFlashIntensity}. */
    public float getSwelling(float partial) {
        return (this.oldSwell + (this.swell - this.oldSwell) * partial) / 28.0f;
    }

    @Override
    public void tick() {
        if (this.isAlive()) {
            this.oldSwell = this.swell;
            int dir = this.getSwellDir();
            if (dir > 0 && this.swell == 0) this.playSound(SoundEvents.CREEPER_PRIMED, 1.0f, 0.5f);
            this.swell += dir;
            if (this.swell < 0) this.swell = 0;
            if (this.swell >= 30) {
                this.swell = 30;
                if (this.level() instanceof ServerLevel level) this.explode(level);
                else for (int a = 0; a < 200; a++) TaintSplosion.effect.accept(this);
            }
        }
        super.tick();
    }

    private void explode(ServerLevel level) {
        level.explode(this, this.getX(), this.getY() + this.getBbHeight() / 2.0f, this.getZ(), 1.5f, Level.ExplosionInteraction.MOB);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0))) {
            if (!(e instanceof TaintedMob) && !e.isInvertedHealAndHarm()) e.addEffect(new MobEffectInstance(TCEffects.FLUX_TAINT, 100, 0));
        }
        TaintSplosion.taintAround(level, this, 10, 5.0f);
        this.discard();
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.entityData.get(POWERED)) output.putBoolean("powered", true);
        output.putShort("Fuse", (short) this.maxSwell);
        output.putByte("ExplosionRadius", (byte) this.explosionRadius);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(POWERED, input.getBooleanOr("powered", false));
        this.maxSwell = input.getShortOr("Fuse", (short) 30);
        this.explosionRadius = input.getByteOr("ExplosionRadius", (byte) 3);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CREEPER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CREEPER_DEATH;
    }

    /** O {@code AICreeperSwell}: a menos de três blocos do alvo acende; longe (sete) ou sem vê-lo, apaga. */
    static class SwellGoal extends Goal {
        private final TaintCreeperEntity creeper;
        private LivingEntity target;

        SwellGoal(TaintCreeperEntity creeper) {
            this.creeper = creeper;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.creeper.getTarget();
            return this.creeper.getSwellDir() > 0 || target != null && this.creeper.distanceToSqr(target) < 9.0;
        }

        @Override
        public void start() {
            this.creeper.getNavigation().stop();
            this.target = this.creeper.getTarget();
        }

        @Override
        public void stop() {
            this.target = null;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.target == null || this.creeper.distanceToSqr(this.target) > 49.0 || !this.creeper.getSensing().hasLineOfSight(this.target)) {
                this.creeper.setSwellDir(-1);
            } else {
                this.creeper.setSwellDir(1);
            }
        }
    }


    /** O {@code dropFewItems}. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        TaintDrops.either(level, this);
    }
}
