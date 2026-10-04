package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * As <b>três sarças</b> e o <b>Lírio-Saltador</b> na tela.
 *
 * <p>As três sarças têm folhas diferentes e é preciso que tenham: quem vê uma sarça de longe precisa de
 * saber se ela o vai <b>espinhar</b>, <b>mandar para quinhentos blocos daqui</b> ou apenas <b>calar o
 * ofício</b> à volta. Três plantas com o mesmo desenho seriam três armadilhas iguais.
 *
 * <p>A do Vazio ainda <b>brilha de leve</b>, que é como quem a plantou sabe onde o próprio silêncio começa.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaBramblesClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            // as três em fila, no chão
            server.runCommand("execute at @p run setblock ~-2 ~ ~4 thaumcraft:wild_bramble");
            server.runCommand("execute at @p run setblock ~ ~ ~4 thaumcraft:ender_bramble");
            server.runCommand("execute at @p run setblock ~2 ~ ~4 thaumcraft:void_bramble");
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 15");
            context.waitTicks(20);
            context.takeScreenshot("sarcas_1_as_tres");

            // e de noite, que é onde a do Vazio se vê brilhar
            server.runCommand("time set midnight");
            context.waitTicks(30);
            context.takeScreenshot("sarcas_2_de_noite");
            server.runCommand("time set noon");

            // o nenúfar, numa poça
            server.runCommand("execute at @p run fill ~-2 ~-1 ~6 ~2 ~-1 ~8 minecraft:water");
            server.runCommand("execute at @p run setblock ~-1 ~ ~7 thaumcraft:leaping_lily");
            server.runCommand("execute at @p run setblock ~ ~ ~7 thaumcraft:leaping_lily");
            server.runCommand("execute at @p run setblock ~1 ~ ~7 thaumcraft:leaping_lily");
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 35");
            context.waitTicks(20);
            context.takeScreenshot("sarcas_3_o_nenufar");

            // e as quatro no inventário
            for (String oquê : new String[]{"wild_bramble", "ender_bramble", "void_bramble", "leaping_lily"}) {
                server.runCommand("give @p thaumcraft:" + oquê);
            }
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("sarcas_4_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
