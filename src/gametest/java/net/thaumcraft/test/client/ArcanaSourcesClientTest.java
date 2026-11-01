package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * As outras duas fontes vistas de perto: o Prisma Celeste com os quatro topos e o Aurem Negro com o
 * redemoinho dele.
 *
 * <p>O Aurem não tem modelo nenhum — é uma figura chapada virada para quem olha. Uma prova de servidor
 * nunca saberia se ela está lá.
 */
public class ArcanaSourcesClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("gamerule doDaylightCycle false");
            server.runCommand("weather clear");

            // o Prisma inteiro: quatro pilares de quartzo com topos de pedra-da-lua e o anel de giz
            server.runCommand("setblock ~-5 ~ ~-9 thaumcraft:celestial_prism");
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) continue;
                    server.runCommand("setblock ~" + (-5 + i) + " ~ ~" + (-9 + j) + " thaumcraft:wizard_chalk");
                }
            }
            for (int x = -2; x <= 2; x += 4) {
                for (int z = -2; z <= 2; z += 4) {
                    server.runCommand("setblock ~" + (-5 + x) + " ~ ~" + (-9 + z) + " minecraft:quartz_block");
                    server.runCommand("setblock ~" + (-5 + x) + " ~1 ~" + (-9 + z) + " minecraft:quartz_block");
                    server.runCommand("setblock ~" + (-5 + x) + " ~2 ~" + (-9 + z) + " thaumcraft:moonstone_block");
                }
            }
            // e o Aurem, com pilares de tijolo do Nether e topos de pedra-do-sol
            server.runCommand("setblock ~5 ~ ~-9 thaumcraft:black_aurem");
            for (int x = -2; x <= 2; x += 4) {
                for (int z = -2; z <= 2; z += 4) {
                    server.runCommand("setblock ~" + (5 + x) + " ~ ~" + (-9 + z) + " minecraft:nether_bricks");
                    server.runCommand("setblock ~" + (5 + x) + " ~1 ~" + (-9 + z) + " minecraft:nether_bricks");
                    server.runCommand("setblock ~" + (5 + x) + " ~2 ~" + (-9 + z) + " thaumcraft:sunstone_block");
                }
            }
            server.runCommand("tp @p ~ ~4 ~5 180 20");
            context.waitTicks(40);
            context.takeScreenshot("arcana_fontes");

            // e as duas na barra
            server.runCommand("item replace entity @p hotbar.0 with thaumcraft:celestial_prism");
            server.runCommand("item replace entity @p hotbar.1 with thaumcraft:black_aurem");
            context.waitTicks(20);
            context.takeScreenshot("arcana_fontes_itens");
        }
    }
}
