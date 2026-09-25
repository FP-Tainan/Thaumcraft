package net.thaumcraft.api;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.crafting.ArcaneRecipe;
import net.thaumcraft.crafting.ArcaneRecipes;
import net.thaumcraft.crafting.CrucibleRecipe;
import net.thaumcraft.crafting.CrucibleRecipes;
import net.thaumcraft.crafting.InfusionRecipe;
import net.thaumcraft.crafting.InfusionRecipes;
import net.thaumcraft.research.BookRecipes;
import net.thaumcraft.research.Research;
import net.thaumcraft.research.ResearchCategories;
import net.thaumcraft.research.Researches;
import net.thaumcraft.research.WarpItems;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A porta de entrada de quem escreve um mod em cima do Thaumcraft — o {@code ThaumcraftApi} da 4.2.3.5.
 *
 * <p>Aqui se abre uma aba no Thaumonomicon, se põe uma pesquisa na árvore, se registra receita de bancada arcana, de
 * crisol e de infusão, se diz de que as coisas do mod são feitas e quanta distorção elas trazem. É o mesmo conjunto
 * de portas que o original dava aos addons — Forbidden Magic, Tainted Magic, Magia Naturalis e companhia entravam
 * todos por aqui.
 *
 * <p>Chame isto uma vez, no carregar do seu mod, depois de o Thaumcraft ter carregado (é o que a dependência no
 * {@code fabric.mod.json} garante). As tabelas do Thaumcraft já estão montadas nessa hora, então o que você registrar
 * entra no fim, sem mexer na ordem do original.
 *
 * <pre>{@code
 * ThaumcraftApi.category("MALEFICIUM", MeuMod.id("textures/misc/aba.png"), MeuMod.id("textures/gui/fundo.png"));
 *
 * ThaumcraftApi.research("WARPWOOD", "MALEFICIUM")
 *         .aspects(new AspectList().add(Aspects.MAGIC, 3).add(Aspects.TREE, 3))
 *         .at(0, 0).complexity(2)
 *         .icon(() -> new ItemStack(MeuMod.TABUA))
 *         .pages(Page.text("meumod.research_page.WARPWOOD.1"), Page.crucible("Warpwood"))
 *         .register();
 *
 * ThaumcraftApi.bookRecipe("Warpwood", ThaumcraftApi.crucible("WARPWOOD",
 *         new ItemStack(MeuMod.TABUA), Items.OAK_PLANKS, new AspectList().add(Aspects.MAGIC, 5)));
 * }</pre>
 */
public final class ThaumcraftApi {
    private ThaumcraftApi() {
    }

    /** O que ainda não pode ser montado ao carregar o mod: roda quando o mundo abre. */
    private static final List<Runnable> SETUP = new java.util.ArrayList<>();
    private static boolean setupDone;

    /**
     * Guarda para depois o que carrega itens — as receitas, principalmente.
     *
     * <p>Ao carregar um mod o jogo ainda não terminou de montar os itens ({@code Components not bound yet}), então
     * uma {@code ItemStack} criada nessa hora estoura. O Thaumcraft monta as tabelas dele do mesmo jeito: tarde, com o
     * mundo abrindo. O que for posto aqui roda uma vez só, antes de o Thaumcraft deduzir os aspectos das receitas —
     * de modo que as coisas do seu mod entram na conta.
     */
    public static void onSetup(Runnable work) {
        if (setupDone) {
            work.run();
            return;
        }
        SETUP.add(work);
    }

    /** Chamado pelo próprio Thaumcraft quando o mundo abre; não é para um mod de fora chamar. */
    public static void runSetup() {
        if (setupDone) return;
        setupDone = true;
        for (Runnable work : SETUP) work.run();
        SETUP.clear();
    }

    // ------------------------------------------------------------------ o livro

    /**
     * Abre uma aba nova no Thaumonomicon. O nome dela sai do idioma, pela chave
     * {@code tc.research_category.<chave>}; o desenho tem dezesseis por dezesseis e o pergaminho de fundo, duzentos e
     * cinquenta e seis por duzentos e cinquenta e seis.
     */
    public static void category(String key, Identifier icon, Identifier background) {
        ResearchCategories.init();
        ResearchCategories.register(key, icon, background);
    }

    /** Começa a montar uma pesquisa para o livro; veja {@link Research#of}. */
    public static Research.Builder research(String key, String category) {
        Researches.init();
        return Research.of(key, category);
    }

    /**
     * Dá nome a uma receita, que é como as páginas do livro a citam ({@code Page.crucible("Warpwood")}). Vale para
     * qualquer receita das que este API devolve, e também para as montagens e as receitas de mesa que o próprio livro
     * monta ({@link BookRecipes.Crafting}, {@link BookRecipes.Compound}).
     */
    public static <T> T bookRecipe(String name, T recipe) {
        BookRecipes.ALL.put(name, recipe);
        return recipe;
    }

    // ------------------------------------------------------------------ as receitas mágicas

    /** Uma receita de crisol, com o catalisador dito item por item. */
    public static CrucibleRecipe crucible(String research, ItemStack result, Item catalyst, AspectList cost) {
        CrucibleRecipe recipe = new CrucibleRecipe(research, result, catalyst, cost);
        CrucibleRecipes.ALL.add(recipe);
        return recipe;
    }

