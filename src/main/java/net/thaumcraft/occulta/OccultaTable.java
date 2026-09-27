package net.thaumcraft.occulta;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.api.ThaumcraftApi;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.research.Page;

import java.util.List;

/**
 * A aba do Ars Occulta no Thaumonomicon.
 *
 * <p>O Witchery não é addon de Thaumcraft e não tem pesquisa nenhuma: lá o que se aprende está num livro escrito
 * à parte. A árvore desta aba é a que a <b>lore de quem joga</b> marca — O Caminho Antigo, Bruxaria, Resonantia
 * Naturae e o Altar da Bruxa, e daí as quatro linhas. O que cada pesquisa ensina, porém, é o que o original faz.
 *
 * <p><b>Uma escolha declarada:</b> na lore as quatro linhas saem todas do Altar. Aqui as duas que já têm coisa
 * dentro — as Plantas de Ritual e os Cozimentos — saem de onde a mão alcança, que é antes dele: sem as plantas não
 * há Pasta de Unção, e sem o caldeirão não há Mutandis nem as madeiras do ofício. A ordem do que se aprende segue
 * a ordem do que se faz.
 */
public final class OccultaTable {
    private OccultaTable() {
    }

    public static void research() {
        // o degrau de entrada: o ramo abre-se a quem topar com uma tradição de bruxa
        ThaumcraftApi.research("AO_OLD_WAYS", Occulta.CATEGORY)
                .at(0, -2)
                .icon(() -> new ItemStack(OccultaItems.MANDRAKE_ROOT))
                .round()
                .auto()
                .special()
                .pages(Page.text("tc.research_page.AO_OLD_WAYS.1"), Page.text("tc.research_page.AO_OLD_WAYS.2"))
                .register();

        // as oito plantas, que é por onde a mão começa
        ThaumcraftApi.research("AO_PLANTS", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.PLANT, 4).add(Aspects.CROP, 3).add(Aspects.MAGIC, 2))
                .at(-2, 0)
                .icon(() -> new ItemStack(OccultaItems.BELLADONNA_FLOWER))
                .parents("AO_OLD_WAYS")
                .round()
                .pages(Page.text("tc.research_page.AO_PLANTS.1"), Page.text("tc.research_page.AO_PLANTS.2"))
                .register();

