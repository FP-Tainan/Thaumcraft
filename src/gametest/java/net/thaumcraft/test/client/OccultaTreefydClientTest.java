package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * O <b>Treefyd</b>, que é a flor carnívora com pernas.
 *
 * <p>É o que há para ver: o talo, a cabeça com a coroa de pétalas por trás e a língua à frente, as duas
 * chapas de folha cruzadas no meio e as quatro pernas. Três fotos: de frente, de lado e de cima.
 */
public class OccultaTreefydClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("clear @p");
            server.runCommand("gamerule doMobSpawning false");

            // o bicho num lugar sabido, e a câmara em três lugares à volta dele
            double[] onde = server.computeOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                return new double[]{Math.floor(jogador.getX()) + 0.5, jogador.getY(),
                        Math.floor(jogador.getZ()) + 0.5};
            });
            server.runCommand(String.format(java.util.Locale.ROOT,
                    "summon thaumcraft:treefyd %.1f %.1f %.1f {NoAI:1b,Silent:1b}",
                    onde[0], onde[1], onde[2] + 3.0));
            olha(server, onde[0], onde[1], onde[2], 0.0f, 0.0f);
            context.waitTicks(30);
            context.takeScreenshot("o_treefyd_de_frente");

            olha(server, onde[0] + 3.0, onde[1], onde[2] + 3.0, 90.0f, 0.0f);
            context.waitTicks(20);
            context.takeScreenshot("o_treefyd_de_lado");

            olha(server, onde[0], onde[1] + 3.0, onde[2], 0.0f, 40.0f);
            context.waitTicks(20);
            context.takeScreenshot("o_treefyd_de_cima");

            // e a semente na mochila
            server.runCommand("give @p thaumcraft:treefyd_seeds 2");
            context.waitTicks(10);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("a_semente_de_treefyd");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }

    /** Põe a câmara naquele ponto, olhando para aquele rumo. */
    private static void olha(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
                             double x, double y, double z, float guinada, float passo) {
        server.runCommand(String.format(java.util.Locale.ROOT, "tp @p %.1f %.1f %.1f %.1f %.1f",
                x, y, z, guinada, passo));
    }
}
