package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * A aldeia maior, vista.
 *
 * <p>O tamanho de uma aldeia é coisa que se <b>vê</b>, e a prova de servidor só sabe dizer que o número no
 * registro é oito. Esta põe uma aldeia no chão com {@code /place structure} — que é determinístico, ao contrário
 * de procurar uma pelo mundo — e a fotografa de viés e de cima.
 *
 * <p><b>Três coisas que esta prova aprendeu à força</b>, e que ficam escritas porque custaram foto:
 *
 * <ol>
 *   <li><b>O jogador tem de ir primeiro.</b> O {@code /place} precisa do trecho carregado, e quem nasce longe
 *       de zero não o tem — a aldeia não aparece e a foto sai de um descampado.</li>
 *   <li><b>A altura se pergunta ao mapa de alturas.</b> Posta num número fixo, a aldeia nasce enterrada
 *       conforme o terreno do mundo sorteado.</li>
 *   <li><b>{@code gamemode} pede alvo.</b> O comando corre a partir do console do servidor, que não é jogador
 *       nenhum; sem {@code @a} ele não faz nada, e quem for posto no ar <b>cai e morre</b>.</li>
 * </ol>
 */
public class OccultaVillageSpreadClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator @a");
            server.runCommand("time set noon");
            server.runCommand("weather clear");

            // o jogador vai primeiro, para o trecho ficar carregado
            server.runCommand("tp @a 0 120 0");
            context.waitTicks(60);

            // onde é o chão naquele ponto
            int[] chão = new int[1];
            server.runOnServer(s -> {
                var level = s.overworld();
                chão[0] = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, 0, 0);
            });

            server.runCommand("place structure minecraft:village_plains 0 " + chão[0] + " 0");
            context.waitTicks(60);

            // de viés e de cima, apontada ao centro: o "facing" do tp aponta a câmera a um ponto, que é mais
            // seguro do que acertar passo e volta à mão
            context.runOnClient(minecraft -> minecraft.options.renderDistance().set(16));
            server.runCommand("tp @a 45 " + (chão[0] + 75) + " 45 facing 0 " + chão[0] + " 0");
            context.waitTicks(100);
            context.takeScreenshot("aldeia_maior");

        }
    }
}
