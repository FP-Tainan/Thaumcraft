package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * As <b>quatro plantas do ofício</b> ao lado das quatro do jogo de que elas são cópia.
 *
 * <p>A tela que importa é a de que <b>não se distinguem</b>. No original as quatro usam as texturas do jogo,
 * e é de propósito: uma teia de bruxa tem de parecer uma teia, um cato de bruxa um cato. Quem vê a diferença
 * é quem tenta rasgar a teia, quem encosta no cato ou quem volta ao pântano um minuto depois e encontra o
 * nenúfar <b>onde não estava</b>.
 *
 * <p>Por isso cada par é uma do jogo à esquerda e a de bruxa à direita, e os pares têm de sair iguais.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaWitchPlantsClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative @a");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            // um chão de pedra limpo, com uma parede ao fundo para os dois cipós
            server.runCommand("execute at @p run fill ~-9 ~-1 ~3 ~9 ~-1 ~22 minecraft:stone");
            server.runCommand("execute at @p run fill ~-9 ~ ~3 ~9 ~4 ~22 minecraft:air");

            // os dois catos, cada um na sua coluna de três
            /*
             * O do jogo precisa de <b>areia</b> e de não ter vizinho sólido; o de bruxa não precisa de nada.
             * Sem a areia o do jogo cai e some, e o par deixa de ser par — por isso ela está aqui.
             */
            server.runCommand("execute at @p run setblock ~5 ~-1 ~7 minecraft:sand");
            for (int i = 0; i < 3; i++) {
                server.runCommand("execute at @p run setblock ~5 ~" + i + " ~7 minecraft:cactus");
                server.runCommand("execute at @p run setblock ~3 ~" + i + " ~7 thaumcraft:witch_cactus");
            }

            // as duas teias, uma ao lado da outra
            server.runCommand("execute at @p run setblock ~1 ~1 ~7 minecraft:cobweb");
            server.runCommand("execute at @p run setblock ~-1 ~1 ~7 thaumcraft:witch_web");

            // e os dois cipós, pendurados numa parede que está atrás deles
            server.runCommand("execute at @p run fill ~-3 ~ ~8 ~-3 ~2 ~8 minecraft:stone");
            server.runCommand("execute at @p run fill ~-5 ~ ~8 ~-5 ~2 ~8 minecraft:stone");
            for (int i = 0; i <= 2; i++) {
                server.runCommand("execute at @p run setblock ~-3 ~" + i + " ~7 minecraft:vine[south=true]");
                server.runCommand("execute at @p run setblock ~-5 ~" + i
                        + " ~7 thaumcraft:witch_vine[facing=south]");
            }

            server.runCommand("execute at @p run tp @p ~ ~ ~2 0 2");
            context.waitTicks(20);
            context.takeScreenshot("plantas_1_os_pares");

            // o poço dos nenúfares: o do jogo de um lado, o de bruxa do outro, com três para gastar
            server.runCommand("execute at @p run fill ~-6 ~-1 ~10 ~6 ~-1 ~20 minecraft:stone");
            server.runCommand("execute at @p run fill ~-6 ~ ~10 ~6 ~ ~20 minecraft:water");
            server.runCommand("execute at @p run setblock ~3 ~1 ~15 minecraft:lily_pad");
            server.runCommand("execute at @p run setblock ~-3 ~1 ~15 thaumcraft:witch_lily_pad[growth=3]");
            context.waitTicks(20);
            context.takeScreenshot("plantas_2_os_dois_nenufares");

            // e um minuto depois: o do jogo onde estava, o de bruxa virado tapete
            context.waitTicks(180);
            server.runCommand("gamemode spectator @a");
            server.runCommand("execute at @p run tp @p ~ ~14 ~13 0 88");
            context.waitTicks(20);
            context.takeScreenshot("plantas_3_o_tapete_de_cima");
            server.runCommand("gamemode creative @a");
            context.waitTicks(5);
        }
    }
}
