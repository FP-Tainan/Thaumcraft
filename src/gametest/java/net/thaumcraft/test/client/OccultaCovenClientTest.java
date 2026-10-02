package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * As <b>cinco caras</b> da Bruxa do Coven.
 *
 * <p>O corpo é o da bruxa do próprio jogo — o original usa o {@code ModelWitch} tal e qual —, e o que muda de
 * uma para a outra é a pele. São cinco, e cada bruxa nasce com uma.
 *
 * <p>É o que faz um coven de seis parecer <b>seis pessoas</b> e não seis cópias, e por isso a foto é das cinco
 * lado a lado: uma sozinha não diria nada.
 */
public class OccultaCovenClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator @a");
            server.runCommand("time set noon");
            server.runCommand("tp @a ~ ~ ~ 0 0");

            // as cinco em fila, de frente, cada uma com a sua cara
            for (int i = 0; i < 5; i++) {
                server.runCommand("summon thaumcraft:coven_witch ~" + (i * 2 - 4) + " ~ ~7 "
                        + "{Cara:" + i + ",Rotation:[180f,0f],NoAI:1b}");
            }
            context.waitTicks(30);
            context.takeScreenshot("bruxas_do_coven");
        }
    }
}
