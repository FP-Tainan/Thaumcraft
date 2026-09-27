package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A aba do Ars Occulta no Thaumonomicon: a árvore das seis pesquisas do ofício, a do Magia Naturalis ao lado
 * dela — que se mudou para cá — e as páginas de duas delas.
 */
public class OccultaBookClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("thaumcraft pesquisa tudo");
            context.waitTicks(20);

            context.runOnClient(minecraft -> {
                net.thaumcraft.client.gui.ThaumonomiconScreen.select(net.thaumcraft.occulta.Occulta.CATEGORY);
                minecraft.setScreenAndShow(new net.thaumcraft.client.gui.ThaumonomiconScreen());
            });
            context.waitTicks(20);
            context.takeScreenshot("ao_livro_aba");

            // e as páginas de duas delas, que é onde as receitas aparecem
            for (String pesquisa : new String[]{"AO_WITCHCRAFT", "AO_ALTAR", "AO_POTIONS", "AO_BREW_HARM",
                    "AO_BREW_LIFE"}) {
                context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                        new net.thaumcraft.client.gui.ResearchPageScreen(null,
                                net.thaumcraft.research.Researches.get(pesquisa))));
                context.waitTicks(20);
                context.takeScreenshot("ao_livro_" + pesquisa.toLowerCase(java.util.Locale.ROOT));
            }
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }
}
