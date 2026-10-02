package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.rite.ActiveRite;
import net.thaumcraft.occulta.rite.Rites;

import java.util.List;

/**
 * O vulcão visto: o cone erguido do chão, com a lava a subir por dentro e a cratera no alto.
 */
public class OccultaVolcanoClientTest implements FabricClientGameTest {
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
                BlockPos meio = jogador.blockPosition().offset(0, 0, 18);

                // uma poça de lava por baixo, que é o que o rito exige
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        mundo.setBlockAndUpdate(meio.offset(dx, -3, dz), Blocks.LAVA.defaultBlockState());
                    }
                }

                // e se corre o rito inteiro de uma vez, que na prova não há tempo de o ver crescer
                var qual = new Rites.Volcano(6, 6);
                var rito = new ActiveRite("tc.rite.volcano", qual, List.of(), null, 0);
                var passo = qual.steps(0).getFirst();
                for (int i = 0; i < 40; i++) {
                    if (passo.run(mundo, meio, 15L * (i + 1), rito)
                            == net.thaumcraft.occulta.rite.RiteStep.Result.COMPLETED) {
                        break;
                    }
                }

                jogador.teleportTo(mundo, jogador.getX(), jogador.getY() + 6.0, jogador.getZ(),
                        java.util.Set.of(), 0.0f, 10.0f, false);
            });
            context.waitTicks(60);
            context.takeScreenshot("ao_vulcao");
        }
    }
}
