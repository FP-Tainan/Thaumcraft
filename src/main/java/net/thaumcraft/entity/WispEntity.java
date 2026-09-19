package net.thaumcraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCSounds;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * O fogo-fátuo: o {@code EntityWisp} da 4.2.3.5. Uma bola de vis de um aspecto que vagueia no ar e, quando vê alguém
 * (ou quando apanha), dá choques de três de dano de longe. Nasce no Nether; nove em dez são de um primordial e o
 * resto de um composto. Morto, larga a essência etérea do aspecto dele.
 */
public class WispEntity extends Monster {
    private static final EntityDataAccessor<String> TYPE = SynchedEntityData.defineId(WispEntity.class, EntityDataSerializers.STRING);

    private int courseChangeCooldown;
    private double waypointX, waypointY, waypointZ;
    @Nullable
    private LivingEntity targeted;
    private int aggroCooldown;
    public int attackCounter;

    public WispEntity(EntityType<? extends WispEntity> type, Level level) {
        super(type, level);
        this.xpReward = 5;
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 22.0).add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, "");
    }

    @Nullable
    public Aspect aspect() {
        return Aspect.of(this.entityData.get(TYPE));
    }

    public void setAspect(Aspect aspect) {
        this.entityData.set(TYPE, aspect == null ? "" : aspect.tag());
    }

    /** Quem o fere vira alvo por duzentos tiques. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof LivingEntity living) {
            this.targeted = living;
            this.aggroCooldown = 200;
        }
        if (source.getEntity() instanceof LivingEntity living) {
            this.targeted = living;
            this.aggroCooldown = 200;
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) return;
        if (this.tickCount <= 1) net.thaumcraft.client.NodeClient.burst(this.level(), this.position().add(0.0, 0.45, 0.0), false);
        Aspect aspect = this.aspect();
        if (aspect != null && this.random.nextBoolean()) {
            net.thaumcraft.client.NodeClient.wisp(this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * 0.7f,
                    this.getY() + 0.45 + (this.random.nextFloat() - this.random.nextFloat()) * 0.7f,
                    this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * 0.7f, 0.1f, aspect.color());
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level().isClientSide()) net.thaumcraft.client.NodeClient.burst(this.level(), this.position().add(0.0, 0.45, 0.0), false);
    }

    /** O {@code updateEntityActionState}: o aspecto, o passeio e o choque. */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.aspect() == null) {
            // na Mata Assombrada, um dos aspectos das trevas
            if (level.getBiome(this.blockPosition()).is(net.thaumcraft.world.TCBiomes.EERIE)) {
                Aspect[] dark = {net.thaumcraft.api.aspects.Aspects.DARKNESS, net.thaumcraft.api.aspects.Aspects.UNDEAD,
                        net.thaumcraft.api.aspects.Aspects.ENTROPY, net.thaumcraft.api.aspects.Aspects.ELDRITCH,
                        net.thaumcraft.api.aspects.Aspects.POISON, net.thaumcraft.api.aspects.Aspects.DEATH};
                this.setAspect(dark[this.random.nextInt(6)]);
            } else if (this.random.nextInt(10) != 0) {
                List<Aspect> primals = Aspects.primals();
                this.setAspect(primals.get(this.random.nextInt(primals.size())));
            } else {
                List<Aspect> compounds = new ArrayList<>();
                for (Aspect aspect : Aspect.ASPECTS.values()) if (!aspect.isPrimal()) compounds.add(aspect);
                this.setAspect(compounds.get(this.random.nextInt(compounds.size())));
            }
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
            return;
        }
        double dx = this.waypointX - this.getX(), dy = this.waypointY - this.getY(), dz = this.waypointZ - this.getZ();
        double d3 = dx * dx + dy * dy + dz * dz;
        if (d3 < 1.0 || d3 > 3600.0) {
            this.waypointX = this.getX() + (this.random.nextFloat() * 2.0f - 1.0f) * 16.0f;
            this.waypointY = this.getY() + (this.random.nextFloat() * 2.0f - 1.0f) * 16.0f;
            this.waypointZ = this.getZ() + (this.random.nextFloat() * 2.0f - 1.0f) * 16.0f;
        }
        if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown += this.random.nextInt(5) + 2;
            d3 = Math.sqrt(d3);
            if (this.isCourseTraversable(d3)) {
                this.setDeltaMovement(this.getDeltaMovement().add(dx / d3 * 0.1, dy / d3 * 0.1, dz / d3 * 0.1));
            } else {
                this.waypointX = this.getX();
                this.waypointY = this.getY();
                this.waypointZ = this.getZ();
            }
        }
        if (this.targeted != null && this.targeted.isRemoved()) this.targeted = null;
        this.aggroCooldown--;
        if (this.random.nextInt(1000) == 0 && (this.targeted == null || this.aggroCooldown-- <= 0)) {
            this.targeted = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 16.0, net.minecraft.world.entity.EntitySelector.NO_CREATIVE_OR_SPECTATOR);
            if (this.targeted != null) this.aggroCooldown = 50;
        }
        double range = 16.0;
        if (this.targeted != null && this.targeted.distanceToSqr(this) < range * range) {
            double tx = this.targeted.getX() - this.getX();
            double tz = this.targeted.getZ() - this.getZ();
            float yaw = -((float) Math.atan2(tx, tz)) * 180.0f / (float) Math.PI;
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            if (this.hasLineOfSight(this.targeted)) {
                this.attackCounter++;
                if (this.attackCounter == 20) {
                    level.playSound(null, this, TCSounds.ZAP.value(), SoundSource.HOSTILE, 1.0f, 1.1f);
                    net.thaumcraft.net.TCNetwork.entityZap(level, this, this.targeted);
                    float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    Vec3 motion = this.targeted.getDeltaMovement();
                    DamageSource source = this.damageSources().mobAttack(this);
                    if (Math.abs(motion.x) <= 0.1 && Math.abs(motion.y) <= 0.1 && Math.abs(motion.z) <= 0.1) {
                        if (this.random.nextFloat() < 0.66f) this.targeted.hurtServer(level, source, damage + 1.0f);
                    } else if (this.random.nextFloat() < 0.4f) {
                        this.targeted.hurtServer(level, source, damage);
                    }
                    this.attackCounter = -20 + this.random.nextInt(20);
                }
            } else if (this.attackCounter > 0) {
                this.attackCounter--;
            }
        } else {
            Vec3 motion = this.getDeltaMovement();
            float yaw = -((float) Math.atan2(motion.x, motion.z)) * 180.0f / (float) Math.PI;
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            if (this.attackCounter > 0) this.attackCounter--;
        }
    }

    /** O caminho até o próximo ponto está livre, o ponto não está em líquido e há chão a até dez blocos abaixo. */
    private boolean isCourseTraversable(double d3) {
        double sx = (this.waypointX - this.getX()) / d3, sy = (this.waypointY - this.getY()) / d3, sz = (this.waypointZ - this.getZ()) / d3;
        AABB box = this.getBoundingBox();
        for (int i = 1; i < d3; i++) {
            box = box.move(sx, sy, sz);
            if (!this.level().noCollision(this, box)) return false;
        }
        BlockPos at = BlockPos.containing(this.waypointX, this.waypointY, this.waypointZ);
        if (!this.level().getFluidState(at).isEmpty()) return false;
        for (int a = 0; a < 11; a++) {
            if (!this.level().isEmptyBlock(at.below(a))) return true;
        }
        return false;
    }

    /** O {@code EntityFlying}: sem gravidade, perdendo nove centésimos da pressa por tique. */
    @Override
    public void travel(Vec3 input) {
        this.move(MoverType.SELF, this.getDeltaMovement());
        float friction = this.onGround() ? 0.546f : 0.91f;
        this.setDeltaMovement(this.getDeltaMovement().scale(friction));
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.WISP_LIVE.value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.FIRE_EXTINGUISH;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.WISP_DEAD.value();
    }

    @Override
    protected float getSoundVolume() {
        return 0.25f;
    }

    /** O {@code dropFewItems}: a essência etérea do aspecto dele. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        Aspect aspect = this.aspect();
        if (aspect == null) return;
        ItemStack essence = new ItemStack(TCItems.WISP_ESSENCE);
        essence.set(TCComponents.CRYSTAL_ASPECT, aspect.tag());
        this.spawnAtLocation(level, essence, 0.0f);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 2;
    }

    /** O {@code getCanSpawnHere}: menos de oito por perto, fora do pacífico, no escuro. */
    public static boolean checkSpawn(EntityType<WispEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        if (level.getEntitiesOfClass(WispEntity.class, new AABB(pos).inflate(16.0)).size() >= 8) return false;
        if (level.getBrightness(LightLayer.SKY, pos) > random.nextInt(32)) return false;
        if (level.getMaxLocalRawBrightness(pos) > random.nextInt(8)) return false;
        return checkMobSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Type", this.entityData.get(TYPE));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(TYPE, input.getStringOr("Type", ""));
    }

    @Override
    public boolean isPushable() {
        return true;
    }

}
