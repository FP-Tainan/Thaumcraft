package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * O Altar da Bruxa na tela: a pedra sozinha, o altar inteiro de duas por três e os enfeites em cima dele.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaAltarClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            context.waitTicks(20);

            // uma pedra sozinha
            perto(server, "setblock ~ ~ ~4 thaumcraft:witch_altar");
            perto(server, "tp @p ~ ~ ~ 0 20");
            context.waitTicks(20);
            context.takeScreenshot("ao_altar_pedra");

            // e o altar inteiro, duas por três, que muda a cara das pedras
            perto(server, "fill ~-1 ~ ~4 ~1 ~ ~5 thaumcraft:witch_altar");
            context.waitTicks(20);
            context.takeScreenshot("ao_altar_inteiro");

            // com a caveira e a tocha em cima, que é o que soma poder
            perto(server, "setblock ~-1 ~1 ~4 minecraft:skeleton_skull");
            perto(server, "setblock ~1 ~1 ~4 minecraft:torch");
            context.waitTicks(20);
            context.takeScreenshot("ao_altar_com_enfeites");

            // e de perto, para ver a pedra
            perto(server, "tp @p ~ ~ ~2 0 35");
            context.waitTicks(20);
            context.takeScreenshot("ao_altar_de_perto");
        }
    }

    /** O comando corre no lugar de quem joga, e não na origem do mundo. */
    private static void perto(TestServerContext server, String command) {
        server.runCommand("execute at @p run " + command);
    }
}
