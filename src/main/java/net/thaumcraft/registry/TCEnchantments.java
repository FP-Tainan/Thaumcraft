package net.thaumcraft.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.thaumcraft.Thaumcraft;

/** Os encantamentos do Thaumcraft 4.2.3.5 (os arquivos ficam em {@code data/thaumcraft/enchantment}). */
public final class TCEnchantments {
    /** A {@code EnchantmentHaste}: botas (e o arreio) mais rápidas. */
    public static final ResourceKey<Enchantment> HASTE = ResourceKey.create(Registries.ENCHANTMENT, Thaumcraft.id("haste"));
    /** A {@code EnchantmentRepair}: as coisas do Thaumcraft se consertam com vis. */
    public static final ResourceKey<Enchantment> REPAIR = ResourceKey.create(Registries.ENCHANTMENT, Thaumcraft.id("repair"));

    private TCEnchantments() {
    }

    /** O nível deste encantamento na coisa. */
    public static int level(Level level, ResourceKey<Enchantment> key, ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel((Holder<Enchantment>) holder, stack)).orElse(0);
    }
}
