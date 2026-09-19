package net.thaumcraft.entity.eldritch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
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
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.thaumcraft.api.EldritchMob;
import net.thaumcraft.event.Champions;
import net.thaumcraft.net.TCNetwork;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.research.Warp;
import org.jetbrains.annotations.Nullable;

/**
 * O guardião-mor: o {@code EntityEldritchWarden} da 4.2.3.5, o chefe de nome antigo que sobe do chão da sala do chefe.
 * Duzentos de vida e mais dois terços disso de escudo, que se refaz um ponto a cada vinte e cinco tiques sem apanhar; por
 * onde anda deixa o campo sugador. Quando o escudo acaba, some para o meio da sala e, invulnerável, espalha anéis de campo
 * sugador em volta. Atira o orbe eldritch, ou grita: empurra, murcha, enfraquece e distorce.
 */
public class EldritchWardenEntity extends ThaumcraftBossEntity implements EldritchMob, RangedAttackMob {
    private static final EntityDataAccessor<Byte> TITLE = SynchedEntityData.defineId(EldritchWardenEntity.class, EntityDataSerializers.BYTE);
    static final String[] TITLES = {"Aphoom-Zhah", "Basatan", "Chaugnar Faugn", "Mnomquah", "Nyogtha", "Oorn", "Shaikorth", "Rhan-Tegoth",
            "Rhogog", "Shudde M'ell", "Vulthoom", "Yag-Kosha", "Yibb-Tstll", "Zathog", "Zushakon"};

    private boolean fieldFrenzy;
    private int fieldFrenzyCounter;
    private boolean lastBlast;
    public float armLiftL, armLiftR;

