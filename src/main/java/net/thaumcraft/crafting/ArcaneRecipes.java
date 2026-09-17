package net.thaumcraft.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.api.wands.WandParts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * As receitas de bancada arcana do Thaumcraft 4.2.3.5.
 *
 * <p><b>Este arquivo é gerado</b> pelo {@code scratchpad/fatia6-arcanas.js} a partir do
 * {@code ConfigRecipesArcaneSlice} do mod original. Só entram as que fecham com coisas que já existem
 * por aqui; as outras chegam com as fatias que trouxerem as peças que faltam.
 */
public final class ArcaneRecipes {
    public static final List<ArcaneRecipe> ALL = new ArrayList<>();

    private ArcaneRecipes() {
    }

    public static void init() {
    }

    /** A receita que esta grade fecha, se houver alguma. */
    public static ArcaneRecipe find(List<ItemStack> grid) {
        for (ArcaneRecipe recipe : ALL) {
            if (recipe.matches(grid)) return recipe;
        }
        return null;
    }

    static {
        // PrimalCharm
        ALL.add(new ArcaneRecipe("BASICARTIFACE", new ItemStack(TCResources.get("primal_charm")),
                Arrays.asList(Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(TCItems.SHARDS.get("fire")), Ingredient.of(TCItems.SHARDS.get("water")), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(TCItems.SHARDS.get("order")), Ingredient.of(TCItems.SHARDS.get("entropy"))),
                new AspectList().add(Aspects.EARTH, 25).add(Aspects.FIRE, 25).add(Aspects.AIR, 25).add(Aspects.WATER, 25).add(Aspects.ORDER, 25).add(Aspects.ENTROPY, 25)));
        // WandCapGold
        ALL.add(new ArcaneRecipe("CAP_gold", new ItemStack(TCItems.WAND_CAPS.get("gold")),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), null, Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), null, null, null),
                new AspectList().add(Aspects.ORDER, WandParts.cap("gold").craftCost()).add(Aspects.FIRE, WandParts.cap("gold").craftCost()).add(Aspects.AIR, WandParts.cap("gold").craftCost())));
        // Goggles
        ALL.add(new ArcaneRecipe("GOGGLES", new ItemStack(TCItems.GOGGLES),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(net.minecraft.world.item.Items.LEATHER), null, Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(TCItems.THAUMOMETER), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(TCItems.THAUMOMETER)),
                new AspectList().add(Aspects.AIR, 5).add(Aspects.FIRE, 5).add(Aspects.WATER, 5).add(Aspects.EARTH, 5).add(Aspects.ENTROPY, 3).add(Aspects.ORDER, 3)));
    }
}
