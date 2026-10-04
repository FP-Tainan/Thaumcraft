package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * As <b>doze Minas de Planta</b> na tela — e o que elas mostram é que são <b>três</b>.
 *
 * <p>A fileira tem uma mina de cada efeito para cada cara, e a prova é que as quatro papoulas são
 * indistinguíveis umas das outras, e o mesmo para os quatro dentes-de-leão e os quatro arbustos. Olhar para
 * esta tela não diz a ninguém onde está a de espinhos.
 *
 * <p>É de propósito que seja assim, e é o que faz da mina o que ela é.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaPlantMineClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            String[] caras = {"rose", "dandelion", "grass"};
            String[] efeitos = {"webs", "ink", "thorns", "sprouting"};
            for (int c = 0; c < caras.length; c++) {
                for (int e = 0; e < efeitos.length; e++) {
                    server.runCommand("execute at @p run setblock ~" + (-3 + e * 2) + " ~ ~"
                            + (4 + c * 2) + " thaumcraft:plant_mine[look=" + caras[c]
                            + ",effect=" + efeitos[e] + "]");
                }
            }
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 25");
            context.waitTicks(30);
            context.takeScreenshot("mina_1_as_doze");

            server.runCommand("give @p thaumcraft:plant_mine");
            server.runCommand(
                    "give @p thaumcraft:plant_mine[block_state={look:\"dandelion\",effect:\"thorns\"}]");
            server.runCommand(
                    "give @p thaumcraft:plant_mine[block_state={look:\"grass\",effect:\"ink\"}]");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("mina_2_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
