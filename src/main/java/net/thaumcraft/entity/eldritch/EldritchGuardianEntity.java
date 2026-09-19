package net.thaumcraft.entity.eldritch;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.thaumcraft.api.EldritchMob;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.WispEssenceItem;
import net.thaumcraft.net.TCNetwork;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.research.Warp;
import net.thaumcraft.world.OuterLands;
import org.jetbrains.annotations.Nullable;

/**
 * O guardião eldritch: o {@code EntityEldritchGuardian} da 4.2.3.5. Cinquenta de vida, quatro de armadura, metade do dano
 * de explosão; atira orbes eldritch alternando os braços e, uma vez em dez, grita — murchando o alvo e dando distorção a
 * um jogador. Fora das Terras de Fora ele é meio transparente e traz a névoa a quem estiver perto (fora do fácil); lá
 * dentro, nasce com escudo e o refaz. Morto, às vezes solta essências de morto-vivo e de eldritch, e raramente um olho
 * eldritch.
 */
public class EldritchGuardianEntity extends Monster implements RangedAttackMob, EldritchMob {
    /** O quanto cada braço está erguido, do lado de quem vê (os eventos 15, 16 e 17). */
    public float armLiftL, armLiftR;
    private boolean lastBlast;

    public EldritchGuardianEntity(EntityType<? extends EldritchGuardianEntity> type, Level level) {
        super(type, level);
        this.xpReward = 20;
        if (this.getNavigation() instanceof net.minecraft.world.entity.ai.navigation.GroundPathNavigation ground) ground.setCanOpenDoors(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ARMOR, 4.0)
                // o escudo das Terras de Fora, de metade da vida
                .add(Attributes.MAX_ABSORPTION, 25.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new CultistGoals.LongRangeAttack(this, 8.0, 1.0, 20, 40, 24.0f));
        this.goalSelector.addGoal(3, new CultistGoals.AttackOnCollide(this, 1.0, false));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, CultistEntity.class, true));
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) damage /= 2.0f;
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (this.armLiftL > 0.0f) this.armLiftL -= 0.05f;
            if (this.armLiftR > 0.0f) this.armLiftR -= 0.05f;
            float x = (float) (this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
            float z = (float) (this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
            GuardianFx.client.wisp(this, x, this.getY() + 0.22 * this.getBbHeight(), z);
        } else if (!OuterLands.is(this.level()) && (this.tickCount == 0 || this.tickCount % 100 == 0)
                && this.level().getDifficulty() != Difficulty.EASY) {
            // a névoa, para quem estiver perto
            double d6 = this.level().getDifficulty() == Difficulty.HARD ? 576.0 : 256.0;
            for (Player p : this.level().players()) {
                if (p.isAlive() && p.distanceToSqr(this) < d6 && p instanceof ServerPlayer sp) TCNetwork.miscEvent(sp, 2);
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean flag = super.doHurtTarget(level, target);
        if (flag) {
            int i = level.getDifficulty().getId();
            if (this.getMainHandItem().isEmpty() && this.isOnFire() && this.random.nextFloat() < i * 0.3f) target.igniteForSeconds(2 * i);
        }
        return flag;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.EG_IDLE.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.EG_DEATH.value();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 500;
    }

    @Override
    protected float getSoundVolume() {
        return 1.5f;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        if (this.random.nextBoolean()) this.spawnAtLocation(level, WispEssenceItem.of(Aspects.UNDEAD), 1.0f);
        if (this.random.nextBoolean()) this.spawnAtLocation(level, WispEssenceItem.of(Aspects.ELDRITCH), 1.0f);
        super.dropCustomDeathLoot(level, source, recentlyHit);
        int looting = 0;
        if (source.getEntity() instanceof LivingEntity killer) {
            var enchantments = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
            looting = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                    enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING), killer.getMainHandItem());
        }
        // o dropRareDrop: o olho eldritch
        if (recentlyHit && this.random.nextInt(200) - looting < 5) this.spawnAtLocation(level, new ItemStack(TCItems.ELDRITCH_EYE));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
                                        @Nullable SpawnGroupData data) {
        SpawnGroupData out = super.finalizeSpawn(level, difficulty, reason, data);
        if (OuterLands.is(level.getLevel())) {
            int bh = (int) this.getAttributeBaseValue(Attributes.MAX_HEALTH) / 2;
            this.setAbsorptionAmount(this.getAbsorptionAmount() + bh);
        }
        return out;
    }

    /** Nas Terras de Fora, o escudo se refaz um ponto a cada vinte e cinco tiques sem apanhar. */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (OuterLands.is(level) && this.hurtTime <= 0 && this.tickCount % 25 == 0) {
            int bh = (int) this.getAttributeBaseValue(Attributes.MAX_HEALTH) / 2;
            if (this.getAbsorptionAmount() < bh) this.setAbsorptionAmount(this.getAbsorptionAmount() + 1.0f);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case 15 -> this.armLiftL = 0.5f;
            case 16 -> this.armLiftR = 0.5f;
            case 17 -> {
                this.armLiftL = 0.9f;
                this.armLiftR = 0.9f;
            }
            default -> super.handleEntityEvent(id);
        }
    }

    /** Some quando longe, a menos que tenha casa ({@code canDespawn}). */
    @Override
    public boolean removeWhenFarAway(double distance) {
        return !this.hasHome();
    }

    @Override
    protected boolean considersEntityAsAlly(Entity other) {
        return other instanceof EldritchMob || super.considersEntityAsAlly(other);
    }

    /** O orbe eldritch, de um braço e do outro; ou, uma vez em dez, o grito. */
    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (this.random.nextFloat() > 0.1f) {
            EldritchOrbEntity blast = new EldritchOrbEntity(this.level(), this);
            this.lastBlast = !this.lastBlast;
            this.level().broadcastEntityEvent(this, (byte) (this.lastBlast ? 16 : 15));
            int rr = this.lastBlast ? 90 : 180;
            double xx = Mth.cos((this.getYRot() + rr) % 360.0f / 180.0f * (float) Math.PI) * 0.5f;
            double yy = 0.057777777 * this.getBbHeight();
            double zz = Mth.sin((this.getYRot() + rr) % 360.0f / 180.0f * (float) Math.PI) * 0.5f;
            blast.setPos(blast.getX() - xx, blast.getY() - yy, blast.getZ() - zz);
            double d0 = target.getX() + target.getDeltaMovement().x - this.getX();
            double d1 = target.getY() - this.getY() - target.getBbHeight() / 2.0f;
            double d2 = target.getZ() + target.getDeltaMovement().z - this.getZ();
            blast.shoot(d0, d1, d2, 1.0f, 2.0f);
            this.playSound(TCSounds.EG_ATTACK.value(), 2.0f, 1.0f + this.random.nextFloat() * 0.1f);
            this.level().addFreshEntity(blast);
        } else if (this.hasLineOfSight(target)) {
            if (this.level() instanceof ServerLevel server) TCNetwork.sonic(server, this);
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 0));
            if (target instanceof Player player) Warp.add(player, 1 + this.random.nextInt(3), true);
            this.playSound(TCSounds.EG_SCREECH.value(), 3.0f, 1.0f + this.random.nextFloat() * 0.1f);
        }
    }

    /** O {@code getCanSpawnHere}: não nasce com outro guardião a até trinta e dois blocos, e nasce em qualquer luz ({@code isValidLightLevel}). */
    public static boolean canSpawn(EntityType<EldritchGuardianEntity> type, LevelAccessor level, EntitySpawnReason reason,
                                   net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
        if (!level.getEntitiesOfClass(EldritchGuardianEntity.class, new AABB(pos).inflate(32.0, 16.0, 32.0)).isEmpty()) return false;
        return Monster.checkAnyLightMonsterSpawnRules(type, level, reason, pos, random);
    }

    /** O {@code spawnGuardian} dos eventos de distorção: na névoa, a sete a vinte e quatro blocos, já de olho em quem a viu. */
    public static void spawnForWarp(net.minecraft.world.entity.player.Player player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        EldritchGuardianEntity guardian = net.thaumcraft.registry.TCEntities.ELDRITCH_GUARDIAN.create(level, EntitySpawnReason.EVENT);
        if (guardian == null) return;
        var random = level.getRandom();
        int i = net.minecraft.util.Mth.floor(player.getX()), j = net.minecraft.util.Mth.floor(player.getY()), k = net.minecraft.util.Mth.floor(player.getZ());
        for (int l = 0; l < 50; l++) {
            int i1 = i + net.minecraft.util.Mth.nextInt(random, 7, 24) * net.minecraft.util.Mth.nextInt(random, -1, 1);
            int j1 = j + net.minecraft.util.Mth.nextInt(random, 7, 24) * net.minecraft.util.Mth.nextInt(random, -1, 1);
            int k1 = k + net.minecraft.util.Mth.nextInt(random, 7, 24) * net.minecraft.util.Mth.nextInt(random, -1, 1);
            net.minecraft.core.BlockPos floor = new net.minecraft.core.BlockPos(i1, j1 - 1, k1);
            if (!level.getBlockState(floor).isFaceSturdy(level, floor, net.minecraft.core.Direction.UP)) continue;
            guardian.setPos(i1, j1, k1);
            if (level.isUnobstructed(guardian) && level.noCollision(guardian) && !level.containsAnyLiquid(guardian.getBoundingBox())) {
                guardian.setTarget(player);
                level.addFreshEntity(guardian);
                break;
            }
        }
    }

    /** A fumaça escura que desce da cintura; o desenho mora no cliente. */
    public static final class GuardianFx {
        public static Client client = (e, x, y, z) -> {
        };

        public interface Client {
            void wisp(EldritchGuardianEntity guardian, double x, double y, double z);
        }

        private GuardianFx() {
        }
    }
}
