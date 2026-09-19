package net.thaumcraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.Month;

/**
 * O morcego de fogo: o {@code EntityFireBat} da 4.2.3.5. Um morcego do Nether em chamas, que dorme de ponta-cabeça
 * até alguém chegar perto, voa em zigue-zague para cima de quem vê a até doze blocos e, quando encosta, põe fogo
 * (metade das vezes), morde ou — uma vez em dez — explode. Não liga para fogo nem explosão, e a água o afoga.
 *
 * <p>O foco dos Nove Infernos o invoca contra um alvo: invocado, morde mais forte, não larga pólvora e, sem alvo,
 * se desfaz em poucos tiques. As variantes de bomba, de diabo e de vampiro vêm das melhorias do foco.
 */
public class FireBatEntity extends Monster {
    private static final EntityDataAccessor<Byte> FLAGS = SynchedEntityData.defineId(FireBatEntity.class, EntityDataSerializers.BYTE);
    private static final int HANGING = 0, SUMMONED = 1, EXPLOSIVE = 2, DEVIL = 3, VAMPIRE = 4;
    private static final TargetingConditions HUNT = TargetingConditions.forCombat().range(12.0);

    @Nullable
    private BlockPos currentFlightTarget;
    /** Quem invocou: ganha a regeneração do vampiro, e é quem conta como autor da mordida. */
    @Nullable
    public Player owner;
    public int damBonus;
    private int attackTime;

