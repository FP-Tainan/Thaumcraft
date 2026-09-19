package net.thaumcraft.entity.eldritch;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.EldritchMob;
import net.thaumcraft.event.Champions;
import net.thaumcraft.registry.TCItems;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * O chefe do Thaumcraft: o {@code EntityThaumcraftBoss} da 4.2.3.5, a base do pretor carmesim e dos chefes das Terras de
 * Fora. Tem a barra de chefe, é sempre campeão (com o nome feito por ele), se cura um ponto a cada segundo e meio e
 * lembra quanto dano cada um deu: troca de alvo para quem bate bem mais que o alvo atual. Para cada jogador a mais que
 * brigue com ele, ganha vida e dano. Golpe de mais de trinta e cinco é cortado, e o deixa com raiva (regeneração, força e
 * velocidade por dez segundos). Morto, deixa a pérola primordial e uma sacola rara.
 */
public abstract class ThaumcraftBossEntity extends Monster implements Champions.Boss {
    private static final EntityDataAccessor<Integer> ANGER = SynchedEntityData.defineId(ThaumcraftBossEntity.class, EntityDataSerializers.INT);
    private static final AttributeModifier[] HP_BUFF = new AttributeModifier[5];
    private static final AttributeModifier[] DMG_BUFF = new AttributeModifier[5];

    static {
        for (int a = 0; a < 5; a++) {
            HP_BUFF[a] = new AttributeModifier(Thaumcraft.id("boss_health_" + (a + 1)), 50.0, AttributeModifier.Operation.ADD_VALUE);
            DMG_BUFF[a] = new AttributeModifier(Thaumcraft.id("boss_damage_" + (a + 1)), 0.5, AttributeModifier.Operation.ADD_VALUE);
        }
    }

    protected final Map<Integer, Integer> aggro = new HashMap<>();
    protected int spawnTimer;
    protected final ServerBossEvent bossEvent = new ServerBossEvent(this.getUUID(), Component.empty(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    protected ThaumcraftBossEntity(EntityType<? extends ThaumcraftBossEntity> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95)
                .add(Attributes.FOLLOW_RANGE, 40.0);
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

    public int getSpawnTimer() {
        return this.spawnTimer;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
                                        @Nullable SpawnGroupData data) {
        this.setHomeTo(this.blockPosition(), 24);
        return data;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.getSpawnTimer() == 0) super.customServerAiStep(level);
        if (this.getTarget() != null && this.getTarget().isRemoved()) this.setTarget(null);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.spawnTimer > 0) this.spawnTimer--;
        if (this.getAnger() > 0) this.setAnger(this.getAnger() - 1);
        if (this.level().isClientSide()) {
            if (this.random.nextInt(15) == 0 && this.getAnger() > 0) {
                this.level().addParticle(ParticleTypes.ANGRY_VILLAGER,
                        this.getX() + this.random.nextFloat() * this.getBbWidth() - this.getBbWidth() / 2.0,
                        this.getBoundingBox().minY + this.getBbHeight() + this.random.nextFloat() * 0.5,
                        this.getZ() + this.random.nextFloat() * this.getBbWidth() - this.getBbWidth() / 2.0,
                        this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02);
            }
            return;
        }
        if (this.tickCount % 30 == 0) this.heal(1.0f);
        if (this.getTarget() != null && this.tickCount % 20 == 0) this.retarget();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    /** A troca de alvo pela raiva de cada um e o reforço por jogador. */
    private void retarget() {
        List<Integer> dl = new ArrayList<>();
        int players = 0;
        int hei = this.getTarget().getId();
        int ad = this.aggro.getOrDefault(hei, 0);
        int ld = ad;
        Entity newTarget = null;
        for (Integer ei : this.aggro.keySet()) {
            int ca = this.aggro.get(ei);
            if (ca > ad + 25 && ca > ad * 1.1 && ca > ld) {
                // o original busca o alvo pelo número de antes de trocá-lo: fica igual
                newTarget = this.level().getEntity(hei);
                if (newTarget != null && !newTarget.isRemoved() && !(this.distanceToSqr(newTarget) > 16384.0)) {
                    hei = ei;
                    ld = ei;
                    if (newTarget instanceof Player) players++;
                } else {
                    dl.add(ei);
                }
            }
        }
        for (Integer ei : dl) this.aggro.remove(ei);
        if (newTarget instanceof LivingEntity living && hei != this.getTarget().getId()) this.setTarget(living);
        float om = this.getMaxHealth();
        AttributeInstance hp = this.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance dmg = this.getAttribute(Attributes.ATTACK_DAMAGE);
        for (int a = 0; a < 5; a++) {
            if (dmg != null) dmg.removeModifier(DMG_BUFF[a].id());
            if (hp != null) hp.removeModifier(HP_BUFF[a].id());
        }
        for (int a = 0; a < Math.min(5, players - 1); a++) {
            if (hp != null) hp.addTransientModifier(HP_BUFF[a]);
            if (dmg != null) dmg.addTransientModifier(DMG_BUFF[a]);
        }
        double mm = this.getMaxHealth() / om;
        this.setHealth((float) (this.getHealth() * mm));
    }

    @Override
    public boolean isInvulnerable() {
        return super.isInvulnerable() || this.getSpawnTimer() > 0;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && !this.isInvulnerable();
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected boolean considersEntityAsAlly(Entity other) {
        return other instanceof EldritchMob || super.considersEntityAsAlly(other);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        SpecialDrops.drop(this, new ItemStack(TCItems.PRIMORDIAL_PEARL), this.getBbHeight() / 2.0f);
        this.spawnAtLocation(level, new ItemStack(TCItems.LOOT_BAG_RARE), 1.5f);
        this.dropEquipmentByChance(level, source, recentlyHit);
    }

    /** O {@code dropEquipment} do jogo, que corre à parte do saque de cada criatura. */
    protected void dropEquipmentByChance(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getEntity() instanceof LivingEntity attacker) {
            int target = attacker.getId();
            int ad = (int) damage + this.aggro.getOrDefault(target, 0);
            this.aggro.put(target, ad);
        }
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

    /** O {@code EntityUtils.entityDropSpecialItem}: a pérola que brilha e não some. */
    public static final class SpecialDrops {
        private SpecialDrops() {
        }

        public static void drop(Entity entity, ItemStack stack, float height) {
            if (!(entity.level() instanceof ServerLevel level)) return;
            var item = new net.thaumcraft.entity.SpecialItemEntity(level, entity.getX(), entity.getY() + height, entity.getZ(), stack);
            item.setPickUpDelay(10);
            item.setDeltaMovement(0.0, 0.1, 0.0);
            level.addFreshEntity(item);
        }
    }

    @Override
    public void generateName() {
    }
}
