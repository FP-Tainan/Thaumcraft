package net.thaumcraft.crafting;

import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;

/**
 * Uma receita de crisol: o que tem de estar dissolvido na água e o que se joga por último.
 *
 * <p>É o {@code CrucibleRecipe} do original. A água precisa ter pelo menos o que a receita pede de cada
 * aspecto; o que sobrar fica lá. O catalisador é a coisa que se joga para fechar a conta, e ela some.
 *
 * @param research a pesquisa que a destranca, como no original
 * @param result   o que sai
 * @param catalyst o que se joga para fechar
 * @param cost     o que a água precisa ter
 */
public record CrucibleRecipe(String research, ItemStack result, net.minecraft.world.item.Item catalyst,
                             AspectList cost) {

    /** Esta água e esta coisa fecham esta receita? */
    public boolean matches(AspectList inside, ItemStack thrown) {
        if (!this.catalystMatches(thrown)) return false;
        for (Aspect aspect : this.cost.getAspects()) {
            if (inside.getAmount(aspect) < this.cost.getAmount(aspect)) return false;
        }
        return true;
    }

    /**
     * O {@code hash} do original: o número que identifica a receita (o taumatório guarda as escolhidas por ele). Sai do
     * que ela é — pesquisa, resultado, catalisador e custo — e não da ordem na tabela.
     */
    public int hash() {
        StringBuilder key = new StringBuilder(this.research).append('|')
                .append(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(this.result.getItem())).append('x').append(this.result.getCount())
                .append('|').append(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(this.catalyst));
        for (Aspect aspect : this.cost.getAspectsSorted()) key.append('|').append(aspect.tag()).append(this.cost.getAmount(aspect));
        return key.toString().hashCode();
    }

    /** O {@code getCrucibleRecipeFromHash}. */
    @org.jetbrains.annotations.Nullable
    public static CrucibleRecipe byHash(int hash) {
        for (CrucibleRecipe recipe : CrucibleRecipes.ALL) {
            if (recipe.hash() == hash) return recipe;
        }
        return null;
    }

    /** O {@code catalystMatches}. */
    public boolean catalystMatches(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(this.catalyst)) return false;
        // o frasco como catalisador é o frasco cheio (o itemEssence 1 do original), de qualquer essência
        return this.catalyst != net.thaumcraft.registry.TCItems.PHIAL || net.thaumcraft.item.PhialItem.aspectOf(stack) != null;
    }

    /** O que sobra na água depois de a receita cobrar o que lhe é devido. */
    public AspectList removeFrom(AspectList inside) {
        AspectList left = inside.copy();
        for (Aspect aspect : this.cost.getAspects()) left.reduce(aspect, this.cost.getAmount(aspect));
        return left;
    }
}
