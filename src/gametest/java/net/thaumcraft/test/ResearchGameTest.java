package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.thaumcraft.research.PlayerKnowledge;
import net.thaumcraft.research.Research;
import net.thaumcraft.research.ResearchCategories;
import net.thaumcraft.research.ResearchManager;
import net.thaumcraft.research.Researches;

import java.util.HashMap;
import java.util.Map;

/**
 * A árvore do Thaumonomicon tem de continuar sendo a do Thaumcraft 4.2.3.5.
 *
 * <p>Ela é gerada a partir do {@code ConfigResearch} do mod original, então estes testes são a cerca: se
 * alguém mexer na tabela na mão e ela sair do lugar, a compilação quebra antes de virar jogo.
 */
public class ResearchGameTest {
    /** Os números que o original tem: duzentas e uma pesquisas, seis abas, e a conta de cada aba. */
    @GameTest
    public void theTreeCameFromTheOriginal(GameTestHelper helper) {
        if (Researches.ALL.size() != 201) {
            helper.fail("a árvore tem " + Researches.ALL.size() + " pesquisas; o original tem 201");
        }
        Map<String, Integer> expected = new HashMap<>();
        expected.put("BASICS", 19);
        expected.put("THAUMATURGY", 42);
        expected.put("ALCHEMY", 35);
        expected.put("ARTIFICE", 50);
        expected.put("GOLEMANCY", 39);
        expected.put("ELDRITCH", 16);
        for (Map.Entry<String, Integer> entry : expected.entrySet()) {
            int found = Researches.of(entry.getKey()).size();
            if (found != entry.getValue()) {
                helper.fail(entry.getKey() + " tem " + found + " pesquisas, devia ter " + entry.getValue());
            }
        }
        if (ResearchCategories.ALL.size() != 6) helper.fail("o original tem seis abas");
        helper.succeed();
    }

    /** Toda ligação aponta para pesquisa que existe: mapa sem linha solta. */
    @GameTest
    public void everyLinkLeadsSomewhere(GameTestHelper helper) {
        for (Research research : Researches.ALL.values()) {
            for (String parent : research.parents()) {
                if (Researches.get(parent) == null) helper.fail(research.key() + " nasce de " + parent + ", que não existe");
            }
            for (String parent : research.parentsHidden()) {
                if (Researches.get(parent) == null) helper.fail(research.key() + " nasce de " + parent + ", que não existe");
            }
            for (String sibling : research.siblings()) {
                if (Researches.get(sibling) == null) helper.fail(research.key() + " é irmã de " + sibling + ", que não existe");
            }
            if (ResearchCategories.get(research.category()) == null) {
                helper.fail(research.key() + " mora na aba " + research.category() + ", que não existe");
            }
        }
        helper.succeed();
    }

    /**
     * Toda página de receita do livro acha a sua receita nas tabelas do mod — tantas quantos nomes a página cita. Ficam
     * de fora só as pesquisas dos metais que vinham de outros mods, que nem aparecem sem
     * o lingote.
     */
    @GameTest
    public void everyBookRecipeResolves(GameTestHelper helper) {
        java.util.List<String> missing = new java.util.ArrayList<>();
        int pages = 0;
        for (Research research : Researches.ALL.values()) {
            if (research.requires() != null) continue;
            for (net.thaumcraft.research.Page page : research.pages()) {
                if (!(page instanceof net.thaumcraft.research.Page.Recipe recipe)) continue;
                pages++;
                int wanted = 0;
                for (String name : recipe.names()) {
                    if (name.endsWith("*")) wanted += net.thaumcraft.api.aspects.Aspect.ASPECTS.size();
                    else wanted++;
                }
                int found = net.thaumcraft.research.BookPages.resolve(recipe).size();
                if (found != wanted) missing.add(research.key() + " " + recipe.kind() + " " + recipe.names() + " (" + found + "/" + wanted + ")");
            }
        }
        if (!missing.isEmpty()) helper.fail(missing.size() + " de " + pages + " páginas sem receita: " + String.join("; ", missing));
        helper.succeed();
    }

