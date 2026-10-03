package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.thaumcraft.occulta.vampire.Vampire;
import net.thaumcraft.occulta.vampire.VampirePowers;

/**
 * A <b>barra de sangue</b> na tela, que é o painel de comando inteiro de um vampiro.
 *
 * <p>Ele não tem menu, não tem livro aberto, não tem roda: tem um número que desce e uma palavra que diz o
 * que o clique vai fazer. Se a barra não aparecer, a vampirice não existe para quem joga — por mais certa que
 * a conta esteja.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaVampirePlayerClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set midnight");
            context.waitTicks(20);

            // de gente, para se ver que não há barra nenhuma
            context.takeScreenshot("vampiro_1_de_gente");

            // e de vampiro, com o sangue cheio
            server.runOnServer(s -> {
                var quem = s.getPlayerList().getPlayers().getFirst();
                Vampire.levantaOTeto(quem, Vampire.TETO);
                Vampire.grau(quem, 5);
                Vampire.sangue(quem, Vampire.tetoDoSangue(quem));
            });
            context.waitTicks(20);
            context.takeScreenshot("vampiro_2_sangue_cheio");

            // com a sede a apertar, que é onde a barra diz alguma coisa
            server.runOnServer(s -> {
                var quem = s.getPlayerList().getPlayers().getFirst();
                Vampire.sangue(quem, Vampire.tetoDoSangue(quem) / 8);
                VampirePowers.escolhe(quem, VampirePowers.Poder.BEBER);
            });
            context.waitTicks(20);
            context.takeScreenshot("vampiro_3_com_sede_e_o_poder");

            // e de volta a gente, para se ver que a barra some
            server.runOnServer(s -> Vampire.grau(s.getPlayerList().getPlayers().getFirst(), 0));
            context.waitTicks(20);
            context.takeScreenshot("vampiro_4_de_volta");
        }
    }
}
