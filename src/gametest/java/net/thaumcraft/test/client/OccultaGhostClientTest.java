package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.ghost.SummonedUndeadEntity;

/**
 * Os três <b>fantasmas</b> do Braseiro, em fila, e os cinco jeitos que eles têm.
 *
 * <p>O que importa nesta tela é a <b>transparência</b>, porque é ela que diz o que cada um é:
 *
 * <ul>
 *   <li>o <b>Espectro apagado</b>, a quinze centésimos, que é como ele nasce: mal se vê que ali vem
 *       alguém;</li>
 *   <li>o <b>Espectro</b> aceso, a seis décimos, de braços estendidos para a frente;</li>
 *   <li>a <b>Banshee calada</b>, a sete décimos, de braços caídos e boca fechada — a mesma malha do
 *       Espectro, com uma bandeira trocada;</li>
 *   <li>a <b>Banshee gritando</b>, com a <b>boca aberta</b> na cara e os braços levantados de lado;</li>
 *   <li>e o <b>Poltergeist</b>, a quatro décimos, com os <b>quatro braços</b> compridos e as pernas de
 *       palito.</li>
 * </ul>
 *
 * <p>O Poltergeist aqui aparece <b>sem a poção</b>, de propósito: no jogo ele é invisível para sempre, e
 * esta é a única maneira de se ver se o boneco dele está certo.
 */
public class OccultaGhostClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator @p");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("difficulty peaceful");
            context.waitTicks(20);

            server.runOnServer(s -> {
                var level = s.overworld();
                var player = s.getPlayerList().getPlayers().getFirst();
                BlockPos meio = player.blockPosition();

                double[] onde = {-4.0, -2.0, 0.0, 2.0, 4.0};
                var quais = new net.minecraft.world.entity.EntityType<?>[]{
                        OccultaEntities.SPECTRE, OccultaEntities.SPECTRE,
                        OccultaEntities.BANSHEE, OccultaEntities.BANSHEE,
                        OccultaEntities.POLTERGEIST,
                };
                boolean[] apagado = {true, false, false, false, false};
                boolean[] gritando = {false, false, false, true, false};

                for (int i = 0; i < quais.length; i++) {
                    var bicho = quais[i].create(level, EntitySpawnReason.COMMAND);
                    if (!(bicho instanceof SummonedUndeadEntity fantasma)) continue;
                    fantasma.snapTo(meio.getX() + 0.5 + onde[i], meio.getY(), meio.getZ() + 7.0,
                            180.0f, 0.0f);
                    fantasma.setYBodyRot(180.0f);
                    fantasma.setYHeadRot(180.0f);
                    fantasma.apagado(apagado[i]);
                    fantasma.gritando(gritando[i]);
                    fantasma.setPersistenceRequired();
                    fantasma.setNoAi(true);
                    level.addFreshEntity(fantasma);
                }

                player.snapTo(meio.getX() + 0.5, meio.getY() + 1.0, meio.getZ(), 0.0f, 0.0f);
                player.setDeltaMovement(Vec3.ZERO);
            });
            context.waitTicks(40);
            context.takeScreenshot("os_tres_fantasmas");
        }
    }
}
