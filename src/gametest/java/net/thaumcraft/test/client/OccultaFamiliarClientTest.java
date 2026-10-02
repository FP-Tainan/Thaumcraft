package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Os três familiares, vistos.
 *
 * <p>O sapo e a coruja são modelos <b>traduzidos caixa por caixa</b> do {@code ModelToad} e do {@code ModelOwl}
 * do Witchery, e uma prova de servidor não sabe se uma caixa ficou do lado errado. Esta é a que julga isso.
 *
 * <p>O <b>gato é o do próprio jogo</b> — o original aceita a jaguatirica dele, e quem herdou esse papel hoje é
 * o gato, que tem a variante preta que um gato de bruxa pede. Ele entra na foto para se ver que os três são
 * família: dois bichos do ofício e um do jogo, lado a lado.
 */
public class OccultaFamiliarClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator @a");
            server.runCommand("time set noon");
            server.runCommand("tp @p ~ ~-0.6 ~ 0 5");

            server.runCommand("summon thaumcraft:toad ~-2 ~ ~3 {NoAI:1b,Rotation:[180f,0f]}");
            server.runCommand("summon thaumcraft:owl ~ ~ ~3 {NoAI:1b,Rotation:[180f,0f]}");
            server.runCommand("summon minecraft:cat ~2 ~ ~3 "
                    + "{NoAI:1b,Rotation:[180f,0f],variant:\"minecraft:black\"}");
            context.waitTicks(30);
            context.takeScreenshot("familiares");

        }
    }
}
