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
        if (!thrown.is(this.catalyst)) return false;
        for (Aspect aspect : this.cost.getAspects()) {
            if (inside.getAmount(aspect) < this.cost.getAmount(aspect)) return false;
        }
        return true;
    }

    /** O que sobra na água depois de a receita cobrar o que lhe é devido. */
    public AspectList removeFrom(AspectList inside) {
        AspectList left = inside.copy();
        for (Aspect aspect : this.cost.getAspects()) left.reduce(aspect, this.cost.getAmount(aspect));
        return left;
    }
}
