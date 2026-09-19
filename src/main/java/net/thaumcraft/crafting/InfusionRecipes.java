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
        // o InfusionRunicAugmentRecipe, que o original põe depois de todas: monta-se para a peça do meio
        InfusionRecipe augment = RunicAugmentRecipe.forCentral(middle);
        return augment != null && augment.matches(middle, around) ? augment : null;
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
        // NodeStabilizerAdv
        ALL.add(new InfusionRecipe("NODESTABILIZERADV", new ItemStack(TCBlocks.NODE_STABILIZER_ADVANCED.asItem()), 10,
                new AspectList().add(Aspects.AURA, 32).add(Aspects.MAGIC, 16).add(Aspects.ORDER, 16).add(Aspects.ENERGY, 16),
                Ingredient.of(TCBlocks.NODE_STABILIZER.asItem()),
                Arrays.asList(Ingredient.of(TCItems.NITOR), Ingredient.of(net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK.asItem()), Ingredient.of(TCItems.ALUMENTUM), Ingredient.of(net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK.asItem()), Ingredient.of(TCItems.NITOR), Ingredient.of(net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK.asItem()), Ingredient.of(TCItems.ALUMENTUM), Ingredient.of(net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK.asItem()))));
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
        // HoverHarness
        ALL.add(new InfusionRecipe("HOVERHARNESS", new ItemStack(TCItems.HOVER_HARNESS), 6,
                new AspectList().add(Aspects.FLIGHT, 32).add(Aspects.ENERGY, 32).add(Aspects.MECHANISM, 32).add(Aspects.TRAVEL, 16),
                Ingredient.of(net.minecraft.world.item.Items.LEATHER_CHESTPLATE),
                Arrays.asList(Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(TCBlocks.GREATWOOD_PLANKS.asItem()), Ingredient.of(TCBlocks.GREATWOOD_PLANKS.asItem()), Ingredient.of(net.minecraft.world.item.Items.COMPARATOR), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT))));
        // HoverGirdle
        ALL.add(new InfusionRecipe("HOVERGIRDLE", new ItemStack(TCItems.HOVER_GIRDLE), 8,
                new AspectList().add(Aspects.FLIGHT, 16).add(Aspects.ENERGY, 32).add(Aspects.AIR, 32).add(Aspects.TRAVEL, 16),
                Ingredient.of(TCItems.MUNDANE_BELT),
                Arrays.asList(Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(net.minecraft.world.item.Items.FEATHER), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(net.minecraft.world.item.Items.FEATHER), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT))));
        // RunicAmulet
        ALL.add(new InfusionRecipe("RUNICARMOR", new ItemStack(TCItems.RUNIC_AMULET), 4,
                new AspectList().add(Aspects.ARMOR, 20).add(Aspects.MAGIC, 35).add(Aspects.ENERGY, 35),
                Ingredient.of(TCItems.MUNDANE_AMULET),
                Arrays.asList(Ingredient.of(TCResources.get("primal_charm")), Ingredient.of(TCResources.get("amber")), Ingredient.of(TCResources.get("enchanted_fabric")), Ingredient.of(TCItems.NITOR), Ingredient.of(TCItems.NITOR), Ingredient.of(TCItems.SCRIBING_TOOLS))));
        // RunicAmuletEmergency
        ALL.add(new InfusionRecipe("RUNICEMERGENCY", new ItemStack(TCItems.RUNIC_AMULET_EMERGENCY), 7,
                new AspectList().add(Aspects.ARMOR, 20).add(Aspects.MAGIC, 35).add(Aspects.EARTH, 32).add(Aspects.VOID, 32),
                Ingredient.of(TCItems.RUNIC_AMULET),
                Arrays.asList(Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(TCItems.SHARDS.get("earth")), net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients.components(net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_STRENGTH)), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(TCItems.SHARDS.get("earth")))));
        // RunicRing
        ALL.add(new InfusionRecipe("RUNICARMOR", new ItemStack(TCItems.RUNIC_RING), 3,
                new AspectList().add(Aspects.ARMOR, 10).add(Aspects.MAGIC, 25).add(Aspects.ENERGY, 25),
                Ingredient.of(TCItems.MUNDANE_RING),
                Arrays.asList(Ingredient.of(TCResources.get("primal_charm")), Ingredient.of(TCResources.get("amber")), Ingredient.of(TCResources.get("enchanted_fabric")), Ingredient.of(TCItems.NITOR), Ingredient.of(TCItems.SCRIBING_TOOLS))));
        // RunicRingCharged
        ALL.add(new InfusionRecipe("RUNICCHARGED", new ItemStack(TCItems.RUNIC_RING_CHARGED), 6,
                new AspectList().add(Aspects.ARMOR, 16).add(Aspects.MAGIC, 16).add(Aspects.ENERGY, 64),
                Ingredient.of(TCItems.RUNIC_RING),
                Arrays.asList(Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(TCItems.SHARDS.get("fire")), Ingredient.of(TCItems.SHARDS.get("fire")), net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients.components(net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.STRONG_SWIFTNESS)), Ingredient.of(TCItems.SHARDS.get("fire")), Ingredient.of(TCItems.SHARDS.get("fire")))));
        // RunicRingHealing
        ALL.add(new InfusionRecipe("RUNICHEALING", new ItemStack(TCItems.RUNIC_RING_REGEN), 6,
                new AspectList().add(Aspects.ARMOR, 16).add(Aspects.MAGIC, 16).add(Aspects.WATER, 32).add(Aspects.HEAL, 32),
                Ingredient.of(TCItems.RUNIC_RING),
                Arrays.asList(Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(TCItems.SHARDS.get("water")), Ingredient.of(TCItems.SHARDS.get("water")), net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients.components(net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.POTION, net.minecraft.world.item.alchemy.Potions.LONG_REGENERATION)), Ingredient.of(TCItems.SHARDS.get("water")), Ingredient.of(TCItems.SHARDS.get("water")))));
        // RunicGirdle
        ALL.add(new InfusionRecipe("RUNICARMOR", new ItemStack(TCItems.RUNIC_GIRDLE), 4,
                new AspectList().add(Aspects.ARMOR, 30).add(Aspects.MAGIC, 50).add(Aspects.ENERGY, 50),
                Ingredient.of(TCItems.MUNDANE_BELT),
                Arrays.asList(Ingredient.of(TCResources.get("primal_charm")), Ingredient.of(TCResources.get("amber")), Ingredient.of(TCResources.get("enchanted_fabric")), Ingredient.of(TCItems.NITOR), Ingredient.of(TCItems.NITOR), Ingredient.of(TCItems.NITOR), Ingredient.of(TCItems.SCRIBING_TOOLS))));
        // RunicGirdleKinetic
        ALL.add(new InfusionRecipe("RUNICKINETIC", new ItemStack(TCItems.RUNIC_GIRDLE_KINETIC), 7,
                new AspectList().add(Aspects.ARMOR, 33).add(Aspects.MAGIC, 55).add(Aspects.AIR, 64),
                Ingredient.of(TCItems.RUNIC_GIRDLE),
                Arrays.asList(Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(TCItems.SHARDS.get("air")), net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients.components(net.minecraft.world.item.alchemy.PotionContents.createItemStack(net.minecraft.world.item.Items.SPLASH_POTION, net.minecraft.world.item.alchemy.Potions.STRONG_HARMING)), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(TCItems.SHARDS.get("air")))));
        // BootsTraveller
        ALL.add(new InfusionRecipe("BOOTSTRAVELLER", new ItemStack(TCItems.TRAVELLER_BOOTS), 1,
                new AspectList().add(Aspects.FLIGHT, 25).add(Aspects.TRAVEL, 25),
                Ingredient.of(net.minecraft.world.item.Items.LEATHER_BOOTS),
                Arrays.asList(Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(TCResources.get("enchanted_fabric")), Ingredient.of(TCResources.get("enchanted_fabric")), Ingredient.of(net.minecraft.world.item.Items.FEATHER), Ingredient.of(net.minecraft.world.item.Items.COD))));
        // ThaumiumFortressHelm
        ALL.add(new InfusionRecipe("ARMORFORTRESS", new ItemStack(TCItems.FORTRESS_HELMET), 3,
                new AspectList().add(Aspects.METAL, 24).add(Aspects.ARMOR, 16).add(Aspects.MAGIC, 16),
                Ingredient.of(TCItems.GEAR.get("thaumium_helmet")),
                Arrays.asList(Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.EMERALD))));
        // ThaumiumFortressChest
        ALL.add(new InfusionRecipe("ARMORFORTRESS", new ItemStack(TCItems.FORTRESS_CHESTPLATE), 3,
                new AspectList().add(Aspects.METAL, 24).add(Aspects.ARMOR, 24).add(Aspects.MAGIC, 16),
                Ingredient.of(TCItems.GEAR.get("thaumium_chestplate")),
                Arrays.asList(Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.LEATHER))));
        // ThaumiumFortressLegs
        ALL.add(new InfusionRecipe("ARMORFORTRESS", new ItemStack(TCItems.FORTRESS_LEGGINGS), 3,
                new AspectList().add(Aspects.METAL, 24).add(Aspects.ARMOR, 20).add(Aspects.MAGIC, 16),
                Ingredient.of(TCItems.GEAR.get("thaumium_leggings")),
                Arrays.asList(Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.LEATHER))));
        // HelmGoggles
        ALL.add(InfusionRecipe.onCentral("HELMGOGGLES", 5,
                new AspectList().add(Aspects.SENSES, 32).add(Aspects.AURA, 16).add(Aspects.ARMOR, 16),
                Ingredient.of(TCItems.FORTRESS_HELMET),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.SLIME_BALL), Ingredient.of(TCItems.GOGGLES)),
                stack -> { stack.set(net.thaumcraft.registry.TCComponents.FORTRESS_GOGGLES, true); return stack; }));
        // MaskAngryGhost
        ALL.add(InfusionRecipe.onCentral("MASKANGRYGHOST", 8,
                new AspectList().add(Aspects.ENTROPY, 64).add(Aspects.DEATH, 64).add(Aspects.ARMOR, 16),
                Ingredient.of(TCItems.FORTRESS_HELMET),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.BONE_MEAL), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(net.minecraft.world.item.Items.POISONOUS_POTATO), Ingredient.of(net.minecraft.world.item.Items.WITHER_SKELETON_SKULL), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT)),
                stack -> { stack.set(net.thaumcraft.registry.TCComponents.FORTRESS_MASK, 1); return stack; }));
        // MaskSippingFiend
        ALL.add(InfusionRecipe.onCentral("MASKSIPPINGFIEND", 8,
                new AspectList().add(Aspects.UNDEAD, 64).add(Aspects.LIFE, 64).add(Aspects.ARMOR, 16),
                Ingredient.of(TCItems.FORTRESS_HELMET),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.DYE.red()), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(net.minecraft.world.item.Items.GHAST_TEAR), Ingredient.of(net.minecraft.world.item.Items.MILK_BUCKET), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT)),
                stack -> { stack.set(net.thaumcraft.registry.TCComponents.FORTRESS_MASK, 2); return stack; }));
    }
}
