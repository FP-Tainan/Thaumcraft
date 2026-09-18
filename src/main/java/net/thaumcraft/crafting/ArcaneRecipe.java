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
 * <p>Algumas do original não têm forma: valem os ingredientes, tanto faz onde estão na grade. A válvula é
 * uma delas — é só um tubo e uma alavanca.
 *
 * @param research a pesquisa que a destranca
 * @param result   o que sai
 * @param pattern  as nove casas, de cima à esquerda para baixo à direita; casa vazia é {@code null}.
 *                 Sem forma, é só a lista do que precisa, sem casa vazia
 * @param cost     o vis que a varinha paga
 * @param shaped   se a posição na grade importa
 */
public record ArcaneRecipe(String research, ItemStack result, List<Ingredient> pattern, AspectList cost,
                           boolean shaped) {

    public ArcaneRecipe(String research, ItemStack result, List<Ingredient> pattern, AspectList cost) {
        this(research, result, pattern, cost, true);
    }

    /** A mesma receita, mas sem forma: valem os ingredientes onde quer que estejam. */
    public static ArcaneRecipe loose(String research, ItemStack result, List<Ingredient> needs, AspectList cost) {
        return new ArcaneRecipe(research, result, needs, cost, false);
    }

    /** Esta grade fecha esta receita? */
    public boolean matches(List<ItemStack> grid) {
        if (grid.size() != 9) return false;
        return this.shaped ? this.matchesShaped(grid) : this.matchesLoose(grid);
    }

    private boolean matchesShaped(List<ItemStack> grid) {
        if (this.pattern.size() != 9) return false;
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

    /**
     * Sem forma: cada coisa na grade tem de casar com um pedido ainda não atendido, e no fim não pode
     * sobrar pedido nenhum.
     */
    private boolean matchesLoose(List<ItemStack> grid) {
        java.util.List<Ingredient> pending = new java.util.ArrayList<>(this.pattern);
        for (ItemStack found : grid) {
            if (found.isEmpty()) continue;
            int at = -1;
            for (int i = 0; i < pending.size(); i++) {
                if (pending.get(i) != null && pending.get(i).test(found)) {
                    at = i;
                    break;
                }
            }
            if (at < 0) return false;
            pending.remove(at);
        }
        return pending.isEmpty();
    }
}
