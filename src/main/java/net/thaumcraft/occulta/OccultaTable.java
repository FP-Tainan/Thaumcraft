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
        // e daqui saem as receitas, por feitio do que fazem
        ThaumcraftApi.research("AO_BREW_BODY", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.MOTION, 4).add(Aspects.LIFE, 3).add(Aspects.WATER, 2))
                .at(4, 2)
                .icon(() -> brewStack(Items.NETHER_WART, Items.SUGAR))
                .parents("AO_POTIONS")
                .pages(Page.text("tc.research_page.AO_BREW_BODY.1"),
                        brewPage(Items.NETHER_WART, Items.SUGAR),
                        brewPage(Items.NETHER_WART, Items.FERMENTED_SPIDER_EYE, Items.SUGAR),
                        brewPage(Items.NETHER_WART, Items.LEATHER),
                        brewPage(Items.NETHER_WART, Items.BLAZE_POWDER),
                        brewPage(OccultaItems.MANDRAKE_ROOT, Items.COD),
                        brewPage(Items.NETHER_WART, Items.FEATHER),
                        brewPage(Items.NETHER_WART, Items.SUGAR_CANE),
                        brewPage(Items.NETHER_WART, Items.RED_MUSHROOM),
                        brewPage(Items.NETHER_WART, Items.COBWEB),
                        brewPage(Items.NETHER_WART, Items.FERMENTED_SPIDER_EYE, Items.COBWEB))
                .register();

        ThaumcraftApi.research("AO_BREW_SENSES", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.SENSES, 4).add(Aspects.AIR, 3).add(Aspects.WATER, 2))
                .at(4, 4)
                .icon(() -> brewStack(Items.NETHER_WART, Items.GOLDEN_CARROT))
                .parents("AO_POTIONS")
                .pages(Page.text("tc.research_page.AO_BREW_SENSES.1"),
                        brewPage(Items.NETHER_WART, Items.GOLDEN_CARROT),
                        brewPage(Items.NETHER_WART, Items.FERMENTED_SPIDER_EYE, Items.GOLDEN_CARROT),
                        brewPage(Items.NETHER_WART, Items.PUFFERFISH),
                        brewPage(Items.NETHER_WART, Items.MAGMA_CREAM))
                .register();

        ThaumcraftApi.research("AO_BREW_LIFE", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.LIFE, 5).add(Aspects.HEAL, 4).add(Aspects.MAGIC, 2))
                .at(6, 3)
                .icon(() -> brewStack(Items.NETHER_WART, Items.GHAST_TEAR))
                .parents("AO_BREW_BODY", "AO_BREW_SENSES")
                .pages(Page.text("tc.research_page.AO_BREW_LIFE.1"),
                        brewPage(Items.NETHER_WART, Items.GHAST_TEAR),
                        brewPage(Items.NETHER_WART, Items.GLISTERING_MELON_SLICE),
                        brewPage(Items.NETHER_WART, Items.DIAMOND, Items.GOLDEN_APPLE),
                        brewPage(Items.NETHER_WART, Items.DIAMOND, Items.ENCHANTED_GOLDEN_APPLE),
                        brewPage(Items.NETHER_WART, Items.GRAVEL),
                        brewPage(Items.NETHER_WART, Items.DIAMOND, OccultaItems.FOUL_FUME))
                .register();

        ThaumcraftApi.research("AO_BREW_HARM", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.POISON, 5).add(Aspects.DEATH, 4).add(Aspects.ENTROPY, 3))
                .at(2, 6)
                .icon(() -> brewStack(Items.NETHER_WART, Items.SPIDER_EYE))
                .parents("AO_POTIONS")
                .pages(Page.text("tc.research_page.AO_BREW_HARM.1"),
                        brewPage(Items.NETHER_WART, Items.SPIDER_EYE),
                        brewPage(Items.NETHER_WART, Items.GLOWSTONE_DUST, Items.SPIDER_EYE),
                        brewPage(Items.NETHER_WART, Items.DIAMOND, Items.WITHER_SKELETON_SKULL),
                        brewPage(Items.NETHER_WART, Items.DIAMOND, Items.INK_SAC),
                        brewPage(Items.NETHER_WART, Items.DIAMOND, Items.NETHER_STAR, Items.SALMON),
                        brewPage(Items.NETHER_WART, Items.DIAMOND, Items.SOUL_SAND),
                        brewPage(Items.NETHER_WART, Items.CACTUS),
                        brewPage(Items.NETHER_WART, Items.DIAMOND, Items.SHORT_GRASS))
                .register();

        splash();
        world();
        distillery();
        circles();
        poppets();
        mirrors();
        kettle();
        machines();
        dreams();
        spirit();
        arthana();
        ghost();
    }

    /** O Caldeirão de Pote e os frascos que ele faz. */
    private static void kettle() {
        ThaumcraftApi.research("AO_KETTLE", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.WATER, 5).add(Aspects.FIRE, 4).add(Aspects.CRAFT, 3)
                        .add(Aspects.MAGIC, 2))
                .at(2, 8)
                .icon(() -> new ItemStack(OccultaItems.WITCHES_KETTLE))
                .parents("AO_BREW_SPLASH")
                .pages(Page.text("tc.research_page.AO_KETTLE.1"),
                        Page.text("tc.research_page.AO_KETTLE.2"),
                        kettlePage(OccultaItems.BREW_OF_VINES),
                        kettlePage(OccultaItems.BREW_OF_THORNS),
                        kettlePage(OccultaItems.BREW_OF_INK),
                        kettlePage(OccultaItems.BREW_OF_SPROUTING),
                        kettlePage(OccultaItems.BREW_OF_EROSION),
                        kettlePage(OccultaItems.BREW_OF_LOVE),
                        kettlePage(OccultaItems.BREW_OF_RAISING),
                        kettlePage(OccultaItems.BREW_OF_WEBS),
                        kettlePage(OccultaItems.BREW_OF_ICE),
                        kettlePage(OccultaItems.BREW_OF_INFECTION),
                        kettlePage(OccultaItems.BREW_SUBSTITUTION),
                        kettlePage(OccultaItems.BREW_OF_THE_DEPTHS),
                        kettlePage(OccultaItems.REDSTONE_SOUP))
                .register();
    }

    /** A Roca, o Braseiro e o Crisol de Sangue: as três coisas que a Pedra Sintonizada abre. */
    private static void machines() {
        ThaumcraftApi.research("AO_MACHINES", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.CRAFT, 5).add(Aspects.MECHANISM, 4).add(Aspects.CLOTH, 3)
                        .add(Aspects.MAGIC, 2))
                .at(4, 6)
                .icon(() -> new ItemStack(OccultaItems.SPINNING_WHEEL))
                .parents("AO_KETTLE", "AO_ARTHANA")
                .pages(Page.text("tc.research_page.AO_MACHINES.1"),
                        Page.crafting("AOAttunedStone"),
                        Page.text("tc.research_page.AO_MACHINES.2"),
                        Page.crafting("AOSpinningWheel"),
                        Page.text("tc.research_page.AO_MACHINES.3"),
                        Page.crafting("AOBrazier"),
                        Page.text("tc.research_page.AO_MACHINES.4"),
                        Page.crafting("AOBloodCrucible"),
                        Page.text("tc.research_page.AO_MACHINES.5"))
                .register();
    }

    /** O outro lado: o sono, o Mundo dos Espíritos e as teias que se pregam à parede. */
    private static void dreams() {
        ThaumcraftApi.research("AO_DREAMS", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.MIND, 5).add(Aspects.SOUL, 4).add(Aspects.TRAVEL, 3)
                        .add(Aspects.ELDRITCH, 2))
                .at(4, 8)
                .icon(() -> new ItemStack(OccultaItems.DREAM_WEAVE_MOVE))
                .parents("AO_MACHINES")
                .pages(Page.text("tc.research_page.AO_DREAMS.1"),
                        kettlePage(OccultaItems.BREW_OF_SLEEPING),
                        Page.text("tc.research_page.AO_DREAMS.2"),
                        Page.crafting("AOSleepingApple"),
                        Page.text("tc.research_page.AO_DREAMS.3"),
                        kettlePage(OccultaItems.BREW_OF_FLOWING_SPIRIT),
                        Page.text("tc.research_page.AO_DREAMS.4"),
                        Page.crafting("AODreamWeaveMove"),
                        Page.crafting("AODreamWeaveDig"),
                        Page.crafting("AODreamWeaveEat"),
                        Page.crafting("AODreamWeaveNightmare"),
                        Page.crafting("AODreamWeaveIntensity"),
                        Page.text("tc.research_page.AO_DREAMS.5"))
                .register();
    }

    /** O Espírito Fluente, o que a Destilaria tira dele e os cinco que endurecem a poça. */
    private static void spirit() {
        ThaumcraftApi.research("AO_SPIRIT", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.SOUL, 5).add(Aspects.WATER, 4).add(Aspects.EXCHANGE, 3)
                        .add(Aspects.MAGIC, 2))
                .at(6, 9)
                .icon(() -> new ItemStack(OccultaItems.BREW_OF_HOLLOW_TEARS))
                .parents("AO_DREAMS")
                .pages(Page.text("tc.research_page.AO_SPIRIT.1"),
                        Page.text("tc.research_page.AO_SPIRIT.2"),
                        stillPage(OccultaItems.BREW_OF_FLOWING_SPIRIT, OccultaItems.OIL_OF_VITRIOL),
                        Page.text("tc.research_page.AO_SPIRIT.3"),
                        kettlePage(OccultaItems.BREW_OF_SOLID_DIRT),
                        Page.text("tc.research_page.AO_SPIRIT.4"))
                .register();
    }

    /** A faca do ofício, o que ela abre nos bichos e a pedra que sai do Rito de Necromancia. */
    private static void arthana() {
        ThaumcraftApi.research("AO_ARTHANA", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.WEAPON, 4).add(Aspects.DEATH, 4).add(Aspects.SOUL, 3)
                        .add(Aspects.MAGIC, 2))
                .at(0, 10)
                .icon(() -> new ItemStack(OccultaItems.ARTHANA))
                .parents("AO_CIRCLES", "AO_KETTLE")
                .pages(Page.text("tc.research_page.AO_ARTHANA.1"),
                        Page.crafting("AOArthana"),
                        Page.text("tc.research_page.AO_ARTHANA.2"),
                        Page.crafting("AOGraveyardDust"),
                        Page.text("tc.research_page.AO_ARTHANA.3"),
                        Page.text("tc.research_page.AO_ARTHANA.4"))
                .register();
    }

    /** O Portal do Espírito e o fantasma que volta por ele. */
    private static void ghost() {
        ThaumcraftApi.research("AO_GHOST", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.SOUL, 5).add(Aspects.TRAVEL, 4).add(Aspects.MAN, 3)
                        .add(Aspects.ELDRITCH, 3))
                .at(2, 12)
                .icon(() -> new ItemStack(net.minecraft.world.item.Items.SNOW_BLOCK))
                .parents("AO_SPIRIT", "AO_ARTHANA")
                .pages(Page.text("tc.research_page.AO_GHOST.1"),
                        Page.text("tc.research_page.AO_GHOST.2"),
                        Page.text("tc.research_page.AO_GHOST.3"),
                        Page.text("tc.research_page.AO_GHOST.4"))
                .register();
    }

    /**
     * Uma página de pote, montada da própria tabela: o que entra, o poder e o que sai saem de lá, e não da mão de
     * quem escreve — e saem <b>na hora de desenhar</b>, que é quando os itens já existem.
     */
    private static Page kettlePage(net.minecraft.world.item.Item sai) {
        java.util.List<java.util.function.Supplier<ItemStack>> entram = new java.util.ArrayList<>();
        for (int i = 0; i < net.thaumcraft.occulta.kettle.KettleBlockEntity.INGREDIENTS; i++) {
            final int qual = i;
            entram.add(() -> {
                var receita = net.thaumcraft.occulta.kettle.KettleRecipes.of(sai);
                return receita == null || qual >= receita.inputs().size() ? ItemStack.EMPTY
                        : new ItemStack(receita.inputs().get(qual));
            });
        }
        var receita = net.thaumcraft.occulta.kettle.KettleRecipes.of(sai);
        return new Page.Kettle(entram, () -> {
            var agora = net.thaumcraft.occulta.kettle.KettleRecipes.of(sai);
            return agora == null ? ItemStack.EMPTY : agora.output();
        }, receita == null ? 0.0f : receita.power());
    }

    /** Os espelhos, e o mundo que há dentro deles. */
    private static void mirrors() {
        ThaumcraftApi.research("AO_MIRRORS", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.SENSES, 5).add(Aspects.ELDRITCH, 4).add(Aspects.TRAVEL, 4)
                        .add(Aspects.MAGIC, 3))
                .at(-4, 6)
                .icon(() -> new ItemStack(OccultaItems.WITCH_MIRROR))
                .parents("AO_CIRCLES")
                .pages(Page.text("tc.research_page.AO_MIRRORS.1"),
                        Page.text("tc.research_page.AO_MIRRORS.2"),
                        Page.text("tc.research_page.AO_MIRRORS.3"),
                        Page.text("tc.research_page.AO_MIRRORS.4"))
                .register();
    }


    /** O que muda quando a pólvora entra: o cozimento deixa de se beber e passa a se atirar. */
    private static void splash() {
        ThaumcraftApi.research("AO_BREW_SPLASH", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.FIRE, 4).add(Aspects.MOTION, 3).add(Aspects.ENTROPY, 2))
                .at(0, 6)
                .icon(() -> brewStack(Items.NETHER_WART, Items.SPIDER_EYE, Items.GUNPOWDER))
                .parents("AO_POTIONS")
                .pages(Page.text("tc.research_page.AO_BREW_SPLASH.1"),
                        Page.text("tc.research_page.AO_BREW_SPLASH.2"),
                        brewPage(Items.NETHER_WART, Items.SPIDER_EYE, Items.GUNPOWDER),
                        brewPage(Items.NETHER_WART, Items.SPIDER_EYE, OccultaItems.WOOD_ASH, Items.GUNPOWDER),
                        brewPage(Items.NETHER_WART, Items.GLISTERING_MELON_SLICE, Items.GUNPOWDER),
                        brewPage(Items.NETHER_WART, Items.FERMENTED_SPIDER_EYE, Items.SUGAR, Items.GUNPOWDER),
                        Page.text("tc.research_page.AO_BREW_SPLASH.3"),
                        brewPage(Items.NETHER_WART, Items.SPIDER_EYE, OccultaItems.BAT_WOOL),
                        brewPage(Items.NETHER_WART, Items.GHAST_TEAR, OccultaItems.BELLADONNA_FLOWER,
                                OccultaItems.BAT_WOOL))
                .register();
    }


    /** As bonecas: a linha da Magia Simpática. */
    private static void poppets() {
        ThaumcraftApi.research("AO_POPPETS", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAN, 5).add(Aspects.MAGIC, 4).add(Aspects.ARMOR, 3)
                        .add(Aspects.CLOTH, 2))
                .at(-4, 4)
                .icon(() -> new ItemStack(OccultaItems.DEATH_POPPET))
                .parents("AO_DISTILLERY")
                .pages(Page.text("tc.research_page.AO_POPPETS.1"), Page.crafting("AOPoppet"),
                        Page.text("tc.research_page.AO_POPPETS.2"), Page.crafting("AOTaglock"),
                        Page.text("tc.research_page.AO_POPPETS.3"), Page.crafting("AOPoppetShelf"))
                .register();
    }

    /** Os círculos de giz e os ritos. */
    private static void circles() {
        ThaumcraftApi.research("AO_CIRCLES", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 5).add(Aspects.ORDER, 4).add(Aspects.CRAFT, 3)
                        .add(Aspects.ENERGY, 2))
                .at(-2, 6)
                .icon(() -> new ItemStack(OccultaItems.GOLDEN_CHALK))
                .parents("AO_DISTILLERY")
                .pages(Page.text("tc.research_page.AO_CIRCLES.1"), Page.crafting("AORitualChalk"),
                        Page.text("tc.research_page.AO_CIRCLES.2"),
                        Page.text("tc.research_page.AO_CIRCLES.3"))
                .register();
    }

    /** A Destilaria, que separa uma coisa em quatro. */
    private static void distillery() {
        ThaumcraftApi.research("AO_DISTILLERY", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.WATER, 4).add(Aspects.FIRE, 3).add(Aspects.EXCHANGE, 3)
                        .add(Aspects.CRAFT, 2))
                .at(-2, 4)
                .icon(() -> new ItemStack(OccultaItems.DISTILLERY))
                .parents("AO_ALTAR")
                .pages(Page.text("tc.research_page.AO_DISTILLERY.1"), Page.crafting("AODistillery"),
                        Page.text("tc.research_page.AO_DISTILLERY.2"),
                        stillPage(OccultaItems.BREATH_OF_THE_GODDESS, Items.LAPIS_LAZULI),
                        stillPage(Items.DIAMOND, OccultaItems.OIL_OF_VITRIOL),
                        stillPage(OccultaItems.DIAMOND_VAPOUR, Items.GHAST_TEAR),
                        stillPage(Items.ENDER_PEARL),
                        stillPage(OccultaItems.FOUL_FUME, OccultaItems.QUICKLIME))
                .register();
    }

    /**
     * Uma página de destilação, montada da própria tabela: o que sai e quantos potes se gastam saem de lá, e não
     * da mão de quem escreve.
     */
    private static Page stillPage(net.minecraft.world.item.Item... entra) {
        java.util.List<java.util.function.Supplier<ItemStack>> entram = new java.util.ArrayList<>();
        for (var item : entra) entram.add(() -> new ItemStack(item));
        return Page.distillery(java.util.List.copyOf(entram),
                () -> {
                    var receita = still(entra);
                    return receita == null ? 0 : receita.jars();
                },
                () -> {
                    var receita = still(entra);
                    return receita == null ? java.util.List.of() : receita.outputs();
                });
    }

    /** A receita daquilo, procurada na hora — cedo demais os itens ainda não existem. */
    private static net.thaumcraft.occulta.DistilleryRecipes.Recipe still(net.minecraft.world.item.Item... entra) {
        return net.thaumcraft.occulta.DistilleryRecipes.find(new ItemStack(entra[0]),
                entra.length > 1 ? new ItemStack(entra[1]) : ItemStack.EMPTY,
                new ItemStack(OccultaItems.CLAY_JAR, 64));
    }

    /** Os cozimentos que não mexem em quem passa: mexem no lugar. */
    private static void world() {
        ThaumcraftApi.research("AO_BREW_WORLD", Occulta.CATEGORY)
                .aspects(new AspectList().add(Aspects.EARTH, 4).add(Aspects.PLANT, 3).add(Aspects.CRAFT, 2))
                .at(0, 8)
                .icon(() -> brewStack(Items.NETHER_WART, Items.FLINT, Items.GUNPOWDER))
                .parents("AO_BREW_SPLASH")
                .pages(Page.text("tc.research_page.AO_BREW_WORLD.1"),
                        brewPage(Items.NETHER_WART, Items.STRING, Items.GUNPOWDER),
                        brewPage(Items.NETHER_WART, Items.BROWN_MUSHROOM, Items.GUNPOWDER),
                        brewPage(Items.NETHER_WART, Items.FLINT, Items.GUNPOWDER),
                        Page.text("tc.research_page.AO_BREW_WORLD.2"),
                        brewPage(Items.NETHER_WART, Items.WHEAT_SEEDS, Items.GUNPOWDER),
                        brewPage(Items.NETHER_WART, Items.LILY_PAD, Items.GUNPOWDER),
                        brewPage(Items.NETHER_WART, Items.DIAMOND, Items.POISONOUS_POTATO, Items.GUNPOWDER),
                        brewPage(Items.NETHER_WART, Items.DIAMOND, Items.BONE, Items.GUNPOWDER))
                .register();
    }

    /**
     * Uma página de receita de cozimento, montada do que se joga dentro.
     *
     * <p>O frasco que sai e o poder que o altar paga <b>não se escrevem à mão</b>: saem do próprio motor, do
     * mesmo jeito que sairiam no caldeirão. Assim o livro não pode ensinar uma receita que a panela recusa — e há
     * uma prova que confere isso, receita por receita.
     */
    private static Page brewPage(net.minecraft.world.item.Item... dentro) {
        java.util.List<net.minecraft.world.item.Item> lista = java.util.List.of(dentro);
        java.util.List<java.util.function.Supplier<ItemStack>> caem = new java.util.ArrayList<>();
        for (net.minecraft.world.item.Item item : lista) caem.add(() -> new ItemStack(item));
        return Page.brew(() -> new ItemStack(OccultaItems.WITCHES_CAULDRON), java.util.List.copyOf(caem),
                () -> brewStack(dentro), net.thaumcraft.occulta.brew.Brew.power(lista));
    }

    /** O frasco que aquela receita dá. */
    public static ItemStack brewStack(net.minecraft.world.item.Item... dentro) {
        return net.thaumcraft.occulta.brew.BrewItem.of(OccultaItems.BREW, java.util.List.of(dentro));
    }


    /** As receitas que o livro mostra. Correm depois de tudo montado, porque pedem os itens prontos. */
    public static void recipes() {
        ThaumcraftApi.bookRecipe("AOPoppet", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.POPPET), 3, 3, List.of(
                        List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.white())),
                        List.of(new ItemStack(OccultaItems.SPANISH_MOSS)),
                        List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.white())),
                        List.of(new ItemStack(net.thaumcraft.mortuorum.MortuorumItems.BONE_NEEDLE)),
                        List.of(new ItemStack(OccultaItems.SPANISH_MOSS)),
                        List.of(new ItemStack(Items.STRING)),
                        List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.white())), List.<ItemStack>of(),
                        List.of(new ItemStack(net.minecraft.world.item.Items.WOOL.white())))));
        ThaumcraftApi.bookRecipe("AOTaglock", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.TAGLOCK), 3, 3, List.of(
                        List.of(new ItemStack(Items.GLASS_BOTTLE)),
                        List.of(new ItemStack(net.thaumcraft.mortuorum.MortuorumItems.BONE_NEEDLE)),
                        List.<ItemStack>of(), List.<ItemStack>of(), List.<ItemStack>of(), List.<ItemStack>of(),
                        List.<ItemStack>of(), List.<ItemStack>of(), List.<ItemStack>of())));
        ThaumcraftApi.bookRecipe("AOPoppetShelf", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.POPPET_SHELF), 3, 3, List.of(
                        List.of(new ItemStack(net.minecraft.world.level.block.Blocks.OAK_PLANKS)), List.of(new ItemStack(net.minecraft.world.level.block.Blocks.OAK_PLANKS)),
                        List.of(new ItemStack(net.minecraft.world.level.block.Blocks.OAK_PLANKS)),
                        List.of(new ItemStack(Items.STRING)), List.<ItemStack>of(),
                        List.of(new ItemStack(Items.STRING)),
                        List.of(new ItemStack(net.minecraft.world.level.block.Blocks.OAK_PLANKS)), List.of(new ItemStack(net.minecraft.world.level.block.Blocks.OAK_PLANKS)),
                        List.of(new ItemStack(net.minecraft.world.level.block.Blocks.OAK_PLANKS)))));
        ThaumcraftApi.bookRecipe("AORitualChalk", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.RITUAL_CHALK), 3, 3, List.of(
                        List.of(new ItemStack(OccultaItems.WOOD_ASH)),
                        List.of(new ItemStack(OccultaItems.TEAR_OF_THE_GODDESS)),
                        List.of(new ItemStack(OccultaItems.WOOD_ASH)),
                        List.of(new ItemStack(OccultaItems.WOOD_ASH)),
                        List.of(new ItemStack(OccultaItems.GYPSUM)),
                        List.of(new ItemStack(OccultaItems.WOOD_ASH)),
                        List.of(new ItemStack(OccultaItems.WOOD_ASH)),
                        List.of(new ItemStack(OccultaItems.GYPSUM)),
                        List.of(new ItemStack(OccultaItems.WOOD_ASH)))));
        ThaumcraftApi.bookRecipe("AODistillery", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.DISTILLERY), 3, 3, List.of(
                        List.of(new ItemStack(OccultaItems.CLAY_JAR)), List.of(new ItemStack(Items.IRON_INGOT)),
                        List.of(new ItemStack(OccultaItems.CLAY_JAR)),
                        List.of(new ItemStack(Items.IRON_INGOT)), List.of(new ItemStack(Items.IRON_INGOT)),
                        List.of(new ItemStack(Items.IRON_INGOT)),
                        List.of(new ItemStack(Items.GOLD_INGOT)), List.of(new ItemStack(Items.DIAMOND)),
                        List.of(new ItemStack(Items.GOLD_INGOT)))));
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

        // ------------------------------------------------ a Pedra Sintonizada e as três coisas dela
        ThaumcraftApi.bookRecipe("AOAttunedStone", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.ATTUNED_STONE), 1, 3, List.of(
                        List.of(new ItemStack(OccultaItems.WHIFF_OF_MAGIC)),
                        List.of(new ItemStack(Items.DIAMOND)),
                        List.of(new ItemStack(Items.LAVA_BUCKET)))));

        ThaumcraftApi.bookRecipe("AOSpinningWheel", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.SPINNING_WHEEL), 3, 3, List.of(
                        List.of(new ItemStack(Items.ITEM_FRAME)), List.of(new ItemStack(Items.ITEM_FRAME)),
                        List.of(new ItemStack(Items.WOOL.pick(net.minecraft.world.item.DyeColor.WHITE))),
                        List.of(new ItemStack(Items.ITEM_FRAME)), List.of(new ItemStack(Items.ITEM_FRAME)),
                        List.of(new ItemStack(Items.STICK)),
                        List.of(new ItemStack(Items.OAK_PLANKS)),
                        List.of(new ItemStack(OccultaItems.ATTUNED_STONE)),
                        List.of(new ItemStack(Items.OAK_PLANKS)))));

        ThaumcraftApi.bookRecipe("AOBrazier", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.BRAZIER), 3, 3, List.of(
                        List.of(new ItemStack(Items.IRON_INGOT)),
                        List.of(new ItemStack(OccultaItems.NECROTIC_STONE)),
                        List.of(new ItemStack(Items.IRON_INGOT)),
                        List.of(new ItemStack(Items.STICK)), List.<ItemStack>of(), List.<ItemStack>of(),
                        List.of(new ItemStack(Items.STICK)), List.of(new ItemStack(Items.STICK)),
                        List.of(new ItemStack(Items.STICK)))));

        ThaumcraftApi.bookRecipe("AOBloodCrucible", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.BLOOD_CRUCIBLE), 3, 2, List.of(
                        List.of(new ItemStack(Items.STONE_BRICK_STAIRS)),
                        List.of(new ItemStack(Items.STONE_BRICK_STAIRS)), List.<ItemStack>of(),
                        List.of(new ItemStack(Items.STONE_BRICKS)),
                        List.of(new ItemStack(Items.STONE_BRICK_SLAB)),
                        List.of(new ItemStack(Items.STONE_BRICKS)))));

        // ------------------------------------------------------------------ o sono e as teias
        ThaumcraftApi.bookRecipe("AOSleepingApple", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.SLEEPING_APPLE), 3, 3, List.of(
                        List.<ItemStack>of(), List.of(new ItemStack(OccultaItems.MUTANDIS)), List.<ItemStack>of(),
                        List.of(new ItemStack(OccultaItems.REEK_OF_MISFORTUNE)),
                        List.of(new ItemStack(OccultaItems.WORMY_APPLE)),
                        List.of(new ItemStack(OccultaItems.REEK_OF_MISFORTUNE)),
                        List.of(new ItemStack(OccultaItems.MUTANDIS)),
                        List.of(new ItemStack(OccultaItems.BREW_OF_SLEEPING)),
                        List.of(new ItemStack(OccultaItems.MUTANDIS)))));

        weaveRecipe("AODreamWeaveMove", OccultaItems.DREAM_WEAVE_MOVE,
                splash(net.minecraft.world.item.alchemy.Potions.LONG_SWIFTNESS),
                splash(net.minecraft.world.item.alchemy.Potions.LONG_SLOWNESS), false);
        weaveRecipe("AODreamWeaveDig", OccultaItems.DREAM_WEAVE_DIG,
                splash(net.minecraft.world.item.alchemy.Potions.LONG_STRENGTH),
                splash(net.minecraft.world.item.alchemy.Potions.LONG_WEAKNESS), false);
        weaveRecipe("AODreamWeaveEat", OccultaItems.DREAM_WEAVE_EAT,
                splash(net.minecraft.world.item.alchemy.Potions.STRONG_HEALING),
                new ItemStack(OccultaItems.MELLIFLUOUS_HUNGER), false);
        weaveRecipe("AODreamWeaveNightmare", OccultaItems.DREAM_WEAVE_NIGHTMARE,
                splash(net.minecraft.world.item.alchemy.Potions.LONG_POISON),
                splash(net.minecraft.world.item.alchemy.Potions.LONG_NIGHT_VISION), true);
        weaveRecipe("AODreamWeaveIntensity", OccultaItems.DREAM_WEAVE_INTENSITY,
                new ItemStack(OccultaItems.BREW_OF_FLOWING_SPIRIT),
                new ItemStack(OccultaItems.BREW_OF_SLEEPING), false);

        ThaumcraftApi.bookRecipe("AOArthana", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.ARTHANA), 3, 3, List.of(
                        List.<ItemStack>of(), List.of(new ItemStack(Items.GOLD_INGOT)), List.<ItemStack>of(),
                        List.of(new ItemStack(Items.GOLD_NUGGET)), List.of(new ItemStack(Items.EMERALD)),
                        List.of(new ItemStack(Items.GOLD_NUGGET)),
                        List.<ItemStack>of(), List.of(new ItemStack(Items.STICK)), List.<ItemStack>of())));

        ThaumcraftApi.bookRecipe("AOGraveyardDust", ThaumcraftApi.crafting(
                () -> new ItemStack(OccultaItems.GRAVEYARD_DUST), 3, 1, List.of(
                        List.of(new ItemStack(OccultaItems.SPECTRAL_DUST)),
                        List.of(new ItemStack(Items.BONE_MEAL)),
                        List.of(new ItemStack(OccultaItems.MUTANDIS)))));

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
                        List.of(new ItemStack(Items.STONE_BRICKS)))));    }

    /** Uma poção de atirar daquele feitio, para a página do livro. */
    private static ItemStack splash(net.minecraft.core.Holder<net.minecraft.world.item.alchemy.Potion> qual) {
        return net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.SPLASH_POTION, qual);
    }

    /**
     * A página de uma Teia de Sonho: a forma é sempre a mesma, e o que muda são os dois cantos de cima.
     *
     * @param cordel se a fileira do meio é de Cordel Atormentado, que é o caso só da teia dos pesadelos
     */
    private static void weaveRecipe(String nome, net.minecraft.world.item.Item teia, ItemStack esquerda,
                                    ItemStack direita, boolean cordel) {
        ItemStack lado = new ItemStack(cordel ? OccultaItems.TORMENTED_TWINE : OccultaItems.FANCIFUL_THREAD);
        ThaumcraftApi.bookRecipe(nome, ThaumcraftApi.crafting(() -> new ItemStack(teia), 3, 3, List.of(
                List.of(esquerda), List.of(new ItemStack(OccultaItems.DIAMOND_VAPOUR)), List.of(direita),
                List.of(lado), List.of(new ItemStack(Items.ITEM_FRAME)), List.of(lado),
                List.of(new ItemStack(Items.FEATHER)),
                List.of(new ItemStack(OccultaItems.TORMENTED_TWINE)),
                List.of(new ItemStack(Items.FEATHER)))));
    }
}
