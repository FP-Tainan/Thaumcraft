package net.thaumcraft.maleficium;

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
 * A árvore e as receitas do Maleficium — o Tainted Magic 8.1.1 de Yulife.
 *
 * <p><b>Este arquivo é gerado</b> pelo {@code scratchpad/mal-tabela.js} a partir do {@code ResearchRegistry} e do
 * {@code RecipeRegistry} do jar original. Só entra o que fecha com coisas que já existem por aqui; o resto chega
 * com as fatias seguintes.
 */
public final class MaleficiumTable {
    private MaleficiumTable() {
    }

    /** As pesquisas, na ordem em que o original as registra. */
    public static void research() {
        ThaumcraftApi.research("MALEFICIUM", Maleficium.CATEGORY)
                .at(2, -1)
                .icon(() -> new ItemStack(TCResources.get("jar_label")))
                .round()
                .auto()
                .pages(Page.text("tc.research_page.MALEFICIUM.1"), Page.text("tc.research_page.MALEFICIUM.2"))
                .register();

        ThaumcraftApi.research("SHADOWMETAL", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.METAL, 1).add(Aspects.DARKNESS, 1).add(Aspects.MAGIC, 1))
                .at(0, 1)
                .complexity(1)
                .icon(() -> new ItemStack(MaleficiumItems.SHADOWMETAL_INGOT))
                .parents("MALEFICIUM")
                .concealed()
                .warp(1)
                .pages(Page.text("tc.research_page.SHADOWMETAL.1"), Page.crucible("ItemMaterial:0"), Page.crafting("ItemShadowmetalPick"), Page.crafting("ItemShadowmetalSpade"), Page.crafting("ItemShadowmetalAxe"), Page.crafting("ItemShadowmetalHoe"), Page.crafting("ItemShadowmetalSword"))
                .register();

