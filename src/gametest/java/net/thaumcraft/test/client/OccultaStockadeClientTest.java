package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A <b>Paliçada</b> na tela: as <b>estacas apontadas</b>, e o que acontece quando elas se juntam.
 *
 * <p>A primeira tela é a que importa: uma fileira de dez, uma por madeira e a de gelo no fim, para se ver que
 * cada uma tem a sua casca — e que a de gelo <b>fica de pé sozinha</b> ao lado da última de madeira, sem se
 * ligar a ela.
 *
 * <p>A segunda mostra as <b>pontas</b>: uma estaca solta aponta; duas empilhadas viram parede, porque a de
 * baixo deixa de apontar. É o {@code oneAbove} do original, e é o que separa uma cerca de um muro.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaStockadeClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            // a fileira das nove madeiras, e o gelo no fim
            String[] madeiras = {"oak", "spruce", "birch", "jungle", "rowan",
                "alder", "hawthorn", "acacia", "dark_oak"};
            for (int i = 0; i < madeiras.length; i++) {
                server.runCommand("execute at @p run setblock ~" + (-4 + i) + " ~ ~5 "
                        + "thaumcraft:stockade[wood=" + madeiras[i] + "]");
            }
            server.runCommand("execute at @p run setblock ~5 ~ ~5 thaumcraft:ice_stockade");
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 8");
            context.waitTicks(30);
            context.takeScreenshot("palicada_1_as_dez");

            // e as pontas: uma solta, duas empilhadas, e uma cruz
            server.runCommand("execute at @p run setblock ~-2 ~ ~2 thaumcraft:stockade[wood=rowan]");
            server.runCommand("execute at @p run setblock ~1 ~ ~2 thaumcraft:stockade[wood=rowan]");
            server.runCommand("execute at @p run setblock ~1 ~1 ~2 thaumcraft:stockade[wood=rowan]");
            for (int d = -1; d <= 1; d++) {
                server.runCommand("execute at @p run setblock ~" + (4 + d)
                        + " ~ ~2 thaumcraft:stockade[wood=rowan]");
                server.runCommand("execute at @p run setblock ~4 ~ ~" + (2 + d)
                        + " thaumcraft:stockade[wood=rowan]");
            }
            server.runCommand("execute at @p run tp @p ~1 ~ ~-1 0 5");
            context.waitTicks(20);
            context.takeScreenshot("palicada_2_as_pontas");

            server.runCommand("give @p thaumcraft:stockade");
            server.runCommand("give @p thaumcraft:stockade[block_state={wood:\"rowan\"}]");
            server.runCommand("give @p thaumcraft:ice_stockade");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("palicada_3_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
