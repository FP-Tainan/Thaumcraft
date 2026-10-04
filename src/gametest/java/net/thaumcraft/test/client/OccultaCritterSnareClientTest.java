package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Os <b>cinco Apanha-Bichos</b> na tela: vazio, com morcego, com lepisma, com gosma e com magma.
 *
 * <p>Cada um tem a sua própria folha, e são cinco folhas diferentes no original — não é uma planta pintada de
 * outra cor, é um desenho por bicho, com o bicho visível lá dentro. Esta tela existe para provar que os cinco
 * chegaram e que cada feitio puxa o seu.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaCritterSnareClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            String[] bichos = {"empty", "bat", "silverfish", "slime", "magmacube"};
            for (int i = 0; i < bichos.length; i++) {
                int x = -4 + i * 2;
                server.runCommand("execute at @p run setblock ~" + x + " ~ ~4 "
                        + "thaumcraft:critter_snare[caught=" + bichos[i] + "]");
            }
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 12");
            context.waitTicks(30);
            context.takeScreenshot("apanha_bicho_1_os_cinco");

            // e um de perto, onde o bicho lá dentro se vê
            server.runCommand("execute at @p run tp @p ~-2 ~ ~2.4 0 5");
            context.waitTicks(20);
            context.takeScreenshot("apanha_bicho_2_o_morcego");

            server.runCommand("give @p thaumcraft:critter_snare");
            server.runCommand(
                    "give @p thaumcraft:critter_snare[block_state={caught:\"bat\"}]");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("apanha_bicho_3_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
