package net.thaumcraft.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.thaumcraft.api.aspects.AspectList;

import java.util.List;

/**
 * Uma receita de bancada arcana: a forma na grade de três por três mais o vis que ela cobra da varinha.
 *
 * <p>É o {@code ShapedArcaneRecipe} do original. A diferença para uma receita de bancada comum é justamente
 * o custo: sem varinha com vis bastante, nada sai dali.
 *
 * @param research a pesquisa que a destranca
 * @param result   o que sai
 * @param pattern  as nove casas, de cima à esquerda para baixo à direita; casa vazia é {@code null}
 * @param cost     o vis que a varinha paga
 */
public record ArcaneRecipe(String research, ItemStack result, List<Ingredient> pattern, AspectList cost) {

    /** Esta grade fecha esta receita? */
    public boolean matches(List<ItemStack> grid) {
        if (grid.size() != 9 || this.pattern.size() != 9) return false;
        for (int slot = 0; slot < 9; slot++) {
            Ingredient wanted = this.pattern.get(slot);
            ItemStack found = grid.get(slot);
            if (wanted == null) {
                if (!found.isEmpty()) return false;
            } else if (found.isEmpty() || !wanted.test(found)) {
                return false;
            }
        }
        return true;
    }
}
