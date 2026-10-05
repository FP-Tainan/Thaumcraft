package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A <b>Bola de Cristal</b> de perto.
 *
 * <p>A primeira tela é a bola sozinha em cima de um pedestal, para se ver o <b>pé de três degraus</b> e as
 * <b>três cascas</b> encaixadas da esfera.
 *
 * <p>A segunda é a mesma bola um pouco depois, para se ver que o miolo dela <b>mudou de tom</b> — é a
 * pulsação, que dá a volta em cento e sessenta batidas.
 *
 * <p>E a terceira é a bola e o Óleo do Acaso no inventário.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaCrystalBallClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            server.runCommand("execute at @p run setblock ~ ~ ~3 minecraft:stone");
            server.runCommand("execute at @p run setblock ~ ~1 ~3 thaumcraft:crystal_ball");
            server.runCommand("execute at @p run tp @p ~ ~ ~1 0 0");
            context.waitTicks(40);
            context.takeScreenshot("bola_1_a_bola");

            // oitenta batidas depois, o miolo está do outro lado do vaivém
            context.waitTicks(80);
            context.takeScreenshot("bola_2_o_miolo_pulsa");

            server.runCommand("give @p thaumcraft:crystal_ball");
            server.runCommand("give @p thaumcraft:happenstance_oil");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("bola_3_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
