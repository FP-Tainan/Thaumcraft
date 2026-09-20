package net.thaumcraft.maleficium.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.registry.TCSounds;

import java.util.List;

/**
 * A lasca de vis que persegue: o {@code EntityHomingShard} do Tainted Magic 8.1.1.
 *
 * <p>Sai torta do foco, freia sozinha e a cada dez tiques vira para o alvo — e vai atrás dele até acertar, até o
 * alvo morrer ou até quinze segundos passarem. Com a melhoria persistente, quando o alvo some ela procura outro do
 * mesmo tipo a dezesseis blocos. Nas folhas e nas plantas ela passa; nas paredes, quica.
 */
public class HomingShardEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Byte> STRENGTH =
            SynchedEntityData.defineId(HomingShardEntity.class, EntityDataSerializers.BYTE);

    /** O estouro, do lado de quem vê. */
    public static java.util.function.Consumer<HomingShardEntity> clientBurst = shard -> {
    };

    private LivingEntity target;
    private boolean persistent;

    public HomingShardEntity(EntityType<? extends HomingShardEntity> type, Level level) {
        super(type, level);
    }

    public HomingShardEntity(Level level, LivingEntity thrower, LivingEntity target, int strength, boolean persistent) {
        super(MaleficiumEntities.HOMING_SHARD, level);
        this.setOwner(thrower);
        this.target = target;
        this.persistent = persistent;
        this.entityData.set(STRENGTH, (byte) strength);

        Vec3 look = thrower.getLookAngle();
        this.snapTo(thrower.getX() + look.x / 2.0, thrower.getEyeY() + look.y / 2.0, thrower.getZ() + look.z / 2.0,
                thrower.getYRot(), thrower.getXRot());
        // sai torta, até sessenta graus para cada lado, como no original
        float yaw = thrower.getYRot() + (this.random.nextFloat() - this.random.nextFloat()) * 60.0f;
        float pitch = thrower.getXRot() + (this.random.nextFloat() - this.random.nextFloat()) * 60.0f;
        double mx = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * 0.5;
        double mz = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * 0.5;
        double my = -Math.sin(Math.toRadians(pitch)) * 0.5;
        this.setDeltaMovement(mx, my, mz);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(STRENGTH, (byte) 0);
    }

    public int strength() {
        return this.entityData.get(STRENGTH);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) return;
        ServerLevel server = (ServerLevel) this.level();

        if (this.persistent && (this.target == null || !this.target.isAlive() || this.distanceToSqr(this.target) > 1250.0)) {
            List<LivingEntity> perto = server.getEntitiesOfClass(LivingEntity.class,
                    new AABB(this.position(), this.position()).inflate(16.0),
                    e -> e.isAlive() && e != this.getOwner());
            this.target = perto.isEmpty() ? null : perto.getFirst();
        }
        if (this.target == null || !this.target.isAlive() || this.tickCount > 300) {
            server.broadcastEntityEvent(this, (byte) 16);
            this.discard();
            return;
        }
        if (this.tickCount % 10 == 0) {
            Vec3 para = this.target.position().add(0.0, this.target.getBbHeight() * 0.6, 0.0).subtract(this.position());
            double distance = para.length();
            if (distance > 0.0) this.setDeltaMovement(para.scale(1.0 / distance));
            server.playSound(null, this, TCSounds.ZAP.value(), SoundSource.HOSTILE, 0.1f, 2.0f * this.random.nextFloat());
        }
        this.setDeltaMovement(this.getDeltaMovement().scale(0.85));
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!(this.level() instanceof ServerLevel server)) return;
        Entity hitEntity = hit.getEntity();
        if (hitEntity == this.getOwner()) return;
        hitEntity.hurtServer(server, this.damageSources().thrown(this, this.getOwner()), 2.0f + this.strength() * 0.5f);
        server.playSound(null, this, TCSounds.ZAP.value(), SoundSource.HOSTILE, 1.0f,
                1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        server.broadcastEntityEvent(this, (byte) 16);
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        // nas folhas e nas plantas ela passa; no resto, quica
        var state = this.level().getBlockState(hit.getBlockPos());
        if (state.is(net.minecraft.tags.BlockTags.LEAVES) || state.is(net.minecraft.tags.BlockTags.REPLACEABLE)) return;
        this.setDeltaMovement(this.getDeltaMovement().scale(-0.8));
    }

    @Override
    protected void onHit(HitResult hit) {
        if (hit.getType() == HitResult.Type.ENTITY) super.onHit(hit);
        else if (hit instanceof BlockHitResult block) this.onHitBlock(block);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) clientBurst.accept(this);
        else super.handleEntityEvent(id);
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("strength", (byte) this.strength());
        output.putBoolean("persistent", this.persistent);
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(STRENGTH, input.getByteOr("strength", (byte) 0));
        this.persistent = input.getBooleanOr("persistent", false);
    }
}
