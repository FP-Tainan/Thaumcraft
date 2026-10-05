package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * O <b>Vidro Sombreado</b> nas dezesseis cores, aberto e fechado — e a <b>Lã Ensanguentada</b> e o
 * <b>Globo de Luz</b> ao lado.
 *
 * <p>As duas fileiras são a tela que importa: a de cima aberta, a de baixo fechada. Elas mostram a mesma
 * cor nos dois feitios, e vê-se que a fechada é mais escura e deixa ver menos — que é a persiana corrida.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaShadedGlassClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            String[] cores = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
                "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
            for (int i = 0; i < cores.length; i++) {
                server.runCommand("execute at @p run setblock ~" + (-8 + i) + " ~1 ~6 "
                        + "thaumcraft:shaded_glass[color=" + cores[i] + ",powered=false]");
                server.runCommand("execute at @p run setblock ~" + (-8 + i) + " ~ ~6 "
                        + "thaumcraft:shaded_glass[color=" + cores[i] + ",powered=true]");
            }
            server.runCommand("execute at @p run tp @p ~ ~1 ~-8 0 3");
            context.waitTicks(30);
            context.takeScreenshot("vidro_1_as_dezesseis");

            // a lã e o globo
            server.runCommand("execute at @p run setblock ~-2 ~ ~11 thaumcraft:blooded_wool");
            server.runCommand("execute at @p run setblock ~ ~ ~11 minecraft:white_wool");
            server.runCommand("execute at @p run setblock ~2 ~1 ~11 thaumcraft:glow_globe");
            server.runCommand("execute at @p run tp @p ~ ~1 ~8 0 5");
            server.runCommand("time set midnight");
            context.waitTicks(30);
            context.takeScreenshot("vidro_2_a_la_e_o_globo");

            server.runCommand("time set noon");
            server.runCommand("give @p thaumcraft:shaded_glass");
            server.runCommand("give @p thaumcraft:shaded_glass[block_state={color:\"red\"}]");
            server.runCommand("give @p thaumcraft:blooded_wool");
            server.runCommand("give @p thaumcraft:dark_cloth");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("vidro_3_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
