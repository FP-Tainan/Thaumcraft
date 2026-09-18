package net.thaumcraft.api.baubles;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * O {@code IBauble} do Baubles 1.0.1.10: um amuleto, anel ou cinto. O mod de 2014 dependia do Baubles, que não existe
 * no Fabric 26.2; as casas dele vieram para dentro deste porte, do mesmo jeito.
 */
public interface BaubleItem {
    BaubleType baubleType(ItemStack stack);

    /** A cada tique, dos dois lados, enquanto a peça está vestida. */
    default void onWornTick(ItemStack stack, LivingEntity wearer) {
    }

    default void onEquipped(ItemStack stack, LivingEntity wearer) {
    }

    default void onUnequipped(ItemStack stack, LivingEntity wearer) {
    }

    default boolean canEquip(ItemStack stack, LivingEntity wearer) {
        return true;
    }

    default boolean canUnequip(ItemStack stack, LivingEntity wearer) {
        return true;
    }
}
