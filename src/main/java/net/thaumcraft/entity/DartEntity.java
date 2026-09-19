package net.thaumcraft.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.thaumcraft.registry.TCEntities;

/**
 * O dardo do golem: o {@code EntityDart} da 4.2.3.5 — uma flecha que sai do lança-dardos do golem com um sopro de
 * fumaça, e que ninguém pode catar do chão.
 */
public class DartEntity extends AbstractArrow {
    private boolean first = true;

    public DartEntity(EntityType<? extends DartEntity> type, Level level) {
        super(type, level);
    }

    /** O construtor de mira do original: da altura dos olhos do golem, um pouco à frente, rumo aos olhos do alvo. */
    public DartEntity(Level level, LivingEntity shooter, LivingEntity target, float speed, float inaccuracy) {
        super(TCEntities.DART, level);
        this.setOwner(shooter);
        this.pickup = Pickup.DISALLOWED;
        double y = shooter.getY() + shooter.getEyeHeight() - 0.1f;
        double dx = target.getX() - shooter.getX();
        double dy = target.getY() + target.getEyeHeight() - 0.7f - y;
        double dz = target.getZ() - shooter.getZ();
        double d = Math.sqrt(dx * dx + dz * dz);
        if (d >= 1.0e-7) {
            float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
            float pitch = (float) -(Math.atan2(dy, d) * 180.0 / Math.PI);
            this.snapTo(shooter.getX() + dx / d / 5.0, y, shooter.getZ() + dz / d / 5.0, yaw, pitch);
            float f = (float) d * 0.2f;
            this.shoot(dx, dy + f, dz, speed, inaccuracy);
        }
    }

    @Override
    public void tick() {
        if (this.first && this.level().isClientSide()) {
            this.first = false;
            var m = this.getDeltaMovement();
            for (int a = 0; a < 5; a++) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX() - m.x / 1.5, this.getY() - m.y / 1.5, this.getZ() - m.z / 1.5,
                        m.x / 9.0 + this.random.nextGaussian() * 0.01, m.y / 9.0 + this.random.nextGaussian() * 0.01,
                        m.z / 9.0 + this.random.nextGaussian() * 0.01);
            }
        }
        super.tick();
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
