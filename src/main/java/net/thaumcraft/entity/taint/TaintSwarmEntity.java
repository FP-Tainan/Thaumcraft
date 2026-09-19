package net.thaumcraft.entity.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.TaintedMob;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.world.TCBiomes;

import java.util.ArrayList;
import java.util.List;

/**
 * O enxame da mácula: o {@code EntityTaintSwarm} da 4.2.3.5. Uma nuvem de mosquinhas (só partículas) de dois blocos,
 * 30 de vida, que voa como o morcego: sem alvo, vagueia pela Terra Maculada; com um jogador a doze blocos, vai nele e
 * pica (2 de dano, fraqueza por 5 s) sem empurrar. Não sofre queda nem pisa em placas.
 */
public class TaintSwarmEntity extends Monster implements TaintedMob {
    private static final EntityDataAccessor<Byte> FLAGS = SynchedEntityData.defineId(TaintSwarmEntity.class, EntityDataSerializers.BYTE);
    private BlockPos flightTarget;
    public int damBonus;
    private int attackTime;
    public final List<Object> swarm = new ArrayList<>();

    public TaintSwarmEntity(EntityType<? extends TaintSwarmEntity> type, Level level) {
        super(type, level);
        this.moveControl = new net.minecraft.world.entity.ai.control.MoveControl(this) {
            @Override
            public void tick() {
            }
        };
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FOLLOW_RANGE, 12.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLAGS, (byte) 0);
    }

    public boolean isSummoned() {
        return (this.entityData.get(FLAGS) & 2) != 0;
    }

    public void setSummoned(boolean summoned) {
        byte b = this.entityData.get(FLAGS);
        this.entityData.set(FLAGS, (byte) (summoned ? b | 2 : b & -4));
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

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    /** As mosquinhas do {@code swarmParticleFX} (o cliente liga isto). */
    public static java.util.function.Function<Entity, Object> swarmEffect = e -> null;

    @Override
    public void tick() {
        super.tick();
        Vec3 m = this.getDeltaMovement();
        this.setDeltaMovement(m.x, m.y * 0.6f, m.z);
        if (this.level().isClientSide()) {
            for (int a = 0; a < this.swarm.size(); a++) {
                Object fx = this.swarm.get(a);
                if (fx == null || TaintSporeEntity.swarmDead.test(fx)) {
                    this.swarm.remove(a);
                    break;
                }
            }
            if (this.swarm.size() < 50) {
                Object fx = swarmEffect.apply(this);
                if (fx != null) this.swarm.add(fx);
            }
        }
    }

    /** O {@code updateEntityActionState}: vaguear pela mácula, ou ir no alvo. */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            target = this.isSummoned() ? null : this.findPlayer(level);
            this.setTarget(target);
        }
        if (target != null && target.isAlive() && this.hasLineOfSight(target)) {
            this.attack(level, target, this.distanceTo(target));
        }
        if (this.attackTime > 0) this.attackTime--;
        Vec3 m = this.getDeltaMovement();
        if (target == null) {
            if (this.isSummoned()) this.hurtServer(level, this.damageSources().generic(), 5.0f);
            if (this.flightTarget != null && (!level.isEmptyBlock(this.flightTarget) || this.flightTarget.getY() < level.getMinY() + 1
                    || this.flightTarget.getY() > level.getHeight(Heightmap.Types.MOTION_BLOCKING, this.flightTarget.getX(), this.flightTarget.getZ()) + 8
                    || !level.getBiome(this.flightTarget).is(TCBiomes.TAINTED_LAND))) {
                this.flightTarget = null;
            }
            if (this.flightTarget == null || this.random.nextInt(30) == 0
                    || this.flightTarget.distToCenterSqr((int) this.getX(), (int) this.getY(), (int) this.getZ()) < 4.0) {
                this.flightTarget = new BlockPos((int) this.getX() + this.random.nextInt(7) - this.random.nextInt(7),
                        (int) this.getY() + this.random.nextInt(6) - 2, (int) this.getZ() + this.random.nextInt(7) - this.random.nextInt(7));
            }
            double dx = this.flightTarget.getX() + 0.5 - this.getX();
            double dy = this.flightTarget.getY() + 0.1 - this.getY();
            double dz = this.flightTarget.getZ() + 0.5 - this.getZ();
            m = new Vec3(m.x + (Math.signum(dx) * 0.5 - m.x) * 0.015, m.y + (Math.signum(dy) * 0.7f - m.y) * 0.1f, m.z + (Math.signum(dz) * 0.5 - m.z) * 0.015);
        } else {
            double dx = target.getX() - this.getX();
            double dy = target.getY() + target.getEyeHeight() - this.getY();
            double dz = target.getZ() - this.getZ();
            m = new Vec3(m.x + (Math.signum(dx) * 0.5 - m.x) * 0.025, m.y + (Math.signum(dy) * 0.7f - m.y) * 0.1f, m.z + (Math.signum(dz) * 0.5 - m.z) * 0.025);
        }
        this.setDeltaMovement(m);
        float yaw = (float) (Math.atan2(m.z, m.x) * 180.0 / Math.PI) - 90.0f;
        this.setYRot(this.getYRot() + Mth.wrapDegrees(yaw - this.getYRot()));
        this.zza = 0.1f;
        if (target instanceof Player player && player.getAbilities().invulnerable) this.setTarget(null);
    }

    private Player findPlayer(ServerLevel level) {
        Player player = level.getNearestPlayer(this, 12.0);
        return player != null && !player.getAbilities().invulnerable ? player : null;
    }

    /** O {@code attackEntity}: a picada, de perto e na mesma altura. */
    private void attack(ServerLevel level, LivingEntity target, float distance) {
        if (this.attackTime > 0 || distance >= 3.0f || target.getBoundingBox().maxY <= this.getBoundingBox().minY
                || target.getBoundingBox().minY >= this.getBoundingBox().maxY) return;
        this.attackTime = 10 + this.random.nextInt(5);
        Vec3 before = target.getDeltaMovement();
        if (this.doHurtTarget(level, target)) target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
        target.hurtMarked = false;
        target.setDeltaMovement(before);
        this.playSound(TCSounds.SWARMATTACK.value(), 0.3f, 0.9f + this.random.nextFloat() * 0.2f);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("Flags", this.entityData.get(FLAGS));
        output.putByte("damBonus", (byte) this.damBonus);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(FLAGS, input.getByteOr("Flags", (byte) 0));
        this.damBonus = input.getByteOr("damBonus", (byte) 0);
    }

    @Override
    protected float getSoundVolume() {
        return 0.1f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TCSounds.SWARMATTACK.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.SWARMATTACK.value();
    }

    /** O {@code dropFewItems}. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (level.getRandom().nextBoolean()) TaintDrops.goo(level, this);
    }
}
