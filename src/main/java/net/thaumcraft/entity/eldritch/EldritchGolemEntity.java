package net.thaumcraft.entity.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.EldritchMob;
import net.thaumcraft.block.eldritch.LootBlock;
import net.thaumcraft.entity.GolemOrbEntity;
import net.thaumcraft.event.Champions;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * O construto eldritch: o {@code EntityEldritchGolem} da 4.2.3.5, o chefe que desperta na sala do chefe. Duzentos e
 * cinquenta de vida, seis de armadura, imune ao fogo; esmaga urnas e caixotes por onde passa e derruba o que é mole na
 * frente. O primeiro golpe que o mataria arranca a cabeça numa explosão (e não passa): sem ela, o pescoço solta vapor e
 * faíscas, o golpe empurra longe e ele passa a atirar orbes que perseguem, carregando os tiros por sete segundos e meio.
 */
public class EldritchGolemEntity extends ThaumcraftBossEntity implements EldritchMob, RangedAttackMob {
    private static final EntityDataAccessor<Boolean> HEADLESS = SynchedEntityData.defineId(EldritchGolemEntity.class, EntityDataSerializers.BOOLEAN);

    private int beamCharge;
    private boolean chargingBeam;
    private int attackTimer;
    /** O arco do pescoço até o chão, do lado de quem vê. */
    public int arcing, ax, ay, az;

