package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.thaumcraft.occulta.CircleTalismanItem;

/**
 * O <b>Talismã de Círculo</b>, nas dez caras dele.
 *
 * <p>É o que há para conferir: que a figura do talismã muda com o <b>maior anel riscado</b> e que as dez
 * do original estão todas lá — a branca, as três do anel de dentro, as três do meio e as três de fora.
 */
public class OccultaCircleTalismanClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("clear @p");

            // o branco e as nove figuras, uma por casa da barra
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                var mochila = jogador.getInventory();
                mochila.setItem(0, CircleTalismanItem.escrito(0));
                int casa = 1;
                for (int giz = 1; giz <= 3; giz++) {
                    mochila.setItem(casa++, CircleTalismanItem.escrito(
                            CircleTalismanItem.empacota(giz, 0, 0)));
                }
                for (int giz = 1; giz <= 3; giz++) {
                    mochila.setItem(casa++, CircleTalismanItem.escrito(
                            CircleTalismanItem.empacota(1, giz, 0)));
                }
                for (int giz = 1; giz <= 3; giz++) {
                    mochila.setItem(casa++, CircleTalismanItem.escrito(
                            CircleTalismanItem.empacota(1, 1, giz)));
                }
                jogador.containerMenu.broadcastChanges();
            });
            context.waitTicks(20);
            context.takeScreenshot("os_dez_talismas");
        }
    }
}
