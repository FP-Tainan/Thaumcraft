package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.thaumcraft.occulta.vampire.Vampire;
import net.thaumcraft.occulta.vampire.VampirePowers;

/**
 * A <b>forma de morcego</b> na tela, que não é um morcego: são <b>três</b>.
 *
 * <p>O original desenha o morcego de mentira três vezes — um no lugar do jogador e dois atrás, mais baixos,
 * menores e com as asas fora de compasso —, de modo que o que atravessa um vale no Witchery é uma
 * <b>nuvenzinha</b> de morcegos e não um bicho. Nenhuma linha do mod o diz: quem vira morcego descobre que
 * virou <b>vários</b>.
 *
 * <p>É por isso que esta fatia precisa de fotos. A conta de onde cada um fica sai de uma volta do vetor do
 * olhar que o autor pediu em graus a um método que a conta em radianos, e o resultado é simétrico
 * <b>por acaso</b>. Um número trocado não falha prova nenhuma: só põe um morcego no lugar errado. Só se vê.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaVampireBatClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode survival");
            server.runCommand("time set noon");
            server.runCommand("weather thunder");
            context.waitTicks(20);

            // um vampiro do décimo grau, de gente, visto por trás
            server.runOnServer(s -> {
                var quem = s.getPlayerList().getPlayers().getFirst();
                Vampire.levantaOTeto(quem, Vampire.TETO);
                Vampire.grau(quem, Vampire.TETO);
                Vampire.sangue(quem, Vampire.tetoDoSangue(quem));
                VampirePowers.escolhe(quem, VampirePowers.Poder.MORCEGO);
            });
            context.runOnClient(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK));
            context.waitTicks(20);
            context.takeScreenshot("morcego_1_de_gente");

            // e de morcego: os três, por trás
            server.runOnServer(s -> {
                var quem = s.getPlayerList().getPlayers().getFirst();
                VampirePowers.usa(quem.level(), quem);
            });
            context.waitTicks(30);
            server.runOnServer(s -> {
                var quem = s.getPlayerList().getPlayers().getFirst();
                if (!VampirePowers.emMorcego(quem)) throw new AssertionError("não virou morcego");
                if (!quem.getAbilities().mayfly) throw new AssertionError("ficou sem asas");
            });
            context.takeScreenshot("morcego_2_os_tres_por_tras");

            // de frente, que é onde se vê que os dois de trás ficam atrás
            context.runOnClient(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
            context.waitTicks(20);
            context.takeScreenshot("morcego_3_de_frente");

            /*
             * E no ar, que é onde a nuvenzinha se vê inteira: os dois de trás ficam seis décimos abaixo do
             * do meio, e por isso no chão eles se enterram. Voando, eles arrastam atrás dele.
             */
            context.runOnClient(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK));
            server.runCommand("tp @p ~ ~6 ~ 135 0");
            server.runOnServer(s -> {
                var quem = s.getPlayerList().getPlayers().getFirst();
                quem.getAbilities().flying = true;
                quem.onUpdateAbilities();
            });
            context.waitTicks(20);
            context.takeScreenshot("morcego_4_no_ar");

            // e o painel de comando com o Supremo escolhido, que é o único número além do sangue
            server.runOnServer(s -> {
                var quem = s.getPlayerList().getPlayers().getFirst();
                VampirePowers.tiraOMorcego(quem);
                VampirePowers.escolhe(quem, VampirePowers.Poder.SUPREMO);
                VampirePowers.dáOSupremo(quem, VampirePowers.Supremo.ENXAME);
            });
            context.runOnClient(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON));
            context.waitTicks(20);
            context.takeScreenshot("morcego_5_o_supremo_e_as_cargas");

            // e o morcego do enxame, que é um bicho de verdade
            server.runCommand("tp @p ~ ~ ~ 0 0");
            server.runCommand("summon thaumcraft:attack_bat ~ ~1.2 ~2 {NoAI:1b,BatFlags:0b}");
            context.waitTicks(20);
            context.takeScreenshot("morcego_6_o_do_enxame");
            context.runOnClient(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON));
            context.waitTicks(5);
        }
    }
}
