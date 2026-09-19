package net.thaumcraft.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * O arco de osso: o {@code ItemBowBone} da 4.2.3.5. Arma na metade do tempo (dez tiques), solta sozinho depois de dezoito,
 * atira mais longe (a força vezes dois e meio, não duas) e a flecha sai meio ponto mais forte.
 */
public class BoneBowItem extends BowItem {
    public BoneBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        int ticks = this.getUseDuration(stack, entity) - count;
        if (ticks > 18) entity.releaseUsingItem();
    }

    /** A força pelo tempo armado, na conta do original: dez tiques e não vinte. */
    public static float power(int ticks) {
        float f = ticks / 10.0f;
        f = (f * f + f * 2.0f) / 3.0f;
        return Math.min(f, 1.0f);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (!(entity instanceof Player player)) return false;
        ItemStack projectile = player.getProjectile(stack);
        if (projectile.isEmpty()) return false;
        float pow = power(this.getUseDuration(stack, entity) - remaining);
        if (pow < 0.1) return false;
        List<ItemStack> fired = draw(stack, projectile, player);
        if (level instanceof ServerLevel server && !fired.isEmpty()) {
            this.shoot(server, player, player.getUsedItemHand(), stack, fired, pow * 3.75f, 1.0f, false, null);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f,
                1.0f / (level.getRandom().nextFloat() * 0.4f + 1.2f) + pow * 0.5f);
        player.awardStat(Stats.ITEM_USED.get(this));
        return true;
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean crit) {
        Projectile projectile = super.createProjectile(level, shooter, weapon, ammo, crit);
        if (projectile instanceof AbstractArrow arrow) arrow.setBaseDamage(((net.thaumcraft.mixin.AbstractArrowAccessor) arrow).thaumcraft$baseDamage() + 0.5);
        return projectile;
    }
}
