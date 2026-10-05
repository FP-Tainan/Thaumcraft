package net.thaumcraft.occulta.infusion;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.thaumcraft.occulta.OccultaItems;

/**
 * A <b>Rocha</b> a voar: o {@code EntityWitchProjectile} do Witchery com a <i>damageValue</i> da rocha.
 *
 * <p>É o bloco que a {@link OverworldInfusion Infusão do Mundo} arrancou de uma parede, a caminho de quem
 * estiver à frente. <b>Seis de dano</b> onde bater, e nada mais: não pega fogo, não envenena, não empurra.
 * Uma pedra atirada muito forte.
 *
 * <p>No original ela é uma das vinte e tantas coisas que a mesma classe de projétil sabe ser, escolhidas por
 * um número guardado no bicho. Aqui cada uma é a sua classe, e esta é a mais curta de todas.
 */
public class RockEntity extends ThrowableItemProjectile {
    /** Quanto ela faz a quem acerta. */
    public static final float DANO = 6.0f;

    /** E quantas fagulhas ela larga ao bater. */
    public static final int FAGULHAS = 8;

    public RockEntity(EntityType<? extends RockEntity> tipo, Level level) {
        super(tipo, level);
    }

    @Override
    protected Item getDefaultItem() {
        return OccultaItems.ROCK;
    }

    @Override
    protected void onHitEntity(EntityHitResult onde) {
        super.onHitEntity(onde);
        if (!(this.level() instanceof ServerLevel level)) return;
        onde.getEntity().hurtServer(level,
                this.damageSources().thrown(this, this.getOwner()), DANO);
    }

    /** Onde ela cai, cai com fagulhas de estouro — e sem o barulho de vidro do original. */
    @Override
    protected void onHit(net.minecraft.world.phys.HitResult onde) {
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(),
                    FAGULHAS, 0.5, 0.5, 0.5, 0.0);
        }
        super.onHit(onde);
        this.discard();
    }
}
