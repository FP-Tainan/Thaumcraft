package net.thaumcraft.entity.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.TaintedMob;
import net.thaumcraft.block.TaintBlock;
import net.thaumcraft.block.TaintFibreBlock;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCDamageTypes;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.world.TCBiomes;

/**
 * O tentáculo da mácula: o {@code EntityTaintacle} da 4.2.3.5. Brota do chão maculado (três blocos de altura, 50 de
 * vida, 7 de dano) e não sai do lugar; aperta quem não é maculado e chega perto (até a altura dele), e, se o alvo está
 * longe mas no chão maculado, faz brotar um tentáculo pequeno aos pés dele. Fora da Terra Maculada, murcha.
 */
public class TaintacleEntity extends Monster implements TaintedMob {
    /** Do lado de quem vê: o quanto ele se debate. */
    public float flailIntensity = 1.0f;
    public int attackTime;

    public TaintacleEntity(EntityType<? extends TaintacleEntity> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        this.moveControl = new net.minecraft.world.entity.ai.control.MoveControl(this) {
            @Override
            public void tick() {
            }
        };
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 50.0).add(Attributes.ATTACK_DAMAGE, 7.0);
    }

    /**
     * O {@code getCanSpawnHere}: só onde não há outro a 24 blocos, em cima da película de fibra ou do solo maculado,
     * dentro da Terra Maculada, e com as regras de monstro.
     */
    public static boolean checkSpawn(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (!level.getEntitiesOfClass(TaintacleEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(24.0, 8.0, 24.0)).isEmpty()) return false;
        BlockState at = level.getBlockState(pos);
        boolean onTaint = (at.is(TCBlocks.TAINT_FIBRES) && at.getValue(TaintFibreBlock.KIND) == 0 || at.is(TCBlocks.TAINT_SOIL))
                && level.getBiome(pos).is(TCBiomes.TAINTED_LAND);
        return onTaint && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    /** O {@code moveEntity}: só desce. */
    @Override
    public void move(MoverType type, Vec3 movement) {
        super.move(type, new Vec3(0.0, Math.min(movement.y, 0.0), 0.0));
    }

    /** O {@code findPlayerToAttack}: o mais perto que não é maculado, a seis alturas de lado e três de altura. */
    protected LivingEntity findTarget() {
        float h = this.getBbHeight();
        LivingEntity found = null;
        double distance = Double.MAX_VALUE;
        for (LivingEntity e : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(h * 6.0f, h * 3.0f, h * 6.0f))) {
            if (e == this || e instanceof TaintedMob || e instanceof net.minecraft.world.entity.player.Player p && p.getAbilities().invulnerable) continue;
            double d = e.distanceToSqr(this);
            if (d < distance) {
                distance = d;
                found = e;
            }
        }
        return found;
    }

    public boolean agitated() {
        LivingEntity target = this.getTarget();
        float r = this.getBbHeight() * 7.0f;
        return target != null && target.distanceToSqr(this) < r * r;
    }

    /** O {@code tentacleAriseFX} (o cliente liga isto). */
    public static java.util.function.Consumer<Entity> ariseEffect = e -> {
    };

    @Override
    protected void customServerAiStep(ServerLevel level) {
        LivingEntity target = this.getTarget();
        if (target != null) {
            double dx = target.getX() - this.getX(), dz = target.getZ() - this.getZ();
            float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
            this.setYRot(this.getYRot() + Mth.clamp(Mth.wrapDegrees(yaw - this.getYRot()), -5.0f, 5.0f));
            this.yBodyRot = this.getYRot();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount % 20 == 0 && !this.level().getBiome(this.blockPosition()).is(TCBiomes.TAINTED_LAND)) {
            this.hurtServer((ServerLevel) this.level(), this.damageSources().starve(), 1.0f);
        }
        float h = this.getBbHeight();
        if (this.level().isClientSide()) {
            LivingEntity target = this.getTarget();
            if (this.tickCount > h * 10.0f && (this.hurtTime > 0 || this.attackTime > 0 || target != null && target.distanceTo(this) < h)) {
                if (this.flailIntensity < 3.0f) this.flailIntensity += 0.2f;
            } else if (this.flailIntensity > 1.0f) {
                this.flailIntensity -= 0.2f;
            }
            if (this.tickCount < h * 10.0f && this.onGround()) ariseEffect.accept(this);
        }
        if (this.attackTime > 0) this.attackTime--;
        if (this.level() instanceof ServerLevel level) {
            LivingEntity target = this.getTarget();
            if (target == null) {
                this.setTarget(this.findTarget());
            } else if (target.isAlive() && this.agitated()) {
                if (this.hasLineOfSight(target)) this.attack(level, target, target.distanceTo(this));
            } else {
                this.setTarget(null);
            }
        }
    }

    /** O {@code attackEntity}: aperta quem está ao alcance; quem está longe no chão ganha um tentáculo aos pés. */
    protected void attack(ServerLevel level, Entity entity, float distance) {
        if (this.attackTime > 0) return;
        if (distance <= this.getBbHeight() && entity.getBoundingBox().maxY > this.getBoundingBox().minY
                && entity.getBoundingBox().minY < this.getBoundingBox().maxY) {
            this.attackTime = 20;
            this.hit(level, entity);
            this.playSound(TCSounds.TENTACLE.value(), this.getSoundVolume(), this.getVoicePitch());
        } else if (distance > this.getBbHeight() && entity.onGround() && !(this instanceof TaintacleSmallEntity)) {
            this.spawnTentacles(level, entity);
        }
    }

    /** O {@code attackEntityAsMob}: o dano, mais força e menos fraqueza, com o dano de tentáculo. */
    protected boolean hit(ServerLevel level, Entity target) {
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        var strength = this.getEffect(MobEffects.STRENGTH);
        if (strength != null) damage += 3 << strength.getAmplifier();
        var weakness = this.getEffect(MobEffects.WEAKNESS);
        if (weakness != null) damage -= 2 << weakness.getAmplifier();
        return target.hurtServer(level, TCDamageTypes.tentacle(this), damage);
    }

    /** O {@code spawnTentacles}: um tentáculo pequeno aos pés de quem está no chão maculado da Terra Maculada. */
    protected void spawnTentacles(ServerLevel level, Entity entity) {
        BlockPos at = BlockPos.containing(entity.getX(), entity.getBoundingBox().minY, entity.getZ());
        if (!level.getBiome(at).is(TCBiomes.TAINTED_LAND)) return;
        if (!TaintBlock.isTaint(level.getBlockState(at)) && !TaintBlock.isTaint(level.getBlockState(at.below()))) return;
        this.attackTime = 40 + level.getRandom().nextInt(20);
        TaintacleSmallEntity small = new TaintacleSmallEntity(TCEntities.TAINTACLE_SMALL, level);
        small.snapTo(entity.getX() + level.getRandom().nextFloat() - level.getRandom().nextFloat(), entity.getY(),
                entity.getZ() + level.getRandom().nextFloat() - level.getRandom().nextFloat(), 0.0f, 0.0f);
        level.addFreshEntity(small);
        this.playSound(TCSounds.TENTACLE.value(), this.getSoundVolume(), this.getVoicePitch());
    }

    /** Ferido de longe (mais de dezesseis blocos), ele responde com um tentáculo aos pés de quem feriu. */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        Entity attacker = source.getEntity();
        if (!(this instanceof TaintacleSmallEntity) && attacker != null && this.distanceTo(attacker) > 16.0f) this.spawnTentacles(level, attacker);
        return super.hurtServer(level, source, damage);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.ROOTS.value();
    }

    @Override
    public float getVoicePitch() {
        return 1.3f - this.getBbHeight() / 10.0f;
    }

    @Override
    protected float getSoundVolume() {
        return this.getBbHeight() / 8.0f;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TCSounds.TENTACLE.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.TENTACLE.value();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return true;
    }

    /** O {@code dropFewItems}. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (!(this instanceof TaintacleSmallEntity)) TaintDrops.either(level, this);
    }
}
