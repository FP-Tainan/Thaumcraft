package net.thaumcraft.entity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.api.TaintedMob;
import net.thaumcraft.entity.taint.TaintSplosion;
import net.thaumcraft.registry.TCEffects;
import net.thaumcraft.registry.TCEntities;
import net.thaumcraft.registry.TCItems;

/**
 * A garrafa de mácula arremessada: o {@code EntityBottleTaint} da 4.2.3.5. Sai devagar (0,5) e mirada um tanto para
 * cima, cai mais (0,05) e, onde quebra, dá o fluxo da mácula (5 s) a quem estiver a cinco blocos e macula o chão em
 * volta como o creeper maculado.
 */
public class BottleTaintEntity extends ThrowableItemProjectile {
    public BottleTaintEntity(EntityType<? extends BottleTaintEntity> type, Level level) {
        super(type, level);
    }

    public BottleTaintEntity(Level level, LivingEntity thrower, ItemStack stack) {
        super(TCEntities.BOTTLE_TAINT, level);
        this.setOwner(thrower);
        this.setItem(stack.copyWithCount(1));
        Throw.once(this, thrower, 0.5f, -20.0f);
    }

    @Override
    protected Item getDefaultItem() {
        return TCItems.BOTTLE_TAINT;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(this.level() instanceof ServerLevel level)) return;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0))) {
            if (!(e instanceof TaintedMob) && !e.isInvertedHealAndHarm()) e.addEffect(new MobEffectInstance(TCEffects.FLUX_TAINT, 100, 0));
        }
        TaintSplosion.taintAround(level, this, 10, 5.0f);
        level.broadcastEntityEvent(this, (byte) 3);
        this.discard();
    }

    /** O evento 3, do lado de quem vê: o estouro roxo e o vidro quebrando (o {@code bottleTaintBreak}). */
    @Override
    public void handleEntityEvent(byte id) {
        if (id != 3) {
            super.handleEntityEvent(id);
            return;
        }
        for (int a = 0; a < 200; a++) TaintSplosion.effect.accept(this);
        var random = this.level().getRandom();
        for (int k = 0; k < 8; k++) {
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem().getItem()), this.getX(), this.getY(), this.getZ(),
                    random.nextGaussian() * 0.15, random.nextDouble() * 0.2, random.nextGaussian() * 0.15);
        }
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 1.0f,
                random.nextFloat() * 0.1f + 0.9f, false);
    }

}
