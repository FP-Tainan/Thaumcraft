package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.ghost.SpectralStoneItem;

/**
 * As quatro caras da <b>Pedra Espectral</b>, e a dica que diz o que ela tem dentro.
 *
 * <p>É o que há para ver: que a pedra em branco, a do Espectro, a da Banshee e a do Poltergeist são
 * quatro desenhos diferentes — e que a dica de uma cheia diz <b>qual bicho e quantos</b>.
 */
public class OccultaSpectralStoneClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("clear @p");

            server.runOnServer(s -> {
                var mochila = s.getPlayerList().getPlayers().getFirst().getInventory();
                mochila.setItem(0, new ItemStack(OccultaItems.SPECTRAL_STONE));
                mochila.setItem(1, SpectralStoneItem.cheia(SpectralStoneItem.ESPECTRO, 1));
                mochila.setItem(2, SpectralStoneItem.cheia(SpectralStoneItem.BANSHEE, 2));
                mochila.setItem(3, SpectralStoneItem.cheia(SpectralStoneItem.POLTERGEIST, 3));
                mochila.setItem(5, new ItemStack(OccultaItems.CONGEALED_SPIRIT));
                s.getPlayerList().getPlayers().getFirst().containerMenu.broadcastChanges();
            });
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("as_quatro_caras_da_pedra");

            // e a dica da que tem três poltergeists: o cursor sobre a quarta casa da barra
            double[] onde = context.computeOnClient(minecraft -> {
                double escala = minecraft.getWindow().getGuiScale();
                int esquerda = (minecraft.getWindow().getGuiScaledWidth() - 176) / 2;
                int cima = (minecraft.getWindow().getGuiScaledHeight() - 166) / 2;
                return new double[]{(esquerda + 16 + 3 * 18) * escala, (cima + 150) * escala};
            });
            context.getInput().setCursorPos(onde[0], onde[1]);
            context.waitTicks(5);
            context.takeScreenshot("a_dica_da_pedra");
            context.getInput().setCursorPos(0, 0);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }
}
