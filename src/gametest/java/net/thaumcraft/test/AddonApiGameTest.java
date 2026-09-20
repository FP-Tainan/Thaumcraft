package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.crafting.CrucibleRecipe;
import net.thaumcraft.crafting.CrucibleRecipes;
import net.thaumcraft.research.BookPages;
import net.thaumcraft.research.Page;
import net.thaumcraft.research.Research;
import net.thaumcraft.research.ResearchCategories;
import net.thaumcraft.research.Researches;
import net.thaumcraft.research.WarpItems;
import net.thaumcraft.test.addon.TestAddon;

/**
 * A porta dos mods de fora ({@code ThaumcraftApi}) tem de continuar aberta: é por ela que os addons do original
 * entravam, e é por ela que os nossos vão entrar.
 *
 * <p>Quem bate na porta é o {@link TestAddon}, um mod de mentira que carrega junto com os testes.
 */
public class AddonApiGameTest {
    @GameTest
    public void anAddonCanOpenATabInTheBook(GameTestHelper helper) {
        var category = ResearchCategories.get(TestAddon.CATEGORY);
        if (category == null) helper.fail("a aba do mod de fora não entrou no livro");
        if (!category.icon().getNamespace().equals("thaumcraft")) helper.fail("a aba perdeu o desenho: " + category.icon());
        helper.succeed();
    }

    @GameTest
    public void anAddonCanPutAResearchInTheTree(GameTestHelper helper) {
        Research research = Researches.get(TestAddon.RESEARCH);
        if (research == null) helper.fail("a pesquisa do mod de fora não entrou na árvore");
        if (!research.category().equals(TestAddon.CATEGORY)) helper.fail("a pesquisa foi parar na aba errada: " + research.category());
        if (!research.is(Research.Mark.AUTO) || !research.is(Research.Mark.ROUND)) helper.fail("as marcas se perderam: " + research.marks());
        if (research.warp() != 1) helper.fail("a distorção da pesquisa se perdeu: " + research.warp());
        if (research.iconStack() == null || research.iconStack().get().isEmpty()) helper.fail("a pesquisa ficou sem figura");
        if (research.pages().size() != 2) helper.fail("as páginas se perderam: " + research.pages());
        helper.succeed();
    }

    @GameTest
    public void theBookFindsTheAddonRecipe(GameTestHelper helper) {
        Research research = Researches.get(TestAddon.RESEARCH);
        if (research == null) helper.fail("a pesquisa do mod de fora não entrou na árvore");
        if (!(research.pages().get(1) instanceof Page.Recipe page)) helper.fail("a segunda página devia ser de receita");
        else {
            var found = BookPages.resolve(page);
            if (found.size() != 1 || !(found.getFirst() instanceof CrucibleRecipe)) {
                helper.fail("a página não achou a receita de crisol do mod de fora: " + found);
            }
        }
        helper.succeed();
    }

    @GameTest
    public void theCrucibleAcceptsTheAddonRecipe(GameTestHelper helper) {
        AspectList inside = new AspectList().add(Aspects.MAGIC, 5).add(Aspects.VOID, 5);
        CrucibleRecipe recipe = CrucibleRecipes.find(inside, new ItemStack(Items.BARRIER));
        if (recipe == null) helper.fail("o crisol não conhece a receita do mod de fora");
        else if (!recipe.result().is(Items.STRUCTURE_VOID)) helper.fail("a receita do mod de fora saiu errada: " + recipe.result());
        helper.succeed();
    }

    @GameTest
    public void theAddonCanSayWhatItsThingsAreMadeOf(GameTestHelper helper) {
        AspectList aspects = ObjectAspects.of(new ItemStack(Items.BARRIER));
        if (aspects.getAmount(Aspects.ARMOR) != 3) {
            helper.fail("a anotação de aspecto do mod de fora não entrou na tabela: " + aspects);
        }
        if (WarpItems.of(new ItemStack(Items.BARRIER)) != 2) helper.fail("a distorção do mod de fora não entrou");
        helper.succeed();
    }
}
