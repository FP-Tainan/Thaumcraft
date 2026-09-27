package net.thaumcraft.occulta.kettle;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A tabela do Caldeirão de Pote: o {@code KettleRecipes} do Witchery.
 *
 * <p>Uma receita são <b>seis coisas</b>, sem ordem nenhuma, e o que sai delas. Ao lado disso ela traz a <b>cor</b>
 * do líquido — que é o que se vê no pote enquanto ele cozinha —, o <b>poder de altar</b> que pede, e o mundo em
 * que ela pega, se for de um mundo só.
 *
 * <p>O casamento é o do original, e tem dois feitios: <b>inteiro</b>, quando as seis casas estão cheias e a lista
 * tem de bater exatamente; e <b>pela metade</b>, enquanto se enche o pote — aí basta que o que já lá está caiba
 * na receita, e é assim que o líquido ganha cor antes de a última coisa entrar.
 */
public final class KettleRecipes {
    private static final List<Recipe> ALL = new ArrayList<>();

    private KettleRecipes() {
    }

    /**
     * Uma receita do pote.
     *
     * @param result  o que sai
     * @param count   e quantos
     * @param inputs  as coisas que entram, sem ordem
     * @param color   a cor do líquido enquanto ela cozinha
     * @param power   o poder de altar que ela pede, ou zero
     * @param level   o mundo em que ela pega, ou nada se pega em qualquer um
     */
    public record Recipe(Item result, int count, List<Item> inputs, int color, float power,
                         @Nullable ResourceKey<Level> level) {
        /**
         * O que sai, montado <b>na hora</b>.
         *
         * <p>Montar pilhas de item no arranque do mod não se pode: os componentes ainda não estão presos, e o
         * jogo cai com um "Components not bound yet". Por isso a tabela guarda o item, e não a pilha.
         */
        public ItemStack output() {
            return new ItemStack(this.result, this.count);
        }

        /** Se o que está no pote é esta receita. */
        public boolean matches(List<ItemStack> noPote, boolean pelaMetade, Level onde) {
            if (this.level != null && onde.dimension() != this.level) return false;

            List<ItemStack> postos = new ArrayList<>();
            for (ItemStack item : noPote) {
                if (!item.isEmpty()) postos.add(item);
            }
            if (!pelaMetade && postos.size() != this.inputs.size()) return false;
            if (postos.size() > this.inputs.size()) return false;

            List<Item> faltam = new ArrayList<>(this.inputs);
            for (ItemStack item : postos) {
                if (!faltam.remove(item.getItem())) return false;
            }
            return faltam.isEmpty() || (pelaMetade && faltam.size() < this.inputs.size());
        }
    }

    /** Põe uma receita na tabela. */
    public static Recipe add(Item result, int count, int color, float power, Item... inputs) {
        Recipe receita = new Recipe(result, count, List.of(inputs), color, power, null);
        ALL.add(receita);
        return receita;
    }

    /** E uma que só pega num mundo. */
    public static Recipe add(Item result, int count, int color, float power, ResourceKey<Level> level,
                             Item... inputs) {
        Recipe receita = new Recipe(result, count, List.of(inputs), color, power, level);
        ALL.add(receita);
        return receita;
    }

    /** A receita que casa com o que está no pote, ou nada. */
    public static @Nullable Recipe find(List<ItemStack> noPote, boolean pelaMetade, Level onde) {
        for (Recipe receita : ALL) {
            if (receita.matches(noPote, pelaMetade, onde)) return receita;
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

    public static int count() {
        return ALL.size();
    }
}
