package net.thaumcraft.crafting;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.research.ResearchManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Uma receita de infusão de encantamento: o {@code InfusionEnchantmentRecipe} da 4.2.3.5. Em vez de fazer uma coisa
 * nova, sobe em um o nível de um encantamento da coisa do meio — se ela aceita o encantamento, se ainda não está no
 * máximo e se os encantamentos que já tem convivem com ele. Cobra também experiência de quem está perto.
 */
public record InfusionEnchantmentRecipe(String research, ResourceKey<Enchantment> enchantment, int instability,
                                        AspectList aspects, List<Ingredient> components) {

    public @Nullable Holder<Enchantment> holder(Level level) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(this.enchantment).orElse(null);
    }

    /** O {@code matches}: a pesquisa, a coisa do meio e exatamente os ingredientes, um por pedestal. */
    public boolean matches(List<ItemStack> input, ItemStack central, Level level, Player player) {
        if (!this.research.isEmpty() && !ResearchManager.knows(player, this.research)) return false;
        Holder<Enchantment> holder = this.holder(level);
        if (holder == null || !holder.value().canEnchant(central) || central.getMaxStackSize() != 1 || !central.isDamageableItem()) return false;
        ItemEnchantments current = EnchantmentHelper.getEnchantmentsForCrafting(central);
        for (var entry : current.entrySet()) {
            Holder<Enchantment> other = entry.getKey();
            if (other.equals(holder)) {
                if (entry.getIntValue() >= holder.value().getMaxLevel()) return false;
            } else if (!Enchantment.areCompatible(holder, other)) {
                return false;
            }
        }
        List<ItemStack> left = new ArrayList<>();
        for (ItemStack is : input) left.add(is.copy());
        for (Ingredient comp : this.components) {
            boolean found = false;
            for (int a = 0; a < left.size(); a++) {
                if (comp.test(left.get(a))) {
                    left.remove(a);
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return left.isEmpty();
    }

    /** O nível que a coisa já tem deste encantamento. */
    public int level(ItemStack central, Level level) {
        Holder<Enchantment> holder = this.holder(level);
        return holder == null ? 0 : EnchantmentHelper.getItemEnchantmentLevel(holder, central);
    }

    /** O {@code calcXP}: um terço do custo mínimo do nível 1, vezes um mais o nível que já tem. */
    public int xp(ItemStack central, Level level) {
        Holder<Enchantment> holder = this.holder(level);
        int base = holder == null ? 1 : Math.max(1, holder.value().getMinCost(1) / 3);
        return base * (1 + this.level(central, level));
    }

    /** O {@code calcInstability}: metade da soma dos níveis que a coisa tem, mais a da receita. */
    public int instabilityFor(ItemStack central) {
        int i = 0;
        for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(central).entrySet()) i += entry.getIntValue();
        return i / 2 + this.instability;
    }

    /** A essência: a da receita, mais tanto quanto o nível que já tem, mais um décimo por nível de outros encantamentos. */
    public AspectList essentiaFor(ItemStack central, Level level) {
        Holder<Enchantment> holder = this.holder(level);
        float mod = this.level(central, level);
        for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(central).entrySet()) {
            if (!entry.getKey().equals(holder)) mod += entry.getIntValue() * 0.1f;
        }
        AspectList cost = this.aspects.copy();
        for (Aspect as : cost.getAspects()) cost.add(as, (int) (cost.getAmount(as) * mod));
        return cost;
    }

    /** A coisa do meio com o encantamento um nível acima. */
    public ItemStack resultFor(ItemStack central, Level level) {
        ItemStack out = central.copy();
        Holder<Enchantment> holder = this.holder(level);
        if (holder != null) EnchantmentHelper.updateEnchantments(out, e -> e.set(holder, e.getLevel(holder) + 1));
        return out;
    }

    public static @Nullable InfusionEnchantmentRecipe find(List<ItemStack> input, ItemStack central, Level level, Player player) {
        for (InfusionEnchantmentRecipe recipe : InfusionEnchantments.ALL) {
            if (recipe.matches(input, central, level, player)) return recipe;
        }
        return null;
    }
}