        ThaumcraftApi.research("CAP_shadowmetal", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.METAL, 4).add(Aspects.DARKNESS, 4).add(Aspects.ELDRITCH, 4).add(Aspects.MAGIC, 4))
                .at(-3, 2)
                .complexity(2)
                .icon(() -> new ItemStack(MaleficiumItems.WAND_CAP_SHADOWMETAL))
                .parents("SHADOWMETAL", "CAP_void", "PRIMPEARL")
                .hiddenParents("INFUSION")
                .concealed()
                .warp(4)
                .pages(Page.text("tc.research_page.CAP_shadowmetal.1"), Page.infusion("ItemWandCap:0"))
                .register();

        ThaumcraftApi.research("SHADOWCLOTH", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.CLOTH, 1).add(Aspects.DARKNESS, 1))
                .at(-2, 4)
                .icon(() -> new ItemStack(MaleficiumItems.SHADOW_CLOTH))
                .parents("ENCHFABRIC", "SHADOWMETAL")
                .secondary()
                .concealed()
                .pages(Page.text("tc.research_page.SHADOWCLOTH.1"), Page.arcane("ItemMaterial:1"))
                .register();

        ThaumcraftApi.research("ROD_warpwood", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.TREE, 4).add(Aspects.DARKNESS, 4).add(Aspects.ELDRITCH, 4))
                .at(8, 0)
                .complexity(3)
                .icon(() -> new ItemStack(MaleficiumItems.WAND_ROD_WARPWOOD))
                .parents("WARPTREE", "VOIDMETAL", "PRIMPEARL", "ROD_primal_staff")
                .hiddenParents("INFUSION", "SHADOWMETAL")
                .concealed()
                .warp(5)
                .pages(Page.text("tc.research_page.ROD_warpwood.1"), Page.infusion("ItemWandRod:0"))
                .register();

        ThaumcraftApi.research("ROD_warpwood_staff", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.TREE, 4).add(Aspects.DARKNESS, 4).add(Aspects.ELDRITCH, 4))
                .at(10, -1)
                .complexity(2)
                .icon(() -> new ItemStack(MaleficiumItems.STAFF_ROD_WARPWOOD))
                .parents("ROD_warpwood")
                .hiddenParents("CREATIONSHARD")
                .secondary()
                .concealed()
                .pages(Page.text("tc.research_page.ROD_warpwood_staff.1"), Page.arcane("ItemWandRod:1"))
                .register();

        ThaumcraftApi.research("UNBALANCEDSHARDS", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.CRYSTAL, 4).add(Aspects.DARKNESS, 4))
                .at(4, -1)
                .icon(() -> new ItemStack(MaleficiumItems.TAINTED_SHARD))
                .parents("MALEFICIUM")
                .secondary()
                .concealed()
                .warp(1)
                .pages(Page.text("tc.research_page.UNBALANCEDSHARDS.1"), Page.crucible("ItemMaterial:4"), Page.crucible("ItemMaterial:3"))
                .register();

        ThaumcraftApi.research("WARPTREE", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 4).add(Aspects.DARKNESS, 4).add(Aspects.TREE, 4))
                .at(6, -2)
                .complexity(3)
                .icon(() -> new ItemStack(MaleficiumItems.WARP_FERTILIZER))
                .parents("UNBALANCEDSHARDS")
                .concealed()
                .warp(2)
                .pages(Page.text("tc.research_page.WARPTREE.1"), Page.arcane("ItemWarpFertilizer"))
                .register();

        ThaumcraftApi.research("CRIMSONROBES", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.CLOTH, 4).add(Aspects.EXCHANGE, 4).add(Aspects.ARMOR, 4))
                .at(-2, -1)
                .complexity(2)
                .icon(() -> new ItemStack(MaleficiumItems.CRIMSON_CLOTH))
                .parents("HOLLOWDAGGER")
                .concealed()
                .pages(Page.text("tc.research_page.CRIMSONROBES.1"), Page.arcane("ItemMaterial:2"), Page.arcane("ItemHelmetCultistRobe"), Page.arcane("ItemChestCultistRobe"), Page.arcane("ItemLegsCultistRobe"), Page.arcane("ItemBootsCultist"))
                .register();

        ThaumcraftApi.research("KNIGHTROBES", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.CLOTH, 4).add(Aspects.DARKNESS, 4).add(Aspects.ARMOR, 4))
                .at(-4, -2)
                .complexity(2)
                .icon(() -> new ItemStack(MaleficiumItems.CRIMSON_PLATING))
                .parents("CRIMSONROBES")
                .hiddenParents("INFUSION", "ELDRITCHMINOR")
                .concealed()
                .pages(Page.text("tc.research_page.KNIGHTROBES.1"), Page.infusion("ItemMaterial:7"), Page.arcane("ItemHelmetCultistPlate"), Page.arcane("ItemChestCultistPlate"), Page.arcane("ItemLegsCultistPlate"))
                .register();

        ThaumcraftApi.research("BREAKPEARL", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.ENTROPY, 12).add(Aspects.AIR, 4).add(Aspects.FIRE, 4).add(Aspects.WATER, 4).add(Aspects.EARTH, 4).add(Aspects.ORDER, 4).add(Aspects.ENTROPY, 4))
                .at(8, -6)
                .icon(() -> new ItemStack(MaleficiumItems.PRIMORDIAL_NODULE))
                .parents("PRIMPEARL")
                .secondary()
                .concealed()
                .pages(Page.text("tc.research_page.BREAKPEARL.1"), Page.arcane("ItemMaterial:9"), Page.arcane("ItemMaterial:10"))
                .register();

        ThaumcraftApi.research("CREATIONSHARD", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.AIR, 16).add(Aspects.FIRE, 16).add(Aspects.WATER, 16).add(Aspects.EARTH, 16).add(Aspects.ORDER, 16).add(Aspects.ENTROPY, 16))
                .at(9, -7)
                .complexity(3)
                .icon(() -> new ItemStack(MaleficiumItems.CREATION_SHARD))
                .parents("ELDRITCHMAJOR")
                .hiddenParents("INFUSION", "PRIMPEARL")
                .special()
                .concealed()
                .warp(9)
                .pages(Page.text("tc.research_page.CREATIONSHARD.1"), Page.infusion("ItemMaterial:5"), Page.arcane("ItemMaterial:11"))
                .register();

        ThaumcraftApi.research("CREATION", Maleficium.CATEGORY)
                .at(11, -5)
                .icon("thaumcraft:textures/misc/r_creation.png")
                .parents("CREATIONSHARD")
                .round()
                .special()
                .concealed()
                .hidden()
                .pages(Page.text("tc.research_page.CREATION.1"), Page.text("tc.research_page.CREATION.2"), Page.text("tc.research_page.CREATION.3"), Page.text("tc.research_page.CREATION.4"), Page.text("tc.research_page.CREATION.5"), Page.text("tc.research_page.CREATION.6"))
                .register();

        ThaumcraftApi.research("SKYSALT", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.WEATHER, 4).add(Aspects.AIR, 4).add(Aspects.FIRE, 4).add(Aspects.WATER, 4).add(Aspects.EARTH, 4).add(Aspects.ORDER, 4).add(Aspects.ENTROPY, 4))
                .at(12, -7)
                .icon(() -> new ItemStack(MaleficiumItems.SALIS_TEMPESTAS))
                .parents("CREATION")
                .hiddenParents("INFUSION")
                .secondary()
                .concealed()
                .pages(Page.text("tc.research_page.SKYSALT.1"), Page.infusion("ItemSalis:0"))
                .register();

        ThaumcraftApi.research("TIMESALT", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.EXCHANGE, 4).add(Aspects.AIR, 4).add(Aspects.FIRE, 4).add(Aspects.WATER, 4).add(Aspects.EARTH, 4).add(Aspects.ORDER, 4).add(Aspects.ENTROPY, 4))
                .at(13, -6)
                .icon(() -> new ItemStack(MaleficiumItems.SALIS_AEVUM))
                .parents("CREATION")
                .hiddenParents("INFUSION")
                .secondary()
                .concealed()
                .pages(Page.text("tc.research_page.TIMESALT.1"), Page.infusion("ItemSalis:1"))
                .register();

        ThaumcraftApi.research("VISHROOMCRAFT", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 4).add(Aspects.CRAFT, 2).add(Aspects.PLANT, 3))
                .at(3, -3)
                .icon(() -> new ItemStack(TCBlocks.VISHROOM.asItem()))
                .secondary()
                .concealed()
                .hidden()
                .pages(Page.text("tc.research_page.VISHROOMCRAFT.1"), Page.crucible("Vishroom_red"), Page.crucible("Vishroom_brown"))
                .register();

        ThaumcraftApi.research("HOLLOWDAGGER", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.WEAPON, 4).add(Aspects.FIRE, 4).add(Aspects.HEAL, 4))
                .at(0, -3)
                .complexity(2)
                .icon(() -> new ItemStack(MaleficiumItems.HOLLOW_DAGGER))
                .parents("MALEFICIUM")
                .hiddenParents("ENCHFABRIC", "ESSENTIACRYSTAL", "ELDRITCHMINOR")
                .warp(2)
                .pages(Page.text("tc.research_page.HOLLOWDAGGER.1"), Page.arcane("ItemHollowDagger"))
                .register();

        ThaumcraftApi.research("CAP_cloth", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 4).add(Aspects.CLOTH, 4))
                .at(3, 1)
                .icon(() -> new ItemStack(MaleficiumItems.WAND_CAP_CLOTH))
                .hiddenParents("CAP_gold", "ENCHFABRIC")
                .secondary()
                .concealed()
                .pages(Page.text("tc.research_page.CAP_cloth.1"), Page.arcane("ItemWandCap:1"))
                .register();

        ThaumcraftApi.research("CAP_crimsoncloth", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 8).add(Aspects.CLOTH, 4).add(Aspects.HEAL, 8))
                .at(-1, -1)
                .icon(() -> new ItemStack(MaleficiumItems.WAND_CAP_CRIMSONCLOTH))
                .parents("CRIMSONROBES")
                .hiddenParents("CAP_cloth")
                .secondary()
                .concealed()
                .pages(Page.text("tc.research_page.CAP_crimsoncloth.1"), Page.arcane("ItemWandCap:2"))
                .register();

        ThaumcraftApi.research("CAP_shadowcloth", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 16).add(Aspects.CLOTH, 8).add(Aspects.VOID, 16).add(Aspects.DARKNESS, 8))
                .at(-3, 5)
                .icon(() -> new ItemStack(MaleficiumItems.WAND_CAP_SHADOWCLOTH))
                .parents("SHADOWCLOTH")
                .hiddenParents("CAP_cloth")
                .secondary()
                .concealed()
                .warp(1)
                .pages(Page.text("tc.research_page.CAP_shadowcloth.1"), Page.arcane("ItemWandCap:3"))
                .register();

    }

    /** As receitas: carregam itens, então só se montam quando o mundo abre. */
    public static void recipes() {
        ThaumcraftApi.bookRecipe("ItemShadowmetalPick", ThaumcraftApi.crafting(() -> new ItemStack(MaleficiumItems.SHADOWMETAL_PICKAXE),
                3, 3, java.util.List.of(java.util.List.of(new ItemStack(MaleficiumItems.SHADOWMETAL_INGOT)), java.util.List.of(new ItemStack(MaleficiumItems.SHADOWMETAL_INGOT)), java.util.List.of(new ItemStack(MaleficiumItems.SHADOWMETAL_INGOT)), java.util.List.<ItemStack>of(), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.STICK)), java.util.List.<ItemStack>of(), java.util.List.<ItemStack>of(), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.STICK)), java.util.List.<ItemStack>of())));
        ThaumcraftApi.bookRecipe("ItemShadowmetalAxe", ThaumcraftApi.crafting(() -> new ItemStack(MaleficiumItems.SHADOWMETAL_AXE),
                2, 3, java.util.List.of(java.util.List.of(new ItemStack(MaleficiumItems.SHADOWMETAL_INGOT)), java.util.List.of(new ItemStack(MaleficiumItems.SHADOWMETAL_INGOT)), java.util.List.of(new ItemStack(MaleficiumItems.SHADOWMETAL_INGOT)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.STICK)), java.util.List.<ItemStack>of(), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.STICK)))));
        ThaumcraftApi.bookRecipe("ItemShadowmetalSpade", ThaumcraftApi.crafting(() -> new ItemStack(MaleficiumItems.SHADOWMETAL_SHOVEL),
                1, 3, java.util.List.of(java.util.List.of(new ItemStack(MaleficiumItems.SHADOWMETAL_INGOT)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.STICK)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.STICK)))));
        ThaumcraftApi.bookRecipe("ItemShadowmetalHoe", ThaumcraftApi.crafting(() -> new ItemStack(MaleficiumItems.SHADOWMETAL_HOE),
                2, 3, java.util.List.of(java.util.List.of(new ItemStack(MaleficiumItems.SHADOWMETAL_INGOT)), java.util.List.of(new ItemStack(MaleficiumItems.SHADOWMETAL_INGOT)), java.util.List.<ItemStack>of(), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.STICK)), java.util.List.<ItemStack>of(), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.STICK)))));
        ThaumcraftApi.bookRecipe("ItemShadowmetalSword", ThaumcraftApi.crafting(() -> new ItemStack(MaleficiumItems.SHADOWMETAL_SWORD),
                1, 3, java.util.List.of(java.util.List.of(new ItemStack(MaleficiumItems.SHADOWMETAL_INGOT)), java.util.List.of(new ItemStack(MaleficiumItems.SHADOWMETAL_INGOT)), java.util.List.of(new ItemStack(net.minecraft.world.item.Items.STICK)))));
        ThaumcraftApi.bookRecipe("ItemMaterial:5", ThaumcraftApi.infusion("CREATIONSHARD",
                new ItemStack(MaleficiumItems.CREATION_SHARD), 8, new AspectList().add(Aspects.AIR, 55).add(Aspects.FIRE, 55).add(Aspects.WATER, 55).add(Aspects.EARTH, 55).add(Aspects.ORDER, 55).add(Aspects.ENTROPY, 55),
                Ingredient.of(TCItems.SHARD_BALANCED),
                java.util.List.of(Ingredient.of(TCItems.PRIMORDIAL_PEARL), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(TCItems.SHARDS.get("fire")), Ingredient.of(TCItems.SHARDS.get("water")), Ingredient.of(net.minecraft.world.item.Items.NETHER_STAR), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(TCItems.SHARDS.get("order")), Ingredient.of(TCItems.SHARDS.get("entropy")))));
        ThaumcraftApi.bookRecipe("ItemWandRod:0", ThaumcraftApi.infusion("ROD_warpwood",
                new ItemStack(MaleficiumItems.WAND_ROD_WARPWOOD), 8, new AspectList().add(Aspects.ELDRITCH, 65).add(Aspects.DARKNESS, 30).add(Aspects.TREE, 12).add(Aspects.MAGIC, 45).add(Aspects.AURA, 35),
                Ingredient.of(MaleficiumBlocks.WARPWOOD_LOG.asItem()),
                java.util.List.of(Ingredient.of(MaleficiumItems.PRIMORDIAL_NODULE), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(MaleficiumItems.WARPED_SHARD), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(TCItems.ZOMBIE_BRAIN), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(MaleficiumItems.WARPED_SHARD), Ingredient.of(TCResources.get("void_ingot")))));
        ThaumcraftApi.bookRecipe("ItemWandCap:0", ThaumcraftApi.infusion("CAP_shadowmetal",
                new ItemStack(MaleficiumItems.WAND_CAP_SHADOWMETAL), 8, new AspectList().add(Aspects.ELDRITCH, 55).add(Aspects.DARKNESS, 47).add(Aspects.MAGIC, 52).add(Aspects.METAL, 45).add(Aspects.VOID, 55),
                Ingredient.of(TCItems.WAND_CAPS.get("void")),
                java.util.List.of(Ingredient.of(MaleficiumItems.WARPED_SHARD), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(MaleficiumItems.PRIMORDIAL_NODULE), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT))));
        ThaumcraftApi.bookRecipe("ItemMaterial:7", ThaumcraftApi.infusion("KNIGHTROBES",
                new ItemStack(MaleficiumItems.CRIMSON_PLATING, 9), 2, new AspectList().add(Aspects.HUNGER, 4).add(Aspects.METAL, 8).add(Aspects.MAGIC, 6),
                Ingredient.of(net.minecraft.world.level.block.Blocks.IRON_BLOCK.asItem()),
                java.util.List.of(Ingredient.of(MaleficiumItems.CRIMSON_BLOOD), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), Ingredient.of(MaleficiumItems.CRIMSON_BLOOD), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), Ingredient.of(MaleficiumItems.CRIMSON_BLOOD), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), Ingredient.of(MaleficiumItems.CRIMSON_BLOOD), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET))));
        ThaumcraftApi.bookRecipe("ItemSalis:0", ThaumcraftApi.infusion("SKYSALT",
                new ItemStack(MaleficiumItems.SALIS_TEMPESTAS, 3), 7, new AspectList().add(Aspects.AURA, 20).add(Aspects.WEATHER, 30).add(Aspects.WATER, 30),
                Ingredient.of(TCResources.get("salis_mundus")),
                java.util.List.of(Ingredient.of(MaleficiumItems.CREATION_FRAGMENT), Ingredient.of(TCResources.get("quicksilver_drop")))));
        ThaumcraftApi.bookRecipe("ItemSalis:1", ThaumcraftApi.infusion("TIMESALT",
                new ItemStack(MaleficiumItems.SALIS_AEVUM, 3), 7, new AspectList().add(Aspects.AURA, 20).add(Aspects.LIGHT, 30).add(Aspects.DARKNESS, 30),
                Ingredient.of(TCResources.get("salis_mundus")),
                java.util.List.of(Ingredient.of(MaleficiumItems.CREATION_FRAGMENT), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET))));
        ThaumcraftApi.bookRecipe("ItemMaterial:2", ThaumcraftApi.arcaneShapeless("CRIMSONROBES",
                new ItemStack(MaleficiumItems.CRIMSON_CLOTH), new AspectList().add(Aspects.FIRE, 5).add(Aspects.ENTROPY, 5),
                java.util.List.of(Ingredient.of(MaleficiumItems.CRIMSON_BLOOD), Ingredient.of(TCResources.get("enchanted_fabric")), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET))));
        ThaumcraftApi.bookRecipe("ItemWarpFertilizer", ThaumcraftApi.arcaneShapeless("WARPTREE",
                new ItemStack(MaleficiumItems.WARP_FERTILIZER), new AspectList().add(Aspects.ENTROPY, 150),
                java.util.List.of(Ingredient.of(TCItems.ZOMBIE_BRAIN), Ingredient.of(MaleficiumItems.WARPED_SHARD), Ingredient.of(TCItems.WISP_ESSENCE), Ingredient.of(net.minecraft.world.item.Items.BONE_MEAL))));
        ThaumcraftApi.bookRecipe("ItemHelmetCultistRobe", ThaumcraftApi.arcane("CRIMSONROBES",
                new ItemStack(TCItems.CULTIST_ROBE_HELMET), new AspectList().add(Aspects.FIRE, 5).add(Aspects.ENTROPY, 5),
                java.util.Arrays.asList(Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), null, Ingredient.of(MaleficiumItems.CRIMSON_CLOTH))));
        ThaumcraftApi.bookRecipe("ItemChestCultistRobe", ThaumcraftApi.arcane("CRIMSONROBES",
                new ItemStack(TCItems.CULTIST_ROBE_CHESTPLATE), new AspectList().add(Aspects.FIRE, 10).add(Aspects.ENTROPY, 10),
                java.util.Arrays.asList(Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), null, Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(MaleficiumItems.CRIMSON_CLOTH))));
        ThaumcraftApi.bookRecipe("ItemLegsCultistRobe", ThaumcraftApi.arcane("CRIMSONROBES",
                new ItemStack(TCItems.CULTIST_ROBE_LEGGINGS), new AspectList().add(Aspects.FIRE, 8).add(Aspects.ENTROPY, 8),
                java.util.Arrays.asList(Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), null, Ingredient.of(MaleficiumItems.CRIMSON_CLOTH))));
        ThaumcraftApi.bookRecipe("ItemBootsCultist", ThaumcraftApi.arcane("CRIMSONROBES",
                new ItemStack(TCItems.CULTIST_BOOTS), new AspectList().add(Aspects.FIRE, 5).add(Aspects.ENTROPY, 5),
                java.util.Arrays.asList(Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), null, Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), null, Ingredient.of(MaleficiumItems.CRIMSON_CLOTH))));
        ThaumcraftApi.bookRecipe("ItemWandRod:1", ThaumcraftApi.arcane("ROD_warpwood_staff",
                new ItemStack(MaleficiumItems.STAFF_ROD_WARPWOOD), new AspectList().add(Aspects.AIR, 120).add(Aspects.FIRE, 120).add(Aspects.WATER, 120).add(Aspects.EARTH, 120).add(Aspects.ORDER, 120).add(Aspects.ENTROPY, 120),
                java.util.Arrays.asList(null, null, Ingredient.of(TCItems.PRIMORDIAL_PEARL), null, Ingredient.of(MaleficiumItems.WAND_ROD_WARPWOOD), null, Ingredient.of(MaleficiumItems.WAND_ROD_WARPWOOD), null, null)));
        ThaumcraftApi.bookRecipe("ItemHelmetCultistPlate", ThaumcraftApi.arcane("KNIGHTROBES",
                new ItemStack(TCItems.CULTIST_PLATE_HELMET), new AspectList().add(Aspects.EARTH, 5).add(Aspects.FIRE, 5).add(Aspects.ENTROPY, 5),
                java.util.Arrays.asList(Ingredient.of(MaleficiumItems.CRIMSON_PLATING), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(MaleficiumItems.CRIMSON_PLATING), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), null, Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT))));
        ThaumcraftApi.bookRecipe("ItemChestCultistPlate", ThaumcraftApi.arcane("KNIGHTROBES",
                new ItemStack(TCItems.CULTIST_PLATE_CHESTPLATE), new AspectList().add(Aspects.EARTH, 10).add(Aspects.FIRE, 10).add(Aspects.ENTROPY, 10),
                java.util.Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), null, Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(MaleficiumItems.CRIMSON_PLATING), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(MaleficiumItems.CRIMSON_PLATING), Ingredient.of(MaleficiumItems.CRIMSON_PLATING), Ingredient.of(MaleficiumItems.CRIMSON_PLATING))));
        ThaumcraftApi.bookRecipe("ItemLegsCultistPlate", ThaumcraftApi.arcane("KNIGHTROBES",
                new ItemStack(TCItems.CULTIST_PLATE_LEGGINGS), new AspectList().add(Aspects.EARTH, 8).add(Aspects.FIRE, 8).add(Aspects.ENTROPY, 8),
                java.util.Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(MaleficiumItems.CRIMSON_PLATING), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), null, Ingredient.of(MaleficiumItems.CRIMSON_PLATING), null)));
        ThaumcraftApi.bookRecipe("ItemMaterial:1", ThaumcraftApi.arcane("SHADOWMETAL",
                new ItemStack(MaleficiumItems.SHADOW_CLOTH), new AspectList().add(Aspects.ORDER, 10).add(Aspects.ENTROPY, 10),
                java.util.Arrays.asList(null, Ingredient.of(MaleficiumItems.SHADOWMETAL_NUGGET), null, Ingredient.of(MaleficiumItems.SHADOWMETAL_NUGGET), Ingredient.of(TCResources.get("enchanted_fabric")), Ingredient.of(MaleficiumItems.SHADOWMETAL_NUGGET), null, Ingredient.of(MaleficiumItems.SHADOWMETAL_NUGGET), null)));
        ThaumcraftApi.bookRecipe("ItemHollowDagger", ThaumcraftApi.arcane("HOLLOWDAGGER",
                new ItemStack(MaleficiumItems.HOLLOW_DAGGER), new AspectList().add(Aspects.ENTROPY, 85),
                java.util.Arrays.asList(null, null, Ingredient.of(TCItems.WAND_RODS.get("bone")), null, Ingredient.of(TCBlocks.GREATWOOD_LOG.asItem()), Ingredient.of(net.minecraft.world.item.Items.IRON_NUGGET), Ingredient.of(net.minecraft.world.item.Items.STICK), null, null)));
        ThaumcraftApi.bookRecipe("ItemWandCap:1", ThaumcraftApi.arcane("CAP_cloth",
                new ItemStack(MaleficiumItems.WAND_CAP_CLOTH), new AspectList().add(Aspects.EARTH, 10).add(Aspects.FIRE, 10).add(Aspects.ENTROPY, 10).add(Aspects.ORDER, 10),
                java.util.Arrays.asList(Ingredient.of(TCResources.get("enchanted_fabric")), Ingredient.of(TCResources.get("enchanted_fabric")), Ingredient.of(TCResources.get("enchanted_fabric")), Ingredient.of(TCResources.get("enchanted_fabric")), null, Ingredient.of(TCResources.get("enchanted_fabric")))));
        ThaumcraftApi.bookRecipe("ItemWandCap:2", ThaumcraftApi.arcane("CAP_crimsoncloth",
                new ItemStack(MaleficiumItems.WAND_CAP_CRIMSONCLOTH), new AspectList().add(Aspects.FIRE, 25).add(Aspects.ENTROPY, 25).add(Aspects.ORDER, 25),
                java.util.Arrays.asList(Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), Ingredient.of(MaleficiumItems.CRIMSON_CLOTH), Ingredient.of(TCResources.get("salis_mundus")), Ingredient.of(MaleficiumItems.CRIMSON_CLOTH))));
        ThaumcraftApi.bookRecipe("ItemWandCap:3", ThaumcraftApi.arcane("CAP_shadowcloth",
                new ItemStack(MaleficiumItems.WAND_CAP_SHADOWCLOTH), new AspectList().add(Aspects.EARTH, 55).add(Aspects.FIRE, 55).add(Aspects.ENTROPY, 55).add(Aspects.ORDER, 55),
                java.util.Arrays.asList(Ingredient.of(MaleficiumItems.SHADOW_CLOTH), Ingredient.of(MaleficiumItems.SHADOW_CLOTH), Ingredient.of(MaleficiumItems.SHADOW_CLOTH), Ingredient.of(MaleficiumItems.SHADOW_CLOTH), Ingredient.of(TCResources.get("salis_mundus")), Ingredient.of(MaleficiumItems.SHADOW_CLOTH))));
        ThaumcraftApi.bookRecipe("ItemMaterial:9", ThaumcraftApi.arcaneShapeless("BREAKPEARL",
                new ItemStack(MaleficiumItems.PRIMORDIAL_NODULE, 3), new AspectList().add(Aspects.ENTROPY, 25),
                java.util.List.of(Ingredient.of(TCItems.PRIMORDIAL_PEARL))));
        ThaumcraftApi.bookRecipe("ItemMaterial:10", ThaumcraftApi.arcaneShapeless("BREAKPEARL",
                new ItemStack(MaleficiumItems.PRIMORDIAL_MOTE, 3), new AspectList().add(Aspects.ENTROPY, 15),
                java.util.List.of(Ingredient.of(MaleficiumItems.PRIMORDIAL_NODULE))));
        ThaumcraftApi.bookRecipe("ItemMaterial:11", ThaumcraftApi.arcaneShapeless("CREATIONSHARD",
                new ItemStack(MaleficiumItems.CREATION_FRAGMENT, 9), new AspectList().add(Aspects.ENTROPY, 99),
                java.util.List.of(Ingredient.of(MaleficiumItems.CREATION_SHARD))));
        ThaumcraftApi.bookRecipe("ItemMaterial:3", ThaumcraftApi.crucible("UNBALANCEDSHARDS",
                new ItemStack(MaleficiumItems.WARPED_SHARD), TCItems.SHARD_BALANCED, new AspectList().add(Aspects.ELDRITCH, 4)));
        ThaumcraftApi.bookRecipe("ItemMaterial:4", ThaumcraftApi.crucible("UNBALANCEDSHARDS",
                new ItemStack(MaleficiumItems.TAINTED_SHARD), TCItems.SHARD_BALANCED, new AspectList().add(Aspects.TAINT, 4)));
        ThaumcraftApi.bookRecipe("ItemMaterial:0", ThaumcraftApi.crucible("SHADOWMETAL",
                new ItemStack(MaleficiumItems.SHADOWMETAL_INGOT), net.minecraft.world.item.Items.IRON_INGOT, new AspectList().add(Aspects.DARKNESS, 3).add(Aspects.METAL, 7).add(Aspects.MAGIC, 2)));
        ThaumcraftApi.bookRecipe("Vishroom_brown", ThaumcraftApi.crucible("VISHROOMCRAFT",
                new ItemStack(TCBlocks.VISHROOM.asItem()), net.minecraft.world.level.block.Blocks.BROWN_MUSHROOM.asItem(), new AspectList().add(Aspects.MAGIC, 3).add(Aspects.POISON, 1)));
        ThaumcraftApi.bookRecipe("Vishroom_red", ThaumcraftApi.crucible("VISHROOMCRAFT",
                new ItemStack(TCBlocks.VISHROOM.asItem()), net.minecraft.world.level.block.Blocks.RED_MUSHROOM.asItem(), new AspectList().add(Aspects.MAGIC, 3).add(Aspects.POISON, 1)));
    }
}
