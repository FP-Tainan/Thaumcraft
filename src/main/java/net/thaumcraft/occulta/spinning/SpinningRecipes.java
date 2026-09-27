package net.thaumcraft.occulta.spinning;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A tabela da Roca: o {@code SpinningRecipes} do Witchery.
 *
 * <p>Uma receita de roca tem uma <b>fibra</b> — o que se fia, e que entra em quantidade — e até <b>três
 * temperos</b>, que entram um a um e não têm ordem. Sem a fibra certa não se fia nada; com ela e os temperos
 * certos, sai o fio.
 */
public final class SpinningRecipes {
    private static final List<Recipe> ALL = new ArrayList<>();

    private SpinningRecipes() {
    }

    /**
     * Uma receita de roca.
     *
     * @param result    o que sai
     * @param count     e quantos
     * @param fibre     a fibra que se fia
     * @param fibreCount quantas dela de cada vez
     * @param modifiers os temperos, sem ordem
     */
    public record Recipe(Item result, int count, Item fibre, int fibreCount, List<Item> modifiers) {
        /** O que sai, montado na hora: montar pilhas no arranque do mod não se pode. */
        public ItemStack output() {
            return new ItemStack(this.result, this.count);
        }

        /** Se aquela fibra e aqueles temperos são esta receita. */
        public boolean matches(ItemStack fibra, List<ItemStack> temperos) {
            if (!fibra.is(this.fibre) || fibra.getCount() < this.fibreCount) return false;

            List<Item> faltam = new ArrayList<>(this.modifiers);
            for (ItemStack tempero : temperos) {
                if (tempero.isEmpty()) continue;
                if (!faltam.remove(tempero.getItem())) return false;
            }
            return faltam.isEmpty();
        }
    }

    public static Recipe add(Item result, int count, Item fibre, int fibreCount, Item... modifiers) {
        Recipe receita = new Recipe(result, count, fibre, fibreCount, List.of(modifiers));
        ALL.add(receita);
        return receita;
    }

    /** A receita daquela fibra com aqueles temperos, ou nada. */
    public static @Nullable Recipe find(ItemStack fibra, List<ItemStack> temperos) {
        for (Recipe receita : ALL) {
            if (receita.matches(fibra, temperos)) return receita;
        }
        return null;
    }

    /** A receita que faz aquilo, para o livro. */
    public static @Nullable Recipe of(Item saída) {
        for (Recipe receita : ALL) {
            if (receita.result == saída) return receita;
        }
        return null;
    }

    public static List<Recipe> all() {
        return List.copyOf(ALL);
    }

    /**
     * As receitas da roca, as do original.
     *
     * <p>São quatro lá; duas aqui. As outras duas fiam o <b>algodão do sonho</b> — o Algodão Sonhador e o
     * Algodão Perturbado —, que só nasce no Mundo dos Sonhos, ao pé do Espírito Fluente. Elas entram com ele.
     */
    public static void register() {
        // a teia, que é linha fiada grossa: oito de linha, e nenhum tempero
        add(net.minecraft.world.item.Items.COBWEB, 1, net.minecraft.world.item.Items.STRING, 8);

        // e o Fio Dourado, que sai de um fardo de feno com um Sopro de Magia
        add(net.thaumcraft.occulta.OccultaItems.GOLDEN_THREAD, 3,
                net.minecraft.world.item.Items.HAY_BLOCK, 1,
                net.thaumcraft.occulta.OccultaItems.WHIFF_OF_MAGIC);
    }
}
