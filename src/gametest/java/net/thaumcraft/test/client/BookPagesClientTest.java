package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.thaumcraft.client.gui.ResearchPageScreen;
import net.thaumcraft.client.gui.ThaumonomiconScreen;
import net.thaumcraft.research.Page;
import net.thaumcraft.research.Research;
import net.thaumcraft.research.Researches;

import java.util.function.Predicate;

/**
 * As páginas abertas do Thaumonomicon, uma de cada tipo: texto com figura, bancada comum e arcana, crisol, fornalha,
 * infusão, encantamento, montagens pequenas e grandes, e os aspectos.
 */
public class BookPagesClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            singleplayer.getServer().runCommand("gamemode creative");
            singleplayer.getServer().runCommand("thaumcraft tudo @p");
            context.waitTicks(20);
            // a faixa de pesquisa completa, descendo no canto
            context.runOnClient(minecraft -> net.thaumcraft.client.ResearchPopup.queue(Researches.get("INFUSION")));
            context.waitTicks(10);
            context.takeScreenshot("pesquisa_completa");

            shot(context, "ORE", p -> p instanceof Page.Text, "texto_figura");
            shot(context, "RESEARCH", p -> p instanceof Page.Text t && t.key().endsWith(".3"), "texto_figura2");
            shot(context, "CRUCIBLE", kind(Page.Kind.COMPOUND), "montagem_crisol");
            shot(context, "CRUCIBLE", kind(Page.Kind.CRUCIBLE), "crisol");
            shot(context, "CRUCIBLE", p -> p instanceof Page.Smelting, "fornalha");
            shot(context, "INFERNALFURNACE", kind(Page.Kind.COMPOUND), "montagem_fornalha");
            shot(context, "INFUSION", kind(Page.Kind.COMPOUND), "montagem_altar");
            shot(context, "ELEMENTALAXE", kind(Page.Kind.INFUSION), "infusao");
            shot(context, "INFUSIONENCHANTMENT", kind(Page.Kind.ENCHANTMENT), "encantamento");
            shot(context, "JARLABEL", kind(Page.Kind.CRAFTING), "bancada");
            shot(context, "ORE", kind(Page.Kind.CRAFTING), "bancada_aglomerados");
            shot(context, "NODETAPPER1", p -> true, "texto");
            shot(context, "WARDEDARCANA", kind(Page.Kind.ARCANE), "arcana");
            shot(context, "THAUMATORIUM", kind(Page.Kind.COMPOUND), "montagem_taumatorio");
            shot(context, "RUNICAUGMENTATION", kind(Page.Kind.RUNIC), "runico");
            shot(context, "ASPECTS", p -> p instanceof Page.Aspects, "aspectos");

            // o cursor sobre um ingrediente: o tooltip com o "clique para pesquisar"
            shot(context, "WARDEDARCANA", kind(Page.Kind.ARCANE), "arcana");
            double[] at = context.computeOnClient(minecraft -> {
                double scale = minecraft.getWindow().getGuiScale();
                int sw = (minecraft.getWindow().getGuiScaledWidth() - 256) / 2;
                int sh = (minecraft.getWindow().getGuiScaledHeight() - 181) / 2;
                // a casa do meio da grade arcana da página da direita
                return new double[]{(sw - 4 + 152 + 16 + 32 + 8) * scale, (sh - 8 + 66 + 32 + 8) * scale};
            });
            context.getInput().setCursorPos(at[0], at[1]);
            context.waitTicks(3);
            context.takeScreenshot("pagina_tooltip");

            // o mapa com tudo aprendido
            context.getInput().setCursorPos(0, 0);
            context.runOnClient(minecraft -> {
                ThaumonomiconScreen.select("ALCHEMY");
                minecraft.setScreenAndShow(new ThaumonomiconScreen());
            });
            context.waitTicks(10);
            context.takeScreenshot("mapa_alquimia");
        }
    }

    private static Predicate<Page> kind(Page.Kind kind) {
        return p -> p instanceof Page.Recipe r && r.kind() == kind;
    }

    /** Abre a pesquisa na folha da primeira página que casa e tira o retrato. */
    private static void shot(ClientGameTestContext context, String key, Predicate<Page> which, String name) {
        context.runOnClient(minecraft -> {
            Research research = Researches.get(key);
            if (research == null) throw new AssertionError("sem pesquisa " + key);
            ResearchPageScreen probe = new ResearchPageScreen(null, research);
            int index = -1;
            for (int i = 0; i < probe.pages().size(); i++) {
                if (which.test(probe.pages().get(i))) {
                    index = i;
                    break;
                }
            }
            if (index < 0) throw new AssertionError(key + " não tem a página pedida para " + name);
            minecraft.setScreenAndShow(new ResearchPageScreen(new ThaumonomiconScreen(), research, index));
        });
        context.waitTicks(5);
        context.takeScreenshot("pagina_" + name);
    }
}
