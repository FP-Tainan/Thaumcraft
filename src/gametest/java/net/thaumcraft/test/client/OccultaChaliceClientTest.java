package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * O <b>Candelabro</b> e o <b>Cálice</b> de perto.
 *
 * <p>A primeira tela é o candelabro sozinho, à noite e em cima de um pedestal de pedra, para se ver que
 * ele <b>dá luz</b>, que as cinco chamas estão nos cinco pratinhos e que o <b>pé</b> e os <b>braços de
 * ferro</b> estão por baixo delas — que é o que não se vê quando ele está no chão.
 *
 * <p>A segunda são os dois cálices lado a lado — o vazio e o cheio. A diferença entre eles é <b>uma chapa
 * chata de líquido</b> dentro da taça, e tem de se ver.
 *
 * <p>A terceira é o altar posto como se usa: as seis pedras com um candelabro numa e um cálice noutra.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaChaliceClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set midnight");
            server.runCommand("weather clear");
            context.waitTicks(20);

            // o candelabro sozinho, no escuro e em cima de um pedestal, para se lhe ver o pé
            server.runCommand("execute at @p run setblock ~ ~ ~3 minecraft:stone");
            server.runCommand("execute at @p run setblock ~ ~1 ~3 thaumcraft:candelabra");
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 -5");
            context.waitTicks(40);
            context.takeScreenshot("taca_1_o_candelabro");

            // os dois cálices, de dia
            server.runCommand("time set noon");
            server.runCommand("execute at @p run setblock ~-1 ~ ~2 thaumcraft:chalice[filled=false]");
            server.runCommand("execute at @p run setblock ~1 ~ ~2 thaumcraft:chalice[filled=true]");
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 25");
            context.waitTicks(30);
            context.takeScreenshot("taca_2_os_dois_calices");

            // e o altar posto como se usa
            for (int volta = 0; volta < 6; volta++) {
                server.runCommand("execute at @p run setblock ~" + (volta % 3 - 4) + " ~ ~"
                        + (volta / 3 + 4) + " thaumcraft:witch_altar");
            }
            server.runCommand("execute at @p run setblock ~-4 ~1 ~4 thaumcraft:candelabra");
            server.runCommand("execute at @p run setblock ~-2 ~1 ~4 thaumcraft:chalice[filled=true]");
            server.runCommand("execute at @p run tp @p ~-3 ~1 ~1 0 20");
            context.waitTicks(30);
            context.takeScreenshot("taca_3_no_altar");

            server.runCommand("give @p thaumcraft:candelabra");
            server.runCommand("give @p thaumcraft:chalice");
            server.runCommand("give @p thaumcraft:filled_chalice");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("taca_4_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
