package net.thaumcraft.entity.eldritch;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.entity.GolemOrbEntity;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCSounds;

/**
 * O clérigo carmesim: o {@code EntityCultistCleric} da 4.2.3.5. De robe, ataca de longe — um terço das vezes um orbe
 * vermelho que persegue o alvo, e no resto três bolas de fogo pequenas. Os quatro que um altar chama ficam no ritual:
 * boiando, cantando e ligados ao altar por um fio vermelho, até alguém bater neles (ou num cultista vizinho) ou o altar
 * sumir.
 */
public class CultistClericEntity extends CultistEntity implements RangedAttackMob {
    private static final EntityDataAccessor<Byte> FLAGS = SynchedEntityData.defineId(CultistClericEntity.class, EntityDataSerializers.BYTE);
    /** A casa (o altar), que o original mandava junto no nascimento ({@code writeSpawnData}) para o fio e o olhar do ritual. */
    private static final EntityDataAccessor<java.util.Optional<net.minecraft.core.BlockPos>> ALTAR =
            SynchedEntityData.defineId(CultistClericEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    public CultistClericEntity(EntityType<? extends CultistClericEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return CultistEntity.attributes().add(Attributes.MAX_HEALTH, 30.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CultistGoals.AltarFocus(this));
        this.goalSelector.addGoal(2, new CultistGoals.LongRangeAttack(this, 2.0, 1.0, 20, 40, 24.0f));
        this.goalSelector.addGoal(3, new CultistGoals.AttackOnCollide(this, 1.0, false));
        this.goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new CultistGoals.HurtByTarget(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLAGS, (byte) 0);
        builder.define(ALTAR, java.util.Optional.empty());
    }

    public boolean isRitualist() {
        return (this.entityData.get(FLAGS) & 1) != 0;
    }

    public void setRitualist(boolean ritualist) {
        byte flags = this.entityData.get(FLAGS);
        this.entityData.set(FLAGS, ritualist ? (byte) (flags | 1) : (byte) (flags & -2));
    }

    @Override
    public void setHomeTo(net.minecraft.core.BlockPos pos, int radius) {
        super.setHomeTo(pos, radius);
        this.entityData.set(ALTAR, java.util.Optional.of(pos.immutable()));
    }

    /** O altar do ritual, também do lado de quem vê. */
    public java.util.Optional<net.minecraft.core.BlockPos> altar() {
        return this.entityData.get(ALTAR);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TCItems.CULTIST_ROBE_HELMET));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TCItems.CULTIST_ROBE_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TCItems.CULTIST_ROBE_LEGGINGS));
        if (random.nextFloat() < (this.level().getDifficulty() == Difficulty.HARD ? 0.3f : 0.1f)) {
            this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TCItems.CULTIST_BOOTS));
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        double d0 = target.getX() - this.getX();
        double d1 = target.getBoundingBox().minY + target.getBbHeight() / 2.0f - (this.getY() + this.getBbHeight() / 2.0f);
        double d2 = target.getZ() - this.getZ();
        this.swing(InteractionHand.MAIN_HAND);
        if (this.random.nextFloat() > 0.66f) {
            GolemOrbEntity blast = new GolemOrbEntity(this.level(), this, target, true);
            blast.setPos(blast.getX() + blast.getDeltaMovement().x / 2.0, blast.getY(), blast.getZ() + blast.getDeltaMovement().z / 2.0);
            blast.shoot(d0, d1 + 2.0, d2, 0.66f, 3.0f);
            this.playSound(TCSounds.EG_ATTACK.value(), 1.0f, 1.0f + this.random.nextFloat() * 0.1f);
            this.level().addFreshEntity(blast);
        } else {
            float f1 = Mth.sqrt(power) * 0.5f;
            // o som do fogo do blaze (o levelEvent 1009 de então)
            this.level().levelEvent(null, 1018, this.blockPosition(), 0);
            for (int i = 0; i < 3; i++) {
                Vec3 dir = new Vec3(d0 + this.random.nextGaussian() * f1, d1, d2 + this.random.nextGaussian() * f1);
                SmallFireball fireball = new SmallFireball(this.level(), this, dir.normalize());
                fireball.setPos(fireball.getX(), this.getY() + this.getBbHeight() / 2.0f + 0.5, fireball.getZ());
                this.level().addFreshEntity(fireball);
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !this.isRitualist();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInvulnerableTo(level, source)) return false;
        this.setRitualist(false);
        return super.hurtServer(level, source, amount);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("Flags", this.entityData.get(FLAGS));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(FLAGS, input.getByteOr("Flags", (byte) 0));
        if (this.hasHome()) this.entityData.set(ALTAR, java.util.Optional.of(this.getHomePosition()));
    }

    /** No ritual, o clérigo olha para o altar. */
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() && this.isRitualist() && this.altar().isPresent()) {
            var home = this.altar().get();
            double d0 = home.getX() + 0.5 - this.getX();
            double d1 = home.getY() + 1.5 - (this.getY() + this.getEyeHeight());
            double d2 = home.getZ() + 0.5 - this.getZ();
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            float f = (float) (Math.atan2(d2, d0) * 180.0 / Math.PI) - 90.0f;
            float f1 = (float) -(Math.atan2(d1, d3) * 180.0 / Math.PI);
            this.setXRot(CultistGoals.updateRotation(this.getXRot(), f1, 10.0f));
            this.yHeadRot = CultistGoals.updateRotation(this.yHeadRot, f, this.getMaxHeadYRot());
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.CHANT.value();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 500;
    }
}
