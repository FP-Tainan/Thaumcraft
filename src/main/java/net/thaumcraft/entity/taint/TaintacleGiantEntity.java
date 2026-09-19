package net.thaumcraft.entity.taint;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.thaumcraft.entity.eldritch.ThaumcraftBossEntity;
import net.thaumcraft.event.Champions;
import net.thaumcraft.registry.TCItems;
import org.jetbrains.annotations.Nullable;

/**
 * O tentáculo gigante: o {@code EntityTaintacleGiant} da 4.2.3.5, o chefe da sala maculada. Seis blocos de altura, cento
 * e vinte e cinco de vida, nove de dano; nasce campeão, tem barra de chefe, se cura aos poucos, respira debaixo d'água e
 * se enfurece com golpes fortes. O último gigante a cair por perto deixa a pérola primordial.
 */
public class TaintacleGiantEntity extends TaintacleEntity {
    private static final EntityDataAccessor<Integer> ANGER = SynchedEntityData.defineId(TaintacleGiantEntity.class, EntityDataSerializers.INT);
    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getUUID(), Component.empty(), BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS);

    public TaintacleGiantEntity(EntityType<? extends TaintacleGiantEntity> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder attributes() {
        return TaintacleEntity.attributes().add(Attributes.MAX_HEALTH, 125.0).add(Attributes.ATTACK_DAMAGE, 9.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANGER, 0);
    }

    public int getAnger() {
        return this.entityData.get(ANGER);
    }

    public void setAnger(int anger) {
        this.entityData.set(ANGER, anger);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
                                        @Nullable SpawnGroupData data) {
        Champions.makeChampion(this, true);
        return data;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getAnger() > 0) this.setAnger(this.getAnger() - 1);
        if (this.level().isClientSide() && this.random.nextInt(15) == 0 && this.getAnger() > 0) {
            this.level().addParticle(ParticleTypes.ANGRY_VILLAGER, this.getX() + this.random.nextFloat() * this.getBbWidth() - this.getBbWidth() / 2.0,
                    this.getBoundingBox().minY + this.getBbHeight() + this.random.nextFloat() * 0.5,
                    this.getZ() + this.random.nextFloat() * this.getBbWidth() - this.getBbWidth() / 2.0,
                    this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02);
        }
        if (!this.level().isClientSide()) {
            if (this.tickCount % 30 == 0) this.heal(1.0f);
            if (this.tickCount % 20 == 0) this.bossEvent.setName(this.getDisplayName());
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }
    }

    /** O último gigante por perto deixa a pérola primordial; nada mais. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        boolean others = !level.getEntitiesOfClass(TaintacleGiantEntity.class, this.getBoundingBox().inflate(48.0), e -> e != this && e.isAlive()).isEmpty();
        if (!others) ThaumcraftBossEntity.SpecialDrops.drop(this, new ItemStack(TCItems.PRIMORDIAL_PEARL), this.getBbHeight() / 2.0f);
    }

    @Override
    protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKilled) {
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (damage > 35.0f) {
            if (this.getAnger() == 0) {
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, (int) (damage / 15.0f)));
                this.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, (int) (damage / 40.0f)));
                this.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, (int) (damage / 40.0f)));
                this.setAnger(200);
                if (source.getEntity() instanceof Player player) {
                    player.sendSystemMessage(this.getName().copy().append(" ").append(Component.translatable("tc.boss.enrage")));
                }
            }
            damage = 35.0f;
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }
}
