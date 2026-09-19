package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** Os orbes das melhorias de foco parados no ar (a bola de fogo e o choque de terra) e o campo estático no chão. */
public class FocusUpgradeClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            singleplayer.getServer().runCommand("gamemode creative");
            singleplayer.getServer().runCommand("time set midnight");
            singleplayer.getServer().runCommand("weather clear");
            singleplayer.getServer().runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 30");
            singleplayer.getServer().runCommand("execute at @p run summon thaumcraft:explosive_orb ~-0.8 ~1.6 ~2.5 {Motion:[0.0,0.0,0.0],NoGravity:1b}");
            singleplayer.getServer().runCommand("execute at @p run summon thaumcraft:shock_orb ~0.8 ~1.6 ~2.5 {Motion:[0.0,0.0,0.0],NoGravity:1b}");
            singleplayer.getServer().runCommand("execute at @p run fill ~-1 ~ ~3 ~1 ~ ~4 thaumcraft:spark_field");
            context.waitTicks(12);
            context.takeScreenshot("orbes_das_melhorias");
        }
    }
}
