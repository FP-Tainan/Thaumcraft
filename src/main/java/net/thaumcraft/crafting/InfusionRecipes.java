package net.thaumcraft.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.wands.WandParts;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * As receitas de infusão do Thaumcraft 4.2.3.5.
 *
 * <p><b>Este arquivo é gerado</b> pelo {@code scratchpad/fatia7-infusao.js} a partir das três fatias
 * de receita do mod original: {@code ConfigRecipesInfusionSlice},
 * {@code ConfigRecipesInfusionDeviceSlice} e {@code ConfigRecipesInfusionEquipmentSlice}. Só entram
 * as que fecham com coisas que já existem por aqui; as outras chegam com as fatias que trouxerem as
 * peças que faltam.
 */
public final class InfusionRecipes {
    public static final List<InfusionRecipe> ALL = new ArrayList<>();

    private InfusionRecipes() {
    }

    public static void init() {
    }

    /** A receita que este arranjo fecha, se houver alguma. */
    public static InfusionRecipe find(ItemStack middle, List<ItemStack> around) {
        for (InfusionRecipe recipe : ALL) {
            if (recipe.matches(middle, around)) return recipe;
        }
        return null;
    }

    static {
        // WandRodObsidian
        ALL.add(new InfusionRecipe("ROD_obsidian", new ItemStack(TCItems.WAND_RODS.get("obsidian")), 3,
                new AspectList().add(Aspects.EARTH, WandParts.rod("obsidian").craftCost()*2).add(Aspects.MAGIC, WandParts.rod("obsidian").craftCost()).add(Aspects.DARKNESS, WandParts.rod("blaze").craftCost()),
                Ingredient.of(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()),
                Arrays.asList(Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(TCItems.SHARDS.get("earth")))));
        // WandRodIce
        ALL.add(new InfusionRecipe("ROD_ice", new ItemStack(TCItems.WAND_RODS.get("ice")), 3,
                new AspectList().add(Aspects.WATER, WandParts.rod("ice").craftCost()*2).add(Aspects.MAGIC, WandParts.rod("ice").craftCost()).add(Aspects.COLD, WandParts.rod("blaze").craftCost()),
                Ingredient.of(net.minecraft.world.level.block.Blocks.PACKED_ICE.asItem()),
                Arrays.asList(Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(TCItems.SHARDS.get("water")))));
        // WandRodQuartz
        ALL.add(new InfusionRecipe("ROD_quartz", new ItemStack(TCItems.WAND_RODS.get("quartz")), 3,
                new AspectList().add(Aspects.ORDER, WandParts.rod("quartz").craftCost()*2).add(Aspects.MAGIC, WandParts.rod("quartz").craftCost()).add(Aspects.CRYSTAL, WandParts.rod("blaze").craftCost()),
                Ingredient.of(net.minecraft.world.level.block.Blocks.QUARTZ_BLOCK.asItem()),
                Arrays.asList(Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(TCItems.SHARDS.get("order")))));
        // WandRodReed
        ALL.add(new InfusionRecipe("ROD_reed", new ItemStack(TCItems.WAND_RODS.get("reed")), 3,
                new AspectList().add(Aspects.AIR, WandParts.rod("reed").craftCost()*2).add(Aspects.MAGIC, WandParts.rod("reed").craftCost()).add(Aspects.MOTION, WandParts.rod("blaze").craftCost()),
                Ingredient.of(net.minecraft.world.item.Items.SUGAR_CANE),
                Arrays.asList(Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(TCItems.SHARDS.get("air")))));
        // WandRodBlaze
        ALL.add(new InfusionRecipe("ROD_blaze", new ItemStack(TCItems.WAND_RODS.get("blaze")), 3,
                new AspectList().add(Aspects.FIRE, WandParts.rod("blaze").craftCost()*2).add(Aspects.MAGIC, WandParts.rod("blaze").craftCost()).add(Aspects.BEAST, WandParts.rod("blaze").craftCost()),
                Ingredient.of(net.minecraft.world.item.Items.BLAZE_ROD),
                Arrays.asList(Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(TCItems.SHARDS.get("fire")))));
        // WandRodBone
        ALL.add(new InfusionRecipe("ROD_bone", new ItemStack(TCItems.WAND_RODS.get("bone")), 3,
                new AspectList().add(Aspects.ENTROPY, WandParts.rod("bone").craftCost()*2).add(Aspects.MAGIC, WandParts.rod("bone").craftCost()).add(Aspects.UNDEAD, WandParts.rod("blaze").craftCost()),
                Ingredient.of(net.minecraft.world.item.Items.BONE),
                Arrays.asList(Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(TCItems.SHARDS.get("entropy")))));
    }
}
