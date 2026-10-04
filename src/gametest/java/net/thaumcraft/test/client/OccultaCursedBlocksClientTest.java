package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * As <b>gêmeas amaldiçoadas</b> na tela — e a foto existe para provar que <b>não há nada para ver</b>.
 *
 * <p>Esta é a única tela deste porte cujo acerto é a <b>ausência</b> de diferença. Em cima, as peças do
 * mundo; em baixo, as gêmeas com um cozimento preso dentro. Se as duas filas não forem iguais pixel a pixel,
 * a armadilha deixou de ser uma armadilha: quem jogasse veria qual botão não apertar.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaCursedBlocksClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            // a parede em que as peças se penduram
            server.runCommand("execute at @p run fill ~-4 ~-1 ~5 ~4 ~2 ~5 minecraft:stone_bricks");

            // em cima, as do mundo
            server.runCommand("execute at @p run setblock ~-3 ~1 ~4 "
                    + "minecraft:stone_button[face=wall,facing=south]");
            server.runCommand("execute at @p run setblock ~-1 ~1 ~4 "
                    + "minecraft:oak_button[face=wall,facing=south]");
            server.runCommand("execute at @p run setblock ~1 ~1 ~4 "
                    + "minecraft:lever[face=wall,facing=south]");
            server.runCommand("execute at @p run setblock ~3 ~ ~4 minecraft:oak_pressure_plate");

            // em baixo, as gêmeas
            server.runCommand("execute at @p run setblock ~-3 ~ ~4 "
                    + "thaumcraft:cursed_stone_button[face=wall,facing=south]");
            server.runCommand("execute at @p run setblock ~-1 ~ ~4 "
                    + "thaumcraft:cursed_wooden_button[face=wall,facing=south]");
            server.runCommand("execute at @p run setblock ~1 ~ ~4 "
                    + "thaumcraft:cursed_lever[face=wall,facing=south]");
            server.runCommand("execute at @p run setblock ~2 ~ ~4 "
                    + "thaumcraft:cursed_wooden_pressure_plate");
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 5");
            context.waitTicks(20);
            context.takeScreenshot("maldito_1_iguais");

            // e as duas portas, lado a lado
            server.runCommand("execute at @p run setblock ~-1 ~ ~4 air");
            server.runCommand("execute at @p run setblock ~1 ~ ~4 air");
            server.runCommand("execute at @p run setblock ~-3 ~ ~4 air");
            server.runCommand("execute at @p run setblock ~-3 ~1 ~4 air");
            server.runCommand("execute at @p run setblock ~-1 ~1 ~4 air");
            server.runCommand("execute at @p run setblock ~1 ~1 ~4 air");
            server.runCommand("execute at @p run setblock ~-1 ~ ~5 air");
            server.runCommand("execute at @p run setblock ~-1 ~1 ~5 air");
            server.runCommand("execute at @p run setblock ~1 ~ ~5 air");
            server.runCommand("execute at @p run setblock ~1 ~1 ~5 air");
            server.runCommand("execute at @p run setblock ~-1 ~ ~5 "
                    + "minecraft:oak_door[facing=north,half=lower,hinge=left,open=false]");
            server.runCommand("execute at @p run setblock ~-1 ~1 ~5 "
                    + "minecraft:oak_door[facing=north,half=upper,hinge=left,open=false]");
            server.runCommand("execute at @p run setblock ~1 ~ ~5 "
                    + "thaumcraft:cursed_wooden_door[facing=north,half=lower,hinge=left,open=false]");
            server.runCommand("execute at @p run setblock ~1 ~1 ~5 "
                    + "thaumcraft:cursed_wooden_door[facing=north,half=upper,hinge=left,open=false]");
            context.waitTicks(20);
            context.takeScreenshot("maldito_2_as_duas_portas");
        }
    }
}
