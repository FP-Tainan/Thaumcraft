package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * O <b>Baú de Sanguessugas</b> na tela, e os <b>sacos de sangue</b> que o denunciam.
 *
 * <p>São quatro, com <b>zero, um, dois e três nomes</b> guardados, e a diferença entre eles é um
 * <b>relevo de um pixel</b> na frente: um saco por nome, por cima do desenho de sacos que a folha já traz.
 * É pouco — e é o pouco do original, que fez a frente do baú parecer cheia de sacos e depois pôs os de
 * verdade em relevo por cima.
 *
 * <p>A <b>tampa de quatro quartos</b> é a outra coisa que esta tela guarda: ela não dobra, ela se abre.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaLeechChestClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            // quatro baús: sem nome nenhum, com um, com dois e com três
            String[] quantos = {
                "{QuemAbriu:[]}",
                "{QuemAbriu:[\"Ana\"]}",
                "{QuemAbriu:[\"Ana\",\"Bento\"]}",
                "{QuemAbriu:[\"Ana\",\"Bento\",\"Clara\"]}",
            };
            for (int i = 0; i < quantos.length; i++) {
                int x = -3 + i * 2;
                server.runCommand("execute at @p run setblock ~" + x + " ~ ~5 "
                        + "thaumcraft:leech_chest[facing=south]");
                server.runCommand("execute at @p run data merge block ~" + x + " ~ ~5 " + quantos[i]);
            }
            server.runCommand("execute at @p run tp @p ~ ~ ~1 0 22");
            context.waitTicks(30);
            context.takeScreenshot("bau_1_os_sacos");

            // e um mais perto, de lado, onde a tampa se vê
            server.runCommand("execute at @p run tp @p ~3 ~ ~3 -45 18");
            context.waitTicks(20);
            context.takeScreenshot("bau_2_de_lado");

            server.runCommand("give @p thaumcraft:leech_chest");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("bau_3_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
