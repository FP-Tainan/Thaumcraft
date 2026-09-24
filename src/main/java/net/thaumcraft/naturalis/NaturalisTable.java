package net.thaumcraft.naturalis;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.thaumcraft.api.ThaumcraftApi;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;
import net.thaumcraft.research.Page;

/**
 * A tabela do Magia Naturalis: as pesquisas e as receitas do 0.5.0, lidas do MNResearch e do MNRecipes do
 * original. Arquivo gerado por {@code scratchpad/mn-tabela.js} — não se escreve à mão.
 */
public final class NaturalisTable {
    private NaturalisTable() {
    }

    /** As pesquisas, na ordem em que o original as registra. */
    public static void research() {
        ThaumcraftApi.research("MN_INTRO", Naturalis.CATEGORY)
                .at(0, 0)
                .icon(() -> new ItemStack(TCResources.get("primal_charm")))
                .round()
                .auto()
                .special()
                .pages(Page.text("tc.research_page.MN_INTRO.1"))
                .register();

        ThaumcraftApi.research("MN_CARPENTRY", Naturalis.CATEGORY)
                .at(-2, 2)
                .icon(() -> new ItemStack(NaturalisBlocks.GREATWOOD_GOLD_ORNAMENT.asItem()))
                .round()
                .auto()
                .pages(Page.text("tc.research_page.MN_CARPENTRY.1"), Page.crafting("GreatwoodOrn"), Page.crafting("PlankSilverwood"), Page.crafting("GreatwoodGoldOrn1"), Page.crafting("GreatwoodGoldOrn2"), Page.crafting("GreatwoodGoldTrim"))
                .register();

        ThaumcraftApi.research("MN_RESEARCH_LOG", Naturalis.CATEGORY)
                .aspects(new AspectList().add(Aspects.MIND, 3).add(Aspects.VOID, 3).add(Aspects.ORDER, 3))
                .at(-1, -2)
                .icon(() -> new ItemStack(NaturalisItems.RESEARCH_LOG))
                .hiddenParents("DECONSTRUCTOR")
                .round()
                .pages(Page.text("tc.research_page.MN_RESEARCH_LOG.1"), Page.crafting("ResearchLog"))
                .register();

        ThaumcraftApi.research("MN_SPECTACLES", Naturalis.CATEGORY)
                .aspects(new AspectList().add(Aspects.SENSES, 3).add(Aspects.AURA, 3).add(Aspects.MAGIC, 3))
                .at(-6, 0)
                .complexity(1)
                .icon(() -> new ItemStack(NaturalisItems.SPECTACLES))
                .parents("GOGGLES")
                .secondary()
                .pages(Page.text("tc.research_page.MN_SPECTACLES.1"), Page.crafting("Spectacles"))
                .register();

        ThaumcraftApi.research("MN_DARK_GOGGLES", Naturalis.CATEGORY)
                .aspects(new AspectList().add(Aspects.SENSES, 6).add(Aspects.AURA, 3).add(Aspects.MAGIC, 3).add(Aspects.DARKNESS, 4))
                .at(-7, 2)
                .complexity(2)
                .icon(() -> new ItemStack(NaturalisItems.DARK_CRYSTAL_GOGGLES))
                .parents("GOGGLES")
                .hiddenParents("MN_SPECTACLES")
                .warp(1)
                .pages(Page.text("tc.research_page.MN_DARK_GOGGLES.1"), Page.crafting("DarkGoggles"))
                .register();

        ThaumcraftApi.research("MN_ARCANE_KEYS", Naturalis.CATEGORY)
                .aspects(new AspectList().add(Aspects.TOOL, 4).add(Aspects.MIND, 3).add(Aspects.MECHANISM, 3))
                .at(-4, -3)
                .complexity(3)
                .icon(() -> new ItemStack(NaturalisItems.KEY_OF_UNRAVELING))
                .parents("WARDEDARCANA")
                .pages(Page.text("tc.research_page.MN_ARCANE_KEYS.1"), Page.crafting("ThaumiumKey1"), Page.text("tc.research_page.MN_ARCANE_KEYS.2"), Page.crafting("ThaumiumKey2"))
                .register();

        ThaumcraftApi.research("MN_ARCANE_CHEST", Naturalis.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 4).add(Aspects.MIND, 3).add(Aspects.MECHANISM, 3).add(Aspects.ARMOR, 3))
                .at(-7, -3)
                .complexity(3)
                .icon(() -> new ItemStack(NaturalisBlocks.ARCANE_CHEST_GREATWOOD.asItem()))
                .parents("WARDEDARCANA")
                .pages(Page.text("tc.research_page.MN_ARCANE_CHEST.1"), Page.crafting("ArcaneChest1"), Page.crafting("ArcaneChest2"))
                .register();