    public EldritchGolemEntity(EntityType<? extends EldritchGolemEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return ThaumcraftBossEntity.attributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MAX_HEALTH, 250.0)
                .add(Attributes.ARMOR, 6.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new CultistGoals.AttackOnCollide(this, 1.1, false));
        this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HEADLESS, false);
    }

    public boolean isHeadless() {
        return this.entityData.get(HEADLESS);
    }

    public void setHeadless(boolean headless) {
        this.entityData.set(HEADLESS, headless);
        this.refreshDimensions();
    }

    /** Sem cabeça, os olhos ficam no pescoço, mais alto que a cabeça baixa: 3,33 em vez de 3. */
    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).withEyeHeight(this.isHeadless() ? 3.33f : 3.0f);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (HEADLESS.equals(key)) this.refreshDimensions();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("headless", this.isHeadless());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setHeadless(input.getBooleanOr("headless", false));
        if (this.isHeadless()) this.makeHeadless();
    }

    @Override
    public void generateName() {
        Champions.Mod mod = Champions.mod(this);
        if (mod != null) this.setCustomName(Component.translatable("entity.thaumcraft.eldritch_golem.titled", mod.displayName()));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
                                        @Nullable SpawnGroupData data) {
        this.spawnTimer = 100;
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    public int getAttackTimer() {
        return this.attackTimer;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.attackTimer > 0) this.attackTimer--;
        Vec3 motion = this.getDeltaMovement();
        if (motion.x * motion.x + motion.z * motion.z > 2.5000003E-7 && this.random.nextInt(5) == 0) {
            BlockPos below = BlockPos.containing(this.getX(), this.getY() - 0.2, this.getZ());
            BlockState state = this.level().getBlockState(below);
            if (!state.isAir()) {
                this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state),
                        this.getX() + (this.random.nextFloat() - 0.5) * this.getBbWidth(), this.getBoundingBox().minY + 0.1,
                        this.getZ() + (this.random.nextFloat() - 0.5) * this.getBbWidth(), 4.0 * (this.random.nextFloat() - 0.5), 0.5,
                        (this.random.nextFloat() - 0.5) * 4.0);
            }
            if (!this.level().isClientSide() && state.getBlock() instanceof LootBlock) this.level().destroyBlock(below, true);
        }
        if (!this.level().isClientSide()) {
            BlockPos ahead = BlockPos.containing(this.getX() + motion.x, this.getBoundingBox().minY, this.getZ() + motion.z);
            float h = this.level().getBlockState(ahead).getDestroySpeed(this.level(), ahead);
            if (h >= 0.0f && h <= 0.15f && !this.level().getBlockState(ahead).isAir()) this.level().destroyBlock(ahead, true);
        }
    }

    /** O golpe que o mataria arranca a cabeça numa explosão, e não passa. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (damage > this.getHealth() && !this.isHeadless()) {
            this.setHeadless(true);
            this.spawnTimer = 100;
            double xx = Mth.cos(this.getYRot() % 360.0f / 180.0f * Mth.PI) * 0.75f;
            double zz = Mth.sin(this.getYRot() % 360.0f / 180.0f * Mth.PI) * 0.75f;
            level.explode(this, this.getX() + xx, this.getY() + this.getEyeHeight(), this.getZ() + zz, 2.0f, Level.ExplosionInteraction.NONE);
            this.makeHeadless();
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    private void makeHeadless() {
        this.goalSelector.addGoal(2, new CultistGoals.LongRangeAttack(this, 3.0, 1.0, 5, 5, 24.0f));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (this.attackTimer > 0) return false;
        this.attackTimer = 10;
        level.broadcastEntityEvent(this, (byte) 4);
        boolean flag = target.hurtServer(level, this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75f);
        if (flag) {
            target.setDeltaMovement(target.getDeltaMovement().add(0.0, 0.2, 0.0));
            if (this.isHeadless()) {
                target.push(-Mth.sin(this.getYRot() * Mth.PI / 180.0f) * 1.5f, 0.1, Mth.cos(this.getYRot() * Mth.PI / 180.0f) * 1.5f);
            }
        }
        return flag;
    }

    /** O tiro: o orbe que persegue, enquanto houver carga. */
    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!this.hasLineOfSight(target) || this.chargingBeam || this.beamCharge <= 0) return;
        this.beamCharge -= 15 + this.random.nextInt(5);
        this.getLookControl().setLookAt(target.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0f, target.getZ(), 30.0f, 30.0f);
        Vec3 v = this.getViewVector(1.0f);
        GolemOrbEntity blast = new GolemOrbEntity(this.level(), this, target, false);
        blast.setPos(blast.getX() + v.x, blast.getY(), blast.getZ() + v.z);
        double d0 = target.getX() + target.getDeltaMovement().x - this.getX();
        double d1 = target.getY() - this.getY() - target.getBbHeight() / 2.0f;
        double d2 = target.getZ() + target.getDeltaMovement().z - this.getZ();
        blast.shoot(d0, d1, d2, 0.66f, 5.0f);
        this.playSound(TCSounds.EG_ATTACK.value(), 1.0f, 1.0f + this.random.nextFloat() * 0.1f);
        this.level().addFreshEntity(blast);
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case 4 -> {
                this.attackTimer = 10;
                this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0f, 1.0f);
            }
            case 18 -> this.spawnTimer = 150;
            case 19 -> {
                if (this.arcing != 0) return;
                float radius = 2.0f + this.random.nextFloat() * 2.0f;
                double radians = Math.toRadians(this.random.nextInt(360));
                int bx = Mth.floor(this.getX() + radius * Math.cos(radians));
                int by = Mth.floor(this.getY());
                int bz = Mth.floor(this.getZ() + radius * Math.sin(radians));
                for (int c = 0; c < 5 && this.level().isEmptyBlock(new BlockPos(bx, by, bz)); by--) c++;
                if (this.level().isEmptyBlock(new BlockPos(bx, by + 1, bz)) && !this.level().isEmptyBlock(new BlockPos(bx, by, bz))) {
                    this.ax = bx;
                    this.ay = by;
                    this.az = bz;
                    this.arcing = 8 + this.random.nextInt(5);
                    this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), TCSounds.JACOBS.value(), this.getSoundSource(), 0.8f,
                            1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.05f, false);
                }
            }
            default -> super.handleEntityEvent(id);
        }
    }

    /** O vapor e as faíscas do pescoço, e o arco até o chão, do lado de quem vê. */
    public static GolemFx clientFx = golem -> {
    };

    public interface GolemFx {
        void neck(EldritchGolemEntity golem);
    }

    @Override
    public void tick() {
        if (this.getSpawnTimer() == 150 && !this.level().isClientSide()) this.level().broadcastEntityEvent(this, (byte) 18);
        if (this.getSpawnTimer() > 0) this.heal(2.0f);
        super.tick();
        if (this.level().isClientSide()) {
            if (this.isHeadless()) clientFx.neck(this);
        } else {
            if (this.isHeadless() && this.beamCharge <= 0) this.chargingBeam = true;
            if (this.isHeadless() && this.chargingBeam) {
                this.beamCharge++;
                this.level().broadcastEntityEvent(this, (byte) 19);
                if (this.beamCharge == 150) this.chargingBeam = false;
            }
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 1.0f, 1.0f);
    }
}