    /** Uma receita de crisol cujo catalisador é tudo o que tiver aquela etiqueta. */
    public static CrucibleRecipe crucible(String research, ItemStack result, TagKey<Item> catalyst, AspectList cost) {
        CrucibleRecipe recipe = new CrucibleRecipe(research, result, catalyst, cost);
        CrucibleRecipes.ALL.add(recipe);
        return recipe;
    }

    /**
     * Uma receita de bancada arcana com forma: a lista tem uma entrada por casa, na ordem de leitura, e as casas
     * vazias entram como {@code null}.
     */
    public static ArcaneRecipe arcane(String research, ItemStack result, AspectList cost, List<Ingredient> pattern) {
        ArcaneRecipe recipe = new ArcaneRecipe(research, result, pattern, cost);
        ArcaneRecipes.ALL.add(recipe);
        return recipe;
    }

    /** Uma receita de bancada arcana sem forma. */
    public static ArcaneRecipe arcaneShapeless(String research, ItemStack result, AspectList cost, List<Ingredient> parts) {
        ArcaneRecipe recipe = new ArcaneRecipe(research, result, parts, cost, false);
        ArcaneRecipes.ALL.add(recipe);
        return recipe;
    }

    /** Uma receita de infusão: o que vai no meio, o que fica nos pedestais, a essência e a instabilidade. */
    public static InfusionRecipe infusion(String research, ItemStack result, int instability, AspectList essentia,
                                          Ingredient central, List<Ingredient> components) {
        InfusionRecipe recipe = new InfusionRecipe(research, result, instability, essentia, central, components, null);
        InfusionRecipes.ALL.add(recipe);
        return recipe;
    }

    /**
     * Uma infusão que muda a própria coisa do meio em vez de fazer outra — é assim que o original encanta e que os
     * reforços rúnicos funcionam.
     */
    public static InfusionRecipe infusionOnCentral(String research, int instability, AspectList essentia,
                                                   Ingredient central, List<Ingredient> components,
                                                   java.util.function.UnaryOperator<ItemStack> change) {
        InfusionRecipe recipe = new InfusionRecipe(research, ItemStack.EMPTY, instability, essentia, central, components, change);
        InfusionRecipes.ALL.add(recipe);
        return recipe;
    }

    /**
     * Uma pesquisa-sombra: o {@code ResearchItemProxy} do Magia Naturalis 0.5.0. Ela põe na aba do ramo uma cópia
     * oca de uma pesquisa do Thaumcraft — mesmo desenho, mesmas páginas —, só de leitura e escondida, para servir
     * de pé das pesquisas do ramo que nascem dela. Sabida a de verdade, a sombra abre junto.
     *
     * @param key      a chave da sombra, no espaço de nome do ramo
     * @param category a aba do ramo
     * @param original a chave da pesquisa do Thaumcraft de que ela é sombra
     */
    public static net.thaumcraft.research.Research proxy(String key, String category, String original,
                                                        int column, int row) {
        net.thaumcraft.research.Research base = net.thaumcraft.research.Researches.get(original);
        if (base == null) {
            net.thaumcraft.Thaumcraft.LOGGER.warn("sombra de pesquisa sem original: {}", original);
            return null;
        }
        var builder = net.thaumcraft.research.Research.of(key, category).at(column, row).complexity(1).stub().hidden();
        if (base.iconStack() != null) builder.icon(base.iconStack());
        else if (base.iconTexture() != null) builder.icon(base.iconTexture());
        builder.pages(base.pages().toArray(new net.thaumcraft.research.Page[0]));
        if (base.is(net.thaumcraft.research.Research.Mark.SECONDARY)) builder.secondary();
        net.thaumcraft.research.Research sombra = builder.register();
        net.thaumcraft.research.Researches.addSibling(original, key);
        return sombra;
    }

    // ------------------------------------------------------------------ aspectos e distorção

    /**
     * De que as coisas do seu mod são feitas. O que não for anotado aqui o Thaumcraft deduz das receitas, como faz
     * com tudo — anote só as peças de base.
     */
    public static void aspects(Consumer<ObjectAspects.Registrar> hook) {
        ObjectAspects.onRegister(hook);
    }

    /** A distorção que gruda em quem fabrica aquela coisa. */
    public static void warp(Item item, int amount) {
        WarpItems.register(item, amount);
    }

    // ------------------------------------------------------------------ atalhos

    /** Uma receita de mesa comum para o livro mostrar, com a forma e as casas ({@code null} onde não vai nada). */
    public static BookRecipes.Crafting crafting(Supplier<ItemStack> result, int width, int height, List<List<ItemStack>> grid) {
        return new BookRecipes.Crafting(result, width, height, grid);
    }

    /** Uma montagem de estrutura para o livro mostrar, camada a camada. */
    public static BookRecipes.Compound compound(AspectList vis, int sizeX, int sizeY, int sizeZ, List<Supplier<ItemStack>> blocks) {
        return new BookRecipes.Compound(vis, sizeX, sizeY, sizeZ, blocks);
    }
}
