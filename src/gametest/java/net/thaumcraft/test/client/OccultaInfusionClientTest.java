package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * As duas coisas da <b>Infusão</b> que se pegam na mão: a <b>Mão de Bruxa</b> e o <b>Espírito do Outro
 * Lugar</b>.
 *
 * <p>A primeira tela é o inventário com as duas. A segunda é a Mão <b>na mão</b>, porque ela é um item que
 * se segura e tem de se ver segurada.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaInfusionClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            server.runCommand("give @p thaumcraft:witch_hand");
            server.runCommand("give @p thaumcraft:spirit_of_otherwhere");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("infusao_1_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(10);

            context.takeScreenshot("infusao_2_a_mao_na_mao");

            /*
             * E a <b>barra de poder</b>: um tubo de vidro à direita da tela, no meio dela, cheio da
             * textura da infusão que se tem. Aqui a do Outro Lugar, que é o portal.
             */
            context.runOnClient(minecraft -> {
                var servidor = minecraft.getSingleplayerServer();
                if (servidor == null) return;
                servidor.execute(() -> {
                    for (var quem : servidor.getPlayerList().getPlayers()) {
                        net.thaumcraft.occulta.infusion.Infusions.infunde(quem,
                                net.thaumcraft.occulta.infusion.Infusions.daquele(3), 200);
                        net.thaumcraft.occulta.infusion.Infusions.põeEnergia(quem, 130);
                    }
                });
            });
            context.waitTicks(40);
            context.takeScreenshot("infusao_3_a_barra_de_poder");
        }
    }
}
