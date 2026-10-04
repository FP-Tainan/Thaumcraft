package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * O <b>Gelo Perpétuo</b> e a família dele na tela.
 *
 * <p>Ele leva a folha do gelo do mundo, de propósito: a diferença entre os dois não é de aparência, é de
 * <b>tempo</b>, e quem olha não tem de saber qual é qual até o sol bater.
 *
 * <p>O que se vê aqui é o que o gelo do mundo nunca teve — <b>escada, laje, cerca, portão, placa e porta</b>
 * —, e é por isso que esta família existe: para dar para construir uma casa de gelo em vez de um lago
 * congelado.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaIceClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            // a família de construção, lado a lado sobre um tapete de gelo
            server.runCommand("execute at @p run fill ~-3 ~-1 ~4 ~3 ~-1 ~6 thaumcraft:perpetual_ice");
            server.runCommand("execute at @p run setblock ~-3 ~ ~5 thaumcraft:ice_stairs[facing=south]");
            server.runCommand("execute at @p run setblock ~-2 ~ ~5 thaumcraft:ice_slab[type=bottom]");
            server.runCommand("execute at @p run setblock ~-1 ~ ~5 thaumcraft:ice_fence");
            server.runCommand("execute at @p run setblock ~ ~ ~5 "
                    + "thaumcraft:ice_fence_gate[facing=east,open=false]");
            server.runCommand("execute at @p run setblock ~1 ~ ~5 thaumcraft:ice_pressure_plate");
            server.runCommand("execute at @p run setblock ~2 ~ ~5 "
                    + "thaumcraft:ice_door[facing=north,half=lower,hinge=left,open=false]");
            server.runCommand("execute at @p run setblock ~2 ~1 ~5 "
                    + "thaumcraft:ice_door[facing=north,half=upper,hinge=left,open=false]");
            server.runCommand("execute at @p run setblock ~3 ~ ~5 minecraft:torch");
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 10");
            context.waitTicks(20);
            context.takeScreenshot("gelo_1_a_familia");

            // e a de neve, que ganhou escada e laje pelo mesmo motivo
            server.runCommand("execute at @p run fill ~-3 ~-1 ~8 ~3 ~-1 ~9 minecraft:snow_block");
            server.runCommand("execute at @p run setblock ~-1 ~ ~9 thaumcraft:snow_stairs[facing=south]");
            server.runCommand("execute at @p run setblock ~ ~ ~9 thaumcraft:snow_slab[type=bottom]");
            server.runCommand("execute at @p run setblock ~1 ~ ~9 thaumcraft:snow_pressure_plate");
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 20");
            context.waitTicks(20);
            context.takeScreenshot("gelo_2_a_neve");

            // e as peças no inventário
            for (String oquê : new String[]{"perpetual_ice", "ice_stairs", "ice_slab", "ice_fence",
                    "ice_fence_gate", "ice_pressure_plate", "ice_door", "snow_stairs", "snow_slab",
                    "snow_pressure_plate", "frozen_heart"}) {
                server.runCommand("give @p thaumcraft:" + oquê);
            }
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("gelo_3_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
