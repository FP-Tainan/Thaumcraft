package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.spirit.LostSoulEntity;

/**
 * As três <b>Almas Perdidas</b>, lado a lado, uma de cada feitio.
 *
 * <p>É a única coisa desta fatia que precisa de ser vista, e precisa por uma razão de jogo: a <b>cor</b>
 * de uma alma é o que diz, à distância, qual arma serve contra ela. Vermelha é fogo, verde é golpe, azul
 * é magia — e quem não as distingue bate na errada a tarde toda.
 *
 * <p>O corpo é o do Espírito, que já estava portado: a mesma lanterna de papel translúcida. O que muda é
 * o tingimento, e é isso que a foto guarda.
 */
public class OccultaLostSoulClientTest implements FabricClientGameTest {
    /** Quanto espaço vai entre uma e a seguinte. */
    private static final double ENTRE = 1.6;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator");
            server.runCommand("time set noon");
            server.runCommand("difficulty peaceful");
            server.runCommand("gamerule doMobSpawning false");

            server.runOnServer(s -> {
                var mundo = s.overworld();
                var jogador = s.getPlayerList().getPlayers().getFirst();
                BlockPos meio = jogador.blockPosition();

                int[] feitios = {LostSoulEntity.FOGO, LostSoulEntity.GOLPE, LostSoulEntity.MAGIA};
                for (int qual = 0; qual < feitios.length; qual++) {
                    var alma = OccultaEntities.LOST_SOUL.create(mundo, EntitySpawnReason.COMMAND);
                    if (alma == null) continue;
                    alma.snapTo(meio.getX() + 0.5 + (qual - 1) * ENTRE, meio.getY() + 1.2,
                            meio.getZ() + 4.5, 180.0f, 0.0f);
                    alma.setPersistenceRequired();
                    alma.setNoAi(true);
                    alma.setSilent(true);
                    alma.feitioDaAlma(feitios[qual]);
                    mundo.addFreshEntity(alma);
                }

                jogador.snapTo(meio.getX() + 0.5, meio.getY() + 1.2, meio.getZ() + 0.5, 0.0f, 0.0f);
                jogador.setDeltaMovement(Vec3.ZERO);
            });
            // o pó delas sai de segundo em segundo: é preciso dar-lhe esse segundo
            context.waitTicks(60);
            context.takeScreenshot("as_tres_almas_perdidas");
        }
    }
}
