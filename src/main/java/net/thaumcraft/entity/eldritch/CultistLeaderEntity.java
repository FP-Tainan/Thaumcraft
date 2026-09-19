package net.thaumcraft.entity.eldritch;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.entity.GolemOrbEntity;
import net.thaumcraft.event.Champions;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

/**
 * O pretor carmesim: o {@code EntityCultistLeader} da 4.2.3.5, o chefe que sai do portal carmesim. Cento e vinte e cinco
 * de vida, a armadura de pretor e a lâmina carmesim (a do vazio no fácil); ataca de perto e, de longe, com o orbe
 * vermelho. Dá regeneração aos cultistas a até oito blocos. O nome é "Pretor fulano, o" mais o tipo de campeão.
 */
public class CultistLeaderEntity extends ThaumcraftBossEntity implements RangedAttackMob {
    private static final EntityDataAccessor<Byte> TITLE = SynchedEntityData.defineId(CultistLeaderEntity.class, EntityDataSerializers.BYTE);
    private static final String[] TITLES = {"Alberic", "Anselm", "Bastian", "Beturian", "Chabier", "Chorache", "Chuse", "Dodorol",
            "Ebardo", "Ferrando", "Fertus", "Guillen", "Larpe", "Obano", "Zelipe"};

    public CultistLeaderEntity(EntityType<? extends CultistLeaderEntity> type, Level level) {
        super(type, level);
        this.xpReward = 40;
    }

    public static AttributeSupplier.Builder attributes() {
        return ThaumcraftBossEntity.attributes()
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.MAX_HEALTH, 125.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new CultistGoals.LongRangeAttack(this, 16.0, 1.0, 30, 40, 24.0f));
        this.goalSelector.addGoal(3, new CultistGoals.AttackOnCollide(this, 1.1, false));
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
        builder.define(TITLE, (byte) 0);
    }

    private String title() {
        int t = this.entityData.get(TITLE);
        return TITLES[Math.floorMod(t, TITLES.length)];
    }

    @Override
    public void generateName() {
        Champions.Mod mod = Champions.mod(this);
        if (mod != null) this.setCustomName(Component.translatable("entity.thaumcraft.cultist_leader.titled", this.title(), mod.displayName()));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
                                        @Nullable SpawnGroupData data) {
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        this.populateDefaultEquipmentEnchantments(level, level.getRandom(), difficulty);
        this.entityData.set(TITLE, (byte) this.random.nextInt(TITLES.length));
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TCItems.CULTIST_LEADER_HELMET));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TCItems.CULTIST_LEADER_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TCItems.CULTIST_LEADER_LEGGINGS));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TCItems.CULTIST_BOOTS));
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(this.level().getDifficulty() == Difficulty.EASY
                ? TCItems.GEAR.get("void_sword") : TCItems.CRIMSON_SWORD));
    }

    @Override
    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor level, RandomSource random, DifficultyInstance difficulty) {
        float f = difficulty.getSpecialMultiplier();
        ItemStack held = this.getMainHandItem();
        if (!held.isEmpty() && random.nextFloat() < 0.5f * f) {
            this.setItemSlot(EquipmentSlot.MAINHAND, net.minecraft.world.item.enchantment.EnchantmentHelper.enchantItem(
                    random, held, (int) (7.0f + f * random.nextInt(22)), level.registryAccess(), java.util.Optional.empty()));
        }
    }

    @Override
    protected boolean considersEntityAsAlly(Entity other) {
        return other instanceof CultistEntity || other instanceof CultistLeaderEntity;
    }

    /** A regeneração dos cultistas por perto. */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        for (CultistEntity e : level.getEntitiesOfClass(CultistEntity.class, new AABB(this.blockPosition()).inflate(8.0),
                c -> c.distanceToSqr(this) <= 64.0)) {
            if (!e.hasEffect(MobEffects.REGENERATION)) e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1));
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!this.hasLineOfSight(target)) return;
        this.swing(InteractionHand.MAIN_HAND);
        this.getLookControl().setLookAt(target.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0f, target.getZ(), 30.0f, 30.0f);
        GolemOrbEntity blast = new GolemOrbEntity(this.level(), this, target, true);
        blast.setPos(blast.getX() + blast.getDeltaMovement().x / 2.0, blast.getY(), blast.getZ() + blast.getDeltaMovement().z / 2.0);
        double d0 = target.getX() - this.getX();
        double d1 = target.getBoundingBox().minY + target.getBbHeight() / 2.0f - (this.getY() + this.getBbHeight() / 2.0f);
        double d2 = target.getZ() - this.getZ();
        blast.shoot(d0, d1 + 2.0, d2, 0.66f, 3.0f);
        this.playSound(TCSounds.EG_ATTACK.value(), 1.0f, 1.0f + this.random.nextFloat() * 0.1f);
        this.level().addFreshEntity(blast);
    }

    /** O pretor deixa só a sacola rara (a pérola é dos chefes das Terras de Fora). */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        this.spawnAtLocation(level, new ItemStack(TCItems.LOOT_BAG_RARE), 1.5f);
        // a armadura e a arma caem com a chance de sempre do jogo
        this.dropEquipmentByChance(level, source, recentlyHit);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("title", this.entityData.get(TITLE));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(TITLE, input.getByteOr("title", (byte) 0));
    }
}