        // o forno, os potes e os sete cheiros
        ThaumcraftApi.research("AO_WITCHCRAFT", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 3).add(Aspects.FIRE, 2).add(Aspects.CRAFT, 2))
                .at(0, 0)
                .icon(() -> new ItemStack(OccultaItems.WITCHES_OVEN))
                .parents("AO_OLD_WAYS")
                .pages(Page.text("tc.research_page.AO_WITCHCRAFT.1"), Page.crafting("AOWitchesOven"),
                        Page.crafting("AOSoftClayJar"), Page.crafting("AOFumeFunnel"),
                        Page.text("tc.research_page.AO_WITCHCRAFT.2"))
                .register();

        // o caldeirão e o que sai dele
        ThaumcraftApi.research("AO_BREWS", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.WATER, 4).add(Aspects.MAGIC, 3).add(Aspects.EXCHANGE, 2))
                .at(2, 0)
                .icon(() -> new ItemStack(OccultaItems.WITCHES_CAULDRON))
                .parents("AO_WITCHCRAFT", "AO_PLANTS")
                .pages(Page.text("tc.research_page.AO_BREWS.1"), Page.crafting("AOAnointingPaste"),
                        Page.text("tc.research_page.AO_BREWS.2"), Page.text("tc.research_page.AO_BREWS.3"))
                .register();

        // as três árvores, que só o Mutandis alcança
        ThaumcraftApi.research("AO_RESONANTIA", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.TREE, 4).add(Aspects.PLANT, 3).add(Aspects.MAGIC, 2))
                .at(0, 2)
                .icon(() -> new ItemStack(OccultaItems.WOOD.get("rowan_sapling")))
                .parents("AO_BREWS")
                .pages(Page.text("tc.research_page.AO_RESONANTIA.1"), Page.text("tc.research_page.AO_RESONANTIA.2"),
                        Page.crafting("AORowanPlanks"))
                .register();

        // e o altar, que junta o poder de tudo isso
        ThaumcraftApi.research("AO_ALTAR", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.EARTH, 5).add(Aspects.MAGIC, 4).add(Aspects.ENERGY, 3))
                .at(0, 4)
                .icon(() -> new ItemStack(OccultaItems.WITCH_ALTAR))
                .parents("AO_RESONANTIA")
                .pages(Page.text("tc.research_page.AO_ALTAR.1"), Page.crafting("AOAltar"),
                        Page.text("tc.research_page.AO_ALTAR.2"))
                .register();

        // e o que o caldeirão faz com o poder do altar: os cozimentos
        ThaumcraftApi.research("AO_POTIONS", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.WATER, 5).add(Aspects.MAGIC, 4).add(Aspects.LIFE, 3)
                        .add(Aspects.ENERGY, 2))
                .at(2, 4)
                .icon(() -> net.thaumcraft.occulta.brew.BrewItem.of(OccultaItems.BREW,
                        java.util.List.of(net.minecraft.world.item.Items.NETHER_WART,
                                net.minecraft.world.item.Items.SPIDER_EYE)))
                .parents("AO_ALTAR")
                .pages(Page.text("tc.research_page.AO_POTIONS.1"), Page.text("tc.research_page.AO_POTIONS.2"),
                        Page.text("tc.research_page.AO_POTIONS.3"), Page.text("tc.research_page.AO_POTIONS.4"))
                .register();
    }

    /** As receitas que o livro mostra. Correm depois de tudo montado, porque pedem os itens prontos. */
    public static void recipes() {
        ThaumcraftApi.bookRecipe("AOWitchesOven", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.WITCHES_OVEN), 3, 3, List.of(
                        List.<ItemStack>of(), List.of(new ItemStack(Items.IRON_BARS)), List.<ItemStack>of(),
                        List.of(new ItemStack(Items.IRON_INGOT)), List.of(new ItemStack(Items.IRON_INGOT)),
                        List.of(new ItemStack(Items.IRON_INGOT)),
                        List.of(new ItemStack(Items.IRON_INGOT)), List.of(new ItemStack(Items.IRON_BARS)),
                        List.of(new ItemStack(Items.IRON_INGOT)))));

        ThaumcraftApi.bookRecipe("AOSoftClayJar", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.SOFT_CLAY_JAR, 4), 3, 2, List.of(
                        List.<ItemStack>of(), List.of(new ItemStack(Items.CLAY_BALL)), List.<ItemStack>of(),
                        List.of(new ItemStack(Items.CLAY_BALL)), List.of(new ItemStack(Items.CLAY_BALL)),
                        List.of(new ItemStack(Items.CLAY_BALL)))));

        ThaumcraftApi.bookRecipe("AOFumeFunnel", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.FUME_FUNNEL), 3, 3, List.of(
                        List.of(new ItemStack(Items.BUCKET)), List.of(new ItemStack(Items.LAVA_BUCKET)),
                        List.of(new ItemStack(Items.BUCKET)),
                        List.of(new ItemStack(Items.BUCKET)), List.of(new ItemStack(Items.GLOWSTONE)),
                        List.of(new ItemStack(Items.BUCKET)),
                        List.of(new ItemStack(Items.IRON_BLOCK)), List.of(new ItemStack(Items.IRON_BARS)),
                        List.of(new ItemStack(Items.IRON_BLOCK)))));

        ThaumcraftApi.bookRecipe("AOAnointingPaste", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.ANOINTING_PASTE), 2, 2, List.of(
                        List.of(new ItemStack(OccultaItems.WATER_ARTICHOKE_SEEDS)),
                        List.of(new ItemStack(OccultaItems.MANDRAKE_SEEDS)),
                        List.of(new ItemStack(OccultaItems.BELLADONNA_SEEDS)),
                        List.of(new ItemStack(OccultaItems.SNOWBELL_SEEDS)))));

        ThaumcraftApi.bookRecipe("AORowanPlanks", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.WOOD.get("rowan_planks"), 4), 1, 1,
                List.of(List.of(new ItemStack(OccultaItems.WOOD.get("rowan_log"))))));

        ThaumcraftApi.bookRecipe("AOAltar", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.WITCH_ALTAR, 3), 3, 3, List.of(
                        List.of(new ItemStack(OccultaItems.BREATH_OF_THE_GODDESS)),
                        List.of(new ItemStack(Items.POTION)),
                        List.of(new ItemStack(OccultaItems.EXHALE_OF_THE_HORNED_ONE)),
                        List.of(new ItemStack(Items.STONE_BRICKS)),
                        List.of(new ItemStack(OccultaItems.WOOD.get("rowan_log"))),
                        List.of(new ItemStack(Items.STONE_BRICKS)),
                        List.of(new ItemStack(Items.STONE_BRICKS)),
                        List.of(new ItemStack(OccultaItems.WOOD.get("rowan_log"))),
                        List.of(new ItemStack(Items.STONE_BRICKS)))));
    }
}
