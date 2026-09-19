package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.thaumcraft.item.WandItem;

/** O cetro na mão: a ponta maior com a achatada embaixo e as dez runas girando em volta. */
public class SceptreClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            singleplayer.getServer().runCommand("gamemode creative");
            singleplayer.getServer().runCommand("time set midnight");
            singleplayer.getServer().runOnServer(server -> {
                var player = server.getPlayerList().getPlayers().getFirst();
                player.getInventory().setItem(0, WandItem.creativeVariants().get(3));
                player.getInventory().setItem(1, WandItem.creativeVariants().get(2));
            });
            context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(0));
            context.waitTicks(20);
            context.takeScreenshot("cetro_mao");
            context.getInput().pressKey(options -> options.keyTogglePerspective);
            context.getInput().pressKey(options -> options.keyTogglePerspective);
            context.waitTicks(10);
            context.takeScreenshot("cetro_frente");
        }
    }
}
