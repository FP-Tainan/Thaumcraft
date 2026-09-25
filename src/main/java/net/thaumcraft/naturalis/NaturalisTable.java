package net.thaumcraft.naturalis;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.thaumcraft.api.ThaumcraftApi;
import net.thaumcraft.crafting.MutationRecipe;
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

        ThaumcraftApi.research("MN_TRANSCRIBING_TABLE", Naturalis.CATEGORY)
                .aspects(new AspectList().add(Aspects.MIND, 3).add(Aspects.VOID, 3).add(Aspects.ORDER, 3))
                .at(-2, -4)
                .icon(() -> new ItemStack(NaturalisBlocks.TRANSCRIBING_TABLE.asItem()))
                .parents("MN_RESEARCH_LOG")
                .pages(Page.text("tc.research_page.MN_TRANSCRIBING_TABLE.1"), Page.crafting("TranscribingTable"))
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

        ThaumcraftApi.research("MN_REVENANT_FOCUS", Naturalis.CATEGORY)
                .aspects(new AspectList().add(Aspects.TRAVEL, 3).add(Aspects.BEAST, 6).add(Aspects.UNDEAD, 3).add(Aspects.MAGIC, 3))
                .at(3, -7)
                .complexity(2)
                .icon(() -> new ItemStack(NaturalisItems.REVENANT_FOCUS))
                .hiddenParents("BASICTHAUMATURGY", "INFUSION")
                .hidden()
                .warp(2)
                .pages(Page.text("tc.research_page.MN_REVENANT_FOCUS.1"), Page.crafting("RevenantFocus"))
                .register();

        ThaumcraftApi.research("MN_MUTATION_STONE", Naturalis.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 3).add(Aspects.EXCHANGE, 4).add(Aspects.EARTH, 2))
                .at(4, -3)
                .complexity(2)
                .icon(() -> new ItemStack(NaturalisItems.MUTATION_STONE))
                .parents("FOCUSTRADE", "CRUCIBLE")
                .secondary()
                .pages(Page.text("tc.research_page.MN_MUTATION_STONE.1"), Page.crafting("StonePheno"), Page.crafting("WoodConversion1", "WoodConversion2", "WoodConversion3", "WoodConversion4", "WoodConversion5", "WoodConversion6"), Page.crafting("ColorConversion1", "ColorConversion2", "ColorConversion3", "ColorConversion4", "ColorConversion5", "ColorConversion6", "ColorConversion7", "ColorConversion8", "ColorConversion9", "ColorConversion10", "ColorConversion11", "ColorConversion12", "ColorConversion13", "ColorConversion14", "ColorConversion15", "ColorConversion16", "ColorConversion17", "ColorConversion18", "ColorConversion19", "ColorConversion20", "ColorConversion21", "ColorConversion22", "ColorConversion23", "ColorConversion24", "ColorConversion25", "ColorConversion26", "ColorConversion27", "ColorConversion28", "ColorConversion29", "ColorConversion30", "ColorConversion31", "ColorConversion32"))
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

        ThaumcraftApi.research("MN_EVIL_TRUNK", Naturalis.CATEGORY)
                .aspects(new AspectList().add(Aspects.SOUL, 3).add(Aspects.BEAST, 3).add(Aspects.TAINT, 3))
                .at(2, 5)
                .complexity(2)
                .icon(() -> new ItemStack(NaturalisItems.TRUNK_SPAWNER_CORRUPTED))
                .parents("TRAVELTRUNK")
                .warp(1)
                .pages(Page.text("tc.research_page.MN_EVIL_TRUNK.1"), Page.crafting("CorruptedTrunk"), Page.crafting("SinisterTrunk"), Page.crafting("DemonicTrunk"), Page.crafting("TaintedTrunk"))
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

        ThaumcraftApi.research("MN_SICKLE_OF_ABUNDANCE", Naturalis.CATEGORY)
                .aspects(new AspectList().add(Aspects.TOOL, 3).add(Aspects.CROP, 3).add(Aspects.HARVEST, 3).add(Aspects.GREED, 6))
                .at(-5, 5)
                .complexity(2)
                .icon(() -> new ItemStack(NaturalisItems.ELEMENTAL_SICKLE))
                .parents("MN_SICKLES")
                .hiddenParents("INFUSION")
                .pages(Page.text("tc.research_page.MN_SICKLE_OF_ABUNDANCE.1"), Page.crafting("ElementalSickle"))
                .register();

        ThaumcraftApi.research("MN_GEO_OCCULTISM", Naturalis.CATEGORY)
                .aspects(new AspectList().add(Aspects.AURA, 4).add(Aspects.EXCHANGE, 3).add(Aspects.WEATHER, 3).add(Aspects.MAGIC, 6).add(Aspects.EARTH, 2).add(Aspects.AIR, 2))
                .at(0, -5)
                .icon(() -> new ItemStack(NaturalisBlocks.GEO_PYLON.asItem()))
                .hiddenParents("INFUSION", "MN_MUTATION_STONE")
                .pages(Page.crafting("GeoPylon"), Page.crafting("BiomeReport"))
                .register();

    }

    /** As trocas da Pedra do Catalisador Fenomorfo, que a bancada faz sem gastar a pedra. */
    public static void mutations() {
        MutationRecipe.add(TCBlocks.GREATWOOD_PLANKS.asItem(), NaturalisBlocks.GREATWOOD_PLANKS_HORIZONTAL.asItem());
        MutationRecipe.add(NaturalisBlocks.GREATWOOD_PLANKS_HORIZONTAL.asItem(), NaturalisBlocks.GREATWOOD_ORNAMENT.asItem());
        MutationRecipe.add(TCBlocks.SILVERWOOD_PLANKS.asItem(), NaturalisBlocks.SILVERWOOD_PLANKS_HORIZONTAL.asItem());
        MutationRecipe.add(NaturalisBlocks.SILVERWOOD_PLANKS_HORIZONTAL.asItem(), NaturalisBlocks.SILVERWOOD_PLANKS_VERTICAL.asItem());
        MutationRecipe.add(NaturalisBlocks.GREATWOOD_ORNAMENT.asItem(), TCBlocks.GREATWOOD_PLANKS.asItem());
        MutationRecipe.add(NaturalisBlocks.SILVERWOOD_PLANKS_VERTICAL.asItem(), TCBlocks.SILVERWOOD_PLANKS.asItem());
        MutationRecipe.add(net.minecraft.world.item.Items.DYED_TERRACOTTA.black(), net.minecraft.world.item.Items.DYED_TERRACOTTA.white());
        MutationRecipe.add(net.minecraft.world.item.Items.DYED_TERRACOTTA.red(), net.minecraft.world.item.Items.DYED_TERRACOTTA.orange());
        MutationRecipe.add(net.minecraft.world.item.Items.DYED_TERRACOTTA.green(), net.minecraft.world.item.Items.DYED_TERRACOTTA.magenta());
        MutationRecipe.add(net.minecraft.world.item.Items.DYED_TERRACOTTA.brown(), net.minecraft.world.item.Items.DYED_TERRACOTTA.lightBlue());
        MutationRecipe.add(net.minecraft.world.item.Items.DYED_TERRACOTTA.blue(), net.minecraft.world.item.Items.DYED_TERRACOTTA.yellow());
        MutationRecipe.add(net.minecraft.world.item.Items.DYED_TERRACOTTA.purple(), net.minecraft.world.item.Items.DYED_TERRACOTTA.lime());
        MutationRecipe.add(net.minecraft.world.item.Items.DYED_TERRACOTTA.cyan(), net.minecraft.world.item.Items.DYED_TERRACOTTA.pink());
        MutationRecipe.add(net.minecraft.world.item.Items.DYED_TERRACOTTA.lightGray(), net.minecraft.world.item.Items.DYED_TERRACOTTA.gray());
        MutationRecipe.add(net.minecraft.world.item.Items.DYED_TERRACOTTA.gray(), net.minecraft.world.item.Items.DYED_TERRACOTTA.lightGray());
        MutationRecipe.add(net.minecraft.world.item.Items.DYED_TERRACOTTA.pink(), net.minecraft.world.item.Items.DYED_TERRACOTTA.cyan());
        MutationRecipe.add(net.minecraft.world.item.Items.DYED_TERRACOTTA.lime(), net.minecraft.world.item.Items.DYED_TERRACOTTA.purple());
        MutationRecipe.add(net.minecraft.world.item.Items.DYED_TERRACOTTA.yellow(), net.minecraft.world.item.Items.DYED_TERRACOTTA.blue());
        MutationRecipe.add(net.minecraft.world.item.Items.DYED_TERRACOTTA.lightBlue(), net.minecraft.world.item.Items.DYED_TERRACOTTA.brown());
        MutationRecipe.add(net.minecraft.world.item.Items.DYED_TERRACOTTA.magenta(), net.minecraft.world.item.Items.DYED_TERRACOTTA.green());
        MutationRecipe.add(net.minecraft.world.item.Items.DYED_TERRACOTTA.orange(), net.minecraft.world.item.Items.DYED_TERRACOTTA.red());
        MutationRecipe.add(net.minecraft.world.item.Items.DYED_TERRACOTTA.white(), net.minecraft.world.item.Items.DYED_TERRACOTTA.black());
        MutationRecipe.add(net.minecraft.world.item.Items.WOOL.white(), net.minecraft.world.item.Items.WOOL.black());
        MutationRecipe.add(net.minecraft.world.item.Items.WOOL.orange(), net.minecraft.world.item.Items.WOOL.red());
        MutationRecipe.add(net.minecraft.world.item.Items.WOOL.magenta(), net.minecraft.world.item.Items.WOOL.green());
        MutationRecipe.add(net.minecraft.world.item.Items.WOOL.lightBlue(), net.minecraft.world.item.Items.WOOL.brown());
        MutationRecipe.add(net.minecraft.world.item.Items.WOOL.yellow(), net.minecraft.world.item.Items.WOOL.blue());
        MutationRecipe.add(net.minecraft.world.item.Items.WOOL.lime(), net.minecraft.world.item.Items.WOOL.purple());
        MutationRecipe.add(net.minecraft.world.item.Items.WOOL.pink(), net.minecraft.world.item.Items.WOOL.cyan());
        MutationRecipe.add(net.minecraft.world.item.Items.WOOL.gray(), net.minecraft.world.item.Items.WOOL.lightGray());
        MutationRecipe.add(net.minecraft.world.item.Items.WOOL.lightGray(), net.minecraft.world.item.Items.WOOL.gray());
        MutationRecipe.add(net.minecraft.world.item.Items.WOOL.cyan(), net.minecraft.world.item.Items.WOOL.pink());
        MutationRecipe.add(net.minecraft.world.item.Items.WOOL.purple(), net.minecraft.world.item.Items.WOOL.lime());
        MutationRecipe.add(net.minecraft.world.item.Items.WOOL.blue(), net.minecraft.world.item.Items.WOOL.yellow());
        MutationRecipe.add(net.minecraft.world.item.Items.WOOL.brown(), net.minecraft.world.item.Items.WOOL.lightBlue());
        MutationRecipe.add(net.minecraft.world.item.Items.WOOL.green(), net.minecraft.world.item.Items.WOOL.magenta());
        MutationRecipe.add(net.minecraft.world.item.Items.WOOL.red(), net.minecraft.world.item.Items.WOOL.orange());
        MutationRecipe.add(net.minecraft.world.item.Items.WOOL.black(), net.minecraft.world.item.Items.WOOL.white());
    }

    /** As receitas: carregam itens, então só se montam quando o mundo abre. */
    public static void recipes() {
        ThaumcraftApi.bookRecipe("ThaumiumSickle", ThaumcraftApi.crafting(() -> new ItemStack(NaturalisItems.THAUMIUM_SICKLE),
                3, 3, java.util.List.of(java.util.List.<ItemStack>of(), java.util.List.of(new ItemStack(TCResources.get("thaumium_ingot"))), java.util.List.<ItemStack>of(), java.util.List.<ItemStack>of(), java.util.List.<ItemStack>of(), java.util.List.of(new ItemStack(TCResources.get("thaumium_ingot"))), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.STICK)), java.util.List.of(new ItemStack(TCResources.get("thaumium_ingot"))), java.util.List.<ItemStack>of())));
        ThaumcraftApi.bookRecipe("VoidSickle", ThaumcraftApi.crafting(() -> new ItemStack(NaturalisItems.VOID_SICKLE),
                3, 3, java.util.List.of(java.util.List.<ItemStack>of(), java.util.List.of(new ItemStack(TCResources.get("void_ingot"))), java.util.List.<ItemStack>of(), java.util.List.<ItemStack>of(), java.util.List.<ItemStack>of(), java.util.List.of(new ItemStack(TCResources.get("void_ingot"))), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.STICK)), java.util.List.of(new ItemStack(TCResources.get("void_ingot"))), java.util.List.<ItemStack>of())));
        ThaumcraftApi.bookRecipe("GreatwoodSlab1", ThaumcraftApi.crafting(() -> new ItemStack(TCBlocks.GREATWOOD_SLAB.asItem(), 6),
                3, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisBlocks.GREATWOOD_PLANKS_HORIZONTAL.asItem())), java.util.List.of(new ItemStack(NaturalisBlocks.GREATWOOD_PLANKS_HORIZONTAL.asItem())), java.util.List.of(new ItemStack(NaturalisBlocks.GREATWOOD_PLANKS_HORIZONTAL.asItem())))));
        ThaumcraftApi.bookRecipe("GreatwoodSlab2", ThaumcraftApi.crafting(() -> new ItemStack(TCBlocks.GREATWOOD_SLAB.asItem(), 6),
                3, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisBlocks.GREATWOOD_ORNAMENT.asItem())), java.util.List.of(new ItemStack(NaturalisBlocks.GREATWOOD_ORNAMENT.asItem())), java.util.List.of(new ItemStack(NaturalisBlocks.GREATWOOD_ORNAMENT.asItem())))));
        ThaumcraftApi.bookRecipe("SilverwoodSlab1", ThaumcraftApi.crafting(() -> new ItemStack(TCBlocks.SILVERWOOD_SLAB.asItem(), 6),
                3, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisBlocks.SILVERWOOD_PLANKS_HORIZONTAL.asItem())), java.util.List.of(new ItemStack(NaturalisBlocks.SILVERWOOD_PLANKS_HORIZONTAL.asItem())), java.util.List.of(new ItemStack(NaturalisBlocks.SILVERWOOD_PLANKS_HORIZONTAL.asItem())))));
        ThaumcraftApi.bookRecipe("SilverwoodSlab2", ThaumcraftApi.crafting(() -> new ItemStack(TCBlocks.SILVERWOOD_SLAB.asItem(), 6),
                3, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisBlocks.SILVERWOOD_PLANKS_VERTICAL.asItem())), java.util.List.of(new ItemStack(NaturalisBlocks.SILVERWOOD_PLANKS_VERTICAL.asItem())), java.util.List.of(new ItemStack(NaturalisBlocks.SILVERWOOD_PLANKS_VERTICAL.asItem())))));
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
        ThaumcraftApi.bookRecipe("WoodConversion1", ThaumcraftApi.crafting(() -> new ItemStack(NaturalisBlocks.GREATWOOD_PLANKS_HORIZONTAL.asItem()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(TCBlocks.GREATWOOD_PLANKS.asItem())))));
        ThaumcraftApi.bookRecipe("WoodConversion2", ThaumcraftApi.crafting(() -> new ItemStack(NaturalisBlocks.GREATWOOD_ORNAMENT.asItem()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(NaturalisBlocks.GREATWOOD_PLANKS_HORIZONTAL.asItem())))));
        ThaumcraftApi.bookRecipe("WoodConversion3", ThaumcraftApi.crafting(() -> new ItemStack(NaturalisBlocks.SILVERWOOD_PLANKS_HORIZONTAL.asItem()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(TCBlocks.SILVERWOOD_PLANKS.asItem())))));
        ThaumcraftApi.bookRecipe("WoodConversion4", ThaumcraftApi.crafting(() -> new ItemStack(NaturalisBlocks.SILVERWOOD_PLANKS_VERTICAL.asItem()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(NaturalisBlocks.SILVERWOOD_PLANKS_HORIZONTAL.asItem())))));
        ThaumcraftApi.bookRecipe("WoodConversion5", ThaumcraftApi.crafting(() -> new ItemStack(TCBlocks.GREATWOOD_PLANKS.asItem()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(NaturalisBlocks.GREATWOOD_ORNAMENT.asItem())))));
        ThaumcraftApi.bookRecipe("WoodConversion6", ThaumcraftApi.crafting(() -> new ItemStack(TCBlocks.SILVERWOOD_PLANKS.asItem()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(NaturalisBlocks.SILVERWOOD_PLANKS_VERTICAL.asItem())))));
        ThaumcraftApi.bookRecipe("ColorConversion1", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.white()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.black())))));
        ThaumcraftApi.bookRecipe("ColorConversion2", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.orange()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.red())))));
        ThaumcraftApi.bookRecipe("ColorConversion3", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.magenta()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.green())))));
        ThaumcraftApi.bookRecipe("ColorConversion4", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.lightBlue()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.brown())))));
        ThaumcraftApi.bookRecipe("ColorConversion5", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.yellow()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.blue())))));
        ThaumcraftApi.bookRecipe("ColorConversion6", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.lime()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.purple())))));
        ThaumcraftApi.bookRecipe("ColorConversion7", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.pink()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.cyan())))));
        ThaumcraftApi.bookRecipe("ColorConversion8", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.gray()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.lightGray())))));
        ThaumcraftApi.bookRecipe("ColorConversion9", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.lightGray()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.gray())))));
        ThaumcraftApi.bookRecipe("ColorConversion10", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.cyan()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.pink())))));
        ThaumcraftApi.bookRecipe("ColorConversion11", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.purple()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.lime())))));
        ThaumcraftApi.bookRecipe("ColorConversion12", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.blue()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.yellow())))));
        ThaumcraftApi.bookRecipe("ColorConversion13", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.brown()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.lightBlue())))));
        ThaumcraftApi.bookRecipe("ColorConversion14", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.green()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.magenta())))));
        ThaumcraftApi.bookRecipe("ColorConversion15", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.red()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.orange())))));
        ThaumcraftApi.bookRecipe("ColorConversion16", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.black()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.DYED_TERRACOTTA.white())))));
        ThaumcraftApi.bookRecipe("ColorConversion17", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.WOOL.black()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.white())))));
        ThaumcraftApi.bookRecipe("ColorConversion18", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.WOOL.red()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.orange())))));
        ThaumcraftApi.bookRecipe("ColorConversion19", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.WOOL.green()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.magenta())))));
        ThaumcraftApi.bookRecipe("ColorConversion20", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.WOOL.brown()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.lightBlue())))));
        ThaumcraftApi.bookRecipe("ColorConversion21", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.WOOL.blue()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.yellow())))));
        ThaumcraftApi.bookRecipe("ColorConversion22", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.WOOL.purple()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.lime())))));
        ThaumcraftApi.bookRecipe("ColorConversion23", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.WOOL.cyan()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.pink())))));
        ThaumcraftApi.bookRecipe("ColorConversion24", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.WOOL.lightGray()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.gray())))));
        ThaumcraftApi.bookRecipe("ColorConversion25", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.WOOL.gray()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.lightGray())))));
        ThaumcraftApi.bookRecipe("ColorConversion26", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.WOOL.pink()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.cyan())))));
        ThaumcraftApi.bookRecipe("ColorConversion27", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.WOOL.lime()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.purple())))));
        ThaumcraftApi.bookRecipe("ColorConversion28", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.WOOL.yellow()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.blue())))));
        ThaumcraftApi.bookRecipe("ColorConversion29", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.WOOL.lightBlue()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.brown())))));
        ThaumcraftApi.bookRecipe("ColorConversion30", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.WOOL.magenta()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.green())))));
        ThaumcraftApi.bookRecipe("ColorConversion31", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.WOOL.orange()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.red())))));
        ThaumcraftApi.bookRecipe("ColorConversion32", ThaumcraftApi.crafting(() -> new ItemStack(net.minecraft.world.item.Items.WOOL.white()),
                2, 1, java.util.List.of(java.util.List.of(new ItemStack(NaturalisItems.MUTATION_STONE)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.black())))));
        ThaumcraftApi.bookRecipe("BiomeReport", ThaumcraftApi.arcane("MN_GEO_OCCULTISM",
                new ItemStack(NaturalisItems.BIOME_SAMPLER), new AspectList().add(Aspects.ORDER, 4).add(Aspects.ENTROPY, 2).add(Aspects.AIR, 2).add(Aspects.EARTH, 4).add(Aspects.FIRE, 2).add(Aspects.WATER, 2),
                java.util.Arrays.asList(null, Ingredient.of(net.minecraft.world.item.Items.STRING), null, Ingredient.of(TCItems.THAUMOMETER), Ingredient.of(net.minecraft.world.item.Items.BOOK), Ingredient.of(TCItems.SCRIBING_TOOLS), null, Ingredient.of(net.minecraft.world.item.Items.STRING), null)));
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
        ThaumcraftApi.bookRecipe("TranscribingTable", ThaumcraftApi.arcane("MN_TRANSCRIBING_TABLE",
                new ItemStack(NaturalisBlocks.TRANSCRIBING_TABLE.asItem()), new AspectList().add(Aspects.ORDER, 30).add(Aspects.ENTROPY, 30).add(Aspects.AIR, 30).add(Aspects.EARTH, 30).add(Aspects.FIRE, 30).add(Aspects.WATER, 30),
                java.util.Arrays.asList(Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(TCItems.SCRIBING_TOOLS), Ingredient.of(TCItems.SHARDS.get("fire")), Ingredient.of(TCItems.SHARDS.get("water")), Ingredient.of(TCBlocks.DECONSTRUCTION_TABLE.asItem()), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(TCItems.SHARDS.get("order")), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone").asItem()), Ingredient.of(TCItems.SHARDS.get("entropy")))));
        ThaumcraftApi.bookRecipe("ArcaneChest1", ThaumcraftApi.arcane("MN_ARCANE_CHEST",
                new ItemStack(NaturalisBlocks.ARCANE_CHEST_GREATWOOD.asItem()), new AspectList().add(Aspects.WATER, 20).add(Aspects.ORDER, 15).add(Aspects.EARTH, 15).add(Aspects.FIRE, 10),
                java.util.Arrays.asList(Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCItems.ZOMBIE_BRAIN), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCBlocks.GREATWOOD_PLANKS.asItem()), Ingredient.of(net.minecraft.world.level.block.Blocks.CHEST.asItem()), Ingredient.of(TCBlocks.GREATWOOD_PLANKS.asItem()), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCBlocks.GREATWOOD_PLANKS.asItem()), Ingredient.of(TCResources.get("thaumium_ingot")))));
        ThaumcraftApi.bookRecipe("ArcaneChest2", ThaumcraftApi.arcane("MN_ARCANE_CHEST",
                new ItemStack(NaturalisBlocks.ARCANE_CHEST_SILVERWOOD.asItem()), new AspectList().add(Aspects.WATER, 20).add(Aspects.ORDER, 15).add(Aspects.EARTH, 15).add(Aspects.FIRE, 10),
                java.util.Arrays.asList(Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCItems.ZOMBIE_BRAIN), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(NaturalisBlocks.SILVERWOOD_PLANKS_HORIZONTAL.asItem()), Ingredient.of(net.minecraft.world.level.block.Blocks.CHEST.asItem()), Ingredient.of(NaturalisBlocks.SILVERWOOD_PLANKS_HORIZONTAL.asItem()), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(NaturalisBlocks.SILVERWOOD_PLANKS_HORIZONTAL.asItem()), Ingredient.of(TCResources.get("thaumium_ingot")))));
        ThaumcraftApi.bookRecipe("GeoPylon", ThaumcraftApi.infusion("MN_GEO_OCCULTISM",
                new ItemStack(NaturalisBlocks.GEO_PYLON.asItem()), 8, new AspectList().add(Aspects.WEATHER, 9).add(Aspects.AURA, 16).add(Aspects.EXCHANGE, 8).add(Aspects.MECHANISM, 12),
                Ingredient.of(TCBlocks.VIS_RELAY.asItem()),
                java.util.List.of(Ingredient.of(TCItems.FOCI.get("trade")), Ingredient.of(NaturalisItems.MUTATION_STONE), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(TCItems.SHARDS.get("fire")), Ingredient.of(TCItems.SHARDS.get("water")), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(TCItems.SHARDS.get("order")), Ingredient.of(TCItems.SHARDS.get("entropy")))));
        ThaumcraftApi.bookRecipe("DarkGoggles", ThaumcraftApi.infusion("MN_DARK_GOGGLES",
                new ItemStack(NaturalisItems.DARK_CRYSTAL_GOGGLES), 3, new AspectList().add(Aspects.SENSES, 32).add(Aspects.ARMOR, 16).add(Aspects.DARKNESS, 32),
                Ingredient.of(TCItems.GOGGLES),
                java.util.List.of(Ingredient.of(TCItems.SHARDS.get("entropy")), Ingredient.of(TCItems.SHARDS.get("entropy")), Ingredient.of(net.minecraft.world.item.Items.SPIDER_EYE), Ingredient.of(net.minecraft.world.item.Items.SPIDER_EYE), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(TCItems.ZOMBIE_BRAIN))));
        ThaumcraftApi.bookRecipe("ElementalSickle", ThaumcraftApi.infusion("MN_SICKLE_OF_ABUNDANCE",
                new ItemStack(NaturalisItems.ELEMENTAL_SICKLE), 1, new AspectList().add(Aspects.GREED, 32).add(Aspects.CROP, 16).add(Aspects.HARVEST, 24).add(Aspects.TOOL, 8),
                Ingredient.of(NaturalisItems.THAUMIUM_SICKLE),
                java.util.List.of(Ingredient.of(TCItems.SHARDS.get("order")), Ingredient.of(TCItems.SHARDS.get("entropy")), Ingredient.of(net.minecraft.world.item.Items.WHEAT_SEEDS), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(net.minecraft.world.item.Items.ENCHANTED_BOOK))));
        ThaumcraftApi.bookRecipe("ConstructionFocus", ThaumcraftApi.infusion("MN_CONSTRUCTION_FOCUS",
                new ItemStack(NaturalisItems.BUILDER_FOCUS), 5, new AspectList().add(Aspects.CRAFT, 32).add(Aspects.TOOL, 16).add(Aspects.EXCHANGE, 8).add(Aspects.MECHANISM, 3),
                Ingredient.of(TCItems.FOCI.get("trade")),
                java.util.List.of(Ingredient.of(TCItems.SHARDS.get("order")), Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(TCItems.SHARDS.get("entropy")), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()), Ingredient.of(TCItems.ELEMENTAL_SHOVEL), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()), Ingredient.of(TCResources.get("quicksilver")), Ingredient.of(TCBlocks.BUILDING.get("arcane_stone_bricks").asItem()))));
        ThaumcraftApi.bookRecipe("RevenantFocus", ThaumcraftApi.infusion("MN_REVENANT_FOCUS",
                new ItemStack(NaturalisItems.REVENANT_FOCUS), 3, new AspectList().add(Aspects.UNDEAD, 25).add(Aspects.FLESH, 15).add(Aspects.BEAST, 15).add(Aspects.ENTROPY, 25),
                Ingredient.of(TCResources.get("quicksilver")),
                java.util.List.of(Ingredient.of(TCItems.ZOMBIE_BRAIN), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(net.minecraft.world.item.Items.ROTTEN_FLESH), Ingredient.of(TCItems.SHARDS.get("water")), Ingredient.of(net.minecraft.world.item.Items.ROTTEN_FLESH), Ingredient.of(TCItems.SHARDS.get("entropy")))));
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
        ThaumcraftApi.bookRecipe("CorruptedTrunk", ThaumcraftApi.infusion("MN_EVIL_TRUNK",
                new ItemStack(NaturalisItems.TRUNK_SPAWNER_CORRUPTED), 2, new AspectList().add(Aspects.MOTION, 16).add(Aspects.SOUL, 16).add(Aspects.ENTROPY, 16).add(Aspects.FLESH, 6),
                Ingredient.of(TCItems.TRUNK_SPAWNER),
                java.util.List.of(Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(TCItems.ZOMBIE_BRAIN), Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.SPIDER_EYE), Ingredient.of(net.minecraft.world.item.Items.ENDER_EYE))));
        ThaumcraftApi.bookRecipe("TaintedTrunk", ThaumcraftApi.infusion("MN_EVIL_TRUNK",
                new ItemStack(NaturalisItems.TRUNK_SPAWNER_TAINTED), 6, new AspectList().add(Aspects.TAINT, 16).add(Aspects.SOUL, 16).add(Aspects.ENTROPY, 16).add(Aspects.FLESH, 8),
                Ingredient.of(NaturalisItems.TRUNK_SPAWNER_CORRUPTED),
                java.util.List.of(Ingredient.of(TCResources.get("taint_tendril")), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(TCResources.get("tainted_goo")), Ingredient.of(TCResources.get("taint_tendril")), Ingredient.of(net.minecraft.world.item.Items.ENDER_EYE), Ingredient.of(TCResources.get("tainted_goo")))));
        ThaumcraftApi.bookRecipe("DemonicTrunk", ThaumcraftApi.infusion("MN_EVIL_TRUNK",
                new ItemStack(NaturalisItems.TRUNK_SPAWNER_DEMONIC), 6, new AspectList().add(Aspects.FIRE, 16).add(Aspects.SOUL, 16).add(Aspects.ENTROPY, 16).add(Aspects.FLESH, 8),
                Ingredient.of(NaturalisItems.TRUNK_SPAWNER_CORRUPTED),
                java.util.List.of(Ingredient.of(net.minecraft.world.item.Items.BLAZE_ROD), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.level.block.Blocks.NETHER_BRICKS.asItem()), Ingredient.of(net.minecraft.world.item.Items.BLAZE_ROD), Ingredient.of(net.minecraft.world.level.block.Blocks.NETHER_BRICKS.asItem()), Ingredient.of(net.minecraft.world.level.block.Blocks.QUARTZ_BLOCK.asItem()))));
        ThaumcraftApi.bookRecipe("SinisterTrunk", ThaumcraftApi.infusion("MN_EVIL_TRUNK",
                new ItemStack(NaturalisItems.TRUNK_SPAWNER_SINISTER), 6, new AspectList().add(Aspects.MIND, 16).add(Aspects.SOUL, 16).add(Aspects.ENTROPY, 16).add(Aspects.FLESH, 8),
                Ingredient.of(NaturalisItems.TRUNK_SPAWNER_CORRUPTED),
                java.util.List.of(Ingredient.of(TCItems.ZOMBIE_BRAIN), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(TCBlocks.BRAIN_JAR.asItem()), Ingredient.of(TCItems.VIS_STONE), Ingredient.of(TCItems.ZOMBIE_BRAIN), Ingredient.of(net.minecraft.world.item.Items.SPIDER_EYE))));
    }
}