    /** Duas pesquisas conferidas na mão contra o ConfigResearch da 4.2.3.5. */
    @GameTest
    public void spotChecksAgainstTheOriginal(GameTestHelper helper) {
        Research aspects = Researches.get("ASPECTS");
        if (aspects == null) helper.fail("faltou ASPECTS");
        if (aspects.column() != 0 || aspects.row() != 0) helper.fail("ASPECTS fica na casa zero, zero");
        if (!aspects.is(Research.Mark.ROUND) || !aspects.is(Research.Mark.AUTO)) {
            helper.fail("ASPECTS é redonda e vem sabida de berço");
        }
        if (aspects.pages().size() != 3) helper.fail("ASPECTS tem três páginas no original");

        Research pech = Researches.get("PECH");
        if (pech.column() != -4 || pech.row() != -4) helper.fail("PECH fica em menos quatro, menos quatro");
        helper.succeed();
    }

    /** Quem entra no mundo já sabe o que o original dá de berço — e só isso. */
    @GameTest
    public void startersComeOpen(GameTestHelper helper) {
        PlayerKnowledge knowledge = new PlayerKnowledge();
        ResearchManager.grantStarters(knowledge);
        int starters = 0;
        for (Research research : Researches.ALL.values()) {
            boolean auto = research.is(Research.Mark.AUTO);
            if (auto) starters++;
            if (auto != knowledge.hasResearch(research.key())) {
                helper.fail(research.key() + (auto ? " devia vir aberta" : " não devia vir aberta"));
            }
        }
        if (starters == 0) helper.fail("nenhuma pesquisa vem aberta; o original abre várias");
        helper.succeed();
    }

    /** Com os pais feitos e os pontos na mão, a pesquisa se destranca e cobra o que pede. */
    @GameTest
    public void researchCostsWhatItAsks(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        PlayerKnowledge knowledge = net.thaumcraft.research.Knowledges.of(player);
        ResearchManager.grantStarters(knowledge);

        // uma pesquisa que cobra alguma coisa e cujos pais já vêm abertos
        Research paid = null;
        for (Research research : Researches.ALL.values()) {
            if (research.tags().isEmpty()) continue;
            if (!ResearchManager.canUnlock(knowledge, research)) continue;
            if (!ResearchManager.isVisible(knowledge, research)) continue;
            paid = research;
            break;
        }
        if (paid == null) helper.fail("nenhuma pesquisa paga está ao alcance de quem começa");

        net.thaumcraft.research.Knowledges.save(player, knowledge);
        if (ResearchManager.unlock(player, paid.key())) {
            helper.fail(paid.key() + " se destrancou sem os pontos");
        }

        // agora com os pontos na mão
        for (var aspect : paid.tags().getAspects()) {
            knowledge.discover(aspect);
            knowledge.award(aspect, paid.tags().getAmount(aspect) * 2);
        }
        net.thaumcraft.research.Knowledges.save(player, knowledge);
        var first = paid.tags().getAspects().get(0);
        int before = net.thaumcraft.research.Knowledges.of(player).points(first);
        if (!ResearchManager.unlock(player, paid.key())) {
            helper.fail(paid.key() + " devia ter se destrancado");
        }
        PlayerKnowledge depois = net.thaumcraft.research.Knowledges.of(player);
        if (!depois.hasResearch(paid.key())) helper.fail("devia ficar sabida");
        if (depois.points(first) >= before) helper.fail("os pontos deviam ter sido cobrados");
        // e não se destranca duas vezes
        if (ResearchManager.unlock(player, paid.key())) helper.fail("não devia destrancar de novo");
        helper.succeed();
    }

