package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import org.lwjgl.glfw.GLFW;

/** O menu radial da tecla de trocar foco, com focos no inventário e numa bolsa, e a tela da bolsa aberta. */
public class FocusRadialClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("clear @p");
            server.runCommand("item replace entity @p hotbar.0 with thaumcraft:wand[thaumcraft:wand_focus=\"fire\"]");
            server.runCommand("item replace entity @p hotbar.1 with thaumcraft:focus_pouch[minecraft:container=[{slot:0,item:{id:\"thaumcraft:focus_frost\"}},{slot:1,item:{id:\"thaumcraft:focus_shock\"}}]]");
            server.runCommand("give @p thaumcraft:focus_excavation");
            server.runCommand("give @p thaumcraft:focus_trade");
            server.runCommand("give @p thaumcraft:focus_primal");
            context.runOnClient(client -> client.player.getInventory().setSelectedSlot(0));
            context.waitTicks(20);
            context.getInput().holdKey(net.thaumcraft.client.FocusRadial.KEY_F);
            context.waitTicks(20);
            context.takeScreenshot("radial_de_focos");
            context.getInput().releaseKey(net.thaumcraft.client.FocusRadial.KEY_F);
            context.waitTicks(20);
            // a bolsa aberta
            context.runOnClient(client -> client.player.getInventory().setSelectedSlot(1));
            context.waitTicks(5);
            context.getInput().pressMouse(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            context.waitTicks(20);
            context.takeScreenshot("bolsa_de_focos");
        }
    }
}
