package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A mácula no bioma dela: crosta, solo e carne; as cinco formas de fibra (a película forrando pedra, o capim, o capim que
 * brilha e os talos); a gosma em três alturas, o gás solto e o gás sob um teto; e a Flor Etérea ao lado.
 */
public class TaintClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("gamerule random_tick_speed 0");
            server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 30");
            server.runCommand("execute at @p run fillbiome ~-12 ~-3 ~-2 ~12 ~10 ~16 thaumcraft:tainted_land");
            server.runCommand("execute at @p run fill ~-6 ~-2 ~3 ~6 ~-2 ~10 minecraft:stone");
            server.runCommand("execute at @p run fill ~-6 ~-1 ~3 ~6 ~4 ~10 minecraft:air");

            // a fila de blocos: crosta, solo, carne
            server.runCommand("execute at @p run setblock ~-4 ~-2 ~4 thaumcraft:taint_crust");
            server.runCommand("execute at @p run setblock ~-3 ~-2 ~4 thaumcraft:taint_soil");
            server.runCommand("execute at @p run setblock ~-2 ~-2 ~4 thaumcraft:flesh_block");
            // as fibras: a película no chão e numa parede, o capim, o que brilha, os talos
            server.runCommand("execute at @p run setblock ~-4 ~-1 ~6 thaumcraft:taint_fibres[kind=0]");
            server.runCommand("execute at @p run fill ~-5 ~-1 ~7 ~-5 ~1 ~7 minecraft:stone");
            server.runCommand("execute at @p run setblock ~-4 ~ ~7 thaumcraft:taint_fibres[kind=0]");
            server.runCommand("execute at @p run setblock ~-3 ~-1 ~6 thaumcraft:taint_fibres[kind=1]");
            server.runCommand("execute at @p run setblock ~-2 ~-1 ~6 thaumcraft:taint_fibres[kind=2]");
            server.runCommand("execute at @p run setblock ~-1 ~-1 ~6 thaumcraft:taint_fibres[kind=3]");
            server.runCommand("execute at @p run setblock ~0 ~-1 ~6 thaumcraft:taint_fibres[kind=4]");
            // a gosma: rasa, meia e cheia e o gás sob um teto de pedra
            server.runCommand("execute at @p run setblock ~1 ~-1 ~4 thaumcraft:flux_goo[level=1]");
            server.runCommand("execute at @p run setblock ~2 ~-1 ~4 thaumcraft:flux_goo[level=4]");
            server.runCommand("execute at @p run setblock ~3 ~-1 ~4 thaumcraft:flux_goo[level=7]");
            server.runCommand("execute at @p run setblock ~4 ~2 ~8 minecraft:stone");
            server.runCommand("execute at @p run setblock ~4 ~1 ~8 thaumcraft:flux_gas[level=3]");
            // e a flor
            server.runCommand("execute at @p run setblock ~5 ~-2 ~5 minecraft:grass_block");
            server.runCommand("execute at @p run setblock ~5 ~-1 ~5 thaumcraft:ethereal_bloom");

            server.runCommand("execute as @p at @s run tp @s ~ ~3 ~1 0 50");
            context.waitTicks(60);
            context.takeScreenshot("macula_e_fluxo");
            server.runCommand("time set midnight");
            context.waitTicks(20);
            context.takeScreenshot("macula_noite");
        }
    }
}
