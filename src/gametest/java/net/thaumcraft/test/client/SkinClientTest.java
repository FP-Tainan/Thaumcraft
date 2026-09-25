package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * As peles que quem joga viu quebradas: os baús arcanos ao lado de um baú do jogo, o Baú Maligno — que ficava
 * meio bloco no ar e meio bloco de lado —, o jarro com bicho dentro, que não cabia no vidro, e a chama branca
 * do Lumos.
 *
 * <p>Não há o que conferir por conta própria numa tela: as fotos ficam em
 * {@code build/run/clientGameTest/screenshots} para quem estiver de olho.
 */
public class SkinClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("item replace entity @p hotbar.0 with air");
            context.waitTicks(20);

            // o baú do jogo ao lado dos dois do ramo, todos virados para quem olha
            perto(server, "setblock ~2 ~ ~4 minecraft:chest[facing=south]");
            perto(server, "setblock ~ ~ ~4 thaumcraft:arcane_chest_greatwood[facing=south]");
            perto(server, "setblock ~-2 ~ ~4 thaumcraft:arcane_chest_silverwood[facing=south]");
            context.waitTicks(20);
            perto(server, "tp @p ~ ~1 ~ 0 20");
            context.waitTicks(20);
            context.takeScreenshot("baus_lado_a_lado");

            // um feitio de cada vez, de frente e sozinho
            perto(server, "tp @p ~6 ~ ~ 0 8");
            context.waitTicks(20);
            String[] feitios = {"corrompido", "sinistro", "demoniaco", "maculado"};
            for (int feitio = 0; feitio < feitios.length; feitio++) {
                perto(server, "kill @e[type=thaumcraft:evil_trunk]");
                perto(server, "summon thaumcraft:evil_trunk ~ ~ ~5 {TrunkType:" + feitio
                        + ",NoAI:1b,Rotation:[180f,0f]}");
                context.waitTicks(20);
                context.takeScreenshot("bau_" + feitios[feitio]);
            }
            perto(server, "kill @e[type=thaumcraft:evil_trunk]");

            // e dois jarros com bicho dentro, que é o que o vidro tem de mostrar
            perto(server, "setblock ~-1 ~ ~3 thaumcraft:prison_jar");
            perto(server, "data merge block ~-1 ~ ~3 {entity:{id:\"minecraft:pig\"}}");
            perto(server, "setblock ~1 ~ ~3 thaumcraft:prison_jar");
            perto(server, "data merge block ~1 ~ ~3 {entity:{id:\"minecraft:bee\"}}");
            context.waitTicks(20);
            perto(server, "tp @p ~ ~ ~ 0 12");
            context.waitTicks(20);
            context.takeScreenshot("jarros");

            // a chama branca do Lumos, de noite, que é quando ela vale
            perto(server, "setblock ~ ~1 ~4 thaumcraft:lumos");
            server.runCommand("time set midnight");
            perto(server, "tp @p ~ ~ ~2 0 0");
            context.waitTicks(40);
            context.takeScreenshot("lumos");
        }
    }

    /** O comando corre no lugar de quem joga, e não na origem do mundo. */
    private static void perto(TestServerContext server, String command) {
        server.runCommand("execute at @p run " + command);
    }
}
