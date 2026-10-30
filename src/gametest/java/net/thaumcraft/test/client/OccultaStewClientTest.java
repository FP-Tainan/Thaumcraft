package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * O <b>Guisado de Carne</b>, cru e cozido, ao lado dos ensopados do jogo.
 *
 * <p>A tela que importa é a dos <b>dois juntos</b>: o cru é pálido e o cozido é escuro e espesso, e dá para
 * saber qual é qual sem ler o nome — que é como tem de ser numa tigela.
 *
 * <p>Ao lado deles vão a <b>sopa de cogumelos</b> e o <b>ensopado de coelho</b> do jogo, que são as tigelas
 * com que ele se compara.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaStewClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative @a");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            server.runCommand("give @p thaumcraft:stew_raw");
            server.runCommand("give @p thaumcraft:stew");
            server.runCommand("give @p minecraft:mushroom_stew");
            server.runCommand("give @p minecraft:rabbit_stew");
            server.runCommand("give @p minecraft:bowl");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("guisado_1_cru_cozido_e_os_do_jogo");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
