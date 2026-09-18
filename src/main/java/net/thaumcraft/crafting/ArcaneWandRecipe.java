package net.thaumcraft.crafting;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.wands.WandParts;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.research.ResearchManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A montagem de varinhas na bancada arcana: o {@code ArcaneWandRecipe} da 4.2.3.5, descompilado.
 *
 * <p>Não é uma receita de tabela: qualquer haste no meio da grade, com duas pontas iguais nos cantos de
 * cima à direita e de baixo à esquerda, e nada mais, vira uma varinha com aquela haste e aquelas pontas. Se
 * a haste for um núcleo de bastão, sai um bastão. O vis cobrado é o custo da ponta vezes o da haste, igual
 * nos seis primários. A de graveto com ferro não sai daqui — essa é a receita comum da bancada de sempre.
 *
 * <p>Só monta quem tem as duas pesquisas: a da ponta ({@code CAP_}) e a da haste ({@code ROD_}, com o
 * sufixo {@code _staff} para os núcleos de bastão).
 */
public final class ArcaneWandRecipe {
    /** As casas que têm de ficar vazias: todas menos as duas pontas e o meio. */
    private static final int[] EMPTY = {0, 1, 3, 5, 7, 8};
    private static final int CAP_A = 2, CAP_B = 6, ROD = 4;

    private ArcaneWandRecipe() {
    }

    /** A varinha que esta grade monta para este jogador, ou nulo. */
    public static ArcaneRecipe find(List<ItemStack> grid, Player player) {
        if (grid.size() != 9) return null;
        for (int slot : EMPTY) {
            if (!grid.get(slot).isEmpty()) return null;
        }
        ItemStack capA = grid.get(CAP_A), capB = grid.get(CAP_B), rodStack = grid.get(ROD);
        if (capA.isEmpty() || capB.isEmpty() || rodStack.isEmpty() || capA.getItem() != capB.getItem()) return null;

        String cap = tagOf(TCItems.WAND_CAPS, capA.getItem());
        if (cap == null) return null;
        WandParts.Cap capPart = WandParts.cap(cap);

        boolean staff = false;
        String rod = rodStack.is(Items.STICK) ? "wood" : tagOf(TCItems.WAND_RODS, rodStack.getItem());
        if (rod == null) {
            rod = tagOf(TCItems.STAFF_RODS, rodStack.getItem());
            staff = rod != null;
        }
        if (rod == null) return null;
        WandParts.Rod rodPart = staff ? WandParts.STAFF_RODS.get(rod) : WandParts.RODS.get(rod);
        if (capPart == null || rodPart == null) return null;

        String rodResearch = "ROD_" + rod + (staff ? "_staff" : "");
        if (!ResearchManager.knows(player, "CAP_" + cap) || !ResearchManager.knows(player, rodResearch)) return null;
        // a de graveto e ferro é a da bancada comum
        if (!staff && rod.equals("wood") && cap.equals("iron")) return null;

        ItemStack out = new ItemStack(staff ? TCItems.STAFF : TCItems.WAND);
        out.set(TCComponents.WAND_ROD, rod);
        out.set(TCComponents.WAND_CAP, cap);

        int each = capPart.craftCost() * rodPart.craftCost();
        AspectList cost = new AspectList();
        for (Aspect primal : Aspects.primals()) cost.add(primal, each);

        Ingredient capIngredient = Ingredient.of(capA.getItem());
        List<Ingredient> pattern = Arrays.asList(null, null, capIngredient, null,
                Ingredient.of(rodStack.getItem()), null, capIngredient, null, null);
        return new ArcaneRecipe("", out, pattern, cost);
    }

    private static String tagOf(Map<String, Item> items, Item item) {
        for (Map.Entry<String, Item> entry : items.entrySet()) {
            if (entry.getValue() == item) return entry.getKey();
        }
        return null;
    }
}
