package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/** A linha da essência montada: forno, alambique, tubos e jarros, para ver se tudo aparece direito. */
public class EssentiaClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");

            // um chão de pedra clara para a linha não sumir no capim, e a vista de frente
            server.runCommand("execute at @p run fill ~-3 ~-1 ~2 ~3 ~-1 ~6 minecraft:smooth_stone");
            server.runCommand("execute at @p run tp @s ~ ~ ~-1 0 5");

            // o forno com a boca virada para quem olha, e um alambique em cima
            server.runCommand("execute at @p run setblock ~1 ~ ~4 thaumcraft:alchemical_furnace[facing=north,lit=true]");
            server.runCommand("execute at @p run setblock ~1 ~1 ~4 thaumcraft:alembic");
            // o cano saindo do alambique e descendo até dois jarros
            server.runCommand("execute at @p run setblock ~ ~1 ~4 thaumcraft:tube");
            server.runCommand("execute at @p run setblock ~-1 ~1 ~4 thaumcraft:tube");
            server.runCommand("execute at @p run setblock ~ ~ ~4 thaumcraft:jar");
            server.runCommand("execute at @p run setblock ~-1 ~ ~4 thaumcraft:jar");
            context.waitTicks(40);
            context.takeScreenshot("essencia_linha_vazia");

            // com essência dentro, para ver a névoa e o símbolo no vidro
            server.runCommand("execute at @p run data merge block ~ ~ ~4 {aspect:\"ignis\",amount:48}");
            server.runCommand("execute at @p run data merge block ~-1 ~ ~4 {aspect:\"aqua\",label:\"aqua\",amount:20}");
            context.waitTicks(40);
            context.takeScreenshot("essencia_linha_cheia");

            // e a tela do forno, que mostra o que ele já guardou
            server.runCommand("execute at @p run data merge block ~1 ~ ~4 {aspects:{terra:12,ignis:7,aqua:3}}");
            server.runCommand("give @p thaumcraft:alembic");
            server.runCommand("give @p thaumcraft:tube 8");
            server.runCommand("give @p thaumcraft:jar");
            context.waitTicks(20);
            context.takeScreenshot("essencia_pecas_na_mao");

            context.runOnClient(minecraft -> {
                var pos = minecraft.player.blockPosition().offset(0, 0, 4);
                var found = minecraft.level.getBlockEntity(pos);
                System.out.println("[ESSENCIA] em " + pos + " achei " + found
                        + (found instanceof net.thaumcraft.block.entity.JarBlockEntity jar
                           ? " aspecto=" + jar.aspect() + " quanto=" + jar.amount()
                             + " rotulo=" + jar.label()
                           : ""));
            });
        }
    }
}
