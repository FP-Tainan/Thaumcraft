package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A <b>chuva de cozimento</b> na tela.
 *
 * <p>É a imagem do rito de poça, e a razão de ele existir: um caldeirão numa cave, e cem casas acima dele
 * começam a cair gotas do caldo sobre um campo. Nenhuma prova de servidor mostra isso — ela conta as
 * gotas, e contar gotas não é ver chover.
 *
 * <p>São duas fotos, e são precisas as duas: uma <b>de longe</b>, que mostra que elas caem espalhadas e
 * não em fila, e uma <b>de perto</b>, que mostra o que cada uma é — um frasco pequeno, da cor do caldo.
 * De longe elas são pontos, e um ponto não prova desenho nenhum.
 */
public class OccultaBrewRainClientTest implements FabricClientGameTest {
    /** Quantas gotas se largam, e de que altura. */
    private static final int GOTAS = 80;
    private static final int ALTURA = 18;

    /** E o raio por onde se espalham. */
    private static final int RAIO = 6;

    /** A cor do caldo desta chuva: um verde de poção, para se ver contra a relva. */
    private static final int COR = 0x55FF55;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator");
            context.waitTicks(20);
            server.runCommand("time set noon");
            server.runCommand("execute at @p run summon minecraft:marker ~ ~ ~ {Tags:[\"chuva\"]}");
            context.waitTicks(10);

            /*
             * A câmara a leste, olhando a oeste e <b>para cima</b>: as gotas caem de vinte casas, e uma
             * câmara ao nível do chão a olhar em frente não apanha nenhuma.
             */
            server.runCommand("execute at @e[tag=chuva,limit=1] run tp @p ~12 ~9 ~ 90 -10");
            context.waitTicks(10);

            /*
             * As gotas largam-se em duas levas, com uma pausa no meio: assim a foto apanha umas no alto e
             * outras já perto do chão, que é o que uma chuva parece.
             */
            for (int volta = 0; volta < 2; volta++) {
                for (int n = 0; n < GOTAS / 2; n++) {
                    int x = (n * 7 % (2 * RAIO)) - RAIO;
                    int z = (n * 11 % (2 * RAIO)) - RAIO;
                    /*
                     * A gota leva um frasco consigo. Sem ele a pilha dela fica <b>vazia</b> e o
                     * desenhista não tem o que desenhar: a chuva cai e não se vê.
                     */
                    server.runCommand("execute at @e[tag=chuva,limit=1] run summon"
                            + " thaumcraft:brew_droplet ~" + x + " ~" + ALTURA + " ~" + z
                            + " {Item:{id:\"thaumcraft:brew\",count:1,components:"
                            + "{\"minecraft:dyed_color\":" + COR + "}}}");
                }
                context.waitTicks(9);
            }
            context.waitTicks(2);
            context.takeScreenshot("a_chuva_de_cozimento");

            /*
             * E uma de perto. As gotas largam-se <b>à frente da própria câmara</b>, com coordenadas
             * locais, e não à volta do marco: é a única maneira de garantir que alguma delas passa perto
             * o bastante para se ver que é um frasco e não um ponto.
             */
            server.runCommand("execute at @e[tag=chuva,limit=1] run tp @p ~ ~12 ~ 90 0");
            for (int n = 0; n < 12; n++) {
                server.runCommand("execute at @p run summon thaumcraft:brew_droplet ^"
                        + (n % 3 - 1) + " ^" + (n % 4) + " ^" + (3 + n % 3)
                        + " {Item:{id:\"thaumcraft:brew\",count:1,components:"
                        + "{\"minecraft:dyed_color\":" + COR + "}}}");
            }
            context.waitTicks(3);
            context.takeScreenshot("uma_gota_de_perto");
        }
    }
}
