package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** As três lâmpadas presas em pedra (a do chão, a da parede e a do teto) e os blocos de âmbar, à noite. */
public class LampsClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set midnight");
            server.runCommand("weather clear");
            server.runCommand("execute as @p at @s run tp @s ~ ~1 ~ 0 20");
            server.runCommand("execute at @p run fill ~-3 ~-1 ~5 ~3 ~2 ~5 minecraft:stone");
            server.runCommand("execute at @p run setblock ~-2 ~-1 ~4 thaumcraft:arcane_lamp[facing=south]");
            server.runCommand("execute at @p run setblock ~ ~-1 ~4 thaumcraft:growth_lamp[facing=south,lit=true]");
            server.runCommand("execute at @p run setblock ~2 ~-1 ~4 thaumcraft:fertility_lamp[facing=south]");
            server.runCommand("execute at @p run setblock ~-1 ~-1 ~3 thaumcraft:amber_block");
            server.runCommand("execute at @p run setblock ~1 ~-1 ~3 thaumcraft:amber_bricks");
            server.runCommand("execute at @p run setblock ~ ~-1 ~2 thaumcraft:hungry_chest[facing=north]");
            server.runCommand("give @p thaumcraft:hungry_chest");
            server.runCommand("give @p thaumcraft:arcane_lamp");
            server.runCommand("give @p thaumcraft:growth_lamp");
            server.runCommand("give @p thaumcraft:fertility_lamp");
            server.runCommand("give @p thaumcraft:amber_block");
            context.waitTicks(60);
            context.takeScreenshot("lampadas_e_ambar");
            server.runCommand("time set noon");
            server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 180 30");
            server.runCommand("execute at @p run setblock ~ ~ ~-2 thaumcraft:hungry_chest[facing=south]");
            server.runCommand("execute at @p run setblock ~2 ~ ~-2 thaumcraft:hungry_chest[facing=east]");
            context.waitTicks(30);
            context.takeScreenshot("bau_faminto");
        }
    }
}
