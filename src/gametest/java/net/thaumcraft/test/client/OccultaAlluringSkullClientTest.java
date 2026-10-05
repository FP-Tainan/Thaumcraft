package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A <b>Caveira do Chamado</b> e o <b>Ovo do Infinito</b>.
 *
 * <p>A primeira tela são as duas caveiras lado a lado — a que dorme e a que está acordada —, porque a
 * diferença entre elas é só a <b>pele</b> e tem de se ver a olho.
 *
 * <p>A segunda põe uma em cada parede e uma no chão, para se ver que as contas de onde ela fica são as do
 * original: um quarto de altura na parede, e a cabeça saindo dela sem a atravessar.
 *
 * <p>E a terceira é o Ovo do Infinito em cima de um altar.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaAlluringSkullClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            server.runCommand("execute at @p run setblock ~-1 ~ ~3 "
                    + "thaumcraft:alluring_skull[facing=up,rotation=0,awake=false]");
            server.runCommand("execute at @p run setblock ~1 ~ ~3 "
                    + "thaumcraft:alluring_skull[facing=up,rotation=0,awake=true]");
            server.runCommand("execute at @p run tp @p ~ ~-0.5 ~1 0 20");
            context.waitTicks(30);
            context.takeScreenshot("caveira_1_dormindo_e_acordada");

            // uma em cada parede de um pilar, e uma em cima dele
            server.runCommand("execute at @p run setblock ~ ~ ~6 minecraft:stone");
            server.runCommand("execute at @p run setblock ~ ~1 ~6 minecraft:stone");
            server.runCommand("execute at @p run setblock ~ ~2 ~6 "
                    + "thaumcraft:alluring_skull[facing=up,rotation=4,awake=true]");
            for (String lado : new String[] {"north", "south", "east", "west"}) {
                server.runCommand("execute at @p run setblock ~" + desvioX(lado) + " ~1 ~"
                        + (6 + desvioZ(lado)) + " thaumcraft:alluring_skull[facing=" + lado
                        + ",rotation=0,awake=false]");
            }
            server.runCommand("execute at @p run tp @p ~ ~1 ~3 0 10");
            context.waitTicks(30);
            context.takeScreenshot("caveira_2_nas_paredes");

            // e o ovo em cima de um altar
            for (int volta = 0; volta < 6; volta++) {
                server.runCommand("execute at @p run setblock ~" + (volta % 3 - 1) + " ~ ~"
                        + (volta / 3 + 9) + " thaumcraft:witch_altar");
            }
            server.runCommand("execute at @p run setblock ~-1 ~1 ~9 thaumcraft:infinity_egg");
            server.runCommand("execute at @p run tp @p ~ ~1 ~6 0 15");
            context.waitTicks(30);
            context.takeScreenshot("caveira_3_o_ovo_no_altar");
        }
    }

    /** O bloco em que a caveira daquela parede se prega fica do lado de fora dela. */
    private static int desvioX(String lado) {
        return switch (lado) {
            case "east" -> 1;
            case "west" -> -1;
            default -> 0;
        };
    }

    private static int desvioZ(String lado) {
        return switch (lado) {
            case "south" -> 1;
            case "north" -> -1;
            default -> 0;
        };
    }
}
