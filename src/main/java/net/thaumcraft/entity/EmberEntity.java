package net.thaumcraft.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.registry.TCEntities;

/**
 * A brasa do foco de fogo: o {@code EntityEmber} da 4.2.3.5, descompilado.
 *
 * <p>Voa a um bloco por tique, sem cair, perdendo cinco por cento da pressa a cada tique (dois e meio, se durar mais
 * que vinte tiques, como a do raio de fogo), e some ao fim da {@code duration}. Quem ela acerta leva o {@code damage}
 * de bola de fogo e fica em chamas por três segundos mais um por nível de fogo alquímico — a não ser que não pegue
 * fogo. Batendo num bloco, com fogo alquímico, às vezes acende o lado. Ela não é alvo de nada.
 */
public class EmberEntity extends ThrowableProjectile {
    /** A duração normal, em tiques. */
    public static final int DURATION = 20;
    private static final EntityDataAccessor<Integer> DURATION_DATA = SynchedEntityData.defineId(EmberEntity.class, EntityDataSerializers.INT);
    /** O {@code firey}: os níveis de fogo alquímico do foco. */
    public int firey;
    /** O {@code damage}: 2 mais a potência, no foco. */
    public float damage = 1.0f;

    public EmberEntity(EntityType<? extends EmberEntity> type, Level level) {
        super(type, level);
    }

    /** O {@code EntityEmber(world, thrower, scatter)}: sai do olho de quem lança, na direção da mira. */
    public EmberEntity(Level level, LivingEntity thrower, float scatter) {
        super(TCEntities.EMBER, level);
        this.setOwner(thrower);
        Throw.from(this, thrower, 1.0f, scatter);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DURATION_DATA, DURATION);
    }

    public int duration() {
        return this.entityData.get(DURATION_DATA);
    }

    public void setDuration(int duration) {
        this.entityData.set(DURATION_DATA, duration);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        if (this.tickCount > this.duration()) {
            this.discard();
            return;
        }
        this.setDeltaMovement(this.getDeltaMovement().scale(this.duration() <= 20 ? 0.95 : 0.975));
        if (this.onGround()) this.setDeltaMovement(this.getDeltaMovement().scale(0.66));
        super.tick();
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (!(this.level() instanceof ServerLevel server)) return;
        Entity target = hit.getEntity();
        if (target.fireImmune()) return;
        DamageSource source = new DamageSource(server.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(DamageTypes.FIREBALL), this, this.getOwner());
        if (target.hurtServer(server, source, this.damage)) target.igniteForSeconds(3 + this.firey);
    }

    /** Com fogo alquímico, uma chance de 2,5% por nível de acender o lado do bloco batido. */
    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (this.level().isClientSide() || this.random.nextFloat() >= 0.025f * this.firey) return;
        BlockPos side = hit.getBlockPos().relative(hit.getDirection());
        if (this.level().isEmptyBlock(side)) this.level().setBlockAndUpdate(side, Blocks.FIRE.defaultBlockState());
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!this.level().isClientSide()) this.discard();
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("damage", this.damage);
        output.putInt("firey", this.firey);
        output.putInt("duration", this.duration());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.damage = input.getFloatOr("damage", 1.0f);
        this.firey = input.getIntOr("firey", 0);
        this.setDuration(input.getIntOr("duration", DURATION));
    }
}
