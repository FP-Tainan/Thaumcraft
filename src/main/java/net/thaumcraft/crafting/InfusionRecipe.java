package net.thaumcraft.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.thaumcraft.api.aspects.AspectList;

import java.util.ArrayList;
import java.util.List;

/**
 * Uma receita de infusão: o que vai no pedestal do meio, o que vai nos de fora, e o que a magia cobra.
 *
 * <p>É o {@code InfusionRecipe} do Thaumcraft 4.2.3.5. Diferente da bancada, aqui a forma não importa —
 * os pedestais de fora podem estar em qualquer lugar ao alcance da matriz, em qualquer ordem. O que
 * importa é o conjunto: a coisa do meio tem de ser aquela, e os pedestais de fora têm de ter exatamente
 * aquelas coisas, uma para cada.
 *
 * <p>A <strong>instabilidade</strong> é o que faz a infusão ser perigosa. Cada receita traz a sua, e ela
 * se soma à falta de simetria dos pedestais em volta — quanto mais torta a construção, mais a magia
 * escapa pelas beiradas.
 *
 * @param research    a pesquisa que a destranca
 * @param result      o que sai
 * @param instability o quanto esta receita é arriscada por natureza
 * @param essentia    a essência que a matriz vai sugar dos jarros em volta
 * @param central     o que vai no pedestal do meio
 * @param components  o que vai nos pedestais de fora, um por pedestal
 */
public record InfusionRecipe(String research, ItemStack result, int instability, AspectList essentia,
                             Ingredient central, List<Ingredient> components) {

    /**
     * Este arranjo fecha esta receita?
     *
     * @param middle o que está no pedestal do meio
     * @param around o que está nos pedestais de fora, em qualquer ordem
     */
    public boolean matches(ItemStack middle, List<ItemStack> around) {
        if (middle.isEmpty() || !this.central.test(middle)) return false;
        if (around.size() != this.components.size()) return false;

        // cada pedido tem de achar um pedestal ainda não usado; a ordem não importa
        List<ItemStack> left = new ArrayList<>(around);
        for (Ingredient wanted : this.components) {
            boolean found = false;
            for (int slot = 0; slot < left.size(); slot++) {
                if (!wanted.test(left.get(slot))) continue;
                left.remove(slot);
                found = true;
                break;
            }
            if (!found) return false;
        }
        return left.isEmpty();
    }
}
