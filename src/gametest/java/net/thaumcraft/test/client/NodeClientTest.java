package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** Põe um nó de aura na frente do nariz e tira retrato, que é a única maneira de conferir um brilho. */
public class NodeClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            singleplayer.getServer().runCommand("gamemode creative");
            singleplayer.getServer().runCommand("time set midnight");
            singleplayer.getServer().runCommand("weather clear");
            singleplayer.getServer().runCommand("execute at @p run tp @s ~ ~ ~ 0 0");
            singleplayer.getServer().runCommand("execute at @p run setblock ~ ~1 ~4 thaumcraft:node");
            context.waitTicks(40);
            context.takeScreenshot("no_de_aura");

            // e de dia, para ver como ele se sai contra a luz
            singleplayer.getServer().runCommand("time set noon");
            context.waitTicks(20);
            context.takeScreenshot("no_de_dia");

            // com o thaumômetro, para ver o que ele diz de um nó
            singleplayer.getServer().runCommand("give @p thaumcraft:thaumometer");
            context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(0));
            context.waitTicks(20);
            context.takeScreenshot("no_no_visor");
        }
    }
}
