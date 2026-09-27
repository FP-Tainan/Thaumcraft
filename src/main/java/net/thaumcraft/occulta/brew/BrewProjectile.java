package net.thaumcraft.occulta.brew;

import net.minecraft.core.particles.ColorParticleOption;
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
 * O frasco de cozimento atirado: o {@code EntityBrew} do Witchery.
 *
 * <p>É o frasco de sempre, com o que estava no caldeirão dentro dele. Onde bate, arrebenta — e o que apanha o
 * cozimento é o que o jeito de espalhar disser ({@link BrewDispersal}).
 *
 * <p><b>Do original fica de fora, declarado:</b> o modo de feitiço ({@code isSpell}), em que o frasco voa reto e
 * sem peso porque quem o atira é uma varinha do Witchery, e não uma mão. As varinhas não estão portadas.
 */
public class BrewProjectile extends AbstractThrownPotion {
    public BrewProjectile(EntityType<? extends BrewProjectile> type, Level level) {
        super(type, level);
    }

    public BrewProjectile(Level level, LivingEntity quemAtirou, ItemStack frasco) {
        super(OccultaEntitiesHook.brewProjectile(), level, quemAtirou, frasco);
    }

    @Override
    protected Item getDefaultItem() {
        return OccultaItems.BREW;
    }

    /** A cor do que vai dentro, para as gotas que saem no estouro. */
    public int color() {
        return BrewItem.color(this.getItem());
    }

    @Override
    protected void onHitAsPotion(ServerLevel level, ItemStack frasco, HitResult onde) {
        LivingEntity quemAtirou = this.getOwner() instanceof LivingEntity vivo ? vivo : null;
        boolean espalhou = Brew.impact(level, BrewItem.contents(frasco), onde, quemAtirou);
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SPLASH_POTION_BREAK,
                SoundSource.NEUTRAL, 1.0f, level.getRandom().nextFloat() * 0.1f + 0.9f);
        if (!espalhou) return;
        level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT,
                        0xFF000000 | this.color()), this.getX(), this.getY(), this.getZ(),
                24, 0.5, 0.5, 0.5, 0.0);
    }

    /** O tipo da criatura vive no {@code OccultaEntities}; este atalho evita a volta pelo pacote de cima. */
    static final class OccultaEntitiesHook {
        private OccultaEntitiesHook() {
        }

        static EntityType<BrewProjectile> brewProjectile() {
            return net.thaumcraft.occulta.OccultaEntities.BREW;
        }
    }
}
