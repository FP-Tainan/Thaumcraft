package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** O altar de infusão montado, para ver a matriz, os pedestais e o que eles seguram. */
public class InfusionClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");

            // um chão claro na frente, e a vista de frente
            server.runCommand("execute at @p run fill ~-6 ~-1 ~2 ~6 ~-1 ~11 minecraft:smooth_stone");
            server.runCommand("execute at @p run tp @s ~ ~ ~ 0 12");

            // o altar: pedestal no meio, pedra arcana nos quatro cantos, matriz dois acima
            server.runCommand("execute at @p run setblock ~ ~ ~7 thaumcraft:pedestal");
            server.runCommand("execute at @p run setblock ~-1 ~ ~6 thaumcraft:arcane_stone");
            server.runCommand("execute at @p run setblock ~1 ~ ~6 thaumcraft:arcane_stone");
            server.runCommand("execute at @p run setblock ~-1 ~ ~8 thaumcraft:arcane_stone");
            server.runCommand("execute at @p run setblock ~1 ~ ~8 thaumcraft:arcane_stone");
            server.runCommand("execute at @p run setblock ~ ~2 ~7 thaumcraft:infusion_matrix");

            // pedestais em volta, e jarros de essência ao lado
            server.runCommand("execute at @p run setblock ~-3 ~ ~7 thaumcraft:pedestal");
            server.runCommand("execute at @p run setblock ~3 ~ ~7 thaumcraft:pedestal");
            server.runCommand("execute at @p run setblock ~-4 ~ ~4 thaumcraft:jar");
            server.runCommand("execute at @p run setblock ~4 ~ ~4 thaumcraft:jar");
            context.waitTicks(30);

            server.runCommand("execute at @p run data merge block ~ ~ ~7 {Items:[{id:\"minecraft:obsidian\",count:1,Slot:0b}]}");
            server.runCommand("execute at @p run data merge block ~-3 ~ ~7 {Items:[{id:\"thaumcraft:shard_balanced\",count:1,Slot:0b}]}");
            server.runCommand("execute at @p run data merge block ~3 ~ ~7 {Items:[{id:\"thaumcraft:shard_earth\",count:1,Slot:0b}]}");
            server.runCommand("execute at @p run data merge block ~-4 ~ ~4 {aspect:\"terra\",amount:60}");
            server.runCommand("execute at @p run data merge block ~4 ~ ~4 {aspect:\"tenebrae\",amount:40}");
            context.waitTicks(40);
            context.takeScreenshot("infusao_altar");

            // e de perto, para ver o que o pedestal segura
            server.runCommand("execute at @p run tp @s ~ ~ ~4 0 22");
            context.waitTicks(20);
            context.takeScreenshot("infusao_pedestal_de_perto");

            context.runOnClient(minecraft -> {
                var pos = minecraft.player.blockPosition().offset(0, 0, 3);
                var found = minecraft.level.getBlockEntity(pos);
                System.out.println("[INFUSAO] em " + pos + " achei " + found
                        + (found instanceof net.thaumcraft.block.entity.PedestalBlockEntity pedestal
                           ? " segurando " + pedestal.held()
                           : ""));
            });
        }
    }
}
