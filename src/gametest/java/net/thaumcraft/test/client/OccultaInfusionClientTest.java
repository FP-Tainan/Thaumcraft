package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * As duas coisas da <b>Infusão</b> que se pegam na mão: a <b>Mão de Bruxa</b> e o <b>Espírito do Outro
 * Lugar</b>.
 *
 * <p>A primeira tela é o inventário com a Mão e os três cozimentos que os três ritos de infusão pedem. A segunda é a Mão <b>na mão</b>, porque ela é um item que
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
            server.runCommand("give @p thaumcraft:ghost_of_the_light");
            server.runCommand("give @p thaumcraft:infernal_animus");
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

            /*
             * E a <b>segunda barra</b>: a dos poderes de bicho, dez pixels mais para dentro, que não é um
             * tubo que se enche mas uma <b>pilha de riscos</b>, um por carga.
             */
            context.runOnClient(minecraft -> {
                var servidor = minecraft.getSingleplayerServer();
                if (servidor == null) return;
                servidor.execute(() -> {
                    for (var quem : servidor.getPlayerList().getPlayers()) {
                        net.thaumcraft.occulta.infusion.Infusions.infunde(quem,
                                net.thaumcraft.occulta.infusion.Infusions.daquele(4), 200);
                        net.thaumcraft.occulta.infusion.Infusions.põeEnergia(quem, 90);
                        net.thaumcraft.occulta.infusion.beast.CreaturePowers.toma(quem,
                                net.thaumcraft.occulta.infusion.beast.CreaturePowers.daquele(6));
                    }
                });
            });
            context.waitTicks(40);
            context.takeScreenshot("infusao_4_as_duas_barras");
        }
    }
}
