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
 * <p>Duas fotos: a torre inteira de fora, e o alto dela de perto — que é onde ficam o telhado de escadas, as
 * ameias e os guardas.
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

            server.runCommand("place template thaumcraft:village/watchtower 0 " + chão[0] + " 0");
            context.waitTicks(60);

            context.runOnClient(minecraft -> minecraft.options.renderDistance().set(12));
            server.runCommand("tp @a 22 " + (chão[0] + 14) + " 22 facing 4 " + (chão[0] + 10) + " 4");
            context.waitTicks(60);
            context.takeScreenshot("torre_de_vigia");

            // e o mirante de perto, que é onde ficam as ameias e os guardas
            server.runCommand("tp @a 4 " + (chão[0] + 18) + " 16 facing 4 " + (chão[0] + 17) + " 4");
            context.waitTicks(40);
            context.takeScreenshot("torre_de_vigia_alto");
        }
    }
}
