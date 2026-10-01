package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.server.level.ServerLevel;
import net.thaumcraft.arcana.ShootingStarEntity;

/**
 * A Estrela Cadente, vista.
 *
 * <p>Ela <b>não se desenha</b> — é o que o original faz, e por isso tudo o que há para ver dela é o
 * <b>rastro</b>: brasas azuladas ao longo do caminho, uma a cada décimo de bloco que ela desce. Se essa conta
 * estivesse errada, a Estrela seria uma coisa invisível que mata sem aviso, e nenhuma prova de servidor
 * daria por isso.
 *
 * <p>A foto é tirada com ela no meio da queda, contra o céu de meia-noite.
 */
public class ArcanaStarClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator");
            server.runCommand("time set midnight");
            server.runCommand("tp @p 0 30 -15 0 -25");

            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                var level = (ServerLevel) jogador.level();
                var estrela = new ShootingStarEntity(level, jogador, 30.0f);
                estrela.snapTo(0.0, 45.0, 0.0);
                level.addFreshEntity(estrela);
            });

            // tempo para ela ganhar velocidade e deixar rastro, mas não para chegar ao chão
            context.waitTicks(8);
            context.takeScreenshot("aa_estrela_cadente");
        }
    }
}
