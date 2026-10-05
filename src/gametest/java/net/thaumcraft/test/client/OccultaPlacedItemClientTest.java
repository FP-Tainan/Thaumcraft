package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A <b>Arthana deitada</b> num altar.
 *
 * <p>A tela mostra o que este bloco existe para fazer: a faca <b>parada</b> em cima da pedra, virada para um
 * lado, sem boiar e sem girar. Uma faca largada no chão é lixo; uma faca deitada é um instrumento, e a
 * diferença entre as duas é só essa quietude.
 *
 * <p>A segunda tela põe quatro delas, uma para cada lado, para se ver que o giro é o do original.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaPlacedItemClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            // seis pedras de altar, e a faca deitada numa delas
            for (int volta = 0; volta < 6; volta++) {
                server.runCommand("execute at @p run setblock ~" + (volta % 3 - 1) + " ~ ~"
                        + (volta / 3 + 3) + " thaumcraft:witch_altar");
            }
            server.runCommand("give @p thaumcraft:arthana");
            context.waitTicks(10);
            server.runCommand("execute at @p run setblock ~-1 ~1 ~3 "
                    + "thaumcraft:placed_item[facing=north]");
            server.runCommand("execute at @p run data merge block ~-1 ~1 ~3 "
                    + "{WITCPlacedItem:{id:\"thaumcraft:arthana\",count:1}}");
            server.runCommand("execute at @p run tp @p ~-1 ~1 ~1 0 35");
            context.waitTicks(30);
            context.takeScreenshot("posto_1_a_faca_deitada");

            // e as quatro voltas
            String[] lados = {"north", "south", "east", "west"};
            for (int volta = 0; volta < 4; volta++) {
                server.runCommand("execute at @p run setblock ~" + (volta - 2) + " ~ ~5 minecraft:stone");
                server.runCommand("execute at @p run setblock ~" + (volta - 2) + " ~1 ~5 "
                        + "thaumcraft:placed_item[facing=" + lados[volta] + "]");
                server.runCommand("execute at @p run data merge block ~" + (volta - 2) + " ~1 ~5 "
                        + "{WITCPlacedItem:{id:\"thaumcraft:arthana\",count:1}}");
            }
            server.runCommand("execute at @p run tp @p ~-0.5 ~2 ~2 0 50");
            context.waitTicks(30);
            context.takeScreenshot("posto_2_as_quatro_voltas");
        }
    }
}