    /** Sem os pais feitos, a pesquisa não abre. */
    @GameTest
    public void childrenWaitForTheirParents(GameTestHelper helper) {
        PlayerKnowledge knowledge = new PlayerKnowledge();
        Research withParents = null;
        for (Research research : Researches.ALL.values()) {
            if (!research.parents().isEmpty()) {
                withParents = research;
                break;
            }
        }
        if (withParents == null) helper.fail("nenhuma pesquisa tem pai; a árvore está solta");
        if (ResearchManager.canUnlock(knowledge, withParents)) {
            helper.fail(withParents.key() + " abriu sem os pais feitos");
        }
        for (String parent : withParents.parents()) knowledge.completeResearch(parent);
        for (String parent : withParents.parentsHidden()) knowledge.completeResearch(parent);
        if (!ResearchManager.canUnlock(knowledge, withParents)) {
            helper.fail(withParents.key() + " não abriu nem com os pais feitos");
        }
        helper.succeed();
    }

    /**
     * Nenhum ponto do mapa sem desenho: ou a pesquisa tem a figura dela nos recursos do mod, ou tem um item que a
     * represente. Foi o que escapou uma vez, com as figuras ainda no lugar da 1.7.10 ({@code textures/blocks/}).
     */
    @GameTest
    public void everyResearchHasAnIcon(GameTestHelper helper) {
        java.util.List<String> sem = new java.util.ArrayList<>();
        for (var research : net.thaumcraft.research.Researches.ALL.values()) {
            if (research.is(net.thaumcraft.research.Research.Mark.VIRTUAL)) continue;
            var icone = research.icon();
            if (icone != null) {
                String caminho = "assets/" + icone.getNamespace() + "/" + icone.getPath();
                if (ResearchGameTest.class.getClassLoader().getResource(caminho) == null) sem.add(research.key() + " -> " + caminho);
            } else if (research.iconStack() == null || research.iconStack().get().isEmpty()) {
                sem.add(research.key() + " (sem figura e sem item)");
            } else {
                // o item existe, mas tem desenho? no jogo de hoje todo item precisa do seu arquivo em items/
                var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(research.iconStack().get().getItem());
                String modelo = "assets/" + id.getNamespace() + "/items/" + id.getPath() + ".json";
                if (id.getNamespace().equals("thaumcraft")) {
                    if (ResearchGameTest.class.getClassLoader().getResource(modelo) == null) {
                        sem.add(research.key() + " -> " + modelo);
                    } else if (!desenhaAlgo(modelo)) {
                        sem.add(research.key() + " -> " + modelo + " (não desenha nada)");
                    }
                }
            }
        }
        if (!sem.isEmpty()) helper.fail(sem.size() + " pesquisas sem desenho: " + String.join(", ", sem));
        helper.succeed();
    }

    /**
     * O arquivo do item desenha alguma coisa? Um modelo comum só desenha se ele, ou algum pai dele, tiver peças
     * ({@code elements}) ou a folha achatada de sempre ({@code layer0}) — foi assim que a mesa de pesquisa saiu em
     * branco, apontando para um modelo que só o bloco desenhava. O que é especial ({@code minecraft:special}) tem
     * quem o desenhe em Java, e aqui se aceita.
     */
    private static boolean desenhaAlgo(String modelo) {
        var json = leJson(modelo);
        if (json == null) return true;
        var raiz = json.getAsJsonObject("model");
        if (raiz == null) return true;
        String tipo = raiz.has("type") ? raiz.get("type").getAsString() : "";
        if (!tipo.equals("minecraft:model")) return true;
        String alvo = raiz.get("model").getAsString();
        for (int volta = 0; volta < 8 && alvo != null; volta++) {
            var id = net.minecraft.resources.Identifier.parse(alvo);
            var peca = leJson("assets/" + id.getNamespace() + "/models/" + id.getPath() + ".json");
            if (peca == null) return true;
            if (peca.has("elements") && !peca.getAsJsonArray("elements").isEmpty()) return true;
            if (peca.has("textures") && peca.getAsJsonObject("textures").has("layer0")) return true;
            alvo = peca.has("parent") ? peca.get("parent").getAsString() : null;
        }
        return false;
    }

    private static com.google.gson.JsonObject leJson(String caminho) {
        try (var stream = ResearchGameTest.class.getClassLoader().getResourceAsStream(caminho)) {
            if (stream == null) return null;
            return com.google.gson.JsonParser.parseReader(
                    new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception erro) {
            return null;
        }
    }
}
