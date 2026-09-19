package net.thaumcraft.entity.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.block.entity.BannerBlockEntity;
import net.thaumcraft.net.TCNetwork;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCSounds;

/**
 * O portal carmesim: o {@code EntityCultistPortal} da 4.2.3.5, que a fechadura antiga das Terras de Fora abre. Parado,
 * quinhentos de vida, imune a fogo e a poções. Primeiro finca quatro estandartes dos cultistas em volta e espalha
 * caixotes; depois, com alguém a até quarenta e oito blocos, vai soltando cultistas — cavaleiros e clérigos — cada vez
 * mais depressa, e na décima terceira leva o pretor; dali em diante cada cultista que sai o fere. Quem encosta leva um
 * choque. Morto, estoura e deixa a pérola primordial.
 */
public class CultistPortalEntity extends Monster {
    private int stage;
    private int stagecounter = 200;
    public int pulse;
    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getUUID(), Component.empty(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    public CultistPortalEntity(EntityType<? extends CultistPortalEntity> type, Level level) {
        super(type, level);
        this.xpReward = 30;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, 5.0);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return false;
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel server) {
            if (this.stagecounter <= 0) {
                if (server.getNearestPlayer(this, 48.0) != null) {
                    server.broadcastEntityEvent(this, (byte) 16);
                    switch (this.stage) {
                        case 0, 1, 2, 3, 4 -> {
                            this.stagecounter = 15 + this.random.nextInt(10 - this.stage) - this.stage;
                            this.spawnMinions(server);
                        }
                        case 12 -> {
                            this.stagecounter = 50 + this.getTiming(server) * 2 + this.random.nextInt(50);
                            this.spawnBoss(server);
                        }
                        default -> {
                            int t = this.getTiming(server);
                            this.stagecounter = t + this.random.nextInt(5 + t / 3);
                            this.spawnMinions(server);
                        }
                    }
                    this.stage++;
                } else {
                    this.stagecounter = 30 + this.random.nextInt(30);
                }
            } else {
                this.stagecounter--;
                if (this.stagecounter == 160 && this.stage == 0) this.plantBanners(server);
                if (this.stagecounter > 20 && this.stagecounter < 150 && this.stage == 0 && this.stagecounter % 13 == 0) this.dropCrate(server);
            }
            if (this.stage < 12) this.heal(1.0f);
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }
        if (this.pulse > 0) this.pulse--;
    }

    /** Os quatro estandartes a seis blocos, com um arco de faíscas até cada um. */
    private void plantBanners(ServerLevel level) {
        level.broadcastEntityEvent(this, (byte) 16);
        int[][] dirs = {{0, -1, 8}, {0, 1, 0}, {-1, 0, 12}, {1, 0, 4}};
        for (int[] d : dirs) {
            BlockPos pos = new BlockPos((int) this.getX() - d[0] * 6, (int) this.getY(), (int) this.getZ() + d[1] * 6);
            level.setBlock(pos, TCBlocks.BANNER.defaultBlockState(), Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof BannerBlockEntity banner) {
                banner.setFacing((byte) d[2]);
                TCNetwork.blockArc(level, pos, this);
                this.playSound(TCSounds.WAND_FAIL.value(), 1.0f, 1.0f);
            }
        }
    }

    /** Um caixote abandonado por perto (os raros, raramente). */
    private void dropCrate(ServerLevel level) {
        int a = (int) this.getX() + this.random.nextInt(5) - this.random.nextInt(5);
        int b = (int) this.getZ() + this.random.nextInt(5) - this.random.nextInt(5);
        BlockPos pos = new BlockPos(a, (int) this.getY(), b);
        if (a == (int) this.getX() || b == (int) this.getZ() || !level.isEmptyBlock(pos)) return;
        level.broadcastEntityEvent(this, (byte) 16);
        float rr = level.getRandom().nextFloat();
        int md = rr < 0.05f ? 2 : (rr < 0.2f ? 1 : 0);
        level.setBlock(pos, TCBlocks.LOOT_CRATES.get(md).defaultBlockState(), Block.UPDATE_ALL);
        TCNetwork.blockArc(level, pos, this);
        this.playSound(TCSounds.WAND_FAIL.value(), 1.0f, 1.0f);
    }

    private int getTiming(ServerLevel level) {
        return level.getEntitiesOfClass(CultistEntity.class, this.getBoundingBox().inflate(32.0), e -> e.distanceToSqr(this) <= 1024.0).size() * 20;
    }

    private void spawnMinions(ServerLevel level) {
        CultistEntity cultist = this.random.nextFloat() > 0.33f
                ? TCEntities.CULTIST_KNIGHT.create(level, EntitySpawnReason.MOB_SUMMONED)
                : TCEntities.CULTIST_CLERIC.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (cultist == null) return;
        cultist.setPos(this.getX() + this.random.nextFloat() - this.random.nextFloat(), this.getY() + 0.25,
                this.getZ() + this.random.nextFloat() - this.random.nextFloat());
        cultist.finalizeSpawn(level, level.getCurrentDifficultyAt(cultist.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
        cultist.spawnAnim();
        cultist.setHomeTo(this.blockPosition(), 32);
        level.addFreshEntity(cultist);
        cultist.playSound(TCSounds.WAND_FAIL.value(), 1.0f, 1.0f);
        if (this.stage > 12) this.hurtServer(level, this.damageSources().fellOutOfWorld(), 5 + this.random.nextInt(5));
    }

    private void spawnBoss(ServerLevel level) {
        CultistLeaderEntity leader = TCEntities.CULTIST_LEADER.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (leader == null) return;
        leader.setPos(this.getX() + this.random.nextFloat() - this.random.nextFloat(), this.getY() + 0.25,
                this.getZ() + this.random.nextFloat() - this.random.nextFloat());
        leader.finalizeSpawn(level, level.getCurrentDifficultyAt(leader.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
        leader.setHomeTo(this.blockPosition(), 32);
        leader.spawnAnim();
        level.addFreshEntity(leader);
        leader.playSound(TCSounds.WAND_FAIL.value(), 1.0f, 1.0f);
    }

    /** Quem encosta leva um choque de oito. */
    @Override
    public void playerTouch(Player player) {
        if (this.distanceToSqr(player) < 3.0 && this.level() instanceof ServerLevel server
                && player.hurtServer(server, this.damageSources().thrown(this, this), 8.0f)) {
            this.playSound(TCSounds.ZAP.value(), 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.1f + 1.0f);
        }
    }

    @Override
    protected float getSoundVolume() {
        return 0.75f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 540;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.MONOLITH.value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TCSounds.ZAP.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.SHOCK.value();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        ThaumcraftBossEntity.SpecialDrops.drop(this, new ItemStack(TCItems.PRIMORDIAL_PEARL), this.getBbHeight() / 2.0f);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            this.pulse = 10;
            this.spawnAnim();
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void die(DamageSource source) {
        if (this.level() instanceof ServerLevel server) {
            server.explode(this, this.getX(), this.getY(), this.getZ(), 2.0f, false, Level.ExplosionInteraction.NONE);
        }
        super.die(source);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.setName(this.getDisplayName());
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("stage", this.stage);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.stage = input.getIntOr("stage", 0);
    }

}
