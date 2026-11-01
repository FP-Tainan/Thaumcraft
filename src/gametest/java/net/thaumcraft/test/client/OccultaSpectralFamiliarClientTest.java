package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * O <b>Familiar Espectral</b> na tela: um porco que não está bem ali.
 *
 * <p>Ele não tem modelo nem pele próprios — é o porco do jogo, desenhado translúcido e tingido. Toda a
 * fatia visual dele está nessas duas coisas, e nenhuma delas acusa numa prova de servidor: um tingimento
 * perdido dá um porco normal, e um porco normal não se distingue de um bug.
 *
 * <p>Os três lado a lado numa foto só: o <b>parado</b>, o que está <b>à procura</b>, e o porco do jogo.
 * É assim que as duas cores se comparam — uma ao lado da outra, e as duas ao lado do que elas tingem.
 */
public class OccultaSpectralFamiliarClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            context.waitTicks(20);
            server.runCommand("time set noon");
            context.waitTicks(10);

            server.runCommand("execute at @p run summon minecraft:marker ~ ~ ~ {Tags:[\"porco\"]}");

            // os dois familiares e o porco do jogo, em fila
            põe(server, "thaumcraft:spectral_familiar ~-3 ~ ~5 {NoAI:1b,Procura:-1}");
            põe(server, "thaumcraft:spectral_familiar ~ ~ ~5 {NoAI:1b,Procura:0}");
            põe(server, "minecraft:pig ~3 ~ ~5 {NoAI:1b}");
            olha(server, "~ ~1 ~9 180 10");
            context.waitTicks(40);
            context.takeScreenshot("os_dois_familiares_e_um_porco");
        }
    }

    private static void põe(TestServerContext server, String resto) {
        server.runCommand("execute at @e[tag=porco,limit=1] run summon " + resto);
    }

    private static void olha(TestServerContext server, String resto) {
        server.runCommand("execute at @e[tag=porco,limit=1] run tp @p " + resto);
    }
}
