package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** As variações do tubo lado a lado: o estreito, o de mão única e o tampão, ligados a jarros. */
public class TubeVariantsClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            singleplayer.getServer().runCommand("gamemode creative");
            singleplayer.getServer().runCommand("time set noon");
            singleplayer.getServer().runCommand("weather clear");
            singleplayer.getServer().runCommand("execute as @p at @s run tp @s ~ ~1 ~ 0 30");
            // uma fileira: jarro, estreito, tampão, mão única, jarro
            singleplayer.getServer().runCommand("execute at @p run setblock ~-2 ~-1 ~3 thaumcraft:jar");
            singleplayer.getServer().runCommand("execute at @p run setblock ~-1 ~-1 ~3 thaumcraft:tube_restrict");
            singleplayer.getServer().runCommand("execute at @p run setblock ~ ~-1 ~3 thaumcraft:tube_buffer");
            singleplayer.getServer().runCommand("execute at @p run setblock ~1 ~-1 ~3 thaumcraft:tube_oneway");
            singleplayer.getServer().runCommand("execute at @p run setblock ~2 ~-1 ~3 thaumcraft:jar");
            singleplayer.getServer().runCommand("give @p thaumcraft:tube_restrict");
            singleplayer.getServer().runCommand("give @p thaumcraft:tube_oneway");
            singleplayer.getServer().runCommand("give @p thaumcraft:tube_buffer");
            context.waitTicks(40);
            context.takeScreenshot("tubos_variantes");
        }
    }
}
