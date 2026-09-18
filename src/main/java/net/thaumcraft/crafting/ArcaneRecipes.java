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
        // ArcaneStone1
        ALL.add(new ArcaneRecipe("ARCANESTONE", new ItemStack(TCBlocks.BUILDING.get("arcane_stone").asItem(), 9),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.STONE), Ingredient.of(net.minecraft.world.item.Items.STONE), Ingredient.of(net.minecraft.world.item.Items.STONE), Ingredient.of(net.minecraft.world.item.Items.STONE), Ingredient.of(TCItems.SHARDS.values().toArray(new net.minecraft.world.item.Item[0])), Ingredient.of(net.minecraft.world.item.Items.STONE), Ingredient.of(net.minecraft.world.item.Items.STONE), Ingredient.of(net.minecraft.world.item.Items.STONE), Ingredient.of(net.minecraft.world.item.Items.STONE)),
                new AspectList().add(Aspects.EARTH, 1).add(Aspects.FIRE, 1)));
        // PaveTravel
        ALL.add(new ArcaneRecipe("PAVETRAVEL", new ItemStack(TCBlocks.BUILDING.get("paving_stone_travel").asItem(), 4),
                Arrays.asList(Ingredient.of(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()), null, null, null),
                new AspectList().add(Aspects.EARTH, 10).add(Aspects.AIR, 10)));
        // ArcaneLamp
        ALL.add(new ArcaneRecipe("ARCANELAMP", new ItemStack(TCBlocks.ARCANE_LAMP.asItem()),
                Arrays.asList(null, Ingredient.of(net.minecraft.world.level.block.Blocks.DAYLIGHT_DETECTOR.asItem()), null, Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(TCBlocks.AMBER_BLOCK.asItem()), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), null, Ingredient.of(TCItems.NITOR), null),
                new AspectList().add(Aspects.FIRE, 8).add(Aspects.AIR, 8).add(Aspects.WATER, 4).add(Aspects.ENTROPY, 4)));
        // PaveWard
        ALL.add(new ArcaneRecipe("PAVEWARD", new ItemStack(TCBlocks.BUILDING.get("paving_stone_warding").asItem(), 4),
                Arrays.asList(Ingredient.of(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()), Ingredient.of(TCItems.SHARDS.get("fire")), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()), Ingredient.of(TCItems.SHARDS.get("order")), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()), null, null, null),
                new AspectList().add(Aspects.FIRE, 10).add(Aspects.ORDER, 10)));
        // InfusionMatrix
        ALL.add(new ArcaneRecipe("INFUSION", new ItemStack(TCBlocks.INFUSION_MATRIX.asItem()),
                Arrays.asList(Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()), Ingredient.of(TCItems.SHARDS.values().toArray(new net.minecraft.world.item.Item[0])), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()), Ingredient.of(TCItems.SHARDS.values().toArray(new net.minecraft.world.item.Item[0])), Ingredient.of(net.minecraft.world.item.Items.ENDER_PEARL), Ingredient.of(TCItems.SHARDS.values().toArray(new net.minecraft.world.item.Item[0])), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()), Ingredient.of(TCItems.SHARDS.values().toArray(new net.minecraft.world.item.Item[0])), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem())),
                new AspectList().add(Aspects.ORDER, 40)));
        // ArcanePedestal
        ALL.add(new ArcaneRecipe("INFUSION", new ItemStack(TCBlocks.PEDESTAL.asItem(), 2),
                Arrays.asList(Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()), null, Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()), null, Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem())),
                new AspectList().add(Aspects.AIR, 5)));
        // WardedJar
        ALL.add(new ArcaneRecipe("DISTILESSENTIA", new ItemStack(TCBlocks.JAR.asItem()),
                Arrays.asList(Ingredient.of(net.minecraft.world.level.block.Blocks.GLASS_PANE.asItem()), Ingredient.of(net.minecraft.world.item.Items.OAK_SLAB), Ingredient.of(net.minecraft.world.level.block.Blocks.GLASS_PANE.asItem()), Ingredient.of(net.minecraft.world.level.block.Blocks.GLASS_PANE.asItem()), null, Ingredient.of(net.minecraft.world.level.block.Blocks.GLASS_PANE.asItem()), Ingredient.of(net.minecraft.world.level.block.Blocks.GLASS_PANE.asItem()), Ingredient.of(net.minecraft.world.level.block.Blocks.GLASS_PANE.asItem()), Ingredient.of(net.minecraft.world.level.block.Blocks.GLASS_PANE.asItem())),
                new AspectList().add(Aspects.WATER, 1)));
        // JarVoid
        ALL.add(new ArcaneRecipe("JARVOID", new ItemStack(TCBlocks.JAR_VOID.asItem()),
                Arrays.asList(Ingredient.of(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), null, null, Ingredient.of(TCBlocks.JAR.asItem()), null, null, Ingredient.of(net.minecraft.world.item.Items.BLAZE_POWDER), null, null),
                new AspectList().add(Aspects.WATER, 5).add(Aspects.ENTROPY, 15)));
        // WandCapGold
        ALL.add(new ArcaneRecipe("CAP_gold", new ItemStack(TCItems.WAND_CAPS.get("gold")),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), null, Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), null, null, null),
                new AspectList().add(Aspects.ORDER, WandParts.cap("gold").craftCost()).add(Aspects.FIRE, WandParts.cap("gold").craftCost()).add(Aspects.AIR, WandParts.cap("gold").craftCost())));
        // WandCapCopper
        ALL.add(new ArcaneRecipe("CAP_copper", new ItemStack(TCItems.WAND_CAPS.get("copper")),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.COPPER_NUGGET), Ingredient.of(net.minecraft.world.item.Items.COPPER_NUGGET), Ingredient.of(net.minecraft.world.item.Items.COPPER_NUGGET), Ingredient.of(net.minecraft.world.item.Items.COPPER_NUGGET), null, Ingredient.of(net.minecraft.world.item.Items.COPPER_NUGGET), null, null, null),
                new AspectList().add(Aspects.ORDER, WandParts.cap("copper").craftCost()).add(Aspects.FIRE, WandParts.cap("copper").craftCost()).add(Aspects.AIR, WandParts.cap("copper").craftCost())));
        // WandCapThaumiumInert
        ALL.add(new ArcaneRecipe("CAP_thaumium", new ItemStack(TCItems.INERT_CAPS.get("thaumium")),
                Arrays.asList(Ingredient.of(TCResources.get("thaumium_nugget")), Ingredient.of(TCResources.get("thaumium_nugget")), Ingredient.of(TCResources.get("thaumium_nugget")), Ingredient.of(TCResources.get("thaumium_nugget")), null, Ingredient.of(TCResources.get("thaumium_nugget")), null, null, null),
                new AspectList().add(Aspects.ORDER, WandParts.cap("thaumium").craftCost()).add(Aspects.FIRE, WandParts.cap("thaumium").craftCost()).add(Aspects.AIR, WandParts.cap("thaumium").craftCost())));
        // WandCapVoidInert
        ALL.add(new ArcaneRecipe("CAP_void", new ItemStack(TCItems.INERT_CAPS.get("void")),
                Arrays.asList(Ingredient.of(TCResources.get("void_nugget")), Ingredient.of(TCResources.get("void_nugget")), Ingredient.of(TCResources.get("void_nugget")), Ingredient.of(TCResources.get("void_nugget")), null, Ingredient.of(TCResources.get("void_nugget")), null, null, null),
                new AspectList().add(Aspects.ENTROPY, WandParts.cap("void").craftCost() * 3).add(Aspects.ORDER, WandParts.cap("void").craftCost() * 3).add(Aspects.FIRE, WandParts.cap("void").craftCost() * 2).add(Aspects.AIR, WandParts.cap("void").craftCost() * 2)));
        // WandRodGreatwood
        ALL.add(new ArcaneRecipe("ROD_greatwood", new ItemStack(TCItems.WAND_RODS.get("greatwood")),
                Arrays.asList(null, Ingredient.of(TCBlocks.GREATWOOD_LOG.asItem()), null, Ingredient.of(TCBlocks.GREATWOOD_LOG.asItem()), null, null, null, null, null),
                new AspectList().add(Aspects.ENTROPY, WandParts.rod("greatwood").craftCost())));
        // WandRodGreatwoodStaff
        ALL.add(new ArcaneRecipe("ROD_greatwood_staff", new ItemStack(TCItems.STAFF_RODS.get("greatwood")),
                Arrays.asList(null, null, Ingredient.of(TCResources.get("primal_charm")), null, Ingredient.of(TCItems.WAND_RODS.get("greatwood")), null, Ingredient.of(TCItems.WAND_RODS.get("greatwood")), null, null),
                new AspectList().add(Aspects.ORDER, WandParts.rod("greatwood_staff").craftCost())));
        // WandRodObsidianStaff
        ALL.add(new ArcaneRecipe("ROD_obsidian_staff", new ItemStack(TCItems.STAFF_RODS.get("obsidian")),
                Arrays.asList(null, null, Ingredient.of(TCResources.get("primal_charm")), null, Ingredient.of(TCItems.WAND_RODS.get("obsidian")), null, Ingredient.of(TCItems.WAND_RODS.get("obsidian")), null, null),
                new AspectList().add(Aspects.ORDER, WandParts.rod("obsidian_staff").craftCost())));
        // WandRodSilverwoodStaff
        ALL.add(new ArcaneRecipe("ROD_silverwood_staff", new ItemStack(TCItems.STAFF_RODS.get("silverwood")),
                Arrays.asList(null, null, Ingredient.of(TCResources.get("primal_charm")), null, Ingredient.of(TCItems.WAND_RODS.get("silverwood")), null, Ingredient.of(TCItems.WAND_RODS.get("silverwood")), null, null),
                new AspectList().add(Aspects.ORDER, WandParts.rod("silverwood_staff").craftCost())));
        // WandRodIceStaff
        ALL.add(new ArcaneRecipe("ROD_ice_staff", new ItemStack(TCItems.STAFF_RODS.get("ice")),
                Arrays.asList(null, null, Ingredient.of(TCResources.get("primal_charm")), null, Ingredient.of(TCItems.WAND_RODS.get("ice")), null, Ingredient.of(TCItems.WAND_RODS.get("ice")), null, null),
                new AspectList().add(Aspects.ORDER, WandParts.rod("ice_staff").craftCost())));
        // WandRodQuartzStaff
        ALL.add(new ArcaneRecipe("ROD_quartz_staff", new ItemStack(TCItems.STAFF_RODS.get("quartz")),
                Arrays.asList(null, null, Ingredient.of(TCResources.get("primal_charm")), null, Ingredient.of(TCItems.WAND_RODS.get("quartz")), null, Ingredient.of(TCItems.WAND_RODS.get("quartz")), null, null),
                new AspectList().add(Aspects.ORDER, WandParts.rod("quartz_staff").craftCost())));
        // WandRodReedStaff
        ALL.add(new ArcaneRecipe("ROD_reed_staff", new ItemStack(TCItems.STAFF_RODS.get("reed")),
                Arrays.asList(null, null, Ingredient.of(TCResources.get("primal_charm")), null, Ingredient.of(TCItems.WAND_RODS.get("reed")), null, Ingredient.of(TCItems.WAND_RODS.get("reed")), null, null),
                new AspectList().add(Aspects.ORDER, WandParts.rod("reed_staff").craftCost())));
        // WandRodBlazeStaff
        ALL.add(new ArcaneRecipe("ROD_blaze_staff", new ItemStack(TCItems.STAFF_RODS.get("blaze")),
                Arrays.asList(null, null, Ingredient.of(TCResources.get("primal_charm")), null, Ingredient.of(TCItems.WAND_RODS.get("blaze")), null, Ingredient.of(TCItems.WAND_RODS.get("blaze")), null, null),
                new AspectList().add(Aspects.ORDER, WandParts.rod("blaze_staff").craftCost())));
        // WandRodBoneStaff
        ALL.add(new ArcaneRecipe("ROD_bone_staff", new ItemStack(TCItems.STAFF_RODS.get("bone")),
                Arrays.asList(null, null, Ingredient.of(TCResources.get("primal_charm")), null, Ingredient.of(TCItems.WAND_RODS.get("bone")), null, Ingredient.of(TCItems.WAND_RODS.get("bone")), null, null),
                new AspectList().add(Aspects.ORDER, WandParts.rod("bone_staff").craftCost())));
        // FocusFire
        ALL.add(new ArcaneRecipe("FOCUSFIRE", new ItemStack(TCItems.FOCI.get("fire")),
                Arrays.asList(Ingredient.of(TCItems.SHARDS.get("fire")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("fire")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(net.minecraft.world.item.Items.FIRE_CHARGE), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("fire")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("fire"))),
                new AspectList().add(Aspects.FIRE, 20).add(Aspects.ENTROPY, 10)));
        // FocusFrost
        ALL.add(new ArcaneRecipe("FOCUSFROST", new ItemStack(TCItems.FOCI.get("frost")),
                Arrays.asList(Ingredient.of(TCItems.SHARDS.get("water")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("water")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(net.minecraft.world.item.Items.DIAMOND), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("water")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("water"))),
                new AspectList().add(Aspects.WATER, 10).add(Aspects.ORDER, 10).add(Aspects.ENTROPY, 10)));
        // FocusShock
        ALL.add(new ArcaneRecipe("FOCUSSHOCK", new ItemStack(TCItems.FOCI.get("shock")),
                Arrays.asList(Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(net.minecraft.world.item.Items.POTATO), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("air"))),
                new AspectList().add(Aspects.AIR, 10).add(Aspects.ORDER, 10).add(Aspects.ENTROPY, 10)));
        // FocusTrade
        ALL.add(new ArcaneRecipe("FOCUSTRADE", new ItemStack(TCItems.FOCI.get("trade")),
                Arrays.asList(Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCResources.get("quicksilver")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARD_BALANCED)),
                new AspectList().add(Aspects.ORDER, 15).add(Aspects.ENTROPY, 15).add(Aspects.EARTH, 10)));
        // FocusExcavation
        ALL.add(new ArcaneRecipe("FOCUSEXCAVATION", new ItemStack(TCItems.FOCI.get("excavation")),
                Arrays.asList(Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(net.minecraft.world.item.Items.EMERALD), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("earth"))),
                new AspectList().add(Aspects.EARTH, 20).add(Aspects.ENTROPY, 5).add(Aspects.ORDER, 5)));
        // FocusPrimal
        ALL.add(new ArcaneRecipe("FOCUSPRIMAL", new ItemStack(TCItems.FOCI.get("primal")),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.DIAMOND), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(net.minecraft.world.item.Items.DIAMOND), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCResources.get("primal_charm")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(net.minecraft.world.item.Items.DIAMOND), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(net.minecraft.world.item.Items.DIAMOND)),
                new AspectList().add(Aspects.EARTH, 25).add(Aspects.ENTROPY, 25).add(Aspects.ORDER, 25).add(Aspects.AIR, 25).add(Aspects.FIRE, 25).add(Aspects.WATER, 25)));
        // Deconstructor
        ALL.add(new ArcaneRecipe("DECONSTRUCTOR", new ItemStack(TCBlocks.DECONSTRUCTION_TABLE.asItem()),
                Arrays.asList(null, Ingredient.of(TCItems.THAUMOMETER), null, Ingredient.of(net.minecraft.world.item.Items.GOLDEN_AXE), Ingredient.of(TCBlocks.TABLE.asItem()), Ingredient.of(net.minecraft.world.item.Items.GOLDEN_PICKAXE), null, null, null),
                new AspectList().add(Aspects.ENTROPY, 20)));
        // EnchantedFabric
        ALL.add(new ArcaneRecipe("ENCHFABRIC", new ItemStack(TCResources.get("enchanted_fabric")),
                Arrays.asList(null, Ingredient.of(net.minecraft.world.item.Items.STRING), null, Ingredient.of(net.minecraft.world.item.Items.STRING), Ingredient.of(net.minecraft.world.item.Items.WOOL.asList().toArray(new net.minecraft.world.item.Item[0])), Ingredient.of(net.minecraft.world.item.Items.STRING), null, Ingredient.of(net.minecraft.world.item.Items.STRING), null),
                new AspectList().add(Aspects.AIR, 1).add(Aspects.EARTH, 1).add(Aspects.FIRE, 1).add(Aspects.WATER, 1).add(Aspects.ORDER, 1).add(Aspects.ENTROPY, 1)));
        // Goggles
        ALL.add(new ArcaneRecipe("GOGGLES", new ItemStack(TCItems.GOGGLES),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(net.minecraft.world.item.Items.LEATHER), null, Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(TCItems.THAUMOMETER), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(TCItems.THAUMOMETER)),
                new AspectList().add(Aspects.AIR, 5).add(Aspects.FIRE, 5).add(Aspects.WATER, 5).add(Aspects.EARTH, 5).add(Aspects.ENTROPY, 3).add(Aspects.ORDER, 3)));
        // HungryChest
        ALL.add(new ArcaneRecipe("HUNGRYCHEST", new ItemStack(TCBlocks.HUNGRY_CHEST.asItem()),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.OAK_PLANKS), Ingredient.of(net.minecraft.world.level.block.Blocks.OAK_TRAPDOOR.asItem()), Ingredient.of(net.minecraft.world.item.Items.OAK_PLANKS), Ingredient.of(net.minecraft.world.item.Items.OAK_PLANKS), null, Ingredient.of(net.minecraft.world.item.Items.OAK_PLANKS), Ingredient.of(net.minecraft.world.item.Items.OAK_PLANKS), Ingredient.of(net.minecraft.world.item.Items.OAK_PLANKS), Ingredient.of(net.minecraft.world.item.Items.OAK_PLANKS)),
                new AspectList().add(Aspects.AIR, 5).add(Aspects.ORDER, 3).add(Aspects.ENTROPY, 3)));
        // GolemBell
        ALL.add(new ArcaneRecipe("GOLEMBELL", new ItemStack(TCItems.GOLEM_BELL),
                Arrays.asList(null, Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), null, Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(net.minecraft.world.item.Items.STICK), null, null),
                new AspectList().add(Aspects.ORDER, 5)));
        // CoreBlank
        ALL.add(new ArcaneRecipe("COREGATHER", new ItemStack(TCItems.GOLEM_CORE_BLANK),
                Arrays.asList(null, Ingredient.of(net.minecraft.world.item.Items.BRICK), null, Ingredient.of(net.minecraft.world.item.Items.BRICK), Ingredient.of(TCItems.NITOR), Ingredient.of(net.minecraft.world.item.Items.BRICK), null, Ingredient.of(net.minecraft.world.item.Items.BRICK), null),
                new AspectList().add(Aspects.ORDER, 5).add(Aspects.FIRE, 5)));
        // Filter
        ALL.add(new ArcaneRecipe("DISTILESSENTIA", new ItemStack(TCResources.get("vis_filter"), 2),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(TCBlocks.SILVERWOOD_PLANKS.asItem()), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), null, null, null, null, null, null),
                new AspectList().add(Aspects.ORDER, 5).add(Aspects.WATER, 5)));
        // AlchemyFurnace
        ALL.add(new ArcaneRecipe("DISTILESSENTIA", new ItemStack(TCBlocks.ALCHEMICAL_FURNACE.asItem()),
                Arrays.asList(Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()), Ingredient.of(TCBlocks.CRUCIBLE.asItem()), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()), Ingredient.of(net.minecraft.world.level.block.Blocks.FURNACE.asItem()), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem())),
                new AspectList().add(Aspects.FIRE, 5).add(Aspects.WATER, 5)));
        // Alembic
        ALL.add(new ArcaneRecipe("DISTILESSENTIA", new ItemStack(TCBlocks.ALEMBIC.asItem()),
                Arrays.asList(Ingredient.of(TCResources.get("vis_filter")), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.BUCKET), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), null, Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT)),
                new AspectList().add(Aspects.AIR, 5).add(Aspects.WATER, 5)));
        // Bellows
        ALL.add(new ArcaneRecipe("BELLOWS", new ItemStack(TCBlocks.BELLOWS.asItem()),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.OAK_PLANKS), Ingredient.of(net.minecraft.world.item.Items.OAK_PLANKS), null, Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.OAK_PLANKS), Ingredient.of(net.minecraft.world.item.Items.OAK_PLANKS), null),
                new AspectList().add(Aspects.AIR, 10).add(Aspects.ORDER, 5)));
        // Tube
        ALL.add(new ArcaneRecipe("TUBES", new ItemStack(TCBlocks.TUBE.asItem(), 8),
                Arrays.asList(null, Ingredient.of(TCResources.get("quicksilver")), null, Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.level.block.Blocks.GLASS.asItem()), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), null, Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), null),
                new AspectList().add(Aspects.WATER, 5).add(Aspects.ORDER, 5)));
        // TubeBuffer
        ALL.add(new ArcaneRecipe("CENTRIFUGE", new ItemStack(TCBlocks.TUBE_BUFFER.asItem()),
                Arrays.asList(Ingredient.of(TCItems.PHIAL), Ingredient.of(TCBlocks.TUBE_VALVE.asItem()), Ingredient.of(TCItems.PHIAL), Ingredient.of(TCBlocks.TUBE.asItem()), null, Ingredient.of(TCBlocks.TUBE.asItem()), Ingredient.of(TCItems.PHIAL), Ingredient.of(TCBlocks.TUBE_RESTRICT.asItem()), Ingredient.of(TCItems.PHIAL)),
                new AspectList().add(Aspects.WATER, 5).add(Aspects.ORDER, 5)));
        // AlchemicalConstruct
        ALL.add(new ArcaneRecipe("DISTILESSENTIA", new ItemStack(TCBlocks.ALCHEMICAL_CONSTRUCT.asItem()),
                Arrays.asList(Ingredient.of(TCBlocks.TUBE_VALVE.asItem()), Ingredient.of(TCBlocks.TUBE.asItem()), Ingredient.of(TCResources.get("vis_filter")), Ingredient.of(TCBlocks.TUBE.asItem()), Ingredient.of(TCBlocks.GREATWOOD_PLANKS.asItem()), Ingredient.of(TCBlocks.TUBE.asItem()), Ingredient.of(TCResources.get("vis_filter")), Ingredient.of(TCBlocks.TUBE.asItem()), Ingredient.of(TCBlocks.TUBE_VALVE.asItem())),
                new AspectList().add(Aspects.WATER, 5).add(Aspects.ORDER, 5)));
        // Centrifuge
        ALL.add(new ArcaneRecipe("CENTRIFUGE", new ItemStack(TCBlocks.CENTRIFUGE.asItem()),
                Arrays.asList(null, Ingredient.of(TCBlocks.TUBE.asItem()), null, Ingredient.of(TCBlocks.ALEMBIC.asItem()), Ingredient.of(TCBlocks.ALCHEMICAL_CONSTRUCT.asItem()), Ingredient.of(net.minecraft.world.level.block.Blocks.PISTON.asItem()), null, Ingredient.of(TCBlocks.TUBE.asItem()), null),
                new AspectList().add(Aspects.WATER, 5).add(Aspects.ORDER, 5).add(Aspects.ENTROPY, 5)));
        // EssentiaCrystalizer
        ALL.add(new ArcaneRecipe("ESSENTIACRYSTAL", new ItemStack(TCBlocks.ESSENTIA_CRYSTALIZER.asItem()),
                Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.level.block.Blocks.DISPENSER.asItem()), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(TCBlocks.ALCHEMICAL_CONSTRUCT.asItem()), Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(net.minecraft.world.item.Items.OAK_PLANKS), Ingredient.of(TCBlocks.TUBE.asItem()), Ingredient.of(net.minecraft.world.item.Items.OAK_PLANKS)),
                new AspectList().add(Aspects.WATER, 5).add(Aspects.EARTH, 15).add(Aspects.ORDER, 5)));
        // MirrorGlass
        ALL.add(ArcaneRecipe.loose("BASICARTIFACE", new ItemStack(TCResources.get("mirrored_glass")),
                Arrays.asList(Ingredient.of(TCResources.get("quicksilver")), Ingredient.of(net.minecraft.world.level.block.Blocks.GLASS_PANE.asItem())),
                new AspectList().add(Aspects.FIRE, 10).add(Aspects.EARTH, 10)));
        // TubeValve
        ALL.add(ArcaneRecipe.loose("TUBES", new ItemStack(TCBlocks.TUBE_VALVE.asItem()),
                Arrays.asList(Ingredient.of(TCBlocks.TUBE.asItem()), Ingredient.of(net.minecraft.world.level.block.Blocks.LEVER.asItem())),
                new AspectList().add(Aspects.WATER, 5).add(Aspects.ORDER, 5)));
        // TubeFilter
        ALL.add(ArcaneRecipe.loose("TUBEFILTER", new ItemStack(TCBlocks.TUBE_FILTER.asItem()),
                Arrays.asList(Ingredient.of(TCBlocks.TUBE.asItem()), Ingredient.of(TCResources.get("vis_filter"))),
                new AspectList().add(Aspects.WATER, 5).add(Aspects.ORDER, 16)));
        // TubeRestrict
        ALL.add(ArcaneRecipe.loose("TUBEFILTER", new ItemStack(TCBlocks.TUBE_RESTRICT.asItem()),
                Arrays.asList(Ingredient.of(TCBlocks.TUBE.asItem()), Ingredient.of(net.minecraft.world.item.Items.STONE)),
                new AspectList().add(Aspects.WATER, 5).add(Aspects.EARTH, 16)));
        // TubeOneway
        ALL.add(ArcaneRecipe.loose("TUBEFILTER", new ItemStack(TCBlocks.TUBE_ONEWAY.asItem()),
                Arrays.asList(Ingredient.of(TCBlocks.TUBE.asItem()), Ingredient.of(net.minecraft.world.item.Items.DYE.blue())),
                new AspectList().add(Aspects.WATER, 5).add(Aspects.ORDER, 8).add(Aspects.ENTROPY, 8)));
    }
}
