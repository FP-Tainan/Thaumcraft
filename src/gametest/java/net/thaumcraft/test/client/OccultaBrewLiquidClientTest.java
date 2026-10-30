package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A <b>poça de cozimento</b> correndo num chão de pedra.
 *
 * <p>A tela que importa é a dos <b>seis degraus</b>: a poça não se desenha toda da mesma altura. A nascente
 * tem catorze pixels, e cada passo que ela dá para o lado tira dois, até o fio de quatro que não vai mais
 * longe. Vistos de rente ao chão os seis degraus leem-se de uma vez, e é por eles que se vê de onde ela veio.
 *
 * <p>E ela abre para os <b>quatro lados ao mesmo tempo</b>: num chão plano nenhum deles leva mais perto de um
 * buraco que os outros. Cada anel abre outra vez, e o que fica no fim é um <b>losango</b> — de cima, ele é a
 * prova disso.
 *
 * <p>A cor é a da água do caldeirão, que é a que o original dá a uma poça sem cozimento nenhum dentro — uma
 * posta por comando não traz receita nenhuma.
 *
 * <p>A nascente <b>seca sozinha</b>, e por isso cada foto arma a poça outra vez e espera só o que ela precisa
 * para dar os cinco passos: cinco batidas por passo, vinte e cinco ao todo.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaBrewLiquidClientTest implements FabricClientGameTest {
    /** As batidas que ela precisa para dar os cinco passos, com uma de folga. */
    private static final int CORRE = 30;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative @a");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            // um chão de pedra largo, limpo por cima: a poça precisa de cinco casas para cada lado
            server.runCommand("execute at @p run fill ~-9 ~-1 ~3 ~9 ~-1 ~21 minecraft:stone");
            server.runCommand("execute at @p run fill ~-9 ~ ~3 ~9 ~3 ~21 minecraft:air");

            // de rente ao chão, com a beira da poça a três casas: os seis degraus de uma vez
            server.runCommand("execute at @p run tp @p ~ ~ ~4 0 12");
            arma(server, context, "~-6 ~ ~2 ~6 ~ ~14", "~ ~ ~8");
            context.takeScreenshot("poca_1_os_seis_degraus");

            /*
             * Doze casas acima da nascente, olhando para baixo: o losango. E de espectador, porque em criativo
             * quem é posto no ar cai — e cairia dentro da própria poça, que foi o que deu da primeira vez.
             */
            server.runCommand("gamemode spectator @a");
            server.runCommand("execute at @p run tp @p ~ ~12 ~8 0 88");
            arma(server, context, "~-6 ~-12 ~-6 ~6 ~-12 ~6", "~ ~-12 ~");
            context.takeScreenshot("poca_2_o_losango_de_cima");

            // e de noite, de rente ao chão outra vez, para se ver que ela não dá luz nenhuma
            server.runCommand("gamemode creative @a");
            server.runCommand("time set midnight");
            server.runCommand("execute at @p run tp @p ~ ~-12 ~-8 0 12");
            arma(server, context, "~-6 ~ ~2 ~6 ~ ~14", "~ ~ ~8");
            context.takeScreenshot("poca_3_de_noite_sem_luz");
            server.runCommand("time set noon");
            context.waitTicks(5);
        }
    }

    /** Limpa o que sobrou da poça de antes, põe a nascente e deixa-a correr os cinco passos. */
    private static void arma(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
                             ClientGameTestContext context, String região, String onde) {
        server.runCommand("execute at @p run fill " + região
                + " minecraft:air replace thaumcraft:brew_liquid");
        server.runCommand("execute at @p run setblock " + onde + " thaumcraft:brew_liquid");
        context.waitTicks(CORRE);
    }
}
