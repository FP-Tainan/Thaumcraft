package net.thaumcraft.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.registry.TCEntities;

/**
 * O {@code EntityFollowingItem} da 4.2.3.5: o item que voa até quem o colheu (a pá e o triturador que cavam três por
 * três), atravessando os blocos, deixando um rastro de faíscas da cor dada (ou de bolhas azuis, no tipo 10); chegando
 * perto, vira item comum.
 */
public class FollowingItemEntity extends SpecialItemEntity {
    /** As faíscas do rastro ({@code sparkle}, ou o {@code crucibleBubble} no tipo 10), desenhadas por quem vê. */
    public interface ClientEffects {
        void trail(Level level, double x, double y, double z, int type);
    }

    public static ClientEffects clientEffects;

    private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(FollowingItemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(FollowingItemEntity.class, EntityDataSerializers.INT);
    private double targetX, targetY, targetZ;
    private int slowdown = 20;
    private boolean arrived;

    public FollowingItemEntity(EntityType<? extends FollowingItemEntity> type, Level level) {
        super(type, level);
    }

    public FollowingItemEntity(Level level, double x, double y, double z, ItemStack stack, Entity target, int type) {
        this(TCEntities.FOLLOWING_ITEM, level);
        this.setPos(x, y, z);
        this.setItem(stack);
        this.setYRot((float) (Math.random() * 360.0));
        this.entityData.set(TARGET, target.getId());
        this.entityData.set(TYPE, type);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET, -1);
        builder.define(TYPE, 3);
    }

    @Override
    public void tick() {
        Entity target = this.arrived ? null : this.level().getEntity(this.entityData.get(TARGET));
        if (target != null) {
            this.targetX = target.getX();
            this.targetY = target.getBoundingBox().minY + target.getBbHeight() / 2.0f;
            this.targetZ = target.getZ();
        }
        if (target == null && this.targetX == 0.0 && this.targetY == 0.0 && this.targetZ == 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04f, 0.0));
        } else if (target != null) {
            float xd = (float) (this.targetX - this.getX()), yd = (float) (this.targetY - this.getY()), zd = (float) (this.targetZ - this.getZ());
            if (this.slowdown > 1) this.slowdown--;
            double distance = Mth.sqrt(xd * xd + yd * yd + zd * zd);
            if (distance > 0.5) {
                distance *= this.slowdown;
                this.setDeltaMovement(xd / distance, yd / distance, zd / distance);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.1f));
                this.targetX = this.targetY = this.targetZ = 0.0;
                this.arrived = true;
                this.noPhysics = false;
            }
            if (this.level().isClientSide() && clientEffects != null) {
                var random = this.getRandom();
                clientEffects.trail(this.level(), this.xo + (random.nextFloat() - random.nextFloat()) * 0.125f,
                        this.yo + this.getBbHeight() / 2.0f + (random.nextFloat() - random.nextFloat()) * 0.125f,
                        this.zo + (random.nextFloat() - random.nextFloat()) * 0.125f, this.entityData.get(TYPE));
            }
        }
        super.tick();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putShort("type", (short) (int) this.entityData.get(TYPE));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(TYPE, (int) input.getShortOr("type", (short) 3));
        this.arrived = true;
    }

    /** Ainda voando para alguém? */
    public boolean isFollowing() {
        return !this.arrived && this.entityData.get(TARGET) >= 0;
    }

    /** O vento que empurra o item na hora de nascer. */
    public void push(Vec3 motion) {
        this.setDeltaMovement(motion);
    }
}
