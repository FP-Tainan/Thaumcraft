package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** Uma mancha de mácula com a Flor Etérea ao lado, para ver as duas. */
public class TaintClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("gamerule randomTickSpeed 0");
            server.runCommand("execute at @p run tp @s ~ ~ ~ 0 25");

            // uma mancha de mácula com fibras por cima
            server.runCommand("execute at @p run fill ~-3 ~-1 ~4 ~1 ~-1 ~8 thaumcraft:taint_soil");
            server.runCommand("execute at @p run setblock ~-1 ~-1 ~6 thaumcraft:taint_crust");
            server.runCommand("execute at @p run setblock ~-2 ~-1 ~5 thaumcraft:taint_crust");
            server.runCommand("execute at @p run setblock ~-1 ~ ~5 thaumcraft:taint_fibres");
            server.runCommand("execute at @p run setblock ~0 ~ ~7 thaumcraft:taint_fibres");
            server.runCommand("execute at @p run setblock ~-3 ~ ~4 thaumcraft:taint_fibres");

            // e a flor que a desfaz, do lado limpo
            server.runCommand("execute at @p run setblock ~3 ~-1 ~6 minecraft:grass_block");
            server.runCommand("execute at @p run setblock ~3 ~ ~6 thaumcraft:ethereal_bloom");
            server.runCommand("execute at @p run setblock ~4 ~-1 ~6 minecraft:grass_block");
            server.runCommand("execute at @p run setblock ~4 ~ ~6 thaumcraft:shimmerleaf");

            context.waitTicks(40);
            context.takeScreenshot("macula_e_flor");

            context.runOnClient(minecraft -> {
                var pos = minecraft.player.blockPosition().offset(0, -1, 6);
                System.out.println("[MACULA] em " + pos + " o bloco é "
                        + minecraft.level.getBlockState(pos).getBlock());
            });
        }
    }
}
