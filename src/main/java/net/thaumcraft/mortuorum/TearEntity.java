package net.thaumcraft.mortuorum;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A lágrima de Isaac: o {@code EntityTear} e o {@code EntityTearBlood} do Necromancy.
 *
 * <p>A de água tira três e espirra; a de sangue tira seis e se desfaz em pó vermelho. As duas saem da altura dos
 * olhos de quem chora e vão ao terço da altura de quem leva, como no original.
 */
public class TearEntity extends ThrowableItemProjectile {
    public TearEntity(EntityType<? extends TearEntity> type, Level level) {
        super(type, level);
    }

    public TearEntity(EntityType<? extends TearEntity> type, Level level, LivingEntity quemChora) {
        super(type, level);
        this.setOwner(quemChora);
        this.setPos(quemChora.getX(), quemChora.getEyeY() - 0.1, quemChora.getZ());
    }

    public boolean isBlood() {
        return this.getType() == MortuorumEntities.TEAR_BLOOD;
    }

    public float damage() {
        return this.isBlood() ? 6.0f : 3.0f;
    }

    /** O tiro do original: mirado no terço da altura de quem leva. */
    public void aimAt(LivingEntity alvo) {
        double dx = alvo.getX() - this.getX();
        double dy = alvo.getBoundingBox().minY + alvo.getBbHeight() / 3.0f - this.getY();
        double dz = alvo.getZ() - this.getZ();
        this.shoot(dx, dy, dz, 1.0f, 2.0f);
    }

    @Override
    protected void onHit(HitResult resultado) {
        super.onHit(resultado);
        if (!(this.level() instanceof ServerLevel server)) return;
        ParticleOptions pó = this.isBlood()
                ? new net.minecraft.core.particles.DustParticleOptions(0xFF0000, 1.0f)
                : ParticleTypes.SPLASH;
        server.sendParticles(pó, this.getX(), this.getY(), this.getZ(), 8, 0.0, 0.0, 0.0, 0.0);
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult resultado) {
        super.onHitEntity(resultado);
        if (!(this.level() instanceof ServerLevel server)) return;
        resultado.getEntity().hurtServer(server, this.damageSources().thrown(this, this.getOwner()), this.damage());
    }

    /** A lágrima não é item nenhum; quem a desenha é o desenhista dela, com a figura do original. */
    @Override
    protected Item getDefaultItem() {
        return Items.AIR;
    }
}
