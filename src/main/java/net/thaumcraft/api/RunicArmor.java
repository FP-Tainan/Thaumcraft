package net.thaumcraft.api;

import net.minecraft.world.item.ItemStack;

/**
 * O {@code IRunicArmor} da 4.2.3.5: o que, vestido, soma cargas ao escudo rúnico de quem veste.
 */
public interface RunicArmor {
    int runicCharge(ItemStack stack);
}
