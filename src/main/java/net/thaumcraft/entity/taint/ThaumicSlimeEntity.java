package net.thaumcraft.entity.taint;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.TaintedMob;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCSounds;

import java.util.List;

/**
 * O slime taumático: o {@code EntityThaumicSlime} da 4.2.3.5. Nasce da gosma de fluxo e de quem morre com o fluxo da
 * mácula; cresce comendo gosma (até cem), e a vida é o tamanho. Pula atrás do jogador a dezesseis blocos (três vezes
 * mais depressa) e, grande (mais de três), cospe de longe um slime pequeno e encolhe um. Sem jogador por perto, vai
 * atrás de outro slime e os dois se juntam. Morto, se divide na raiz do tamanho.
 */
public class ThaumicSlimeEntity extends Monster implements TaintedMob {
    private static final EntityDataAccessor<Integer> SIZE = SynchedEntityData.defineId(ThaumicSlimeEntity.class, EntityDataSerializers.INT);
    public float targetSquish, squish, oSquish;
    private int jumpDelay;
    public int launched = 10;
    private int spitCounter = 100;

    public ThaumicSlimeEntity(EntityType<? extends ThaumicSlimeEntity> type, Level level) {
        super(type, level);
        this.jumpDelay = this.random.nextInt(20) + 10;
        // quem move o slime é o customServerAiStep, como no updateEntityActionState: os controles do jogo ficam quietos
        this.moveControl = new net.minecraft.world.entity.ai.control.MoveControl(this) {
            @Override
            public void tick() {
            }
        };
        this.jumpControl = new net.minecraft.world.entity.ai.control.JumpControl(this) {
            @Override
            public void tick() {
            }
        };
        this.setSize(1 << this.random.nextInt(3));
    }

    /** O slime cuspido: pequeno, lançado de {@code from} na direção de {@code at}. */
    public static ThaumicSlimeEntity spit(Level level, LivingEntity from, LivingEntity at) {
        ThaumicSlimeEntity slime = new ThaumicSlimeEntity(TCEntities.THAUMIC_SLIME, level);
        slime.setSize(1);
        double y = (from.getBoundingBox().minY + from.getBoundingBox().maxY) / 2.0;
        double dx = at.getX() - from.getX();
        double dy = at.getBoundingBox().minY + at.getBbHeight() / 3.0f - y;
        double dz = at.getZ() - from.getZ();
        double d = Math.sqrt(dx * dx + dz * dz);
        if (d >= 1.0E-7) {
            float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
            float pitch = (float) (-(Math.atan2(dy, d) * 180.0 / Math.PI));
            slime.snapTo(from.getX() + dx / d, y, from.getZ() + dz / d, yaw, pitch);
            slime.heading(dx, dy + d * 0.2f, dz, 1.5f, 1.0f);
        }
        return slime;
    }

