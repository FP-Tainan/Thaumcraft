package net.thaumcraft.entity.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * O caranguejo eldritch: o {@code EntityEldritchCrab} da 4.2.3.5. Vinte de vida, pula no alvo e, sem elmo, sobe na
 * cabeça de quem está embaixo e fica mordendo — de vez em quando cai. Os que nascem com o elmo (os de dentro do zumbi
 * habitado, um terço dos outros, e todos no difícil) têm cinco de armadura e andam mais devagar, até o elmo quebrar na
 * metade da vida. Imune a veneno; morto por alguém, às vezes deixa uma pérola do fim.
 */
public class EldritchCrabEntity extends Monster {
    private static final EntityDataAccessor<Byte> FLAGS = SynchedEntityData.defineId(EldritchCrabEntity.class, EntityDataSerializers.BYTE);
    /** O {@code attackTime} de então: a espera entre uma mordida e outra, montado na cabeça de alguém. */
    private int biteCooldown;

    public EldritchCrabEntity(EntityType<? extends EldritchCrabEntity> type, Level level) {
        super(type, level);
        this.xpReward = 6;
        if (this.getNavigation() instanceof net.minecraft.world.entity.ai.navigation.GroundPathNavigation ground) ground.setCanOpenDoors(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ARMOR, 0.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.63f));
        this.goalSelector.addGoal(3, new CultistGoals.AttackOnCollide(this, 1.0, false));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, CultistEntity.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLAGS, (byte) 0);
    }

    public boolean hasHelm() {
        return (this.entityData.get(FLAGS) & 1) != 0;
    }

    /** Com elmo, cinco de armadura e um pouco mais lento. */
    public void setHelm(boolean helm) {
        byte flags = this.entityData.get(FLAGS);
        this.entityData.set(FLAGS, helm ? (byte) (flags | 1) : (byte) (flags & -2));
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(helm ? 0.275 : 0.3);
        var armor = this.getAttribute(Attributes.ARMOR);
        if (armor != null) armor.setBaseValue(helm ? 5.0 : 0.0);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
                                        @Nullable SpawnGroupData data) {
        this.setHelm(level.getDifficulty() == Difficulty.HARD || this.random.nextFloat() < 0.33f);
        // como a aranha de então: no difícil, às vezes um efeito que dura para sempre
        if (data == null) {
            var group = new net.minecraft.world.entity.monster.spider.Spider.SpiderEffectsGroupData();
            if (level.getDifficulty() == Difficulty.HARD && level.getRandom().nextFloat() < 0.1f * difficulty.getSpecialMultiplier()) {
                group.setRandomEffect(level.getRandom());
            }
            data = group;
        }
        if (data instanceof net.minecraft.world.entity.monster.spider.Spider.SpiderEffectsGroupData group && group.effect != null) {
            this.addEffect(new MobEffectInstance(group.effect, -1));
        }
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    /** Sentado na cabeça de alguém, meio bloco acima. */
    @Override
    public net.minecraft.world.phys.Vec3 getVehicleAttachmentPoint(Entity vehicle) {
        return super.getVehicleAttachmentPoint(vehicle).add(0.0, -0.5, 0.0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.biteCooldown > 0) this.biteCooldown--;
        if (this.tickCount < 20) this.fallDistance = 0.0;
        LivingEntity attacker = this.getLastHurtByMob();
        if (this.getVehicle() == null && attacker != null && attacker.getPassengers().isEmpty() && !this.onGround() && !this.hasHelm()
                && attacker.isAlive() && this.getY() - attacker.getY() >= attacker.getBbHeight() / 2.0f && this.distanceToSqr(attacker) < 4.0) {
            this.startRiding(attacker, true, true);
        }
        if (this.level() instanceof ServerLevel server && this.getVehicle() != null && this.biteCooldown <= 0) {
            this.biteCooldown = 10 + this.random.nextInt(10);
            this.doHurtTarget(server, this.getVehicle());
            if (this.getVehicle() != null && this.random.nextFloat() < 0.2f) this.stopRiding();
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (super.doHurtTarget(level, target)) {
            this.playSound(TCSounds.CRAB_CLAW.value(), 1.0f, 0.9f + this.random.nextFloat() * 0.2f);
            return true;
        }
        return false;
    }

    /** Batido até a metade da vida, o elmo quebra. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage);
        if (this.hasHelm() && this.getHealth() / this.getMaxHealth() <= 0.5f) {
            this.setHelm(false);
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, TCItems.CULTIST_PLATE_CHESTPLATE),
                    this.getX(), this.getY() + this.getBbHeight() / 2.0, this.getZ(), 5, 0.1, 0.1, 0.1, 0.05);
            this.playSound(SoundEvents.ITEM_BREAK.value(), 0.8f, 0.8f + this.random.nextFloat() * 0.4f);
        }
        return hurt;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int looting = 0;
        if (source.getEntity() instanceof LivingEntity killer) {
            var enchantments = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
            looting = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                    enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING), killer.getMainHandItem());
        }
        if (recentlyHit && (this.random.nextInt(3) == 0 || this.random.nextInt(1 + looting) > 0)) {
            this.spawnAtLocation(level, new ItemStack(Items.ENDER_PEARL));
        }
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.is(MobEffects.POISON) && super.canBeAffected(effect);
    }

    @Override
    protected boolean considersEntityAsAlly(Entity other) {
        return other instanceof EldritchCrabEntity || super.considersEntityAsAlly(other);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.CRAB_TALK.value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.HOSTILE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.CRAB_DEATH.value();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15f, 1.0f);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("Flags", this.entityData.get(FLAGS));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setHelm((input.getByteOr("Flags", (byte) 0) & 1) != 0);
    }
}
