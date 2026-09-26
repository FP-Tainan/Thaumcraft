package net.thaumcraft.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.thaumcraft.occulta.Occulta;
import net.thaumcraft.research.Research;
import net.thaumcraft.research.ResearchCategories;
import net.thaumcraft.research.Researches;

import java.util.List;

/**
 * A aba do Ars Occulta no Thaumonomicon: as seis pesquisas da lore de quem joga, e o que cada uma mostra.
 */
public class OccultaTableGameTest {
    /** As chaves das seis, na ordem em que se aprendem. */
    private static final List<String> AS_SEIS = List.of(
            "AO_OLD_WAYS", "AO_PLANTS", "AO_WITCHCRAFT", "AO_BREWS", "AO_RESONANTIA", "AO_ALTAR");

    @GameTest
    public void theBranchHasItsOwnTab(GameTestHelper helper) {
        if (ResearchCategories.get(Occulta.CATEGORY) == null) helper.fail("faltou a aba do ramo no livro");
        for (String chave : AS_SEIS) {
            Research pesquisa = Researches.get(chave);
            if (pesquisa == null) {
                helper.fail("faltou a pesquisa " + chave);
                continue;
            }
            if (!pesquisa.category().equals(Occulta.CATEGORY)) {
                helper.fail(chave + " devia estar na aba do ramo; está em " + pesquisa.category());
            }
            if (pesquisa.pages().isEmpty()) helper.fail(chave + " devia ter o que ler");
        }
        // a aba é partilhada com o Magia Naturalis, que se mudou para cá: contam-se as seis do ofício pela chave
        long minhas = Researches.of(Occulta.CATEGORY).stream()
                .filter(pesquisa -> AS_SEIS.contains(pesquisa.key())).count();
        if (minhas != AS_SEIS.size()) helper.fail("a aba tem as seis pesquisas da lore; tem " + minhas);
        if (Researches.of(Occulta.CATEGORY).stream().noneMatch(pesquisa -> pesquisa.key().startsWith("MN_"))) {
            helper.fail("e as do Magia Naturalis, que agora moram nesta aba");
        }
        helper.succeed();
    }

    /** Na aba partilhada, nenhuma pesquisa cai em cima de outra. */
    @GameTest
    public void nothingSitsOnTopOfAnythingElse(GameTestHelper helper) {
        var lugares = new java.util.HashMap<String, String>();
        for (Research pesquisa : Researches.of(Occulta.CATEGORY)) {
            String onde = pesquisa.column() + "," + pesquisa.row();
            String outra = lugares.put(onde, pesquisa.key());
            if (outra != null) helper.fail(pesquisa.key() + " cai em cima de " + outra + " em " + onde);
        }
        helper.succeed();
    }

    /** A primeira abre-se sozinha; as outras penduram-se umas nas outras. */
    @GameTest
    public void theTreeHangsTogether(GameTestHelper helper) {
        Research entrada = Researches.get("AO_OLD_WAYS");
        if (entrada == null || !entrada.marks().contains(Research.Mark.AUTO)) {
            helper.fail("o degrau de entrada abre-se sozinho");
        }

        confere(helper, "AO_PLANTS", "AO_OLD_WAYS");
        confere(helper, "AO_WITCHCRAFT", "AO_OLD_WAYS");
        confere(helper, "AO_BREWS", "AO_WITCHCRAFT");
        confere(helper, "AO_BREWS", "AO_PLANTS");
        confere(helper, "AO_RESONANTIA", "AO_BREWS");
        confere(helper, "AO_ALTAR", "AO_RESONANTIA");
        helper.succeed();
    }

    /** E as receitas que o livro mostra estão todas registradas. */
    @GameTest
    public void theBookShowsItsRecipes(GameTestHelper helper) {
        for (String nome : List.of("AOWitchesOven", "AOSoftClayJar", "AOFumeFunnel", "AOAnointingPaste",
                "AORowanPlanks", "AOAltar")) {
            if (!net.thaumcraft.research.BookRecipes.ALL.containsKey(nome)) {
                helper.fail("a receita " + nome + " devia estar no livro");
            }
        }
        helper.succeed();
    }

    private static void confere(GameTestHelper helper, String filha, String mãe) {
        Research pesquisa = Researches.get(filha);
        if (pesquisa == null) {
            helper.fail("faltou a pesquisa " + filha);
            return;
        }
        if (!pesquisa.parents().contains(mãe)) {
            helper.fail(filha + " devia pendurar-se em " + mãe);
        }
    }
}
