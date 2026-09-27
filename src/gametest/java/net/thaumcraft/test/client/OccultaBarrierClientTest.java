package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.thaumcraft.occulta.rite.ActiveRite;
import net.thaumcraft.occulta.rite.Rites;

import java.util.List;
import java.util.UUID;

/**
 * A cúpula de barreira vista: o chão, a parede e o teto que o rito desenha em volta do círculo.
 */
public class OccultaBarrierClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");

            server.runOnServer(s -> {
                ServerLevel mundo = s.overworld();
                var jogador = s.getPlayerList().getPlayers().getFirst();
                BlockPos meio = jogador.blockPosition().offset(0, 1, 10);

                // o portátil não come poder, e por isso corre sem altar
                var qual = new Rites.Barrier(5, 5, 0.0f, false, 60);
                var rito = new ActiveRite("tc.rite.barrierportable", qual, List.of(), UUID.randomUUID(), 0);
                qual.steps(0).getFirst().run(mundo, meio, 20L, rito);

                jogador.teleportTo(mundo, jogador.getX(), jogador.getY() + 2.0, jogador.getZ(),
                        java.util.Set.of(), 0.0f, 10.0f, false);
            });
            context.waitTicks(20);
            context.takeScreenshot("ao_barreira");
        }
    }
}
