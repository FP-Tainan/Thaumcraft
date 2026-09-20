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

        ThaumcraftApi.research("VOIDFORTRESS", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.ARMOR, 5).add(Aspects.ELDRITCH, 3).add(Aspects.DARKNESS, 3).add(Aspects.VOID, 5))
                .at(8, -9)
                .complexity(2)
                .icon(() -> new ItemStack(MaleficiumItems.VOID_FORTRESS_HELMET))
                .parents("ELDRITCHMAJOR")
                .hiddenParents("INFUSION", "ARMORVOIDFORTRESS", "ARMORFORTRESS")
                .concealed()
                .warp(2)
                .pages(Page.text("tc.research_page.VOIDFORTRESS.1"), Page.infusion("ItemVoidFortressHelmet"), Page.infusion("ItemVoidFortressChestplate"), Page.infusion("ItemVoidFortressLeggings"))
                .register();

        ThaumcraftApi.research("WARPEDGOGGLES", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.ARMOR, 4).add(Aspects.ELDRITCH, 4).add(Aspects.DARKNESS, 4).add(Aspects.ARMOR, 4))
                .at(2, 4)
                .complexity(1)
                .icon(() -> new ItemStack(MaleficiumItems.WARPED_GOGGLES))
                .parents("SHADOWMETAL")
                .hiddenParents("INFUSION", "MALEFICIUM", "GOGGLES")
                .secondary()
                .concealed()
                .pages(Page.text("tc.research_page.WARPEDGOGGLES.1"), Page.infusion("ItemWarpedGoggles"))
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

        ThaumcraftApi.research("PRAETORARMOR", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.CLOTH, 4).add(Aspects.DARKNESS, 4).add(Aspects.ARMOR, 4).add(Aspects.ELDRITCH, 4))
                .at(-6, 0)
                .complexity(2)
                .icon(() -> new ItemStack(TCItems.CULTIST_LEADER_HELMET))
                .parents("KNIGHTROBES")
                .hiddenParents("CRIMSONROBES", "ELDRITCHMAJOR", "VOIDMETAL")
                .concealed()
                .pages(Page.text("tc.research_page.PRAETORARMOR.1"), Page.arcane("ItemHelmetCultistLeaderPlate"), Page.arcane("ItemChestCultistLeaderPlate"), Page.arcane("ItemLegsCultistLeaderPlate"))
                .register();

        ThaumcraftApi.research("VOIDBLOOD", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 14).add(Aspects.DARKNESS, 8).add(Aspects.ARMOR, 18).add(Aspects.AURA, 4))
                .at(-2, -5)
                .icon(() -> new ItemStack(MaleficiumItems.VOID_BLOOD))
                .parents("HOLLOWDAGGER")
                .hiddenParents("ELDRITCHMAJOR", "BREAKPEARL")
                .secondary()
                .concealed()
                .warp(3)
                .pages(Page.text("tc.research_page.VOIDBLOOD.1"), Page.arcane("ItemVoidBlood"))
                .register();

        ThaumcraftApi.research("VOIDWALKERBOOTS", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 4).add(Aspects.DARKNESS, 8).add(Aspects.ARMOR, 8).add(Aspects.ELDRITCH, 8))
                .at(4, -8)
                .complexity(3)
                .icon(() -> new ItemStack(MaleficiumItems.VOIDWALKER_BOOTS))
                .parents("ELDRITCHMAJOR")
                .hiddenParents("INFUSION", "PRIMPEARL", "BOOTSTRAVELLER", "SHADOWCLOTH", "ARMORVOIDFORTRESS")
                .concealed()
                .warp(4)
                .pages(Page.text("tc.research_page.VOIDWALKERBOOTS.1"), Page.infusion("ItemVoidwalkerBoots"))
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

        ThaumcraftApi.research("THAUMICDISASSEMBLER", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.METAL, 4).add(Aspects.WEAPON, 8).add(Aspects.TOOL, 4))
                .at(6, -9)
                .complexity(3)
                .icon(() -> new ItemStack(MaleficiumItems.THAUMIC_DISASSEMBLER))
                .parents("ELDRITCHMAJOR")
                .hiddenParents("INFUSION", "THAUMIUM", "VOIDMETAL", "PRIMPEARL")
                .concealed()
                .pages(Page.text("tc.research_page.THAUMICDISASSEMBLER.1"), Page.infusion("ItemThaumicDisassembler"), Page.arcane("ItemMaterial:6"), Page.text("tc.research_page.THAUMICDISASSEMBLER.2"))
                .register();

        ThaumcraftApi.research("VOIDSASH", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 4).add(Aspects.METAL, 8).add(Aspects.ARMOR, 4))
                .at(3, -9)
                .icon(() -> new ItemStack(MaleficiumItems.VOIDWALKER_SASH))
                .parents("VOIDWALKERBOOTS")
                .hiddenParents("INFUSION", "PRIMPEARL")
                .secondary()
                .concealed()
                .warp(3)
                .pages(Page.text("tc.research_page.VOIDSASH.1"), Page.infusion("ItemVoidwalkerSash"))
                .register();

        ThaumcraftApi.research("MAGICFUNGUAR", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 2).add(Aspects.HUNGER, 4).add(Aspects.PLANT, 2))
                .at(2, -4)
                .icon(() -> new ItemStack(MaleficiumItems.MAGIC_FUNGUAR))
                .parents("VISHROOMCRAFT")
                .secondary()
                .concealed()
                .pages(Page.text("tc.research_page.MAGICFUNGUAR.1"), Page.arcane("ItemMagicFunguar"))
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

        ThaumcraftApi.research("CRIMSONBLADE", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.ENTROPY, 8).add(Aspects.ELDRITCH, 2).add(Aspects.WEAPON, 16).add(Aspects.VOID, 6))
                .at(-6, 2)
                .icon(() -> new ItemStack(TCItems.CRIMSON_SWORD))
                .parents("PRAETORARMOR")
                .hiddenParents("INFUSION")
                .secondary()
                .concealed()
                .warp(5)
                .pages(Page.text("tc.research_page.CRIMSONBLADE.1"), Page.infusion("ItemSwordCrimson"))
                .register();

        ThaumcraftApi.research("SHADOWFORTRESS", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.METAL, 4).add(Aspects.DARKNESS, 8).add(Aspects.ARMOR, 8).add(Aspects.VOID, 2))
                .at(0, 3)
                .complexity(3)
                .icon(() -> new ItemStack(MaleficiumItems.SHADOW_FORTRESS_HELMET))
                .parents("SHADOWMETAL")
                .hiddenParents("INFUSION", "VOIDFORTRESS", "ELDRITCHMAJOR", "UNBALANCEDSHARDS")
                .concealed()
                .warp(3)
                .pages(Page.text("tc.research_page.SHADOWFORTRESS.1"), Page.infusion("ItemShadowFortressHelmet"), Page.infusion("ItemShadowFortressChestplate"), Page.infusion("ItemShadowFortressLeggings"))
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

        ThaumcraftApi.research("PRIMALBLADE", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 1).add(Aspects.ELDRITCH, 1).add(Aspects.WEAPON, 1).add(Aspects.VOID, 1).add(Aspects.AURA, 1))
                .at(11, -3)
                .complexity(3)
                .icon(() -> new ItemStack(MaleficiumItems.PRIMAL_BLADE))
                .parents("CREATION")
                .hiddenParents("INFUSION", "VOIDMETAL", "PRIMALCRUSHER", "UNBALANCEDSHARDS", "HOLLOWDAGGER")
                .concealed()
                .warp(5)
                .pages(Page.text("tc.research_page.PRIMALBLADE.1"), Page.infusion("ItemPrimalBlade"))
                .register();

        ThaumcraftApi.research("THAUMIUMKATANA", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.METAL, 8).add(Aspects.MAGIC, 4).add(Aspects.WEAPON, 6))
                .at(12, -1)
                .complexity(3)
                .icon(() -> new ItemStack(MaleficiumItems.THAUMIUM_FORTRESS_BLADE))
                .hiddenParents("INFUSION", "ARMORFORTRESS")
                .concealed()
                .pages(Page.text("tc.research_page.THAUMIUMKATANA.1"), Page.infusion("ItemKatana:0"))
                .register();

        ThaumcraftApi.research("VOIDMETALKATANA", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.METAL, 16).add(Aspects.MAGIC, 8).add(Aspects.WEAPON, 12).add(Aspects.VOID, 12))
                .at(7, -10)
                .complexity(3)
                .icon(() -> new ItemStack(MaleficiumItems.VOIDMETAL_FORTRESS_BLADE))
                .parents("VOIDFORTRESS")
                .hiddenParents("INFUSION", "THAUMIUMKATANA")
                .secondary()
                .concealed()
                .warp(3)
                .pages(Page.text("tc.research_page.VOIDMETALKATANA.1"), Page.infusion("ItemKatana:1"))
                .register();

        ThaumcraftApi.research("SHADOWMETALKATANA", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.METAL, 16).add(Aspects.MAGIC, 8).add(Aspects.WEAPON, 12).add(Aspects.VOID, 12).add(Aspects.DARKNESS, 14))
                .at(0, 5)
                .complexity(3)
                .icon(() -> new ItemStack(MaleficiumItems.SHADOWMETAL_FORTRESS_BLADE))
                .parents("SHADOWFORTRESS")
                .hiddenParents("INFUSION", "VOIDMETALKATANA")
                .secondary()
                .concealed()
                .warp(5)
                .pages(Page.text("tc.research_page.SHADOWMETALKATANA.1"), Page.infusion("ItemKatana:2"))
                .register();

        ThaumcraftApi.research("INSCRIPTIONFIRE", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.FIRE, 8).add(Aspects.ENTROPY, 4).add(Aspects.METAL, 6))
                .at(14, 0)
                .icon("thaumcraft:textures/misc/r_inscription0.png")
                .parents("THAUMIUMKATANA")
                .hiddenParents("INFUSION", "FOCUSFIRE")
                .secondary()
                .concealed()
                .pages(Page.text("tc.research_page.INSCRIPTIONFIRE.1"), Page.infusion("ItemKatanaThaumium:inscription0"))
                .register();

        ThaumcraftApi.research("INSCRIPTIONTHUNDER", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.ENTROPY, 8).add(Aspects.MOTION, 4).add(Aspects.METAL, 6))
                .at(14, 1)
                .icon("thaumcraft:textures/misc/r_inscription1.png")
                .parents("THAUMIUMKATANA")
                .hiddenParents("INFUSION", "FOCUSSHOCKWAVE")
                .secondary()
                .concealed()
                .pages(Page.text("tc.research_page.INSCRIPTIONTHUNDER.1"), Page.infusion("ItemKatanaThaumium:inscription1"))
                .register();

        ThaumcraftApi.research("INSCRIPTIONHEAL", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.HEAL, 8).add(Aspects.UNDEAD, 4).add(Aspects.METAL, 6))
                .at(14, 2)
                .icon("thaumcraft:textures/misc/r_inscription2.png")
                .parents("THAUMIUMKATANA")
                .hiddenParents("INFUSION", "BATHSALTS")
                .secondary()
                .concealed()
                .warp(2)
                .pages(Page.text("tc.research_page.INSCRIPTIONHEAL.1"), Page.infusion("ItemKatanaThaumium:inscription2"))
                .register();

        ThaumcraftApi.research("VOIDGOGGLES", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.VOID, 8).add(Aspects.DARKNESS, 4).add(Aspects.MAGIC, 6).add(Aspects.SENSES, 12))
                .at(3, 3)
                .icon(() -> new ItemStack(MaleficiumItems.VOIDMETAL_GOGGLES))
                .parents("WARPEDGOGGLES")
                .hiddenParents("INFUSION", "VOIDMETAL")
                .secondary()
                .concealed()
                .warp(2)
                .pages(Page.text("tc.research_page.VOIDGOGGLES.1"), Page.infusion("ItemVoidmetalGoggles"))
                .register();

        ThaumcraftApi.research("ELDRITCHFOCUS", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.ELDRITCH, 22).add(Aspects.ENTROPY, 14).add(Aspects.AIR, 4).add(Aspects.DARKNESS, 6))
                .at(5, -5)
                .complexity(3)
                .icon(() -> new ItemStack(MaleficiumItems.FOCUS_DARK_MATTER))
                .parents("ELDRITCHMAJOR")
                .hiddenParents("INFUSION", "OUTERREV")
                .special()
                .concealed()
                .warp(7)
                .pages(Page.text("tc.research_page.ELDRITCHFOCUS.1"), Page.infusion("ItemFocusDarkMatter"))
                .register();

        ThaumcraftApi.research("DIFFUSIONUPGRADE", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 4).add(Aspects.DARKNESS, 8).add(Aspects.WEAPON, 8).add(Aspects.ELDRITCH, 10))
                .at(6, -4)
                .icon("thaumcraft:textures/foci/diffusion.png")
                .parents("ELDRITCHFOCUS")
                .hiddenParents("FOCALMANIPULATION")
                .secondary()
                .concealed()
                .warp(2)
                .pages(Page.text("tc.research_page.DIFFUSIONUPGRADE.1"))
                .register();

        ThaumcraftApi.research("MACEFOCUS", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.ENTROPY, 10).add(Aspects.EARTH, 4).add(Aspects.WEAPON, 6).add(Aspects.MAGIC, 8))
                .at(7, 3)
                .complexity(2)
                .icon(() -> new ItemStack(MaleficiumItems.FOCUS_MAGE_MACE))
                .hiddenParents("INFUSION", "THAUMIUM", "FOCUSFIRE")
                .concealed()
                .pages(Page.text("tc.research_page.MACEFOCUS.1"), Page.infusion("ItemFocusMageMace"))
                .register();

        ThaumcraftApi.research("FOCUSSHOCKWAVE", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 20).add(Aspects.ENTROPY, 12).add(Aspects.AIR, 6).add(Aspects.MOTION, 12))
                .at(7, 2)
                .icon(() -> new ItemStack(MaleficiumItems.FOCUS_SHOCKWAVE))
                .hiddenParents("INFUSION", "FOCUSSHOCK")
                .secondary()
                .concealed()
                .pages(Page.text("tc.research_page.FOCUSSHOCKWAVE.1"), Page.infusion("ItemFocusShockwave"))
                .register();

        ThaumcraftApi.research("FOCUSSHARD", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.CRYSTAL, 2).add(Aspects.MAGIC, 4).add(Aspects.MOTION, 6))
                .at(6, 1)
                .icon(() -> new ItemStack(MaleficiumItems.FOCUS_VIS_SHARD))
                .parents("UNBALANCEDSHARDS")
                .hiddenParents("FOCUSFIRE")
                .secondary()
                .concealed()
                .pages(Page.text("tc.research_page.FOCUSSHARD.1"), Page.arcane("ItemFocusVisShard"))
                .register();

        ThaumcraftApi.research("TAINTFOCUS", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.TAINT, 4).add(Aspects.LIFE, 4).add(Aspects.MOTION, 4))
                .at(5, 2)
                .complexity(3)
                .icon(() -> new ItemStack(MaleficiumItems.FOCUS_TAINT_SWARM))
                .parents("FOCUSSHARD")
                .hiddenParents("INFUSION", "INFUSION", "BOTTLETAINT")
                .concealed()
                .warp(3)
                .pages(Page.text("tc.research_page.TAINTFOCUS.1"), Page.infusion("ItemFocusTaintSwarm"))
                .register();

        ThaumcraftApi.research("FOCUSLUMOS", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.FIRE, 16).add(Aspects.LIGHT, 24).add(Aspects.ENERGY, 8))
                .at(5, 3)
                .icon(() -> new ItemStack(MaleficiumItems.FOCUS_LUMOS))
                .hiddenParents("FOCUSFIRE")
                .secondary()
                .concealed()
                .pages(Page.text("tc.research_page.FOCUSLUMOS.1"), Page.arcane("ItemFocusLumos"))
                .register();

        ThaumcraftApi.research("LUMOSRING", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.ARMOR, 16).add(Aspects.AURA, 12).add(Aspects.LIGHT, 24).add(Aspects.ENERGY, 8))
                .at(6, 4)
                .complexity(2)
                .icon(() -> new ItemStack(MaleficiumItems.LUMOS_RING))
                .parents("FOCUSLUMOS")
                .hiddenParents("INFUSION", "RUNICARMOR")
                .concealed()
                .pages(Page.text("tc.research_page.LUMOSRING.1"), Page.infusion("ItemLumosRing"))
                .register();

        ThaumcraftApi.research("FLYTECHARM", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.FLIGHT, 15).add(Aspects.AIR, 20).add(Aspects.SENSES, 8).add(Aspects.MAGIC, 12))
                .at(13, -4)
                .complexity(3)
                .icon(() -> new ItemStack(MaleficiumItems.FLYTE_CHARM))
                .parents("CREATION")
                .hiddenParents("INFUSION", "VOIDSASH", "PRIMALARROW")
                .concealed()
                .pages(Page.text("tc.research_page.FLYTECHARM.1"), Page.infusion("ItemFlyteCharm"))
                .register();

        ThaumcraftApi.research("GATEKEY", Maleficium.CATEGORY)
                .aspects(new AspectList().add(Aspects.TRAVEL, 30).add(Aspects.EXCHANGE, 15).add(Aspects.AURA, 20).add(Aspects.FLIGHT, 10))
                .at(9, -4)
                .complexity(3)
                .icon(() -> new ItemStack(MaleficiumItems.GATE_KEY))
                .parents("CREATION")
                .hiddenParents("INFUSION")
                .concealed()
                .pages(Page.text("tc.research_page.GATEKEY.1"), Page.infusion("ItemGateKey"))
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
        ThaumcraftApi.bookRecipe("ItemFocusShockwave", ThaumcraftApi.infusion("FOCUSSHOCKWAVE",
                new ItemStack(MaleficiumItems.FOCUS_SHOCKWAVE), 6, new AspectList().add(Aspects.AIR, 35).add(Aspects.MOTION, 42).add(Aspects.ENERGY, 42).add(Aspects.MAGIC, 16),
                Ingredient.of(TCItems.FOCI.get("shock")),
                java.util.List.of(Ingredient.of(net.minecraft.world.level.block.Blocks.TNT.asItem()), Ingredient.of(net.minecraft.world.item.Items.GUNPOWDER), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(net.minecraft.world.level.block.Blocks.TNT.asItem()), Ingredient.of(net.minecraft.world.item.Items.GUNPOWDER), Ingredient.of(TCItems.SHARDS.get("air")))));
        ThaumcraftApi.bookRecipe("ItemPrimalBlade", ThaumcraftApi.infusion("PRIMALBLADE",
                new ItemStack(MaleficiumItems.PRIMAL_BLADE), 8, new AspectList().add(Aspects.WEAPON, 65).add(Aspects.ELDRITCH, 46).add(Aspects.DARKNESS, 16).add(Aspects.METAL, 60).add(Aspects.VOID, 56),
                Ingredient.of(TCItems.GEAR.get("void_sword")),
                java.util.List.of(Ingredient.of(MaleficiumItems.CREATION_SHARD), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(TCItems.SHARDS.get("fire")), Ingredient.of(TCItems.SHARDS.get("water")), Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(TCItems.SHARDS.get("order")), Ingredient.of(TCItems.SHARDS.get("entropy")))));
        ThaumcraftApi.bookRecipe("ItemFocusMageMace", ThaumcraftApi.infusion("MACEFOCUS",
                new ItemStack(MaleficiumItems.FOCUS_MAGE_MACE), 3, new AspectList().add(Aspects.WEAPON, 32).add(Aspects.METAL, 8).add(Aspects.ENTROPY, 18).add(Aspects.MAGIC, 26),
                Ingredient.of(net.minecraft.world.level.block.Blocks.IRON_BLOCK.asItem()),
                java.util.List.of(Ingredient.of(net.minecraft.world.item.Items.IRON_SWORD), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("entropy")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(net.minecraft.world.item.Items.IRON_SWORD), Ingredient.of(net.minecraft.world.item.Items.QUARTZ), Ingredient.of(TCItems.SHARDS.get("entropy")), Ingredient.of(net.minecraft.world.item.Items.QUARTZ))));
        ThaumcraftApi.bookRecipe("ItemSwordCrimson", ThaumcraftApi.infusion("CRIMSONBLADE",
                new ItemStack(TCItems.CRIMSON_SWORD), 7, new AspectList().add(Aspects.WEAPON, 32).add(Aspects.METAL, 16).add(Aspects.ENTROPY, 8).add(Aspects.ELDRITCH, 18).add(Aspects.VOID, 32),
                Ingredient.of(TCItems.GEAR.get("void_sword")),
                java.util.List.of(Ingredient.of(MaleficiumItems.CRIMSON_BLOOD), Ingredient.of(MaleficiumItems.CRIMSON_BLOOD), Ingredient.of(MaleficiumItems.CRIMSON_BLOOD))));
        ThaumcraftApi.bookRecipe("ItemVoidwalkerSash", ThaumcraftApi.infusion("VOIDSASH",
                new ItemStack(MaleficiumItems.VOIDWALKER_SASH), 7, new AspectList().add(Aspects.VOID, 56).add(Aspects.MAGIC, 40).add(Aspects.ARMOR, 76).add(Aspects.TRAVEL, 16).add(Aspects.FLIGHT, 10),
                Ingredient.of(TCItems.RUNIC_GIRDLE),
                java.util.List.of(Ingredient.of(MaleficiumItems.PRIMORDIAL_MOTE), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(MaleficiumItems.SHADOW_CLOTH), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(MaleficiumItems.SHADOW_CLOTH), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT))));
        ThaumcraftApi.bookRecipe("ItemMaterial:5", ThaumcraftApi.infusion("CREATIONSHARD",
                new ItemStack(MaleficiumItems.CREATION_SHARD), 8, new AspectList().add(Aspects.AIR, 55).add(Aspects.FIRE, 55).add(Aspects.WATER, 55).add(Aspects.EARTH, 55).add(Aspects.ORDER, 55).add(Aspects.ENTROPY, 55),
                Ingredient.of(TCItems.SHARD_BALANCED),
                java.util.List.of(Ingredient.of(TCItems.PRIMORDIAL_PEARL), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(TCItems.SHARDS.get("fire")), Ingredient.of(TCItems.SHARDS.get("water")), Ingredient.of(net.minecraft.world.item.Items.NETHER_STAR), Ingredient.of(TCItems.SHARDS.get("earth")), Ingredient.of(TCItems.SHARDS.get("order")), Ingredient.of(TCItems.SHARDS.get("entropy")))));
        ThaumcraftApi.bookRecipe("ItemWandRod:0", ThaumcraftApi.infusion("ROD_warpwood",
                new ItemStack(MaleficiumItems.WAND_ROD_WARPWOOD), 8, new AspectList().add(Aspects.ELDRITCH, 65).add(Aspects.DARKNESS, 30).add(Aspects.TREE, 12).add(Aspects.MAGIC, 45).add(Aspects.AURA, 35),
                Ingredient.of(MaleficiumBlocks.WARPWOOD_LOG.asItem()),
                java.util.List.of(Ingredient.of(MaleficiumItems.PRIMORDIAL_NODULE), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(MaleficiumItems.WARPED_SHARD), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(TCItems.ZOMBIE_BRAIN), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(MaleficiumItems.WARPED_SHARD), Ingredient.of(TCResources.get("void_ingot")))));
        ThaumcraftApi.bookRecipe("ItemVoidFortressHelmet:goggles", ThaumcraftApi.infusionOnCentral("HELMGOGGLES",
                5, new AspectList().add(Aspects.SENSES, 32).add(Aspects.AURA, 16).add(Aspects.ARMOR, 16),
                Ingredient.of(MaleficiumItems.VOID_FORTRESS_HELMET),
                java.util.List.of(Ingredient.of(net.minecraft.world.item.Items.SLIME_BALL), Ingredient.of(TCItems.GOGGLES)),
                stack -> { stack.set(net.thaumcraft.registry.TCComponents.FORTRESS_GOGGLES, true); return stack; }));
        ThaumcraftApi.bookRecipe("ItemVoidFortressHelmet:mask0", ThaumcraftApi.infusionOnCentral("MASKGRINNINGDEVIL",
                8, new AspectList().add(Aspects.MIND, 64).add(Aspects.HEAL, 64).add(Aspects.ARMOR, 16),
                Ingredient.of(MaleficiumItems.VOID_FORTRESS_HELMET),
                java.util.List.of(Ingredient.of(net.minecraft.world.item.Items.INK_SAC), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(TCBlocks.SHIMMERLEAF.asItem()), Ingredient.of(TCItems.ZOMBIE_BRAIN), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT)),
                stack -> { stack.set(net.thaumcraft.registry.TCComponents.FORTRESS_MASK, 0); return stack; }));
        ThaumcraftApi.bookRecipe("ItemVoidFortressHelmet:mask1", ThaumcraftApi.infusionOnCentral("MASKANGRYGHOST",
                8, new AspectList().add(Aspects.ENTROPY, 64).add(Aspects.DEATH, 64).add(Aspects.ARMOR, 16),
                Ingredient.of(MaleficiumItems.VOID_FORTRESS_HELMET),
                java.util.List.of(Ingredient.of(net.minecraft.world.item.Items.BONE_MEAL), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(net.minecraft.world.item.Items.POISONOUS_POTATO), Ingredient.of(net.minecraft.world.item.Items.WITHER_SKELETON_SKULL), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT)),
                stack -> { stack.set(net.thaumcraft.registry.TCComponents.FORTRESS_MASK, 1); return stack; }));
        ThaumcraftApi.bookRecipe("ItemVoidFortressHelmet:mask2", ThaumcraftApi.infusionOnCentral("MASKSIPPINGFIEND",
                8, new AspectList().add(Aspects.UNDEAD, 64).add(Aspects.LIFE, 64).add(Aspects.ARMOR, 16),
                Ingredient.of(MaleficiumItems.VOID_FORTRESS_HELMET),
                java.util.List.of(Ingredient.of(net.minecraft.world.item.Items.DYE.red()), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(net.minecraft.world.item.Items.GHAST_TEAR), Ingredient.of(net.minecraft.world.item.Items.MILK_BUCKET), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT)),
                stack -> { stack.set(net.thaumcraft.registry.TCComponents.FORTRESS_MASK, 2); return stack; }));
        ThaumcraftApi.bookRecipe("ItemVoidFortressHelmet", ThaumcraftApi.infusion("VOIDFORTRESS",
                new ItemStack(MaleficiumItems.VOID_FORTRESS_HELMET), 6, new AspectList().add(Aspects.METAL, 24).add(Aspects.ARMOR, 16).add(Aspects.MAGIC, 8).add(Aspects.ELDRITCH, 16).add(Aspects.VOID, 16),
                Ingredient.of(TCItems.GEAR.get("void_helmet")),
                java.util.List.of(Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.EMERALD))));
        ThaumcraftApi.bookRecipe("ItemVoidFortressChestplate", ThaumcraftApi.infusion("VOIDFORTRESS",
                new ItemStack(MaleficiumItems.VOID_FORTRESS_CHESTPLATE), 6, new AspectList().add(Aspects.METAL, 24).add(Aspects.ARMOR, 24).add(Aspects.MAGIC, 8).add(Aspects.ELDRITCH, 16).add(Aspects.VOID, 24),
                Ingredient.of(TCItems.GEAR.get("void_chestplate")),
                java.util.List.of(Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.LEATHER))));
        ThaumcraftApi.bookRecipe("ItemVoidFortressLeggings", ThaumcraftApi.infusion("VOIDFORTRESS",
                new ItemStack(MaleficiumItems.VOID_FORTRESS_LEGGINGS), 6, new AspectList().add(Aspects.METAL, 24).add(Aspects.ARMOR, 20).add(Aspects.MAGIC, 8).add(Aspects.ELDRITCH, 16).add(Aspects.VOID, 20),
                Ingredient.of(TCItems.GEAR.get("void_leggings")),
                java.util.List.of(Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.LEATHER))));
        ThaumcraftApi.bookRecipe("ItemWarpedGoggles", ThaumcraftApi.infusion("WARPEDGOGGLES",
                new ItemStack(MaleficiumItems.WARPED_GOGGLES), 3, new AspectList().add(Aspects.ELDRITCH, 35).add(Aspects.DARKNESS, 12).add(Aspects.MAGIC, 16).add(Aspects.ARMOR, 8),
                Ingredient.of(TCItems.GOGGLES),
                java.util.List.of(Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(TCResources.get("quicksilver_drop")))));
        ThaumcraftApi.bookRecipe("ItemWandCap:0", ThaumcraftApi.infusion("CAP_shadowmetal",
                new ItemStack(MaleficiumItems.WAND_CAP_SHADOWMETAL), 8, new AspectList().add(Aspects.ELDRITCH, 55).add(Aspects.DARKNESS, 47).add(Aspects.MAGIC, 52).add(Aspects.METAL, 45).add(Aspects.VOID, 55),
                Ingredient.of(TCItems.WAND_CAPS.get("void")),
                java.util.List.of(Ingredient.of(MaleficiumItems.WARPED_SHARD), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(MaleficiumItems.PRIMORDIAL_NODULE), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT))));
        ThaumcraftApi.bookRecipe("ItemFocusTaintSwarm", ThaumcraftApi.infusion("TAINTFOCUS",
                new ItemStack(MaleficiumItems.FOCUS_TAINT_SWARM), 4, new AspectList().add(Aspects.TAINT, 45).add(Aspects.LIFE, 24).add(Aspects.MOTION, 24).add(Aspects.MAGIC, 16).add(Aspects.DEATH, 37),
                Ingredient.of(MaleficiumItems.FOCUS_VIS_SHARD),
                java.util.List.of(Ingredient.of(TCResources.get("tainted_goo")), Ingredient.of(MaleficiumItems.TAINTED_SHARD), Ingredient.of(TCItems.BOTTLE_TAINT), Ingredient.of(MaleficiumItems.TAINTED_SHARD), Ingredient.of(TCResources.get("tainted_goo")), Ingredient.of(MaleficiumItems.TAINTED_SHARD), Ingredient.of(TCItems.BOTTLE_TAINT), Ingredient.of(MaleficiumItems.TAINTED_SHARD))));
        ThaumcraftApi.bookRecipe("ItemFocusDarkMatter", ThaumcraftApi.infusion("ELDRITCHFOCUS",
                new ItemStack(MaleficiumItems.FOCUS_DARK_MATTER), 6, new AspectList().add(Aspects.ELDRITCH, 64).add(Aspects.DARKNESS, 32).add(Aspects.MAGIC, 32).add(Aspects.DEATH, 32).add(Aspects.VOID, 32),
                Ingredient.of(TCItems.FOCI.get("portable_hole")),
                java.util.List.of(Ingredient.of(TCItems.PRIMORDIAL_PEARL), Ingredient.of(MaleficiumItems.WARPED_SHARD), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(MaleficiumItems.WARPED_SHARD), Ingredient.of(TCItems.BUCKET_DEATH), Ingredient.of(MaleficiumItems.WARPED_SHARD), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(MaleficiumItems.WARPED_SHARD))));
        ThaumcraftApi.bookRecipe("ItemMaterial:7", ThaumcraftApi.infusion("KNIGHTROBES",
                new ItemStack(MaleficiumItems.CRIMSON_PLATING, 9), 2, new AspectList().add(Aspects.HUNGER, 4).add(Aspects.METAL, 8).add(Aspects.MAGIC, 6),
                Ingredient.of(net.minecraft.world.level.block.Blocks.IRON_BLOCK.asItem()),
                java.util.List.of(Ingredient.of(MaleficiumItems.CRIMSON_BLOOD), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), Ingredient.of(MaleficiumItems.CRIMSON_BLOOD), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), Ingredient.of(MaleficiumItems.CRIMSON_BLOOD), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET), Ingredient.of(MaleficiumItems.CRIMSON_BLOOD), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET))));
        ThaumcraftApi.bookRecipe("ItemVoidwalkerBoots", ThaumcraftApi.infusion("VOIDWALKERBOOTS",
                new ItemStack(MaleficiumItems.VOIDWALKER_BOOTS), 8, new AspectList().add(Aspects.DARKNESS, 42).add(Aspects.VOID, 56).add(Aspects.ELDRITCH, 38).add(Aspects.ARMOR, 60).add(Aspects.TRAVEL, 45),
                Ingredient.of(TCItems.TRAVELLER_BOOTS),
                java.util.List.of(Ingredient.of(MaleficiumItems.PRIMORDIAL_MOTE), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(MaleficiumItems.SHADOW_CLOTH), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(MaleficiumItems.SHADOW_CLOTH), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(TCItems.ELDRITCH_EYE), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(MaleficiumItems.SHADOW_CLOTH), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(MaleficiumItems.SHADOW_CLOTH), Ingredient.of(TCResources.get("void_ingot")))));
        ThaumcraftApi.bookRecipe("ItemSalis:0", ThaumcraftApi.infusion("SKYSALT",
                new ItemStack(MaleficiumItems.SALIS_TEMPESTAS, 3), 7, new AspectList().add(Aspects.AURA, 20).add(Aspects.WEATHER, 30).add(Aspects.WATER, 30),
                Ingredient.of(TCResources.get("salis_mundus")),
                java.util.List.of(Ingredient.of(MaleficiumItems.CREATION_FRAGMENT), Ingredient.of(TCResources.get("quicksilver_drop")))));
        ThaumcraftApi.bookRecipe("ItemSalis:1", ThaumcraftApi.infusion("TIMESALT",
                new ItemStack(MaleficiumItems.SALIS_AEVUM, 3), 7, new AspectList().add(Aspects.AURA, 20).add(Aspects.LIGHT, 30).add(Aspects.DARKNESS, 30),
                Ingredient.of(TCResources.get("salis_mundus")),
                java.util.List.of(Ingredient.of(MaleficiumItems.CREATION_FRAGMENT), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET))));
        ThaumcraftApi.bookRecipe("ItemThaumicDisassembler", ThaumcraftApi.infusion("THAUMICDISASSEMBLER",
                new ItemStack(MaleficiumItems.THAUMIC_DISASSEMBLER), 3, new AspectList().add(Aspects.METAL, 22).add(Aspects.TOOL, 24).add(Aspects.WEAPON, 14).add(Aspects.MECHANISM, 12),
                Ingredient.of(MaleficiumItems.THAUMIC_PLATING),
                java.util.List.of(Ingredient.of(MaleficiumItems.PRIMORDIAL_MOTE), Ingredient.of(TCItems.GEAR.get("void_pickaxe")), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCItems.GEAR.get("void_shovel")), Ingredient.of(net.minecraft.world.item.Items.DIAMOND), Ingredient.of(TCItems.GEAR.get("void_sword")), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCItems.GEAR.get("void_axe")))));
        ThaumcraftApi.bookRecipe("ItemShadowFortressHelmet:goggles", ThaumcraftApi.infusionOnCentral("HELMGOGGLES",
                5, new AspectList().add(Aspects.SENSES, 32).add(Aspects.AURA, 16).add(Aspects.ARMOR, 16),
                Ingredient.of(MaleficiumItems.SHADOW_FORTRESS_HELMET),
                java.util.List.of(Ingredient.of(net.minecraft.world.item.Items.SLIME_BALL), Ingredient.of(TCItems.GOGGLES)),
                stack -> { stack.set(net.thaumcraft.registry.TCComponents.FORTRESS_GOGGLES, true); return stack; }));
        ThaumcraftApi.bookRecipe("ItemShadowFortressHelmet:mask0", ThaumcraftApi.infusionOnCentral("MASKGRINNINGDEVIL",
                8, new AspectList().add(Aspects.MIND, 64).add(Aspects.HEAL, 64).add(Aspects.ARMOR, 16),
                Ingredient.of(MaleficiumItems.SHADOW_FORTRESS_HELMET),
                java.util.List.of(Ingredient.of(net.minecraft.world.item.Items.INK_SAC), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(TCBlocks.SHIMMERLEAF.asItem()), Ingredient.of(TCItems.ZOMBIE_BRAIN), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT)),
                stack -> { stack.set(net.thaumcraft.registry.TCComponents.FORTRESS_MASK, 0); return stack; }));
        ThaumcraftApi.bookRecipe("ItemShadowFortressHelmet:mask1", ThaumcraftApi.infusionOnCentral("MASKANGRYGHOST",
                8, new AspectList().add(Aspects.ENTROPY, 64).add(Aspects.DEATH, 64).add(Aspects.ARMOR, 16),
                Ingredient.of(MaleficiumItems.SHADOW_FORTRESS_HELMET),
                java.util.List.of(Ingredient.of(net.minecraft.world.item.Items.BONE_MEAL), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(net.minecraft.world.item.Items.POISONOUS_POTATO), Ingredient.of(net.minecraft.world.item.Items.WITHER_SKELETON_SKULL), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT)),
                stack -> { stack.set(net.thaumcraft.registry.TCComponents.FORTRESS_MASK, 1); return stack; }));
        ThaumcraftApi.bookRecipe("ItemShadowFortressHelmet:mask2", ThaumcraftApi.infusionOnCentral("MASKSIPPINGFIEND",
                8, new AspectList().add(Aspects.UNDEAD, 64).add(Aspects.LIFE, 64).add(Aspects.ARMOR, 16),
                Ingredient.of(MaleficiumItems.SHADOW_FORTRESS_HELMET),
                java.util.List.of(Ingredient.of(net.minecraft.world.item.Items.DYE.red()), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.LEATHER), Ingredient.of(net.minecraft.world.item.Items.GHAST_TEAR), Ingredient.of(net.minecraft.world.item.Items.MILK_BUCKET), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT)),
                stack -> { stack.set(net.thaumcraft.registry.TCComponents.FORTRESS_MASK, 2); return stack; }));
        ThaumcraftApi.bookRecipe("ItemShadowFortressHelmet", ThaumcraftApi.infusion("SHADOWFORTRESS",
                new ItemStack(MaleficiumItems.SHADOW_FORTRESS_HELMET), 7, new AspectList().add(Aspects.METAL, 28).add(Aspects.ARMOR, 20).add(Aspects.MAGIC, 12).add(Aspects.DARKNESS, 30).add(Aspects.VOID, 22),
                Ingredient.of(TCItems.GEAR.get("void_helmet")),
                java.util.List.of(Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.EMERALD))));
        ThaumcraftApi.bookRecipe("ItemShadowFortressChestplate", ThaumcraftApi.infusion("SHADOWFORTRESS",
                new ItemStack(MaleficiumItems.SHADOW_FORTRESS_CHESTPLATE), 7, new AspectList().add(Aspects.METAL, 38).add(Aspects.ARMOR, 26).add(Aspects.MAGIC, 18).add(Aspects.DARKNESS, 34).add(Aspects.VOID, 26),
                Ingredient.of(TCItems.GEAR.get("void_chestplate")),
                java.util.List.of(Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.LEATHER))));
        ThaumcraftApi.bookRecipe("ItemShadowFortressLeggings", ThaumcraftApi.infusion("SHADOWFORTRESS",
                new ItemStack(MaleficiumItems.SHADOW_FORTRESS_LEGGINGS), 7, new AspectList().add(Aspects.METAL, 32).add(Aspects.ARMOR, 24).add(Aspects.MAGIC, 16).add(Aspects.DARKNESS, 32).add(Aspects.VOID, 24),
                Ingredient.of(TCItems.GEAR.get("void_leggings")),
                java.util.List.of(Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.LEATHER))));
        ThaumcraftApi.bookRecipe("ItemKatana:0", ThaumcraftApi.infusion("THAUMIUMKATANA",
                new ItemStack(MaleficiumItems.THAUMIUM_FORTRESS_BLADE), 3, new AspectList().add(Aspects.METAL, 42).add(Aspects.WEAPON, 45).add(Aspects.MAGIC, 22),
                Ingredient.of(TCItems.GEAR.get("thaumium_sword")),
                java.util.List.of(Ingredient.of(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.EMERALD), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.EMERALD), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(TCResources.get("thaumium_ingot")))));
        ThaumcraftApi.bookRecipe("ItemKatana:1", ThaumcraftApi.infusion("VOIDMETALKATANA",
                new ItemStack(MaleficiumItems.VOIDMETAL_FORTRESS_BLADE), 6, new AspectList().add(Aspects.METAL, 52).add(Aspects.WEAPON, 55).add(Aspects.MAGIC, 32).add(Aspects.VOID, 46).add(Aspects.ELDRITCH, 12),
                Ingredient.of(TCItems.GEAR.get("void_sword")),
                java.util.List.of(Ingredient.of(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.EMERALD), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.EMERALD), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(TCResources.get("void_ingot")))));
        ThaumcraftApi.bookRecipe("ItemKatana:2", ThaumcraftApi.infusion("SHADOWMETALKATANA",
                new ItemStack(MaleficiumItems.SHADOWMETAL_FORTRESS_BLADE), 7, new AspectList().add(Aspects.METAL, 62).add(Aspects.WEAPON, 65).add(Aspects.MAGIC, 42).add(Aspects.VOID, 16).add(Aspects.DARKNESS, 46),
                Ingredient.of(MaleficiumItems.SHADOWMETAL_SWORD),
                java.util.List.of(Ingredient.of(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.EMERALD), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(net.minecraft.world.level.block.Blocks.OBSIDIAN.asItem()), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.EMERALD), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(MaleficiumItems.SHADOWMETAL_INGOT))));
        ThaumcraftApi.bookRecipe("ItemFlyteCharm", ThaumcraftApi.infusion("FLYTECHARM",
                new ItemStack(MaleficiumItems.FLYTE_CHARM), 7, new AspectList().add(Aspects.FLIGHT, 86).add(Aspects.AURA, 56).add(Aspects.SENSES, 25).add(Aspects.MAGIC, 48).add(Aspects.ENERGY, 65),
                Ingredient.of(TCItems.PRIMAL_ARROWS.get("air")),
                java.util.List.of(Ingredient.of(MaleficiumItems.CREATION_SHARD), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.FEATHER), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT))));
        ThaumcraftApi.bookRecipe("ItemGateKey", ThaumcraftApi.infusion("GATEKEY",
                new ItemStack(MaleficiumItems.GATE_KEY), 7, new AspectList().add(Aspects.MOTION, 40).add(Aspects.AURA, 20).add(Aspects.TRAVEL, 25).add(Aspects.MAGIC, 30),
                Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT),
                java.util.List.of(Ingredient.of(MaleficiumItems.CREATION_FRAGMENT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(net.minecraft.world.item.Items.ENDER_PEARL), Ingredient.of(net.minecraft.world.item.Items.FEATHER), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(net.minecraft.world.item.Items.ENDER_PEARL))));
        ThaumcraftApi.bookRecipe("ItemVoidmetalGoggles", ThaumcraftApi.infusion("VOIDGOGGLES",
                new ItemStack(MaleficiumItems.VOIDMETAL_GOGGLES), 5, new AspectList().add(Aspects.VOID, 40).add(Aspects.SENSES, 35).add(Aspects.ARMOR, 20),
                Ingredient.of(MaleficiumItems.WARPED_GOGGLES),
                java.util.List.of(Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(TCResources.get("quicksilver_drop")))));
        ThaumcraftApi.bookRecipe("ItemLumosRing", ThaumcraftApi.infusion("LUMOSRING",
                new ItemStack(MaleficiumItems.LUMOS_RING), 1, new AspectList().add(Aspects.LIGHT, 35).add(Aspects.SENSES, 25).add(Aspects.AURA, 10),
                Ingredient.of(TCItems.MUNDANE_RING),
                java.util.List.of(Ingredient.of(MaleficiumItems.FOCUS_LUMOS), Ingredient.of(TCResources.get("amber")), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(TCResources.get("salis_mundus")), Ingredient.of(TCResources.get("amber")), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT))));
        ThaumcraftApi.bookRecipe("ItemKatanaThaumium:inscription0", ThaumcraftApi.infusionOnCentral("INSCRIPTIONFIRE",
                8, new AspectList().add(Aspects.FIRE, 64).add(Aspects.ENERGY, 64).add(Aspects.WEAPON, 16),
                Ingredient.of(MaleficiumItems.THAUMIUM_FORTRESS_BLADE),
                java.util.List.of(Ingredient.of(TCItems.FOCI.get("fire")), Ingredient.of(net.minecraft.world.item.Items.COAL), Ingredient.of(net.minecraft.world.level.block.Blocks.NETHERRACK.asItem()), Ingredient.of(net.minecraft.world.item.Items.FIRE_CHARGE), Ingredient.of(net.minecraft.world.item.Items.BLAZE_POWDER), Ingredient.of(TCItems.NITOR)),
                stack -> { stack.set(net.thaumcraft.registry.TCComponents.KATANA_INSCRIPTION, 0); return stack; }));
        ThaumcraftApi.bookRecipe("ItemKatanaThaumium:inscription1", ThaumcraftApi.infusionOnCentral("INSCRIPTIONTHUNDER",
                8, new AspectList().add(Aspects.AIR, 64).add(Aspects.WEATHER, 64).add(Aspects.WEAPON, 16),
                Ingredient.of(MaleficiumItems.THAUMIUM_FORTRESS_BLADE),
                java.util.List.of(Ingredient.of(MaleficiumItems.FOCUS_SHOCKWAVE), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(net.minecraft.world.item.Items.FEATHER), Ingredient.of(net.minecraft.world.level.block.Blocks.TNT.asItem()), Ingredient.of(TCItems.SHARDS.get("entropy")), Ingredient.of(net.minecraft.world.item.Items.GUNPOWDER)),
                stack -> { stack.set(net.thaumcraft.registry.TCComponents.KATANA_INSCRIPTION, 1); return stack; }));
        ThaumcraftApi.bookRecipe("ItemKatanaThaumium:inscription2", ThaumcraftApi.infusionOnCentral("INSCRIPTIONHEAL",
                8, new AspectList().add(Aspects.HEAL, 64).add(Aspects.UNDEAD, 64).add(Aspects.WEAPON, 16),
                Ingredient.of(MaleficiumItems.THAUMIUM_FORTRESS_BLADE),
                java.util.List.of(Ingredient.of(TCItems.FOCI.get("pech")), Ingredient.of(net.minecraft.world.item.Items.BONE), Ingredient.of(net.minecraft.world.item.Items.ROTTEN_FLESH), Ingredient.of(net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE), Ingredient.of(TCItems.ZOMBIE_BRAIN), Ingredient.of(TCItems.BATH_SALTS)),
                stack -> { stack.set(net.thaumcraft.registry.TCComponents.KATANA_INSCRIPTION, 2); return stack; }));
        ThaumcraftApi.bookRecipe("ItemKatana:inscription0", ThaumcraftApi.infusionOnCentral("INSCRIPTIONFIRE",
                8, new AspectList().add(Aspects.FIRE, 64).add(Aspects.ENERGY, 64).add(Aspects.WEAPON, 16),
                Ingredient.of(MaleficiumItems.THAUMIUM_FORTRESS_BLADE, MaleficiumItems.VOIDMETAL_FORTRESS_BLADE, MaleficiumItems.SHADOWMETAL_FORTRESS_BLADE),
                java.util.List.of(Ingredient.of(TCItems.FOCI.get("fire")), Ingredient.of(net.minecraft.world.item.Items.COAL), Ingredient.of(net.minecraft.world.level.block.Blocks.NETHERRACK.asItem()), Ingredient.of(net.minecraft.world.item.Items.FIRE_CHARGE), Ingredient.of(net.minecraft.world.item.Items.BLAZE_POWDER), Ingredient.of(TCItems.NITOR)),
                stack -> { stack.set(net.thaumcraft.registry.TCComponents.KATANA_INSCRIPTION, 0); return stack; }));
        ThaumcraftApi.bookRecipe("ItemKatana:inscription1", ThaumcraftApi.infusionOnCentral("INSCRIPTIONTHUNDER",
                8, new AspectList().add(Aspects.AIR, 64).add(Aspects.WEATHER, 64).add(Aspects.WEAPON, 16),
                Ingredient.of(MaleficiumItems.THAUMIUM_FORTRESS_BLADE, MaleficiumItems.VOIDMETAL_FORTRESS_BLADE, MaleficiumItems.SHADOWMETAL_FORTRESS_BLADE),
                java.util.List.of(Ingredient.of(MaleficiumItems.FOCUS_SHOCKWAVE), Ingredient.of(TCItems.SHARDS.get("air")), Ingredient.of(net.minecraft.world.item.Items.FEATHER), Ingredient.of(net.minecraft.world.level.block.Blocks.TNT.asItem()), Ingredient.of(TCItems.SHARDS.get("entropy")), Ingredient.of(net.minecraft.world.item.Items.GUNPOWDER)),
                stack -> { stack.set(net.thaumcraft.registry.TCComponents.KATANA_INSCRIPTION, 1); return stack; }));
        ThaumcraftApi.bookRecipe("ItemKatana:inscription2", ThaumcraftApi.infusionOnCentral("INSCRIPTIONHEAL",
                8, new AspectList().add(Aspects.HEAL, 64).add(Aspects.UNDEAD, 64).add(Aspects.WEAPON, 16),
                Ingredient.of(MaleficiumItems.THAUMIUM_FORTRESS_BLADE, MaleficiumItems.VOIDMETAL_FORTRESS_BLADE, MaleficiumItems.SHADOWMETAL_FORTRESS_BLADE),
                java.util.List.of(Ingredient.of(TCItems.FOCI.get("pech")), Ingredient.of(net.minecraft.world.item.Items.BONE), Ingredient.of(net.minecraft.world.item.Items.ROTTEN_FLESH), Ingredient.of(net.minecraft.world.item.Items.FERMENTED_SPIDER_EYE), Ingredient.of(TCItems.ZOMBIE_BRAIN), Ingredient.of(TCItems.BATH_SALTS)),
                stack -> { stack.set(net.thaumcraft.registry.TCComponents.KATANA_INSCRIPTION, 2); return stack; }));
        ThaumcraftApi.bookRecipe("ItemMaterial:2", ThaumcraftApi.arcaneShapeless("CRIMSONROBES",
                new ItemStack(MaleficiumItems.CRIMSON_CLOTH), new AspectList().add(Aspects.FIRE, 5).add(Aspects.ENTROPY, 5),
                java.util.List.of(Ingredient.of(MaleficiumItems.CRIMSON_BLOOD), Ingredient.of(TCResources.get("enchanted_fabric")), Ingredient.of(net.minecraft.world.item.Items.GOLD_NUGGET))));
        ThaumcraftApi.bookRecipe("ItemWarpFertilizer", ThaumcraftApi.arcaneShapeless("WARPTREE",
                new ItemStack(MaleficiumItems.WARP_FERTILIZER), new AspectList().add(Aspects.ENTROPY, 150),
                java.util.List.of(Ingredient.of(TCItems.ZOMBIE_BRAIN), Ingredient.of(MaleficiumItems.WARPED_SHARD), Ingredient.of(TCItems.WISP_ESSENCE), Ingredient.of(net.minecraft.world.item.Items.BONE_MEAL))));
        ThaumcraftApi.bookRecipe("ItemMaterial:6", ThaumcraftApi.arcane("THAUMICDISASSEMBLER",
                new ItemStack(MaleficiumItems.THAUMIC_PLATING), new AspectList().add(Aspects.ORDER, 50).add(Aspects.ENTROPY, 50),
                java.util.Arrays.asList(Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(TCResources.get("thaumium_ingot")), null, Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCResources.get("void_ingot")), Ingredient.of(TCResources.get("thaumium_ingot")), Ingredient.of(TCResources.get("void_ingot")))));
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
        ThaumcraftApi.bookRecipe("ItemHelmetCultistLeaderPlate", ThaumcraftApi.arcane("PRAETORARMOR",
                new ItemStack(TCItems.CULTIST_LEADER_HELMET), new AspectList().add(Aspects.ORDER, 15).add(Aspects.ENTROPY, 15).add(Aspects.FIRE, 15),
                java.util.Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(MaleficiumItems.CRIMSON_PLATING), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(TCItems.CULTIST_PLATE_HELMET), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(MaleficiumItems.CRIMSON_PLATING), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT))));
        ThaumcraftApi.bookRecipe("ItemChestCultistLeaderPlate", ThaumcraftApi.arcane("PRAETORARMOR",
                new ItemStack(TCItems.CULTIST_LEADER_CHESTPLATE), new AspectList().add(Aspects.ORDER, 25).add(Aspects.ENTROPY, 25).add(Aspects.FIRE, 25),
                java.util.Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(MaleficiumItems.CRIMSON_PLATING), Ingredient.of(TCItems.CULTIST_PLATE_CHESTPLATE), Ingredient.of(MaleficiumItems.CRIMSON_PLATING), Ingredient.of(MaleficiumItems.CRIMSON_PLATING), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(MaleficiumItems.CRIMSON_PLATING))));
        ThaumcraftApi.bookRecipe("ItemLegsCultistLeaderPlate", ThaumcraftApi.arcane("PRAETORARMOR",
                new ItemStack(TCItems.CULTIST_LEADER_LEGGINGS), new AspectList().add(Aspects.ORDER, 20).add(Aspects.ENTROPY, 20).add(Aspects.FIRE, 20),
                java.util.Arrays.asList(Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(net.minecraft.world.item.Items.GOLD_INGOT), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(MaleficiumItems.CRIMSON_PLATING), Ingredient.of(TCItems.CULTIST_PLATE_LEGGINGS), Ingredient.of(MaleficiumItems.CRIMSON_PLATING), Ingredient.of(MaleficiumItems.CRIMSON_PLATING), Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT), Ingredient.of(MaleficiumItems.CRIMSON_PLATING))));
        ThaumcraftApi.bookRecipe("ItemMaterial:1", ThaumcraftApi.arcane("SHADOWMETAL",
                new ItemStack(MaleficiumItems.SHADOW_CLOTH), new AspectList().add(Aspects.ORDER, 10).add(Aspects.ENTROPY, 10),
                java.util.Arrays.asList(null, Ingredient.of(MaleficiumItems.SHADOWMETAL_NUGGET), null, Ingredient.of(MaleficiumItems.SHADOWMETAL_NUGGET), Ingredient.of(TCResources.get("enchanted_fabric")), Ingredient.of(MaleficiumItems.SHADOWMETAL_NUGGET), null, Ingredient.of(MaleficiumItems.SHADOWMETAL_NUGGET), null)));
        ThaumcraftApi.bookRecipe("ItemMagicFunguar", ThaumcraftApi.arcaneShapeless("MAGICFUNGUAR",
                new ItemStack(MaleficiumItems.MAGIC_FUNGUAR), new AspectList().add(Aspects.AIR, 1).add(Aspects.FIRE, 1).add(Aspects.WATER, 1).add(Aspects.EARTH, 1).add(Aspects.ORDER, 1).add(Aspects.ENTROPY, 1),
                java.util.List.of(Ingredient.of(TCBlocks.VISHROOM.asItem()), Ingredient.of(TCItems.SHARDS.values().toArray(new net.minecraft.world.item.Item[0])), Ingredient.of(net.minecraft.world.item.Items.GLOWSTONE_DUST), Ingredient.of(net.minecraft.world.item.Items.REDSTONE))));
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
        ThaumcraftApi.bookRecipe("ItemFocusVisShard", ThaumcraftApi.arcane("FOCUSSHARD",
                new ItemStack(MaleficiumItems.FOCUS_VIS_SHARD), new AspectList().add(Aspects.AIR, 46).add(Aspects.ENTROPY, 38).add(Aspects.ORDER, 22),
                java.util.Arrays.asList(Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(TCItems.WISP_ESSENCE), Ingredient.of(MaleficiumItems.TAINTED_SHARD), Ingredient.of(TCItems.WISP_ESSENCE), Ingredient.of(TCResources.get("quicksilver")), Ingredient.of(TCItems.WISP_ESSENCE), Ingredient.of(MaleficiumItems.TAINTED_SHARD), Ingredient.of(TCItems.WISP_ESSENCE), Ingredient.of(TCItems.SHARD_BALANCED))));
        ThaumcraftApi.bookRecipe("ItemFocusLumos", ThaumcraftApi.arcane("FOCUSLUMOS",
                new ItemStack(MaleficiumItems.FOCUS_LUMOS), new AspectList().add(Aspects.AIR, 52).add(Aspects.FIRE, 56).add(Aspects.ORDER, 28),
                java.util.Arrays.asList(Ingredient.of(TCItems.NITOR), Ingredient.of(net.minecraft.world.item.Items.GLOWSTONE_DUST), Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(net.minecraft.world.item.Items.GLOWSTONE_DUST), Ingredient.of(TCResources.get("quicksilver")), Ingredient.of(net.minecraft.world.item.Items.GLOWSTONE_DUST), Ingredient.of(TCItems.SHARD_BALANCED), Ingredient.of(net.minecraft.world.item.Items.GLOWSTONE_DUST), Ingredient.of(TCItems.NITOR))));
        ThaumcraftApi.bookRecipe("ItemMaterial:9", ThaumcraftApi.arcaneShapeless("BREAKPEARL",
                new ItemStack(MaleficiumItems.PRIMORDIAL_NODULE, 3), new AspectList().add(Aspects.ENTROPY, 25),
                java.util.List.of(Ingredient.of(TCItems.PRIMORDIAL_PEARL))));
        ThaumcraftApi.bookRecipe("ItemMaterial:10", ThaumcraftApi.arcaneShapeless("BREAKPEARL",
                new ItemStack(MaleficiumItems.PRIMORDIAL_MOTE, 3), new AspectList().add(Aspects.ENTROPY, 15),
                java.util.List.of(Ingredient.of(MaleficiumItems.PRIMORDIAL_NODULE))));
        ThaumcraftApi.bookRecipe("ItemMaterial:11", ThaumcraftApi.arcaneShapeless("CREATIONSHARD",
                new ItemStack(MaleficiumItems.CREATION_FRAGMENT, 9), new AspectList().add(Aspects.ENTROPY, 99),
                java.util.List.of(Ingredient.of(MaleficiumItems.CREATION_SHARD))));
        ThaumcraftApi.bookRecipe("ItemVoidBlood", ThaumcraftApi.arcane("VOIDBLOOD",
                new ItemStack(MaleficiumItems.VOID_BLOOD), new AspectList().add(Aspects.ORDER, 35).add(Aspects.ENTROPY, 25),
                java.util.Arrays.asList(null, Ingredient.of(MaleficiumItems.PRIMORDIAL_MOTE), null, Ingredient.of(TCResources.get("void_seed")), Ingredient.of(MaleficiumItems.CRIMSON_BLOOD), Ingredient.of(TCResources.get("void_seed")), null, Ingredient.of(TCItems.WISP_ESSENCE), null)));
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
