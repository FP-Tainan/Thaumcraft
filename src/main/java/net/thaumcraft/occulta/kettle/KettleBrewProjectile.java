package net.thaumcraft.occulta.kettle;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.occulta.OccultaItems;

/**
 * O frasco do pote a voar: o {@code EntityWitchProjectile} do Witchery.
 *
 * <p>Onde ele bate, arrebenta e faz o que o seu {@link KettleBrews.Kind} manda. <b>Não fazendo nada</b> — um
 * frasco de espinhos atirado contra pedra, por exemplo —, ele volta ao chão em item, como no original.
 */
public class KettleBrewProjectile extends AbstractThrownPotion {
    public KettleBrewProjectile(EntityType<? extends KettleBrewProjectile> type, Level level) {
        super(type, level);
    }

    public KettleBrewProjectile(ServerLevel level, LivingEntity quemAtirou, ItemStack frasco) {
        super(net.thaumcraft.occulta.OccultaEntities.KETTLE_BREW, level, quemAtirou, frasco);
    }

    @Override
    protected Item getDefaultItem() {
        return OccultaItems.BREW_OF_VINES;
    }

    @Override
    protected void onHitAsPotion(ServerLevel level, ItemStack frasco, HitResult onde) {
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SPLASH_POTION_BREAK,
                SoundSource.NEUTRAL, 1.0f, level.getRandom().nextFloat() * 0.1f + 0.9f);

        KettleBrews.Kind qual = KettleBrewItem.kindOf(frasco);
        LivingEntity quemAtirou = this.getOwner() instanceof LivingEntity vivo ? vivo : null;
        boolean fez = qual != null && qual.impact(level, onde, quemAtirou);
        if (!fez) {
            // não pegou: o frasco cai de volta, para não se perder
            var caiu = new net.minecraft.world.entity.item.ItemEntity(level, onde.getLocation().x,
                    onde.getLocation().y, onde.getLocation().z, frasco.copyWithCount(1));
            level.addFreshEntity(caiu);
            return;
        }
        level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY(), this.getZ(),
                16, 0.5, 0.5, 0.5, 0.0);
    }
}
