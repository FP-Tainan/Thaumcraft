package net.thaumcraft.entity.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.api.TaintedMob;
import net.thaumcraft.block.TaintFibreBlock;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.world.TCBiomes;

import java.util.ArrayList;
import java.util.List;

/**
 * O esporo da mácula: o {@code EntityTaintSpore} da 4.2.3.5. A bolha roxa que fica em cima do talo de esporos (e que
 * cresce um por minuto, até dez); fora da Terra Maculada perde vida, e se tocada, ferida ou sem o talo embaixo, estoura
 * em aranhas da mácula (um terço do tamanho mais um tanto ao acaso) e o talo volta a ser talo sem esporo. Um enxame de
 * mosquinhas voa em volta dela.
 */
public class TaintSporeEntity extends Monster implements TaintedMob {
    private static final EntityDataAccessor<Integer> SIZE = SynchedEntityData.defineId(TaintSporeEntity.class, EntityDataSerializers.INT);
    protected int growth;
    public float displaySize;
    /** As mosquinhas que voam em volta, do lado de quem vê. */
    public final List<Object> swarm = new ArrayList<>();

    public TaintSporeEntity(EntityType<? extends TaintSporeEntity> type, Level level) {
        super(type, level);
        this.setSporeSize(2);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SIZE, 1);
    }

    public int getSporeSize() {
        return this.entityData.get(SIZE);
    }

    public void setSporeSize(int size) {
        this.entityData.set(SIZE, size);
        this.refreshDimensions();
        this.xpReward = size;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        if (SIZE.equals(accessor)) this.refreshDimensions();
        super.onSyncedDataUpdated(accessor);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        float s = Math.max(0.15f * this.getSporeSize(), 0.5f);
        return EntityDimensions.scalable(s, s);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Size", this.getSporeSize() - 1);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setSporeSize(input.getIntOr("Size", 1) + 1);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity, this.getSporeSize());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.displaySize = packet.getData();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    /** O talo com esporo está logo embaixo? */
    protected boolean onStalk() {
        BlockPos below = BlockPos.containing(this.getX(), this.getBoundingBox().minY, this.getZ()).below();
        BlockState state = this.level().getBlockState(below);
        return state.is(TCBlocks.TAINT_FIBRES) && state.getValue(TaintFibreBlock.KIND) == 4;
    }

    /** O {@code moveEntity}: nunca de lado nem para cima; e em cima do talo, parado. */
    @Override
    public void move(MoverType type, Vec3 movement) {
        double y = Math.min(movement.y, 0.0);
        if (!this.onStalk()) super.move(type, new Vec3(0.0, y, 0.0));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount % 20 == 0 && !this.level().getBiome(this.blockPosition()).is(TCBiomes.TAINTED_LAND)) {
            this.hurtServer((ServerLevel) this.level(), this.damageSources().starve(), 1.0f);
        }
        this.sporeTick();
    }

    /** As mosquinhas do {@code swarmParticleFX} (o cliente liga isto): devolve a partícula, para contar. */
    public static java.util.function.Function<Entity, Object> swarmEffect = e -> null;
    /** Esta mosquinha já morreu? */
    public static java.util.function.Predicate<Object> swarmDead = fx -> true;
    /** O {@code splooshFX}. */
    public static java.util.function.Consumer<Entity> splooshEffect = e -> {
    };

    protected void sporeTick() {
        if (this.getSporeSize() < 10 && this.growth++ == 1200) {
            this.setSporeSize(this.getSporeSize() + 1);
            this.growth = 0;
        }
        if (this.level().isClientSide()) {
            if (this.displaySize < this.getSporeSize()) this.displaySize += 0.02f;
            this.pruneSwarm();
            if (this.swarm.size() < this.getSporeSize() / 3) {
                Object fx = swarmEffect.apply(this);
                if (fx != null) this.swarm.add(fx);
            }
        }
        if (!this.onStalk() || this.deathTime > 0) this.burst();
    }

    /** Tira da conta as mosquinhas que já morreram (uma por tique, como no original). */
    protected void pruneSwarm() {
        for (int a = 0; a < this.swarm.size(); a++) {
            Object fx = this.swarm.get(a);
            if (fx == null || swarmDead.test(fx)) {
                this.swarm.remove(a);
                break;
            }
        }
    }

    @Override
    public void playerTouch(Player player) {
        this.burst();
    }

    /** O {@code spiderBurst}: as aranhas saem, o talo esvazia e o esporo some. */
    protected void burst() {
        if (this.level() instanceof ServerLevel level) {
            this.playSound(TCSounds.GORE.value(), 1.0f, 0.9f + level.getRandom().nextFloat() * 0.1f);
            int q = this.getSporeSize() / 3 + level.getRandom().nextInt(this.getSporeSize() / 2 + 1);
            for (int a = 0; a < q; a++) {
                TaintSpiderEntity spider = new TaintSpiderEntity(TCEntities.TAINT_SPIDER, level);
                spider.snapTo(this.getX() + level.getRandom().nextFloat() - level.getRandom().nextFloat(), this.getY() + level.getRandom().nextFloat(),
                        this.getZ() + level.getRandom().nextFloat() - level.getRandom().nextFloat(), level.getRandom().nextFloat() * 360.0f, 0.0f);
                level.addFreshEntity(spider);
            }
            BlockPos below = BlockPos.containing(this.getX(), this.getBoundingBox().minY, this.getZ()).below();
            BlockState state = level.getBlockState(below);
            if (state.is(TCBlocks.TAINT_FIBRES) && state.getValue(TaintFibreBlock.KIND) == 4) {
                level.setBlockAndUpdate(below, state.setValue(TaintFibreBlock.KIND, 3));
            }
            this.discard();
        } else {
            this.sploosh(50);
        }
    }

    protected void sploosh(int amount) {
        for (int a = 0; a < amount; a++) splooshEffect.accept(this);
    }

    @Override
    protected float getSoundVolume() {
        return 0.1f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.SWARM.value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TCSounds.GORE.value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.GORE.value();
    }

    /** O tamanho que se vê, andando devagar até o de verdade. */
    public float renderSize(float partial) {
        float f = this.displaySize;
        if (this.displaySize < this.getSporeSize()) f += 0.02f * partial;
        return f;
    }

    /** O pulsar do desenho. */
    public float pulse() {
        return 0.025f * Mth.sin(this.tickCount * 0.075f);
    }

    /** O {@code dropFewItems}. */
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        super.dropCustomDeathLoot(level, source, killedByPlayer);
        TaintDrops.either(level, this);
    }
}
