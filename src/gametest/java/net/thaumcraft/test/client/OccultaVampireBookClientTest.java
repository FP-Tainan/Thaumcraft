package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.thaumcraft.occulta.client.MarkupBookScreen;

/**
 * O <b>Livro do Vampiro</b> aberto, e o <b>Caixão</b> no chão.
 *
 * <p>O livro é a única coisa deste ramo que se <b>lê</b>, e o que ele faz na tela é o que ele faz no jogo:
 * um capítulo que peça mais páginas do que o exemplar tem <b>não abre</b>, e a seta fica apagada. Não há
 * texto nenhum a dizer "falta-te uma página" — há uma seta que não anda, e é só isso que o Witchery dá a
 * quem o lê.
 *
 * <p>E o Caixão é um modelo com <b>tampa que anda</b>: fechado é um bloco, aberto é uma banheira baixa, e a
 * tampa gira em volta da borda com a curva dos baús. Um número trocado não falha prova nenhuma — só põe a
 * tampa a abrir para o lado errado. Só se vê.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaVampireBookClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            // o livro com três páginas: lê-se até onde elas chegam
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new MarkupBookScreen("tc.vampirebook", 3, "toc")));
            context.waitTicks(10);
            context.takeScreenshot("livro_1_indice");

            // o rito desenhado, três folhas adiante
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new MarkupBookScreen("tc.vampirebook", 3, "ritual3")));
            context.waitTicks(10);
            context.takeScreenshot("livro_2_o_rito_desenhado");

            // e a gaiola, que o livro mostra antes de o jogador saber para quê
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new MarkupBookScreen("tc.vampirebook", 9, "maker2")));
            context.waitTicks(10);
            context.takeScreenshot("livro_3_a_gaiola");

            // o capítulo que o exemplar de três páginas não alcança: a seta fica apagada
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new MarkupBookScreen("tc.vampirebook", 1, "knockback")));
            context.waitTicks(10);
            context.takeScreenshot("livro_4_a_seta_apagada");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);

            // o Caixão fechado, que é um bloco inteiro
            server.runCommand("execute at @p run setblock ~ ~ ~3 thaumcraft:coffin[facing=north,part=foot]");
            server.runCommand("execute at @p run setblock ~ ~ ~2 thaumcraft:coffin[facing=north,part=head]");
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 25");
            context.waitTicks(20);
            context.takeScreenshot("caixao_1_fechado");

            // e aberto, com a tampa deitada para o lado
            server.runCommand(
                    "execute at @p run setblock ~ ~ ~3 thaumcraft:coffin[facing=north,part=foot,aberto=true]");
            server.runCommand(
                    "execute at @p run setblock ~ ~ ~2 thaumcraft:coffin[facing=north,part=head,aberto=true]");
            context.waitTicks(30);
            context.takeScreenshot("caixao_2_aberto");

            // o Coletor de Luz com o sol pela metade, e cheio
            server.runCommand("execute at @p run setblock ~2 ~ ~3 thaumcraft:daylight_collector[sol=7]");
            server.runCommand("execute at @p run setblock ~-2 ~ ~3 thaumcraft:daylight_collector[sol=15]");
            context.waitTicks(20);
            context.takeScreenshot("caixao_3_o_coletor");

            // e as coisas novas no inventário
            server.runCommand("give @p thaumcraft:vampire_book[thaumcraft:vampire_pages=9]");
            server.runCommand("give @p thaumcraft:torn_page");
            server.runCommand("give @p thaumcraft:quartz_sphere");
            server.runCommand("give @p thaumcraft:sun_grenade");
            server.runCommand("give @p thaumcraft:coffin");
            server.runCommand("give @p thaumcraft:daylight_collector");
            context.runOnClient(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON));
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("caixao_4_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
