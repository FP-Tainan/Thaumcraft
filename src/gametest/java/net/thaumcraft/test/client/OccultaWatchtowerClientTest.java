package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * A Torre de Vigia, vista.
 *
 * <p>O molde dela foi <b>gerado</b> a partir do código do original, chamada por chamada. Uma prova de servidor
 * só sabe dizer que ele carrega e que mede nove por vinte e quatro por nove; se o telhado estiver virado do
 * avesso ou a escada de mão na parede errada, ela passa na mesma. Esta é a que julga isso.
 *
 * <p>Três fotos: a torre inteira de fora, o mirante de perto com os guardas, e a <b>torre do deserto</b> ao
 * lado — que é arenito e bétula, porque o Witchery re-veste a aldeia conforme o bioma.
 */
public class OccultaWatchtowerClientTest implements FabricClientGameTest {
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

            int[] chão = new int[1];
            server.runOnServer(s -> {
                var level = s.overworld();
                chão[0] = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, 0, 0);
            });

            server.runCommand("place template thaumcraft:village/watchtower_plains 0 " + chão[0] + " 0");
            context.waitTicks(60);

            context.runOnClient(minecraft -> minecraft.options.renderDistance().set(12));
            server.runCommand("tp @a 22 " + (chão[0] + 14) + " 22 facing 4 " + (chão[0] + 10) + " 4");
            context.waitTicks(60);
            context.takeScreenshot("torre_de_vigia");

            // e o mirante de perto, que é onde ficam as ameias e os guardas
            server.runCommand("tp @a 4 " + (chão[0] + 18) + " 16 facing 4 " + (chão[0] + 17) + " 4");
            context.waitTicks(40);
            context.takeScreenshot("torre_de_vigia_alto");

            // e a do deserto, que é arenito e bétula
            server.runCommand("place template thaumcraft:village/watchtower_desert 20 " + chão[0] + " 0");
            context.waitTicks(40);
            server.runCommand("tp @a 42 " + (chão[0] + 14) + " 22 facing 24 " + (chão[0] + 10) + " 4");
            context.waitTicks(60);
            context.takeScreenshot("torre_de_vigia_deserto");

            // e o Forte, que é a maior peça da aldeia: dezessete por vinte e sete por dezessete
            server.runCommand("place template thaumcraft:village/keep_plains -40 " + chão[0] + " 0");
            context.waitTicks(60);
            server.runCommand("tp @a -8 " + (chão[0] + 26) + " 30 facing -32 " + (chão[0] + 10) + " 8");
            context.waitTicks(80);
            context.takeScreenshot("forte");

            // e o Boticário, que é a casa com porta, placa e morador
            server.runCommand("place template thaumcraft:village/apothecary_plains 0 " + chão[0] + " -30");
            context.waitTicks(60);
            server.runCommand("tp @a 2 " + (chão[0] + 5) + " -38 facing 3 " + (chão[0] + 3) + " -29");
            context.waitTicks(80);
            context.takeScreenshot("boticario");

            // e a Livraria. O que ela tem de seu são os quatro quadros na parede do fundo, e eles ficam
            // dentro de uma loja fechada: para a foto tira-se o telhado com um fill e olha-se de cima, que é
            // mais honesto do que acertar uma câmera entre as paredes.
            server.runCommand("place template thaumcraft:village/bookshop_plains 20 " + chão[0] + " -30");
            context.waitTicks(60);
            server.runCommand("fill 20 " + (chão[0] + 5) + " -30 30 " + (chão[0] + 9) + " -21 air");
            context.waitTicks(20);
            server.runCommand("tp @a 25 " + (chão[0] + 11) + " -32 facing 25 " + (chão[0] + 3) + " -25");
            context.waitTicks(60);
            context.takeScreenshot("livraria");
        }
    }
}
