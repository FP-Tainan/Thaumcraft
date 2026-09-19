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
        ArcaneRecipe sceptre = sceptre(grid, player);
        if (sceptre != null) return sceptre;
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

    /** As casas vazias do cetro, e onde vão as três pontas, a haste e o amuleto primordial. */
    private static final int[] SCEPTRE_EMPTY = {0, 3, 7, 8};
    private static final int[] SCEPTRE_CAPS = {1, 5, 6};
    private static final int CHARM = 2;

    /**
     * O {@code ArcaneSceptreRecipe}: três pontas iguais em volta do canto de cima à direita, o amuleto primordial nesse
     * canto e a haste no meio. Sai um cetro — a varinha com a marca — e custa uma vez e meia o da varinha. Pede a
     * pesquisa do cetro e as da ponta e da haste.
     */
    private static ArcaneRecipe sceptre(List<ItemStack> grid, Player player) {
        for (int slot : SCEPTRE_EMPTY) {
            if (!grid.get(slot).isEmpty()) return null;
        }
        ItemStack capA = grid.get(SCEPTRE_CAPS[0]), capB = grid.get(SCEPTRE_CAPS[1]), capC = grid.get(SCEPTRE_CAPS[2]);
        ItemStack rodStack = grid.get(ROD), charm = grid.get(CHARM);
        if (capA.isEmpty() || rodStack.isEmpty() || !charm.is(net.thaumcraft.registry.TCResources.get("primal_charm"))) return null;
        if (capB.getItem() != capA.getItem() || capC.getItem() != capA.getItem()) return null;
        if (!ResearchManager.knows(player, "SCEPTRE")) return null;
        String cap = tagOf(TCItems.WAND_CAPS, capA.getItem());
        if (cap == null) return null;
        WandParts.Cap capPart = WandParts.cap(cap);
        boolean staff = false;
        String rod = rodStack.is(Items.STICK) ? "wood" : tagOf(TCItems.WAND_RODS, rodStack.getItem());
        if (rod == null) {
            rod = tagOf(TCItems.STAFF_RODS, rodStack.getItem());
            staff = rod != null;
        }
        if (rod == null || capPart == null) return null;
        WandParts.Rod rodPart = staff ? WandParts.STAFF_RODS.get(rod) : WandParts.RODS.get(rod);
        if (rodPart == null) return null;
        if (!ResearchManager.knows(player, "CAP_" + cap) || !ResearchManager.knows(player, "ROD_" + rod + (staff ? "_staff" : ""))) return null;

        ItemStack out = new ItemStack(staff ? TCItems.STAFF : TCItems.WAND);
        out.set(TCComponents.WAND_ROD, rod);
        out.set(TCComponents.WAND_CAP, cap);
        out.set(TCComponents.WAND_SCEPTRE, net.minecraft.util.Unit.INSTANCE);
        int each = (int) (capPart.craftCost() * rodPart.craftCost() * 1.5f);
        AspectList cost = new AspectList();
        for (Aspect primal : Aspects.primals()) cost.add(primal, each);
        Ingredient capIngredient = Ingredient.of(capA.getItem());
        List<Ingredient> pattern = Arrays.asList(null, capIngredient, Ingredient.of(charm.getItem()), null,
                Ingredient.of(rodStack.getItem()), capIngredient, capIngredient, null, null);
        return new ArcaneRecipe("SCEPTRE", out, pattern, cost);
    }

    private static String tagOf(Map<String, Item> items, Item item) {
        for (Map.Entry<String, Item> entry : items.entrySet()) {
            if (entry.getValue() == item) return entry.getKey();
        }
        return null;
    }
}
