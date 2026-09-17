package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.thaumcraft.crafting.ArcaneRecipe;
import net.thaumcraft.crafting.ArcaneRecipes;
import net.thaumcraft.crafting.CrucibleRecipe;
import net.thaumcraft.crafting.CrucibleRecipes;
import net.thaumcraft.research.PlayerKnowledge;
import net.thaumcraft.research.Research;
import net.thaumcraft.research.ResearchManager;
import net.thaumcraft.research.Researches;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * O jogo tem de ter caminho: tudo o que já existe precisa ser alcançável a partir do zero.
 *
 * <p>Não adianta uma receita existir se a pesquisa que a destranca estiver atrás de uma pesquisa que
 * ninguém consegue abrir. Este teste segue a árvore desde o que vem de berço e confere que cada receita
 * pronta tem caminho até ela.
 */
public class ProgressionGameTest {
    /** Tudo o que se destranca partindo do que vem de berço. */
    private static Set<String> reachable() {
        PlayerKnowledge knowledge = new PlayerKnowledge();
        ResearchManager.grantStarters(knowledge);
        Set<String> found = new HashSet<>(knowledge.research());
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Research research : Researches.ALL.values()) {
                if (found.contains(research.key())) continue;
                boolean open = true;
                for (String parent : research.parents()) {
                    if (!found.contains(parent)) open = false;
                }
                for (String parent : research.parentsHidden()) {
                    if (!found.contains(parent)) open = false;
                }
                if (!open) continue;
                found.add(research.key());
                changed = true;
            }
        }
        return found;
    }

    /** Do zero, o caminho chega em boa parte da árvore. */
    @GameTest
    public void theTreeHasAPath(GameTestHelper helper) {
        Set<String> found = reachable();
        if (found.size() < 100) {
            helper.fail("só " + found.size() + " pesquisas têm caminho desde o começo, de "
                    + Researches.ALL.size());
        }
        helper.succeed();
    }

    /** Toda receita que já funciona tem a pesquisa dela ao alcance de quem começa do zero. */
    @GameTest
    public void everyWorkingRecipeCanBeReached(GameTestHelper helper) {
        Set<String> found = reachable();
        List<String> stuck = new ArrayList<>();
        for (CrucibleRecipe recipe : CrucibleRecipes.ALL) {
            if (Researches.get(recipe.research()) == null) continue;
            if (!found.contains(recipe.research())) stuck.add("crisol/" + recipe.research());
        }
        for (ArcaneRecipe recipe : ArcaneRecipes.ALL) {
            if (Researches.get(recipe.research()) == null) continue;
            if (!found.contains(recipe.research())) stuck.add("bancada/" + recipe.research());
        }
        if (!stuck.isEmpty()) {
            helper.fail("receita pronta atrás de pesquisa sem caminho: " + String.join(", ", stuck));
        }
        helper.succeed();
    }
}