    public EldritchWardenEntity(EntityType<? extends EldritchWardenEntity> type, Level level) {
        super(type, level);
        if (this.getNavigation() instanceof net.minecraft.world.entity.ai.navigation.GroundPathNavigation ground) ground.setCanOpenDoors(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return ThaumcraftBossEntity.attributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.ARMOR, 4.0)
                // o escudo de dois terços da vida
                .add(Attributes.MAX_ABSORPTION, 132.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new CultistGoals.LongRangeAttack(this, 3.0, 1.0, 20, 40, 24.0f));
        this.goalSelector.addGoal(3, new CultistGoals.AttackOnCollide(this, 1.1, false));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, CultistEntity.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TITLE, (byte) 0);
    }

    private String title() {
        return TITLES[Math.floorMod(this.entityData.get(TITLE), TITLES.length)];
    }

    @Override
    public void generateName() {
        Champions.Mod mod = Champions.mod(this);
        if (mod != null) this.setCustomName(Component.translatable("entity.thaumcraft.eldritch_warden.titled", this.title(), mod.displayName()));
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

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
                                        @Nullable SpawnGroupData data) {
        this.spawnTimer = 150;
        this.entityData.set(TITLE, (byte) this.random.nextInt(TITLES.length));
        this.setAbsorptionAmount((float) (this.getAbsorptionAmount() + this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 0.66));
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.fieldFrenzyCounter == 0) super.customServerAiStep(level);
        if (this.hurtTime <= 0 && this.tickCount % 25 == 0) {
            int bh = (int) (this.getAttributeBaseValue(Attributes.MAX_HEALTH) * 0.66);
            if (this.getAbsorptionAmount() < bh) this.setAbsorptionAmount(this.getAbsorptionAmount() + 1.0f);
        }
    }

    /** O desenho do lado de quem vê: a névoa escura e, subindo do chão, a espiral de fumaça. */
    public static WardenFx clientFx = warden -> {
    };

    public interface WardenFx {
        void tick(EldritchWardenEntity warden);
    }

    @Override
    public void tick() {
        if (this.getSpawnTimer() == 150 && !this.level().isClientSide()) this.level().broadcastEntityEvent(this, (byte) 18);
        super.tick();
        if (this.level().isClientSide()) {
            if (this.armLiftL > 0.0f) this.armLiftL -= 0.05f;
            if (this.armLiftR > 0.0f) this.armLiftR -= 0.05f;
            clientFx.tick(this);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) return;
        // o campo sugador por onde passa
        for (int l = 0; l < 4; l++) {
            BlockPos p = BlockPos.containing(this.getX() + (l % 2 * 2 - 1) * 0.25f, this.getY(), this.getZ() + (l / 2 % 2 * 2 - 1) * 0.25f);
            if (this.level().isEmptyBlock(p)) this.level().setBlock(p, TCBlocks.SAPPING_FIELD.defaultBlockState(), Block.UPDATE_ALL);
        }
        if (this.fieldFrenzyCounter > 0) {
            if (this.fieldFrenzyCounter == 150) this.teleportHome();
            this.performFieldFrenzy();
        }
    }

    private void performFieldFrenzy() {
        if (this.fieldFrenzyCounter < 121 && this.fieldFrenzyCounter % 10 == 0 && this.level() instanceof ServerLevel server) {
            server.broadcastEntityEvent(this, (byte) 17);
            double radius = (150 - this.fieldFrenzyCounter) / 8.0;
            int d = 1 + this.fieldFrenzyCounter / 8;
            int i = Mth.floor(this.getX()), j = Mth.floor(this.getY()), k = Mth.floor(this.getZ());
            for (int q = 0; q < 180 / d; q++) {
                double radians = Math.toRadians(q * 2 * d);
                int dx = (int) (radius * Math.cos(radians));
                int dz = (int) (radius * Math.sin(radians));
                BlockPos p = new BlockPos(i + dx, j, k + dz);
                if (server.isEmptyBlock(p) && server.getBlockState(p.below()).isRedstoneConductor(server, p.below())) {
                    server.setBlock(p, TCBlocks.SAPPING_FIELD.defaultBlockState(), Block.UPDATE_ALL);
                    server.scheduleTick(p, TCBlocks.SAPPING_FIELD, 250 + this.random.nextInt(150));
                    if (this.random.nextFloat() < 0.3f) TCNetwork.blockArc(server, p, this);
                    else TCNetwork.blockSparkle(server, p, 8388736);
                }
            }
            server.playSound(null, this.getX(), this.getY(), this.getZ(), TCSounds.ZAP.value(), SoundSource.HOSTILE, 1.0f,
                    0.9f + this.random.nextFloat() * 0.1f);
        }
        this.fieldFrenzyCounter--;
    }

    /** O {@code teleportHome}: para o meio da sala (a casa), num ponto firme a até oito blocos. */
    protected void teleportHome() {
        if (!this.hasHome()) return;
        BlockPos home = this.getHomePosition();
        double ox = this.getX(), oy = this.getY(), oz = this.getZ();
        int i = home.getX(), j = home.getY(), k = home.getZ();
        boolean found = false;
        if (this.level().hasChunkAt(new BlockPos(i, j, k))) {
            for (int tries = 20; tries > 0 && !found; ) {
                BlockPos floor = new BlockPos(i, j - 1, k);
                if (this.level().getBlockState(floor).blocksMotion() && !this.level().getBlockState(floor.above()).blocksMotion()) {
                    found = true;
                } else {
                    i = home.getX() + this.random.nextInt(8) - this.random.nextInt(8);
                    k = home.getZ() + this.random.nextInt(8) - this.random.nextInt(8);
                    tries--;
                }
            }
        }
        if (found) {
            this.setPos(i + 0.5, j + 0.1, k + 0.5);
            if (!this.level().noCollision(this)) found = false;
        }
        if (!found) {
            this.setPos(ox, oy, oz);
            return;
        }
        if (this.level() instanceof ServerLevel server) {
            for (int l = 0; l < 128; l++) {
                double d6 = l / 127.0;
                server.sendParticles(ParticleTypes.PORTAL, ox + (this.getX() - ox) * d6 + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 2.0,
                        oy + (this.getY() - oy) * d6 + this.random.nextDouble() * this.getBbHeight(),
                        oz + (this.getZ() - oz) * d6 + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 2.0, 1, 0.0, 0.0, 0.0, 0.0);
            }
            server.playSound(null, ox, oy, oz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 1.0f);
            this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }
    }

    @Override
    public boolean isInvulnerable() {
        return this.fieldFrenzyCounter > 0 || super.isInvulnerable();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (this.isInvulnerable() || source.is(DamageTypes.DROWN) || source.is(DamageTypes.WITHER)) return false;
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && !this.fieldFrenzy && this.getAbsorptionAmount() <= 0.0f) {
            this.fieldFrenzy = true;
            this.fieldFrenzyCounter = 150;
        }
        return hurt;
    }

    /** O orbe eldritch de um braço e do outro, ou, uma vez em cinco, o grito. */
    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (this.random.nextFloat() > 0.2f) {
            EldritchOrbEntity blast = new EldritchOrbEntity(this.level(), this);
            this.lastBlast = !this.lastBlast;
            this.level().broadcastEntityEvent(this, (byte) (this.lastBlast ? 16 : 15));
            int rr = this.lastBlast ? 90 : 180;
            double xx = Mth.cos((this.getYRot() + rr) % 360.0f / 180.0f * Mth.PI) * 0.5f;
            double zz = Mth.sin((this.getYRot() + rr) % 360.0f / 180.0f * Mth.PI) * 0.5f;
            blast.setPos(blast.getX() - xx, blast.getY() - 0.13, blast.getZ() - zz);
            double d0 = target.getX() + target.getDeltaMovement().x - this.getX();
            double d1 = target.getY() - this.getY() - target.getBbHeight() / 2.0f;
            double d2 = target.getZ() + target.getDeltaMovement().z - this.getZ();
            blast.shoot(d0, d1, d2, 1.0f, 2.0f);
            this.playSound(TCSounds.EG_ATTACK.value(), 2.0f, 1.0f + this.random.nextFloat() * 0.1f);
            this.level().addFreshEntity(blast);
        } else if (this.hasLineOfSight(target)) {
            if (this.level() instanceof ServerLevel server) TCNetwork.sonic(server, this);
            target.push(-Mth.sin(this.getYRot() * Mth.PI / 180.0f) * 1.5f, 0.1, Mth.cos(this.getYRot() * Mth.PI / 180.0f) * 1.5f);
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 0));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 0));
            if (target instanceof Player player) Warp.add(player, 3 + this.random.nextInt(3), true);
            this.playSound(TCSounds.EG_SCREECH.value(), 4.0f, 1.0f + this.random.nextFloat() * 0.1f);
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
            case 18 -> this.spawnTimer = 150;
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    protected boolean considersEntityAsAlly(net.minecraft.world.entity.Entity other) {
        return other instanceof EldritchGuardianEntity || super.considersEntityAsAlly(other);
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
}