        ThaumcraftApi.research("MN_CONSTRUCTION_FOCUS", Naturalis.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 3).add(Aspects.CRAFT, 6).add(Aspects.ORDER, 2).add(Aspects.EARTH, 2))
                .at(4, -5)
                .complexity(2)
                .icon(() -> new ItemStack(NaturalisItems.BUILDER_FOCUS))
                .parents("FOCUSTRADE")
                .pages(Page.text("tc.research_page.MN_CONSTRUCTION_FOCUS.1"), Page.crafting("ConstructionFocus"))
                .register();

        ThaumcraftApi.research("MN_QUICKSILVER_STONE", Naturalis.CATEGORY)
                .aspects(new AspectList().add(Aspects.SENSES, 3).add(Aspects.EXCHANGE, 4).add(Aspects.AURA, 2))
                .at(6, -1)
                .complexity(2)
                .icon(() -> new ItemStack(NaturalisItems.QUICKSILVER_STONE))
                .parents("CRUCIBLE")
                .pages(Page.text("tc.research_page.MN_QUICKSILVER_STONE.1"), Page.crafting("StoneQuick"))
                .register();

        ThaumcraftApi.research("MN_ENDER_POUCH", Naturalis.CATEGORY)
                .aspects(new AspectList().add(Aspects.ELDRITCH, 3).add(Aspects.VOID, 3))
                .at(6, 3)
                .complexity(2)
                .icon(() -> new ItemStack(NaturalisItems.ENDER_POUCH))
                .parents("FOCUSPOUCH")
                .round()
                .pages(Page.text("tc.research_page.MN_ENDER_POUCH.1"), Page.crafting("EnderPouch"))
                .register();

        ThaumcraftApi.research("MN_PRISON_JAR", Naturalis.CATEGORY)
                .aspects(new AspectList().add(Aspects.TRAP, 6).add(Aspects.GREED, 3).add(Aspects.EXCHANGE, 3).add(Aspects.MOTION, 3))
                .at(-1, 4)
                .complexity(3)
                .icon(() -> new ItemStack(NaturalisBlocks.PRISON_JAR.asItem()))
                .hiddenParents("JARLABEL")
                .pages(Page.text("tc.research_page.MN_PRISON_JAR.1"), Page.crafting("JarPrison"))
                .register();

        ThaumcraftApi.research("MN_SICKLES", Naturalis.CATEGORY)
                .aspects(new AspectList().add(Aspects.TOOL, 3).add(Aspects.CROP, 3).add(Aspects.HARVEST, 3))
                .at(-4, 3)
                .complexity(1)
                .icon(() -> new ItemStack(NaturalisItems.THAUMIUM_SICKLE))
                .hiddenParents("THAUMIUM")
                .secondary()
                .pages(Page.text("tc.research_page.MN_SICKLES.1"), Page.crafting("ThaumiumSickle"), Page.crafting("VoidSickle"))
                .register();

    }

    /** As receitas: carregam itens, então só se montam quando o mundo abre. */
    public static void recipes() {
        ThaumcraftApi.bookRecipe("ThaumiumSickle", ThaumcraftApi.crafting(() -> new ItemStack(NaturalisItems.THAUMIUM_SICKLE),
                3, 3, java.util.List.of(java.util.List.<ItemStack>of(), java.util.List.of(new ItemStack(TCResources.get("thaumium_ingot"))), java.util.List.<ItemStack>of(), java.util.List.<ItemStack>of(), java.util.List.<ItemStack>of(), java.util.List.of(new ItemStack(TCResources.get("thaumium_ingot"))), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.STICK)), java.util.List.of(new ItemStack(TCResources.get("thaumium_ingot"))), java.util.List.<ItemStack>of())));
        ThaumcraftApi.bookRecipe("VoidSickle", ThaumcraftApi.crafting(() -> new ItemStack(NaturalisItems.VOID_SICKLE),
                3, 3, java.util.List.of(java.util.List.<ItemStack>of(), java.util.List.of(new ItemStack(TCResources.get("void_ingot"))), java.util.List.<ItemStack>of(), java.util.List.<ItemStack>of(), java.util.List.<ItemStack>of(), java.util.List.of(new ItemStack(TCResources.get("void_ingot"))), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.STICK)), java.util.List.of(new ItemStack(TCResources.get("void_ingot"))), java.util.List.<ItemStack>of())));
        ThaumcraftApi.bookRecipe("PlankSilverwood", ThaumcraftApi.crafting(() -> new ItemStack(NaturalisBlocks.SILVERWOOD_PLANKS_HORIZONTAL.asItem()),
                1, 2, java.util.List.of(java.util.List.of(new ItemStack(TCBlocks.SILVERWOOD_SLAB.asItem(), 6)), java.util.List.of(new ItemStack(TCBlocks.SILVERWOOD_SLAB.asItem(), 6)))));
        ThaumcraftApi.bookRecipe("GreatwoodOrn", ThaumcraftApi.crafting(() -> new ItemStack(NaturalisBlocks.GREATWOOD_ORNAMENT.asItem()),
                1, 2, java.util.List.of(java.util.List.of(new ItemStack(TCBlocks.GREATWOOD_SLAB.asItem(), 6)), java.util.List.of(new ItemStack(TCBlocks.GREATWOOD_SLAB.asItem(), 6)))));
        ThaumcraftApi.bookRecipe("GreatwoodGoldTrim", ThaumcraftApi.crafting(() -> new ItemStack(NaturalisBlocks.GREATWOOD_GOLD_TRIM.asItem(), 3),
                3, 3, java.util.List.of(java.util.List.of(new ItemStack(TCBlocks.GREATWOOD_SLAB.asItem(), 6)), java.util.List.of(new ItemStack(TCBlocks.GREATWOOD_SLAB.asItem(), 6)), java.util.List.of(new ItemStack(TCBlocks.GREATWOOD_SLAB.asItem(), 6)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET)), java.util.List.of(new ItemStack(TCBlocks.GREATWOOD_SLAB.asItem(), 6)), java.util.List.of(new ItemStack(TCBlocks.GREATWOOD_SLAB.asItem(), 6)), java.util.List.of(new ItemStack(TCBlocks.GREATWOOD_SLAB.asItem(), 6)))));
        ThaumcraftApi.bookRecipe("GreatwoodGoldOrn1", ThaumcraftApi.crafting(() -> new ItemStack(NaturalisBlocks.GREATWOOD_GOLD_ORNAMENT.asItem(), 4),
                3, 3, java.util.List.of(java.util.List.of(new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET)), java.util.List.of(new ItemStack(TCBlocks.GREATWOOD_PLANKS.asItem())), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET)), java.util.List.of(new ItemStack(TCBlocks.GREATWOOD_PLANKS.asItem())), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET)), java.util.List.of(new ItemStack(TCBlocks.GREATWOOD_PLANKS.asItem())), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET)), java.util.List.of(new ItemStack(TCBlocks.GREATWOOD_PLANKS.asItem())), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET)))));
        ThaumcraftApi.bookRecipe("GreatwoodGoldOrn2", ThaumcraftApi.crafting(() -> new ItemStack(NaturalisBlocks.GREATWOOD_GOLD_ORNAMENT_2.asItem(), 4),
                3, 3, java.util.List.of(java.util.List.of(new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET)), java.util.List.of(new ItemStack(TCBlocks.GREATWOOD_PLANKS.asItem())), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET)), java.util.List.of(new ItemStack(TCBlocks.GREATWOOD_PLANKS.asItem())), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT)), java.util.List.of(new ItemStack(TCBlocks.GREATWOOD_PLANKS.asItem())), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET)), java.util.List.of(new ItemStack(TCBlocks.GREATWOOD_PLANKS.asItem())), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.GOLD_NUGGET)))));
        ThaumcraftApi.bookRecipe("ResearchLog", ThaumcraftApi.arcane("MN_RESEARCH_LOG",
                new ItemStack(NaturalisItems.RESEARCH_LOG), new AspectList().add(Aspects.ORDER, 20).add(Aspects.ENTROPY, 20).add(Aspects.AIR, 20).add(Aspects.EARTH, 20).add(Aspects.FIRE, 20).add(Aspects.WATER, 20),
                java.util.Arrays.asList(Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(TCItems.SHARDS.get("fire")), Ingredient.of(TCItems.SHARDS.get("water")), Ingredient.of(net.minecraft.world.item.Items.BOOK), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(TCItems.SHARDS.get("order")), Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(TCItems.SHARDS.get("entropy")))));
        ThaumcraftApi.bookRecipe("ThaumiumKey1", ThaumcraftApi.arcane("MN_ARCANE_KEYS",
                new ItemStack(NaturalisItems.KEY_OF_UNRAVELING, 2), new AspectList().add(Aspects.ORDER, 2).add(Aspects.ENTROPY, 2).add(Aspects.AIR, 2).add(Aspects.EARTH, 2).add(Aspects.FIRE, 2).add(Aspects.WATER, 2),
                java.util.Arrays.asList(Ingredient.of(TCResources.get("thaumium_nugget")), Ingredient.of(net.minecraft.world.item.Items.IRON_NUGGET), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCResources.get("thaumium_nugget")), null, null)));
        ThaumcraftApi.bookRecipe("ThaumiumKey2", ThaumcraftApi.arcane("MN_ARCANE_KEYS",
                new ItemStack(NaturalisItems.KEY_OF_ENDORSING, 2), new AspectList().add(Aspects.ORDER, 2).add(Aspects.ENTROPY, 2).add(Aspects.AIR, 2).add(Aspects.EARTH, 2).add(Aspects.FIRE, 2).add(Aspects.WATER, 2),
                java.util.Arrays.asList(Ingredient.of(TCResources.get("thaumium_nugget")), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCResources.get("thaumium_nugget")), null, null)));
        ThaumcraftApi.bookRecipe("Spectacles", ThaumcraftApi.arcane("MN_SPECTACLES",
                new ItemStack(NaturalisItems.SPECTACLES), new AspectList().add(Aspects.ORDER, 5).add(Aspects.ENTROPY, 5).add(Aspects.AIR, 5).add(Aspects.EARTH, 5).add(Aspects.FIRE, 5).add(Aspects.WATER, 5),
                java.util.Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(TCItems.THAUMOMETER), Ingredient.of(TCItems.GOGGLES), Ingredient.of(TCItems.THAUMOMETER))));
        ThaumcraftApi.bookRecipe("ArcaneChest1", ThaumcraftApi.arcane("MN_ARCANE_CHEST",
                new ItemStack(NaturalisBlocks.ARCANE_CHEST_GREATWOOD.asItem()), new AspectList().add(Aspects.WATER, 20).add(Aspects.ORDER, 15).add(Aspects.EARTH, 15).add(Aspects.FIRE, 10),
                java.util.Arrays.asList(Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCItems.ZOMBIE_BRAIN), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCBlocks.GREATWOOD_PLANKS.asItem()), Ingredient.of(net.minecraft.world.level.block.Blocks.CHEST.asItem()), Ingredient.of(TCBlocks.GREATWOOD_PLANKS.asItem()), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCBlocks.GREATWOOD_PLANKS.asItem()), Ingredient.of(TCResources.get("thaumium_ingot")))));
        ThaumcraftApi.bookRecipe("ArcaneChest2", ThaumcraftApi.arcane("MN_ARCANE_CHEST",
                new ItemStack(NaturalisBlocks.ARCANE_CHEST_SILVERWOOD.asItem()), new AspectList().add(Aspects.WATER, 20).add(Aspects.ORDER, 15).add(Aspects.EARTH, 15).add(Aspects.FIRE, 10),
                java.util.Arrays.asList(Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCItems.ZOMBIE_BRAIN), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(NaturalisBlocks.SILVERWOOD_PLANKS_HORIZONTAL.asItem()), Ingredient.of(net.minecraft.world.level.block.Blocks.CHEST.asItem()), Ingredient.of(NaturalisBlocks.SILVERWOOD_PLANKS_HORIZONTAL.asItem()), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(NaturalisBlocks.SILVERWOOD_PLANKS_HORIZONTAL.asItem()), Ingredient.of(TCResources.get("thaumium_ingot")))));
        ThaumcraftApi.bookRecipe("DarkGoggles", ThaumcraftApi.infusion("MN_DARK_GOGGLES",
                new ItemStack(NaturalisItems.DARK_CRYSTAL_GOGGLES), 3, new AspectList().add(Aspects.SENSES, 32).add(Aspects.ARMOR, 16).add(Aspects.DARKNESS, 32),
                Ingredient.of(TCItems.GOGGLES),
                java.util.List.of(Ingredient.of(TCItems.SHARDS.get("entropy")), Ingredient.of(TCItems.SHARDS.get("entropy")), Ingredient.of(net.minecraft.world.item.Items.SPIDER_EYE), Ingredient.of(net.minecraft.world.item.Items.SPIDER_EYE), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(TCItems.ZOMBIE_BRAIN))));
        ThaumcraftApi.bookRecipe("ConstructionFocus", ThaumcraftApi.infusion("MN_CONSTRUCTION_FOCUS",
                new ItemStack(NaturalisItems.BUILDER_FOCUS), 5, new AspectList().add(Aspects.CRAFT, 32).add(Aspects.TOOL, 16).add(Aspects.EXCHANGE, 8).add(Aspects.MECHANISM, 3),
                Ingredient.of(TCItems.FOCI.get("trade")),
                java.util.List.of(Ingredient.of(TCItems.SHARDS.get("order")), Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(TCItems.SHARDS.get("entropy")), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()), Ingredient.of(TCItems.ELEMENTAL_SHOVEL), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()), Ingredient.of(TCResources.get("quicksilver")), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()))));
        ThaumcraftApi.bookRecipe("EnderPouch", ThaumcraftApi.infusion("MN_ENDER_POUCH",
                new ItemStack(NaturalisItems.ENDER_POUCH), 1, new AspectList().add(Aspects.ELDRITCH, 8).add(Aspects.VOID, 8).add(Aspects.TRAVEL, 8).add(Aspects.EXCHANGE, 3),
                Ingredient.of(TCItems.FOCUS_POUCH),
                java.util.List.of(Ingredient.of(net.minecraft.world.item.Items.ENDER_PEARL), Ingredient.of(net.minecraft.world.level.block.Blocks.ENDER_CHEST.asItem()), Ingredient.of(net.minecraft.world.item.Items.ENDER_PEARL))));
        ThaumcraftApi.bookRecipe("JarPrison", ThaumcraftApi.infusion("MN_PRISON_JAR",
                new ItemStack(NaturalisBlocks.PRISON_JAR.asItem()), 2, new AspectList().add(Aspects.ELDRITCH, 8).add(Aspects.VOID, 8).add(Aspects.ARMOR, 8).add(Aspects.EXCHANGE, 8),
                Ingredient.of(TCBlocks.JAR.asItem()),
                java.util.List.of(Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.ENDER_PEARL), Ingredient.of(net.minecraft.world.item.Items.LEAD), Ingredient.of(net.minecraft.world.item.Items.ENDER_PEARL))));
        ThaumcraftApi.bookRecipe("StonePheno", ThaumcraftApi.infusion("MN_MUTATION_STONE",
                new ItemStack(NaturalisItems.MUTATION_STONE), 2, new AspectList().add(Aspects.EXCHANGE, 32),
                Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()),
                java.util.List.of(Ingredient.of(net.minecraft.world.item.Items.EMERALD), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()))));
        ThaumcraftApi.bookRecipe("StoneQuick", ThaumcraftApi.infusion("MN_QUICKSILVER_STONE",
                new ItemStack(NaturalisItems.QUICKSILVER_STONE), 2, new AspectList().add(Aspects.EXCHANGE, 16).add(Aspects.WATER, 16).add(Aspects.MAGIC, 8).add(Aspects.FLESH, 6),
                Ingredient.of(TCResources.get("quicksilver")),
                java.util.List.of(Ingredient.of(net.minecraft.world.item.Items.GLOWSTONE_DUST), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()), Ingredient.of(net.minecraft.world.item.Items.GLOWSTONE_DUST), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()))));
    }
}
