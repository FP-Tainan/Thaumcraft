package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * O <b>Demônio</b> e o <b>Coração</b> dele na tela.
 *
 * <p>O demônio é a coisa mais alta que este mod põe no chão sem ser um chefe, e o que o faz ser lido como
 * demônio e não como golem pintado são <b>três coisas</b>, todas visuais: os <b>chifres</b> de oito de
 * altura, os <b>dentes</b> que descem do lábio de cima, e as <b>asas chatas</b> abertas em ângulos
 * diferentes uma da outra. Nenhuma delas falha prova nenhuma. Só se vê.
 *
 * <p>E as asas são o que mais se arrisca a sair errado: são <b>chapas sem grossura</b>, e uma chapa posta no
 * ângulo trocado desaparece quando se olha de frente para ela. É por isso que há uma foto de frente e uma
 * de lado.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaDemonClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("gamerule doMobSpawning false");
            context.waitTicks(20);

            // ele, de frente, parado — os chifres, os dentes e o focinho
            server.runCommand("execute at @p run summon thaumcraft:demon ~ ~ ~5 "
                    + "{NoAI:1b,PersistenceRequired:1b,Rotation:[180f,0f]}");
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 0");
            context.waitTicks(30);
            context.takeScreenshot("demonio_1_de_frente");

            /*
             * E de lado, que é onde as duas asas se abrem. A câmara se põe a partir <b>do demônio</b> e não
             * de onde ela está: um salto relativo depois do outro se soma, e a segunda foto saía para o
             * campo vazio.
             */
            server.runCommand("execute at @e[type=thaumcraft:demon,limit=1] run tp @p ~5 ~ ~ 90 0");
            context.waitTicks(20);
            context.takeScreenshot("demonio_2_de_lado");

            // de trás, que é onde elas se prendem
            server.runCommand("execute at @e[type=thaumcraft:demon,limit=1] run tp @p ~ ~ ~5 180 0");
            context.waitTicks(20);
            context.takeScreenshot("demonio_3_de_tras");

            // o Coração no chão, que bate e larga fogo
            server.runCommand("kill @e[type=thaumcraft:demon]");
            server.runCommand("execute at @p run setblock ~ ~ ~3 minecraft:stone_bricks");
            server.runCommand("execute at @p run setblock ~ ~1 ~3 thaumcraft:demon_heart[facing=south]");
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 0");
            context.waitTicks(40);
            context.takeScreenshot("demonio_4_o_coracao_no_chao");

            // e mais perto, que é onde o músculo inchando se vê
            server.runCommand("execute at @p run tp @p ~ ~ ~1.6 0 10");
            context.waitTicks(13);
            context.takeScreenshot("demonio_5_o_coracao_de_perto");

            // e o Coração na mão
            server.runCommand("give @p thaumcraft:demon_heart");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("demonio_6_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