    private void heading(double x, double y, double z, float speed, float spread) {
        double f = Math.sqrt(x * x + y * y + z * z);
        x = x / f + this.random.nextGaussian() * 0.0075 * spread;
        y = y / f + this.random.nextGaussian() * 0.0075 * spread;
        z = z / f + this.random.nextGaussian() * 0.0075 * spread;
        this.setDeltaMovement(x * speed, y * speed, z * speed);
        float h = (float) Math.sqrt(x * x + z * z);
        this.setYRot((float) (Math.atan2(x, z) * 180.0 / Math.PI));
        this.setXRot((float) (Math.atan2(y, h) * 180.0 / Math.PI));
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.MOVEMENT_SPEED, 0.1);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SIZE, 1);
    }

    public int getSize() {
        return this.entityData.get(SIZE);
    }

    /** O {@code setSlimeSize}: a caixa cresce com a raiz, e a vida vira o tamanho, cheia. */
    public void setSize(int size) {
        this.entityData.set(SIZE, size);
        this.reapplyPosition();
        this.refreshDimensions();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(size);
        this.setHealth(this.getMaxHealth());
        this.xpReward = (int) Math.sqrt(size);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        if (SIZE.equals(accessor)) this.refreshDimensions();
        super.onSyncedDataUpdated(accessor);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        float s = (float) Math.sqrt(this.getSize()) * 0.25f + 0.25f;
        return EntityDimensions.scalable(s, s);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Size", this.getSize() - 1);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSize(input.getIntOr("Size", 0) + 1);
    }

    private SoundEvent jumpSound() {
        return this.getSize() > 3 ? SoundEvents.SLIME_JUMP : SoundEvents.SLIME_JUMP_SMALL;
    }

    /** Os respingos de quem pula e cai (o {@code slimeJumpFX}; o cliente liga isto). */
    public static java.util.function.BiConsumer<Entity, Integer> jumpEffect = (e, i) -> {
    };

    @Override
    public void tick() {
        if (!this.level().isClientSide() && this.level().getDifficulty() == Difficulty.PEACEFUL && this.getSize() > 0) {
            this.discard();
            return;
        }
        this.squish += (this.targetSquish - this.squish) * 0.5f;
        this.oSquish = this.squish;
        boolean wasOnGround = this.onGround();
        super.tick();
        int i = (int) Math.sqrt(this.getSize());
        if (this.launched > 0) {
            this.launched--;
            if (this.level().isClientSide()) for (int j = 0; j < i * (this.launched + 1); j++) jumpEffect.accept(this, i);
        }
        if (this.onGround() && !wasOnGround) {
            if (this.level().isClientSide()) for (int j = 0; j < i * 8; j++) jumpEffect.accept(this, i);
            if (this.getSize() > 5) {
                this.playSound(this.jumpSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) / 0.8f);
            }
            this.targetSquish = -0.5f;
        } else if (!this.onGround() && wasOnGround) {
            this.targetSquish = 1.0f;
        }
        this.targetSquish *= 0.6f;
    }

    /** O {@code getClosestMergableSlime}, com a mesma conta da distância do original. */
    private ThaumicSlimeEntity closestMergable() {
        ThaumicSlimeEntity closest = null;
        double distance = Double.MAX_VALUE;
        List<ThaumicSlimeEntity> slimes = this.level().getEntitiesOfClass(ThaumicSlimeEntity.class,
                this.getBoundingBox().inflate(16.0, 8.0, 16.0));
        for (ThaumicSlimeEntity slime : slimes) {
            if (slime.getId() != this.getId() && slime.tickCount > 100 && slime.getSize() < 100 && this.distanceToSqr(slime) < distance) {
                closest = slime;
            }
            distance = this.distanceToSqr(slime);
        }
        return closest;
    }

    /** O {@code updateEntityActionState}: pular atrás do jogador, cuspir, ou se juntar a outro slime. */
    @Override
    protected void customServerAiStep(ServerLevel level) {
        Player player = level.getNearestPlayer(this, 16.0);
        if (player != null && (player.isCreative() || player.isSpectator())) player = null;
        if (player != null) {
            if (this.spitCounter > 0) this.spitCounter--;
            this.lookAt(player, 10.0f, 20.0f);
            if (this.distanceTo(player) > 4.0f && this.spitCounter <= 0 && this.getSize() > 3) {
                this.spitCounter = 101;
                level.addFreshEntity(spit(level, this, player));
                this.playSound(TCSounds.GORE.value(), 1.0f, ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) * 0.8f);
                this.setSize(this.getSize() - 1);
            }
        } else {
            ThaumicSlimeEntity slime = this.closestMergable();
            if (slime != null) {
                this.lookAt(slime, 10.0f, 20.0f);
                if (this.distanceTo(slime) < this.getBbWidth() + slime.getBbWidth()) {
                    slime.setSize(Math.min(100, slime.getSize() + this.getSize()));
                    this.discard();
                    return;
                }
            }
        }
        this.setSpeed(0.1f);
        if (this.onGround() && this.jumpDelay-- <= 0) {
            this.jumpDelay = this.random.nextInt(16) + 8;
            if (player != null) this.jumpDelay /= 3;
            this.setJumping(true);
            if (this.getSize() > 3) {
                this.playSound(this.jumpSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) * 0.8f);
            }
            this.xxa = 1.0f - this.random.nextFloat() * 2.0f;
            this.zza = (float) Math.sqrt(this.getSize());
        } else {
            this.setJumping(false);
            if (this.onGround()) this.xxa = this.zza = 0.0f;
        }
    }

    @Override
    public int getMaxHeadXRot() {
        return 0;
    }

    /** O {@code setDead}: morto, se divide em pequenos, na raiz do tamanho. */
    @Override
    public void remove(Entity.RemovalReason reason) {
        int i = (int) Math.sqrt(this.getSize());
        if (!this.level().isClientSide() && i > 1 && this.isDeadOrDying()) {
            for (int k = 0; k < i; k++) {
                float f = (k % 2 - 0.5f) * i / 4.0f;
                float f1 = (k / 2 - 0.5f) * i / 4.0f;
                ThaumicSlimeEntity small = new ThaumicSlimeEntity(TCEntities.THAUMIC_SLIME, this.level());
                small.setSize(1);
                small.snapTo(this.getX() + f, this.getY() + 0.5, this.getZ() + f1, this.random.nextFloat() * 360.0f, 0.0f);
                this.level().addFreshEntity(small);
            }
        }
        super.remove(reason);
    }

    /** O {@code onCollideWithPlayer}: machuca quem encosta, do tanto do tamanho. */
    @Override
    public void playerTouch(Player player) {
        if (this.getSize() <= 0 || !(this.level() instanceof ServerLevel level)) return;
        int i = (int) Math.max(1.0, Math.sqrt(this.getSize()));
        if (this.launched > 0 && i == 2) i = 3;
        if (this.hasLineOfSight(player) && this.distanceToSqr(player) < 0.8 * i * 0.8 * i
                && player.hurtServer(level, this.damageSources().mobAttack(this), this.getSize())) {
            this.playSound(SoundEvents.SLIME_ATTACK, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.getSize() > 3 ? SoundEvents.SLIME_HURT : SoundEvents.SLIME_HURT_SMALL;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.getSize() > 3 ? SoundEvents.SLIME_DEATH : SoundEvents.SLIME_DEATH_SMALL;
    }

    @Override
    protected float getSoundVolume() {
        return 0.1f * (float) Math.sqrt(this.getSize());
    }

    /** O tamanho que se vê (para o desenho). */
    public float renderSize() {
        return Mth.sqrt(this.getSize());
    }

    /** O {@code dropFewItems}. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        if (this.getSize() < 3 && this.random.nextInt(3) == 0) TaintDrops.goo(level, this);
    }
}
