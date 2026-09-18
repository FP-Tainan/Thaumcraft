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
        // WandCapSilver
        ALL.add(new InfusionRecipe("CAP_silver", new ItemStack(TCItems.WAND_CAPS.get("silver")), 4,
                new AspectList().add(Aspects.ENERGY, WandParts.cap("silver").craftCost()*2).add(Aspects.AURA, WandParts.cap("silver").craftCost()),
                Ingredient.of(TCItems.INERT_CAPS.get("silver")),
                Arrays.asList(Ingredient.of(TCResources.get("salis_mundus")), Ingredient.of(TCResources.get("salis_mundus")))));
        // WandCapThaumium
        ALL.add(new InfusionRecipe("CAP_thaumium", new ItemStack(TCItems.WAND_CAPS.get("thaumium")), 5,
                new AspectList().add(Aspects.ENERGY, WandParts.cap("thaumium").craftCost()*2).add(Aspects.AURA, WandParts.cap("thaumium").craftCost()),
                Ingredient.of(TCItems.INERT_CAPS.get("thaumium")),
                Arrays.asList(Ingredient.of(TCResources.get("salis_mundus")), Ingredient.of(TCResources.get("salis_mundus")), Ingredient.of(TCResources.get("salis_mundus")))));
        // WandCapVoid
        ALL.add(new InfusionRecipe("CAP_void", new ItemStack(TCItems.WAND_CAPS.get("void")), 8,
                new AspectList().add(Aspects.ENERGY, WandParts.cap("void").craftCost()*2).add(Aspects.VOID, WandParts.cap("void").craftCost()*2).add(Aspects.ELDRITCH, WandParts.cap("void").craftCost()*2).add(Aspects.AURA, WandParts.cap("void").craftCost()*2),
                Ingredient.of(TCItems.INERT_CAPS.get("void")),
                Arrays.asList(Ingredient.of(TCResources.get("salis_mundus")), Ingredient.of(TCResources.get("salis_mundus")), Ingredient.of(TCResources.get("salis_mundus")), Ingredient.of(TCResources.get("salis_mundus")))));
        // WandRodObsidian
        ALL.add(new InfusionRecipe("ROD_obsidian", new ItemStack(TCItems.WAND_RODS.get("obsidian")), 3,
                new AspectList().add(Aspects.EARTH, WandParts.rod("obsidian").craftCost()*2).add(Aspects.MAGIC, WandParts.rod("obsidian").craftCost()).add(Aspects.DARKNESS, WandParts.rod("blaze").craftCost()),
                Ingredient.of(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()),
                Arrays.asList(Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(TCItems.SHARDS.get("earth")))));
        // WandRodIce
        ALL.add(new InfusionRecipe("ROD_ice", new ItemStack(TCItems.WAND_RODS.get("ice")), 3,
                new AspectList().add(Aspects.WATER, WandParts.rod("ice").craftCost()*2).add(Aspects.MAGIC, WandParts.rod("ice").craftCost()).add(Aspects.COLD, WandParts.rod("blaze").craftCost()),
                Ingredient.of(net.minecraft.world.level.block.Blocks.ICE.asItem()),
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
        // WandRodSilverwood
        ALL.add(new InfusionRecipe("ROD_silverwood", new ItemStack(TCItems.WAND_RODS.get("silverwood")), 5,
                new AspectList().add(Aspects.AIR, WandParts.rod("silverwood").craftCost()).add(Aspects.FIRE, WandParts.rod("silverwood").craftCost()).add(Aspects.WATER, WandParts.rod("silverwood").craftCost()).add(Aspects.EARTH, WandParts.rod("silverwood").craftCost()).add(Aspects.ORDER, WandParts.rod("silverwood").craftCost()).add(Aspects.ENTROPY, WandParts.rod("silverwood").craftCost()).add(Aspects.MAGIC, WandParts.rod("silverwood").craftCost()),
                Ingredient.of(TCBlocks.SILVERWOOD_LOG.asItem()),
                Arrays.asList(Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(TCItems.SHARDS.get("fire")), Ingredient.of(TCItems.SHARDS.get("water")), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(TCItems.SHARDS.get("order")), Ingredient.of(TCItems.SHARDS.get("entropy")))));
        // WandRodPrimalStaff
        ALL.add(new InfusionRecipe("ROD_primal_staff", new ItemStack(TCItems.STAFF_RODS.get("primal")), 8,
                new AspectList().add(Aspects.AIR, WandParts.rod("primal_staff").craftCost()).add(Aspects.FIRE, WandParts.rod("primal_staff").craftCost()).add(Aspects.WATER, WandParts.rod("primal_staff").craftCost()).add(Aspects.EARTH, WandParts.rod("primal_staff").craftCost()).add(Aspects.ORDER, WandParts.rod("primal_staff").craftCost()).add(Aspects.ENTROPY, WandParts.rod("primal_staff").craftCost()).add(Aspects.MAGIC, WandParts.rod("primal_staff").craftCost()*2),
                Ingredient.of(TCItems.WAND_RODS.get("silverwood")),
                Arrays.asList(Ingredient.of(TCResources.get("primal_charm")), Ingredient.of(TCItems.WAND_RODS.get("obsidian")), Ingredient.of(TCItems.WAND_RODS.get("ice")), Ingredient.of(TCItems.WAND_RODS.get("quartz")), Ingredient.of(TCResources.get("primal_charm")), Ingredient.of(TCItems.WAND_RODS.get("reed")), Ingredient.of(TCItems.WAND_RODS.get("blaze")), Ingredient.of(TCItems.WAND_RODS.get("bone")))));
        // FocusPortableHole
        ALL.add(new InfusionRecipe("FOCUSPORTABLEHOLE", new ItemStack(TCItems.FOCI.get("portable_hole")), 3,
                new AspectList().add(Aspects.TRAVEL, 25).add(Aspects.ELDRITCH, 10).add(Aspects.EXCHANGE, 10).add(Aspects.ENTROPY, 25),
                Ingredient.of(net.minecraft.world.item.Items.ENDER_PEARL),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("entropy")))));
        // FocusWarding
        ALL.add(new InfusionRecipe("FOCUSWARDING", new ItemStack(TCItems.FOCI.get("warding")), 4,
                new AspectList().add(Aspects.EARTH, 25).add(Aspects.ARMOR, 25).add(Aspects.ORDER, 25).add(Aspects.MIND, 10),
                Ingredient.of(net.minecraft.world.item.Items.NETHER_STAR),
                Arrays.asList(Ingredient.of(TCResources.get("quicksilver")), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("order")), Ingredient.of(TCResources.get("quicksilver")), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("order")))));
        // CoreAlchemy
        ALL.add(new InfusionRecipe("COREALCHEMY", new ItemStack(TCItems.GOLEM_CORES.get("alchemy")), 2,
                new AspectList().add(Aspects.MAGIC, 15).add(Aspects.WATER, 15).add(Aspects.MOTION, 15),
                Ingredient.of(TCItems.GOLEM_CORES.get("decanting")),
                Arrays.asList(Ingredient.of(TCBlocks.JAR.asItem()), Ingredient.of(net.minecraft.world.item.Items.POTION), Ingredient.of(net.minecraft.world.item.Items.POTION), Ingredient.of(net.minecraft.world.item.Items.POTION))));
        // CoreFishing
        ALL.add(new InfusionRecipe("COREFISHING", new ItemStack(TCItems.GOLEM_CORES.get("fishing")), 3,
                new AspectList().add(Aspects.WATER, 16).add(Aspects.HARVEST, 16).add(Aspects.BEAST, 16),
                Ingredient.of(TCItems.GOLEM_CORES.get("harvest")),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.FISHING_ROD), Ingredient.of(net.minecraft.world.item.Items.COD), Ingredient.of(net.minecraft.world.item.Items.PUFFERFISH), Ingredient.of(net.minecraft.world.item.Items.SALMON))));
        // CoreUse
        ALL.add(new InfusionRecipe("COREUSE", new ItemStack(TCItems.GOLEM_CORES.get("use")), 3,
                new AspectList().add(Aspects.TOOL, 20).add(Aspects.MECHANISM, 20).add(Aspects.MAN, 20),
                Ingredient.of(TCItems.GOLEM_CORES.get("empty")),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.COMPARATOR), Ingredient.of(net.minecraft.world.item.Items.FLINT_AND_STEEL), Ingredient.of(net.minecraft.world.item.Items.SHEARS), Ingredient.of(net.minecraft.world.level.block.Blocks.LEVER.asItem()))));
        // LampGrowth
        ALL.add(new InfusionRecipe("LAMPGROWTH", new ItemStack(TCBlocks.GROWTH_LAMP.asItem()), 4,
                new AspectList().add(Aspects.PLANT, 16).add(Aspects.LIGHT, 8).add(Aspects.LIFE, 16),
                Ingredient.of(TCBlocks.ARCANE_LAMP.asItem()),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.BONE_MEAL), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.BONE_MEAL), Ingredient.of(TCItems.SHARDS.get("earth")))));
        // LampFertility
        ALL.add(new InfusionRecipe("LAMPFERTILITY", new ItemStack(TCBlocks.FERTILITY_LAMP.asItem()), 4,
                new AspectList().add(Aspects.BEAST, 16).add(Aspects.LIFE, 16).add(Aspects.LIGHT, 8),
                Ingredient.of(TCBlocks.ARCANE_LAMP.asItem()),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.WHEAT), Ingredient.of(TCItems.SHARDS.get("fire")), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.CARROT), Ingredient.of(TCItems.SHARDS.get("fire")))));
    }
}
