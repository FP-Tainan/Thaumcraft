package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A faca do ofício e o que ela abre, vistos: a Arthana na mão e os três pós e pedras que vêm dela.
 */
public class OccultaArthanaClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");

            server.runCommand("item replace entity @p weapon.mainhand with thaumcraft:arthana");
            server.runCommand("item replace entity @p hotbar.1 with thaumcraft:spectral_dust");
            server.runCommand("item replace entity @p hotbar.2 with thaumcraft:graveyard_dust");
            server.runCommand("item replace entity @p hotbar.3 with thaumcraft:necrotic_stone");
            server.runCommand("item replace entity @p hotbar.4 with thaumcraft:attuned_stone");
            server.runCommand("item replace entity @p hotbar.5 with minecraft:skeleton_skull");
            server.runCommand("item replace entity @p hotbar.6 with minecraft:zombie_head");
            server.runCommand("item replace entity @p hotbar.7 with minecraft:creeper_head");
            server.runCommand("item replace entity @p hotbar.8 with thaumcraft:brazier");
            context.waitTicks(30);
            context.takeScreenshot("ao_arthana_itens");
        }
    }
}
