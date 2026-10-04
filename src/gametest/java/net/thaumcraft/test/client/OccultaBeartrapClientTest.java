package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * As duas armadilhas na tela: <b>armada</b>, <b>disparada</b> e <b>escondida</b>.
 *
 * <p>Esta tela existe por causa da terceira. Uma armadilha armada por outra pessoa fica a <b>três décimos de
 * opaca</b>, e num chão de pedra isso é quase nada — é a coisa mais fácil deste mod de se portar errado, e a
 * mais difícil de se descobrir sem olhar.
 *
 * <p>As duas primeiras mostram o que o modelo faz: <b>armada</b>, os arcos deitados e os dentes para cima;
 * <b>disparada</b>, os dois arcos levantados 1,2 radiano com os dentes se encontrando no meio.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaBeartrapClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            // quatro: disparada, armada, armada de outra pessoa, e a de prata
            String[] feitio = {
                "thaumcraft:beartrap[facing=south]",
                "thaumcraft:beartrap[facing=south]",
                "thaumcraft:beartrap[facing=south]",
                "thaumcraft:wolftrap[facing=south]",
            };
            String[] alma = {
                "{Sprung:1b}",
                "{Sprung:0b}",
                "{Sprung:0b,Owner:[I;1,2,3,4]}",
                "{Sprung:0b}",
            };
            for (int i = 0; i < feitio.length; i++) {
                int x = -3 + i * 2;
                server.runCommand("execute at @p run setblock ~" + x + " ~ ~4 " + feitio[i]);
                server.runCommand("execute at @p run data merge block ~" + x + " ~ ~4 " + alma[i]);
            }
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 40");
            context.waitTicks(30);
            context.takeScreenshot("armadilha_1_as_quatro");

            // e a disparada de perto, onde os dentes se vêem
            server.runCommand("execute at @p run tp @p ~-3 ~ ~2 0 35");
            context.waitTicks(20);
            context.takeScreenshot("armadilha_2_os_dentes");

            server.runCommand("give @p thaumcraft:beartrap");
            server.runCommand("give @p thaumcraft:wolftrap");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("armadilha_3_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
