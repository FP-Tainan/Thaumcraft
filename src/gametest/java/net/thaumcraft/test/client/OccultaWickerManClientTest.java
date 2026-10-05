package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * O <b>Homem de Vime</b> de pé, e os dois feixes de que ele é feito.
 *
 * <p>A primeira tela é a figura inteira — oito blocos de alto, duas colunas de largura, com os braços
 * abertos no meio. É a única coisa deste mod que se <b>constrói</b> em vez de se pôr, e por isso merece ser
 * vista do chão para cima.
 *
 * <p>A segunda são os dois feixes lado a lado: o <b>simples</b> e o <b>ensanguentado</b>. Só o segundo serve
 * para a figura, e a diferença entre eles tem de se ver a olho.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaWickerManClientTest implements FabricClientGameTest {
    /** A figura, coluna por coluna: o desvio ao longo e a altura. */
    private static final int[][] FIGURA = {
        {0, 6}, {1, 6}, {0, 5}, {1, 5},
        {-1, 4}, {0, 4}, {1, 4}, {2, 4},
        {-1, 3}, {0, 3}, {1, 3}, {2, 3},
        {-1, 2}, {0, 2}, {1, 2}, {2, 2},
        {0, 1}, {0, 0}, {1, 0},
    };

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            for (int[] onde : FIGURA) {
                server.runCommand("execute at @p run setblock ~" + onde[0] + " ~" + onde[1] + " ~8 "
                        + "thaumcraft:wicker_bundle[axis=y,bloodied=true]");
            }
            server.runCommand("execute at @p run tp @p ~ ~1 ~-4 0 -12");
            context.waitTicks(30);
            context.takeScreenshot("vime_1_o_homem");

            server.runCommand("execute at @p run setblock ~-3 ~ ~4 "
                    + "thaumcraft:wicker_bundle[axis=y,bloodied=false]");
            server.runCommand("execute at @p run setblock ~-1 ~ ~4 "
                    + "thaumcraft:wicker_bundle[axis=y,bloodied=true]");
            server.runCommand("execute at @p run setblock ~1 ~ ~4 "
                    + "thaumcraft:wicker_bundle[axis=x,bloodied=true]");
            server.runCommand("execute at @p run tp @p ~-1 ~ ~2 0 15");
            context.waitTicks(20);
            context.takeScreenshot("vime_2_os_dois_feixes");

            server.runCommand("give @p thaumcraft:wicker_bundle");
            server.runCommand("give @p thaumcraft:wicker_bundle[block_state={bloodied:\"true\"}]");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("vime_3_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
