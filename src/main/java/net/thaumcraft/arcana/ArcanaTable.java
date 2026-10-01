package net.thaumcraft.arcana;

import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.ThaumcraftApi;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.research.Page;

/**
 * A aba do Ars Arcana no Thaumonomicon.
 *
 * <p>O Ars Magica 2 não é addon de Thaumcraft e não tem pesquisa nenhuma: lá o que se aprende está num
 * compêndio próprio e na árvore de perícias. A árvore desta aba é a que a <b>lore de quem joga</b> marca — a
 * Gramática da Magia — e ela segue a <b>ordem do que se faz</b>, que é a única ordem que ensina alguma coisa:
 *
 * <ol>
 *   <li>descobrir que um feitiço é uma <b>frase</b>;
 *   <li>construir o <b>Óculus</b>, para ver o que se pode aprender;
 *   <li>construir a <b>Mesa de Inscrição</b>, para escrever;
 *   <li>e então as três coisas que o ramo tem de próprio: a <b>Mana</b>, a <b>Afinidade</b> e as Formas.
 * </ol>
 *
 * <p>Sem esta aba o ramo existia e ninguém dava por ele: as peças estavam na aba do criativo e mais nada.
 */
public final class ArcanaTable {
    private ArcanaTable() {
    }

    public static void research() {
        // o degrau de entrada: o ramo abre-se a quem topar com a ideia de que magia tem gramática
        ThaumcraftApi.research("AA_GRAMMAR", Arcana.CATEGORY)
                .at(0, -3)
                .icon(() -> new ItemStack(ArcanaItems.SPELL))
                .round()
                .auto()
                .special()
                .pages(Page.text("tc.research_page.AA_GRAMMAR.1"),
                        Page.text("tc.research_page.AA_GRAMMAR.2"),
                        Page.text("tc.research_page.AA_GRAMMAR.3"))
                .register();

        // o Óculus: onde se olha para o que se pode aprender
        ThaumcraftApi.research("AA_OCCULUS", Arcana.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 3).add(Aspects.MIND, 3).add(Aspects.SENSES, 2))
                .at(0, -1)
                .icon(() -> new ItemStack(ArcanaItems.OCCULUS))
                .parents("AA_GRAMMAR")
                .pages(Page.text("tc.research_page.AA_OCCULUS.1"),
                        Page.crafting("AAOcculus"),
                        Page.text("tc.research_page.AA_OCCULUS.2"))
                .register();

