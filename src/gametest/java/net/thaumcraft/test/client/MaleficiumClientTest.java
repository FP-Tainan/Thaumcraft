package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.thaumcraft.client.gui.ResearchPageScreen;
import net.thaumcraft.client.gui.ThaumonomiconScreen;
import net.thaumcraft.research.Researches;

/** A aba do Maleficium no Thaumonomicon, e a página do metal das sombras. */
public class MaleficiumClientTest implements FabricClientGameTest {
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
                ThaumonomiconScreen.select("MALEFICIUM");
                minecraft.setScreenAndShow(new ThaumonomiconScreen());
            });
            context.waitTicks(20);
            context.takeScreenshot("livro_maleficium");
            context.runOnClient(minecraft -> {
                if (minecraft.gui.screen() instanceof ThaumonomiconScreen book) {
                    minecraft.setScreenAndShow(new ResearchPageScreen(book, Researches.get("SHADOWMETAL")));
                }
            });
            context.waitTicks(20);
            context.takeScreenshot("pagina_metal_das_sombras");
            // e a página das ferramentas, que o livro monta da receita de mesa
            context.runOnClient(minecraft ->
                    minecraft.setScreenAndShow(new ResearchPageScreen(new ThaumonomiconScreen(), Researches.get("SHADOWMETAL"), 2)));
            context.waitTicks(20);
            context.takeScreenshot("pagina_ferramentas");
        }
    }
}
