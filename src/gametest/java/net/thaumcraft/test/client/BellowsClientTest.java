package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** Os foles em volta de um forno alquímico, soprando, e um na mão. */
public class BellowsClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 20");
            server.runCommand("execute at @p run setblock ~ ~ ~3 thaumcraft:alchemical_furnace");
            server.runCommand("execute at @p run setblock ~-1 ~ ~3 thaumcraft:bellows[facing=east]");
            server.runCommand("execute at @p run setblock ~1 ~ ~3 thaumcraft:bellows[facing=west]");
            server.runCommand("execute at @p run setblock ~ ~1 ~3 thaumcraft:bellows[facing=down]");
            server.runCommand("give @p thaumcraft:bellows");
            context.waitTicks(25);
            context.takeScreenshot("foles_no_forno");
        }
    }
}
