package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A fauna da mácula parada numa fileira (sem IA): galinha, vaca, porco, ovelha, creeper e aldeão maculados; atrás, o
 * slime taumático grande, a aranha, o esporo no talo, o enxameador e os dois tentáculos.
 */
public class TaintFaunaClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("gamerule random_tick_speed 0");
            server.runCommand("execute at @p run fillbiome ~-12 ~-3 ~-2 ~12 ~10 ~16 thaumcraft:tainted_land");
            server.runCommand("execute at @p run fill ~-8 ~ ~2 ~8 ~6 ~14 minecraft:air");
            String[] front = {"taint_chicken", "taint_cow", "taint_pig", "taint_sheep", "taint_creeper", "taint_villager"};
            for (int i = 0; i < front.length; i++) {
                server.runCommand("execute at @p run summon thaumcraft:" + front[i] + " ~" + (5 - i * 2) + " ~ ~6 {NoAI:1b,Rotation:[180f,0f]}");
            }
            server.runCommand("execute at @p run summon thaumcraft:thaumic_slime ~5 ~ ~10 {NoAI:1b,Size:8}");
            server.runCommand("execute at @p run summon thaumcraft:taint_spider ~2 ~ ~9 {NoAI:1b,Rotation:[180f,0f]}");
            server.runCommand("execute at @p run setblock ~0 ~-1 ~10 minecraft:stone");
            server.runCommand("execute at @p run setblock ~0 ~ ~10 thaumcraft:taint_fibres[kind=4]");
            server.runCommand("execute at @p run summon thaumcraft:taint_spore ~ ~1 ~10 {Size:8}");
            server.runCommand("execute at @p run summon thaumcraft:taint_spore_swarmer ~-2 ~ ~10");
            server.runCommand("execute at @p run setblock ~-5 ~-1 ~10 thaumcraft:taint_soil");
            server.runCommand("execute at @p run summon thaumcraft:taintacle ~-5 ~ ~11 {NoAI:1b}");
            server.runCommand("execute at @p run summon thaumcraft:taintacle_small ~-7 ~ ~9 {NoAI:1b}");
            server.runCommand("execute as @p at @s run tp @s ~ ~2 ~1 0 15");
            context.waitTicks(140);
            context.takeScreenshot("fauna_macula");
        }
    }
}
