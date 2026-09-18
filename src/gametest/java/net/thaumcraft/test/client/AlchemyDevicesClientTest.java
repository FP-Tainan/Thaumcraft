package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** A centrífuga, o cristalizador e o construto alquímico, e a essência cristalizada na mão. */
public class AlchemyDevicesClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("execute as @p at @s run tp @s ~ ~1 ~ 0 25");
            server.runCommand("execute at @p run setblock ~-2 ~-1 ~3 thaumcraft:centrifuge");
            server.runCommand("execute at @p run setblock ~-2 ~-2 ~3 thaumcraft:tube");
            server.runCommand("execute at @p run setblock ~ ~-1 ~3 thaumcraft:alchemical_construct");
            server.runCommand("execute at @p run setblock ~2 ~-1 ~3 thaumcraft:essentia_crystalizer[facing=down]");
            server.runCommand("execute at @p run setblock ~2 ~-1 ~5 thaumcraft:essentia_crystalizer[facing=north]");
            server.runCommand("execute at @p run data merge block ~2 ~-1 ~3 {aspect:\"ignis\"}");
            server.runCommand("give @p thaumcraft:crystal_essence[thaumcraft:crystal_aspect=\"aqua\"] 4");
            server.runCommand("give @p thaumcraft:crystal_essence[thaumcraft:crystal_aspect=\"ignis\"] 4");
            server.runCommand("give @p thaumcraft:centrifuge");
            server.runCommand("give @p thaumcraft:essentia_crystalizer");
            server.runCommand("give @p thaumcraft:alchemical_construct");
            context.waitTicks(60);
            context.takeScreenshot("alquimia_aparelhos");
        }
    }
}