    public FireBatEntity(EntityType<? extends FireBatEntity> type, Level level) {
        super(type, level);
        this.setIsBatHanging(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 5.0).add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLAGS, (byte) 0);
    }

    private boolean flag(int bit) {
        return (this.entityData.get(FLAGS) & 1 << bit) != 0;
    }

    private void setFlag(int bit, boolean on) {
        byte flags = this.entityData.get(FLAGS);
        this.entityData.set(FLAGS, (byte) (on ? flags | 1 << bit : flags & ~(1 << bit)));
    }

    public boolean isBatHanging() {
        return this.flag(HANGING);
    }

    public void setIsBatHanging(boolean hanging) {
        this.setFlag(HANGING, hanging);
    }

    public boolean isSummoned() {
        return this.flag(SUMMONED);
    }

    public void setIsSummoned(boolean summoned) {
        this.setFlag(SUMMONED, summoned);
        this.updateDamage();
    }

    public boolean isExplosive() {
        return this.flag(EXPLOSIVE);
    }

    public void setIsExplosive(boolean explosive) {
        this.setFlag(EXPLOSIVE, explosive);
    }

    public boolean isDevil() {
        return this.flag(DEVIL);
    }

    public void setIsDevil(boolean devil) {
        this.setFlag(DEVIL, devil);
        if (devil) this.updateDamage();
    }

    public boolean isVampire() {
        return this.flag(VAMPIRE);
    }

    public void setIsVampire(boolean vampire) {
        this.setFlag(VAMPIRE, vampire);
    }

    /** Invocado morde com dois (três o diabo) mais a potência do foco; solto, com um. */
    private void updateDamage() {
        var damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) damage.setBaseValue(this.isSummoned() ? (this.isDevil() ? 3 : 2) + this.damBonus : 1.0);
    }

    @Override
    protected float getSoundVolume() {
        return 0.1f;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.95f;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isBatHanging() && this.random.nextInt(4) != 0 ? null : SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    /** A água (e a chuva) o afoga um ponto por tique. */
    @Override
    public void aiStep() {
        if (this.isInWaterOrRain() && this.level() instanceof ServerLevel server) {
            this.hurtServer(server, this.damageSources().drown(), 1.0f);
        }
        super.aiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() && this.isExplosive()) {
            net.thaumcraft.client.NodeClient.batBomb(this.xo + (this.random.nextFloat() - this.random.nextFloat()) * 0.1f,
                    this.yo + this.getBbHeight() / 2.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.1f,
                    this.zo + (this.random.nextFloat() - this.random.nextFloat()) * 0.1f, this.random);
        }
        if (this.isBatHanging()) {
            this.setDeltaMovement(0.0, 0.0, 0.0);
            this.setPosRaw(this.getX(), Mth.floor(this.getY()) + 1.0 - this.getBbHeight(), this.getZ());
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
        }
        if (this.level().isClientSide() && !this.isVampire()) {
            RandomSource r = this.level().getRandom();
            this.level().addParticle(ParticleTypes.SMOKE, this.xo + (r.nextFloat() - r.nextFloat()) * 0.2f,
                    this.yo + this.getBbHeight() / 2.0f + (r.nextFloat() - r.nextFloat()) * 0.2f,
                    this.zo + (r.nextFloat() - r.nextFloat()) * 0.2f, 0.0, 0.0, 0.0);
            this.level().addParticle(ParticleTypes.FLAME, this.xo + (r.nextFloat() - r.nextFloat()) * 0.2f,
                    this.yo + this.getBbHeight() / 2.0f + (r.nextFloat() - r.nextFloat()) * 0.2f,
                    this.zo + (r.nextFloat() - r.nextFloat()) * 0.2f, 0.0, 0.0, 0.0);
        }
    }

    /**
     * O {@code updateEntityActionState}: primeiro o do {@code EntityCreature} de então — achar alguém e, se o vê,
     * tentar a mordida —, depois o voo do morcego.
     */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.attackTime > 0) this.attackTime--;
        LivingEntity target = this.getTarget();
        if (target == null) {
            if (!this.isSummoned()) this.setTarget(level.getNearestPlayer(HUNT, this));
        } else if (target.isAlive()) {
            if (this.hasLineOfSight(target)) this.attackEntity(level, target, this.distanceTo(target));
        } else {
            this.setTarget(null);
        }

        BlockPos pos = this.blockPosition();
        BlockPos above = BlockPos.containing(this.getX(), (int) this.getY() + 1, this.getZ());
        if (this.isBatHanging()) {
            if (!level.getBlockState(above).isRedstoneConductor(level, above)) {
                this.setIsBatHanging(false);
                level.levelEvent(null, LevelEvent.SOUND_BAT_LIFTOFF, pos, 0);
            } else {
                if (this.random.nextInt(200) == 0) this.yHeadRot = this.random.nextInt(360);
                if (level.getNearestPlayer(this, 4.0) != null) {
                    this.setIsBatHanging(false);
                    level.levelEvent(null, LevelEvent.SOUND_BAT_LIFTOFF, pos, 0);
                }
            }
            return;
        }
        target = this.getTarget();
        double dx, dy, dz;
        if (target == null) {
            if (this.isSummoned()) this.hurtServer(level, this.damageSources().generic(), 2.0f);
            if (this.currentFlightTarget != null
                    && (!level.isEmptyBlock(this.currentFlightTarget) || this.currentFlightTarget.getY() < level.getMinY() + 1)) {
                this.currentFlightTarget = null;
            }
            if (this.currentFlightTarget == null || this.random.nextInt(30) == 0
                    || this.currentFlightTarget.distSqr(new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ())) < 4.0) {
                this.currentFlightTarget = new BlockPos((int) this.getX() + this.random.nextInt(7) - this.random.nextInt(7),
                        (int) this.getY() + this.random.nextInt(6) - 2,
                        (int) this.getZ() + this.random.nextInt(7) - this.random.nextInt(7));
            }
            dx = this.currentFlightTarget.getX() + 0.5 - this.getX();
            dy = this.currentFlightTarget.getY() + 0.1 - this.getY();
            dz = this.currentFlightTarget.getZ() + 0.5 - this.getZ();
        } else {
            dx = target.getX() - this.getX();
            dy = target.getY() + target.getEyeHeight() * 0.66f - this.getY();
            dz = target.getZ() - this.getZ();
        }
        var motion = this.getDeltaMovement();
        double mx = motion.x + (Math.signum(dx) * 0.5 - motion.x) * 0.1f;
        double my = motion.y + (Math.signum(dy) * 0.7f - motion.y) * 0.1f;
        double mz = motion.z + (Math.signum(dz) * 0.5 - motion.z) * 0.1f;
        this.setDeltaMovement(mx, my, mz);
        float yaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI) - 90.0f;
        this.zza = 0.5f;
        this.setYRot(this.getYRot() + Mth.wrapDegrees(yaw - this.getYRot()));
        if (target == null && this.random.nextInt(100) == 0 && level.getBlockState(above).isRedstoneConductor(level, above)) {
            this.setIsBatHanging(true);
        }
        if (this.getTarget() instanceof Player player && player.getAbilities().invulnerable) this.setTarget(null);
    }

    /** O {@code attackEntity}: de perto e na mesma altura, uma vez por segundo. */
    private void attackEntity(ServerLevel level, LivingEntity target, float distance) {
        if (this.attackTime > 0 || distance >= Math.max(2.5f, target.getBbWidth() * 1.1f)
                || target.getBoundingBox().maxY <= this.getBoundingBox().minY || target.getBoundingBox().minY >= this.getBoundingBox().maxY) {
            return;
        }
        // o original marca o alvo como ferido por jogador, para ele largar o que só larga assim
        if (this.isSummoned() && this.owner != null) target.setLastHurtByPlayer(this.owner, 100);
        if (this.isVampire()) {
            if (this.owner != null && !this.owner.hasEffect(MobEffects.REGENERATION)) {
                this.owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 26, 1));
            }
            this.heal(1.0f);
        }
        this.attackTime = 20;
        if ((this.isExplosive() || level.getRandom().nextInt(10) == 0) && !this.isDevil()) {
            target.invulnerableTime = 0;
            level.explode(this, this.getX(), this.getY(), this.getZ(), 1.5f + (this.isExplosive() ? this.damBonus * 0.33f : 0.0f),
                    false, Level.ExplosionInteraction.NONE);
            this.discard();
        } else if (!this.isVampire() && !level.getRandom().nextBoolean()) {
            target.igniteForSeconds(this.isSummoned() ? 4 : 2);
        } else {
            var motion = target.getDeltaMovement();
            this.doHurtTarget(level, target);
            target.setDeltaMovement(motion);
        }
        this.playSound(SoundEvents.BAT_HURT, 0.5f, 0.9f + level.getRandom().nextFloat() * 0.2f);
    }

    /** Fogo e explosão não o ferem; quem o fere o acorda e vira o alvo dele. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInvulnerableTo(level, source) || source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }
        if (this.isBatHanging()) this.setIsBatHanging(false);
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && source.getEntity() instanceof LivingEntity attacker && attacker != this) this.setTarget(attacker);
        return hurt;
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    /** Invocado não larga nada. */
    @Override
    protected boolean shouldDropLoot(ServerLevel level) {
        return !this.isSummoned() && super.shouldDropLoot(level);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("BatFlags", this.entityData.get(FLAGS));
        output.putByte("damBonus", (byte) this.damBonus);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(FLAGS, input.getByteOr("BatFlags", (byte) 0));
        this.damBonus = input.getByteOr("damBonus", (byte) 0);
    }

    /** O {@code getCanSpawnHere}: no escuro (a luz não passa de um sorteio até sete) e fora do pacífico. */
    public static boolean checkSpawn(EntityType<FireBatEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        if (level.getMaxLocalRawBrightness(pos) > random.nextInt(7)) return false;
        return checkMobSpawnRules(type, level, reason, pos, random);
    }

    /** No Dia das Bruxas o original solta morcegos de fogo em todo lugar. */
    public static boolean halloween() {
        LocalDate today = LocalDate.now();
        return today.getMonth() == Month.OCTOBER && today.getDayOfMonth() == 31;
    }
}