        // a Mesa de Inscrição: onde as peças viram frase
        ThaumcraftApi.research("AA_INSCRIPTION", Arcana.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 4).add(Aspects.CRAFT, 3).add(Aspects.MIND, 2))
                .at(0, 1)
                .icon(() -> new ItemStack(ArcanaItems.INSCRIPTION_TABLE))
                .parents("AA_OCCULUS")
                .pages(Page.text("tc.research_page.AA_INSCRIPTION.1"),
                        Page.crafting("AAInscriptionTable"),
                        Page.text("tc.research_page.AA_INSCRIPTION.2"),
                        Page.text("tc.research_page.AA_INSCRIPTION.3"))
                .register();

        // a Mana: o cano, e não a água
        ThaumcraftApi.research("AA_MANA", Arcana.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 4).add(Aspects.ENERGY, 3).add(Aspects.LIFE, 2))
                .at(-2, 0)
                .icon(() -> new ItemStack(ArcanaItems.itemOf(Shapes.SELF)))
                .parents("AA_OCCULUS")
                .round()
                .pages(Page.text("tc.research_page.AA_MANA.1"),
                        Page.text("tc.research_page.AA_MANA.2"))
                .register();

        // a Afinidade: o que lançar faz de quem lança
        ThaumcraftApi.research("AA_AFFINITY", Arcana.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 5).add(Aspects.EXCHANGE, 3).add(Aspects.MIND, 3))
                .at(-2, 2)
                .icon(() -> new ItemStack(ArcanaItems.itemOf(Essences.FIRE_DAMAGE)))
                .parents("AA_MANA", "AA_INSCRIPTION")
                .round()
                .pages(Page.text("tc.research_page.AA_AFFINITY.1"),
                        Page.text("tc.research_page.AA_AFFINITY.2"),
                        Page.text("tc.research_page.AA_AFFINITY.3"))
                .register();

        // as Formas: as quinze maneiras de um feitiço entrar no mundo
        ThaumcraftApi.research("AA_SHAPES", Arcana.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 4).add(Aspects.MOTION, 3).add(Aspects.VOID, 2))
                .at(2, 0)
                .icon(() -> new ItemStack(ArcanaItems.itemOf(Shapes.PROJECTILE)))
                .parents("AA_INSCRIPTION")
                .pages(Page.text("tc.research_page.AA_SHAPES.1"),
                        Page.text("tc.research_page.AA_SHAPES.2"),
                        Page.text("tc.research_page.AA_SHAPES.3"))
                .register();

        // os efeitos: o que um feitiço deixa em quem o leva
        ThaumcraftApi.research("AA_EFFECTS", Arcana.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 4).add(Aspects.LIFE, 3).add(Aspects.MIND, 2))
                .at(2, -2)
                .icon(() -> new ItemStack(ArcanaItems.itemOf(Essences.HASTE)))
                .parents("AA_INSCRIPTION")
                .pages(Page.text("tc.research_page.AA_EFFECTS.1"),
                        Page.text("tc.research_page.AA_EFFECTS.2"))
                .register();

        // o céu e os temporais
        ThaumcraftApi.research("AA_WEATHER", Arcana.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 5).add(Aspects.WEATHER, 4).add(Aspects.AIR, 3))
                .at(-2, 4)
                .icon(() -> new ItemStack(ArcanaItems.itemOf(Essences.STORM)))
                .parents("AA_AFFINITY")
                .pages(Page.text("tc.research_page.AA_WEATHER.1"),
                        Page.text("tc.research_page.AA_WEATHER.2"))
                .register();

        // e os segredos, que são a única coisa deste ramo que o livro TEM de contar que existe
        ThaumcraftApi.research("AA_SECRETS", Arcana.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 6).add(Aspects.MIND, 5).add(Aspects.VOID, 3))
                .at(0, 3)
                .icon(() -> new ItemStack(ArcanaItems.itemOf(Essences.BLIZZARD)))
                .parents("AA_AFFINITY", "AA_SHAPES")
                .round()
                .special()
                .pages(Page.text("tc.research_page.AA_SECRETS.1"),
                        Page.text("tc.research_page.AA_SECRETS.2"))
                .register();

        // as três peças que pedem uma escolha
        ThaumcraftApi.research("AA_CHOICE", Arcana.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 4).add(Aspects.EXCHANGE, 4).add(Aspects.CRAFT, 2))
                .at(-4, 2)
                .icon(() -> new ItemStack(ArcanaItems.itemOf(Modifiers.COLOUR)))
                .parents("AA_INSCRIPTION")
                .pages(Page.text("tc.research_page.AA_CHOICE.1"),
                        Page.text("tc.research_page.AA_CHOICE.2"))
                .register();

        // e o Vínculo, que é o fim da estrada
        ThaumcraftApi.research("AA_BINDING", Arcana.CATEGORY)
                .aspects(new AspectList().add(Aspects.MAGIC, 5).add(Aspects.TOOL, 4).add(Aspects.EXCHANGE, 3))
                .at(2, 2)
                .icon(() -> new ItemStack(ArcanaItems.BOUND.get(BoundToolItem.Kind.PICKAXE)))
                .parents("AA_SHAPES")
                .pages(Page.text("tc.research_page.AA_BINDING.1"),
                        Page.text("tc.research_page.AA_BINDING.2"))
                .register();
    }

    /**
     * As receitas que o livro mostra.
     *
     * <p>São as mesmas dos arquivos de dados, escritas outra vez aqui porque o livro desenha a grade e não
     * lê receitas do jogo. Esperam a montagem acabar, porque pedem itens já registrados.
     */
    public static void recipes() {
        var vidro = new ItemStack(net.minecraft.world.item.Items.GLASS);
        var pedra = new ItemStack(net.minecraft.world.item.Items.STONE);
        var livro = new ItemStack(net.minecraft.world.item.Items.BOOK);
        var nada = ItemStack.EMPTY;

        ThaumcraftApi.bookRecipe("AAOcculus", ThaumcraftApi.crafting(
                () -> new ItemStack(ArcanaItems.OCCULUS), 3, 3, java.util.List.of(
                        java.util.List.of(vidro), java.util.List.of(pedra), java.util.List.of(vidro),
                        java.util.List.of(pedra), java.util.List.of(livro), java.util.List.of(pedra),
                        java.util.List.of(pedra), java.util.List.of(pedra), java.util.List.of(pedra))));

        var papel = new ItemStack(net.minecraft.world.item.Items.PAPER);
        var tinta = new ItemStack(net.minecraft.world.item.Items.INK_SAC);
        var tabua = new ItemStack(net.minecraft.world.item.Items.OAK_PLANKS);

        ThaumcraftApi.bookRecipe("AAInscriptionTable", ThaumcraftApi.crafting(
                () -> new ItemStack(ArcanaItems.INSCRIPTION_TABLE), 3, 3, java.util.List.of(
                        java.util.List.of(papel), java.util.List.of(tinta), java.util.List.of(papel),
                        java.util.List.of(tabua), java.util.List.of(livro), java.util.List.of(tabua),
                        java.util.List.of(tabua), java.util.List.of(nada), java.util.List.of(tabua))));
    }
}
