package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * As <b>quatro Mais Belas</b> em fila, e o Coração de Ouro na mão.
 *
 * <p>Elas são quatro peles e nada mais — não há modelo novo, não há animação, não há efeito. E é
 * exatamente por isso que só uma foto as prova: um tipo lido ao contrário dá a pele errada, e nada no
 * servidor dá por isso.
 *
 * <p>A foto tem de mostrar as <b>quatro ao lado de Elle</b>, que é a quinta pele do mesmo bicho: se as
 * cinco saírem iguais, o tipo não chegou ao desenhista.
 */
public class OccultaFairestClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            context.waitTicks(20);

            server.runCommand("execute at @p run summon minecraft:marker ~ ~ ~ {Tags:[\"belas\"]}");

            // Elle à esquerda, e as quatro ao lado dela
            põe(server, "~-3 ~ ~6", 0);
            for (int tipo = 1; tipo <= 4; tipo++) {
                põe(server, "~" + (tipo * 2 - 3) + " ~ ~6", tipo);
            }
            /*
             * A câmara vai ao sul delas. Com NoAI o <b>corpo</b> fica sempre virado a sul, e a cabeça segue
             * a rotação do invocar: pondo-a a zero, as duas olham para o mesmo lado e vê-se a cara.
             */
            olha(server, "~1 ~ ~12 180 0");
            context.waitTicks(40);
            context.takeScreenshot("as_quatro_mais_belas");

            // e o coração que cai delas
            olha(server, "~40 ~ ~40 0 0");
            server.runCommand("item replace entity @p weapon.mainhand with thaumcraft:heart_of_gold");
            context.waitTicks(20);
            context.takeScreenshot("o_coracao_de_ouro");
        }
    }

    private static void põe(TestServerContext server, String onde, int tipo) {
        server.runCommand("execute at @e[tag=belas,limit=1] run summon thaumcraft:follower " + onde
                + " {NoAI:1b,Rotation:[0f,0f],Tipo:" + tipo + "}");
    }

    private static void olha(TestServerContext server, String resto) {
        server.runCommand("execute at @e[tag=belas,limit=1] run tp @p " + resto);
    }
}
