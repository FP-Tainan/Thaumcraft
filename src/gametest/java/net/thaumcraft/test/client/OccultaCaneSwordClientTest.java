package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.hunter.CaneSwordItem;

/**
 * A <b>Bengala-Espada</b> nas duas caras e as duas <b>Varas de Rabdomante</b>.
 *
 * <p>O que há para ver é a <b>dica</b> da bengala: ela diz quanto há no cantil, e esse número muda com o
 * que quem a traz matou. É o único item do ramo com um número vivo na mão.
 */
public class OccultaCaneSwordClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("clear @p");

            // a bengala guardada, a bengala sacada e as duas varas
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                var mochila = jogador.getInventory();

                ItemStack guardada = new ItemStack(OccultaItems.CANE_SWORD);
                ItemStack sacada = new ItemStack(OccultaItems.CANE_SWORD);
                CaneSwordItem.saca(sacada, true);
                mochila.setItem(0, guardada);
                mochila.setItem(1, sacada);
                mochila.setItem(2, new ItemStack(OccultaItems.DIVINER_WATER));
                mochila.setItem(3, new ItemStack(OccultaItems.DIVINER_LAVA));

                // e o cantil com alguma coisa dentro, para a dica ter o que dizer
                net.thaumcraft.occulta.vampire.Vampire.grau(jogador, 1);
                net.thaumcraft.occulta.vampire.BloodReserve.enche(jogador, 137);
                jogador.containerMenu.broadcastChanges();
            });
            context.waitTicks(20);
            context.takeScreenshot("a_bengala_e_as_varas");

            // a dica da bengala, com o número do cantil: o cursor sobre a primeira casa da barra
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            double[] onde = context.computeOnClient(minecraft -> {
                double escala = minecraft.getWindow().getGuiScale();
                int esquerda = (minecraft.getWindow().getGuiScaledWidth() - 176) / 2;
                int cima = (minecraft.getWindow().getGuiScaledHeight() - 166) / 2;
                return new double[]{(esquerda + 16) * escala, (cima + 150) * escala};
            });
            context.getInput().setCursorPos(onde[0], onde[1]);
            context.waitTicks(5);
            context.takeScreenshot("a_dica_do_cantil");
            context.getInput().setCursorPos(0, 0);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }
}
