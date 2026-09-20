package net.thaumcraft.maleficium;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.registry.TCItems;

/**
 * O punhal oco: o {@code ItemHollowDagger} do Tainted Magic.
 *
 * <p>Feito de uma haste de osso, ele quase não fere — mas tira sangue. Quem golpeia com ele levando um frasco vazio
 * no inventário vê o frasco encher de sangue carmesim, que é da mesma natureza do que o Culto Carmesim usa.
 */
public class HollowDaggerItem extends Item {
    public HollowDaggerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (attacker.level().isClientSide() || !(attacker instanceof net.minecraft.world.entity.player.Player player)) return;
        // o primeiro frasco vazio do inventário vira sangue
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack found = inventory.getItem(slot);
            if (!found.is(TCItems.PHIAL)) continue;
            found.shrink(1);
            ItemStack blood = new ItemStack(MaleficiumItems.CRIMSON_BLOOD);
            if (!player.getInventory().add(blood)) player.drop(blood, false);
            return;
        }
    }
}
