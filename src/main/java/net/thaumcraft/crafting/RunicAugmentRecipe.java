package net.thaumcraft.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.event.RunicShield;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCResources;

import java.util.ArrayList;
import java.util.List;

/**
 * O reforço rúnico: o {@code InfusionRunicAugmentRecipe} da 4.2.3.5. Qualquer peça que aceite escudo rúnico vai no
 * meio; em volta, um diamante e um sal mundus, mais um sal por carga que a peça já tem. Sai a mesma peça com uma
 * carga a mais ({@code RS.HARDEN}). A essência dobra a cada carga — 32 × 2<sup>cargas</sup>, metade em Tutamen e
 * Praecantatio, tudo em Potentia — e a instabilidade sobe meio ponto por carga.
 */
public final class RunicAugmentRecipe {
    private RunicAugmentRecipe() {
    }

    /** A receita para esta peça do meio, ou nada se ela não aceita escudo rúnico. */
    public static InfusionRecipe forCentral(ItemStack central) {
        if (central.isEmpty() || !RunicShield.isRunic(central)) return null;
        int charge = RunicShield.finalCharge(central);
        List<Ingredient> components = new ArrayList<>();
        components.add(Ingredient.of(Items.DIAMOND));
        components.add(Ingredient.of(TCResources.get("salis_mundus")));
        for (int c = 0; c < charge; c++) components.add(Ingredient.of(TCResources.get("salis_mundus")));
        AspectList essentia = new AspectList();
        int vis = (int) (32.0 * Math.pow(2.0, charge));
        if (vis > 0) {
            essentia.add(Aspects.ARMOR, vis / 2);
            essentia.add(Aspects.MAGIC, vis / 2);
            essentia.add(Aspects.ENERGY, vis);
        }
        return InfusionRecipe.onCentral("RUNICAUGMENTATION", 5 + charge / 2, essentia,
                Ingredient.of(central.getItem()), components, stack -> {
                    stack.set(TCComponents.RUNIC_HARDEN, stack.getOrDefault(TCComponents.RUNIC_HARDEN, 0) + 1);
                    return stack;
                });
    }
}
