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
 * @param onCentral   quando a receita não faz uma coisa nova e sim grava uma marca na do meio (o {@code Object[]}
 *                    de saída do original, como os óculos e as máscaras do elmo de fortaleza): o que gravar
 */
public record InfusionRecipe(String research, ItemStack result, int instability, AspectList essentia,
                             Ingredient central, List<Ingredient> components,
                             java.util.function.@org.jetbrains.annotations.Nullable UnaryOperator<ItemStack> onCentral) {

    public InfusionRecipe(String research, ItemStack result, int instability, AspectList essentia,
                          Ingredient central, List<Ingredient> components) {
        this(research, result, instability, essentia, central, components, null);
    }

    /** Uma receita que grava uma marca na coisa do meio em vez de fazer outra. */
    public static InfusionRecipe onCentral(String research, int instability, AspectList essentia, Ingredient central,
                                           List<Ingredient> components, java.util.function.UnaryOperator<ItemStack> mark) {
        return new InfusionRecipe(research, ItemStack.EMPTY, instability, essentia, central, components, mark);
    }

    /** O que sai desta receita com aquela coisa no meio. */
    public ItemStack resultFor(ItemStack middle) {
        return this.onCentral == null ? this.result.copy() : this.onCentral.apply(middle.copyWithCount(1));
    }

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
