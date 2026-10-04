package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * O <b>Apanha-Erva</b> na tela, e o quadrado de quatro que uma mutação pede.
 *
 * <p>Vazio ele é uma moita: quatro folhas chatas no chão, dois pedaços de caule tortos e quatro pétalas.
 * Cheio, ele <b>mostra o que segura</b> — e é essa a razão de ele existir, porque uma receita de quatro
 * Apanha-Ervas só se lê se as quatro bocas estiverem à vista.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaGrassperClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            // um vazio e um cheio, lado a lado
            server.runCommand("execute at @p run setblock ~-1 ~ ~4 thaumcraft:grassper[facing=north]");
            server.runCommand("execute at @p run setblock ~1 ~ ~4 thaumcraft:grassper[facing=north]");
            server.runCommand("execute at @p run data merge block ~1 ~ ~4 "
                    + "{NaBoca:{id:\"minecraft:ender_pearl\",count:1}}");
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 25");
            context.waitTicks(30);
            context.takeScreenshot("apanha_1_vazio_e_cheio");

            // e o quadrado de quatro de uma mutação, com a cana no meio
            server.runCommand("execute at @p run fill ~-2 ~-1 ~7 ~2 ~-1 ~11 minecraft:water");
            server.runCommand("execute at @p run setblock ~ ~-1 ~9 minecraft:dirt");
            server.runCommand("execute at @p run setblock ~ ~ ~9 minecraft:sugar_cane");
            for (int[] quina : new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}}) {
                server.runCommand("execute at @p run setblock ~" + quina[0] + " ~-1 ~" + (9 + quina[1])
                        + " minecraft:dirt");
                server.runCommand("execute at @p run setblock ~" + quina[0] + " ~ ~" + (9 + quina[1])
                        + " thaumcraft:grassper[facing=north]");
                server.runCommand("execute at @p run data merge block ~" + quina[0] + " ~ ~"
                        + (9 + quina[1]) + " {NaBoca:{id:\"minecraft:ender_pearl\",count:1}}");
            }
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 40");
            context.waitTicks(30);
            context.takeScreenshot("apanha_2_o_quadrado");

            server.runCommand("give @p thaumcraft:grassper");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("apanha_3_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
