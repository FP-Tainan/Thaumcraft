package net.thaumcraft.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;
import org.jetbrains.annotations.Nullable;

/**
 * A flecha primordial: o {@code EntityPrimalArrow} da 4.2.3.5. De ar (fura armadura, como magia), de fogo (queima cinco
 * segundos a mais), de água (lentidão forte por dez segundos), de terra (uma vez e meia o dano e um de repulsão a mais),
 * de ordem (fura armadura e enfraquece; oito décimos do dano) e de entropia (murcha; oito décimos). Não se recolhe e some
 * cinco segundos depois de cravada.
 */
public class PrimalArrowEntity extends AbstractArrow {
    public static final String[] TYPES = {"air", "fire", "water", "earth", "order", "entropy"};
    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(PrimalArrowEntity.class, EntityDataSerializers.INT);
    public static final ResourceKey<DamageType> AIR = ResourceKey.create(Registries.DAMAGE_TYPE, Thaumcraft.id("airarrow"));
    public static final ResourceKey<DamageType> FIRE = ResourceKey.create(Registries.DAMAGE_TYPE, Thaumcraft.id("firearrow"));
    public static final ResourceKey<DamageType> ORDER = ResourceKey.create(Registries.DAMAGE_TYPE, Thaumcraft.id("orderarrow"));

    public PrimalArrowEntity(EntityType<? extends PrimalArrowEntity> type, Level level) {
        super(type, level);
    }

    public PrimalArrowEntity(Level level, LivingEntity owner, ItemStack pickup, @Nullable ItemStack weapon, int type) {
        super(TCEntities.PRIMAL_ARROW, owner, level, pickup, weapon);
        this.entityData.set(TYPE, type);
        this.pickup = Pickup.DISALLOWED;
        this.setBaseDamage(2.1 * multiplier(type));
    }

    public PrimalArrowEntity(Level level, double x, double y, double z, ItemStack pickup, int type) {
        super(TCEntities.PRIMAL_ARROW, x, y, z, level, pickup, null);
        this.entityData.set(TYPE, type);
        this.pickup = Pickup.DISALLOWED;
        this.setBaseDamage(2.1 * multiplier(type));
    }

    /** O {@code getDamage} por tipo. */
    private static double multiplier(int type) {
        return switch (type) {
            case 3 -> 1.5;
            case 4, 5 -> 0.8;
            default -> 1.0;
        };
    }

    public int arrowType() {
        return this.entityData.get(TYPE);
    }

    public int inGroundTicks() {
        return this.inGroundTime;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TYPE, 0);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(TCItems.PRIMAL_ARROWS.get(TYPES[Math.clamp(this.arrowType(), 0, 5)]));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.isInGround() && this.inGroundTime >= 100) this.discard();
    }

    /** O dano de cada tipo (o {@code DamageSourceIndirectThaumcraftEntity} do ar, do fogo e da ordem). */
    public DamageSource damageSource(@Nullable Entity owner) {
        Entity cause = owner != null ? owner : this;
        ResourceKey<DamageType> key = switch (this.arrowType()) {
            case 0 -> AIR;
            case 1 -> FIRE;
            case 4 -> ORDER;
            default -> null;
        };
        if (key == null) return this.damageSources().arrow(this, cause);
        return new DamageSource(this.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key), this, cause);
    }

    /** O {@code inflictDamage}: o fogo a mais e os efeitos de cada tipo. */
    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        switch (this.arrowType()) {
            case 1 -> {
                if (!(target instanceof EnderMan)) target.igniteForSeconds(this.isOnFire() ? 10.0f : 5.0f);
            }
            case 2 -> target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 4));
            case 4 -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 4));
            case 5 -> target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100));
            default -> {
            }
        }
    }

    /** A de terra empurra um a mais (o {@code knockbackStrength + 1} do {@code bowShot}). */
    @Override
    protected void doKnockback(LivingEntity target, DamageSource source) {
        super.doKnockback(target, source);
        if (this.arrowType() != 3) return;
        Vec3 m = this.getDeltaMovement();
        double f3 = Math.sqrt(m.x * m.x + m.z * m.z);
        if (f3 > 0.0) target.push(m.x * 0.6 / f3, 0.1, m.z * 0.6 / f3);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("type", (byte) this.arrowType());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(TYPE, (int) input.getByteOr("type", (byte) 0));
    }
}
