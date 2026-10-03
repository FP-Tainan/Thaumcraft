package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;

/**
 * As três caras do Caçador de Bruxas, lado a lado.
 *
 * <p>É o chapéu que importa nesta tela: <b>aba de catorze</b>, maior que a das roupas que se podem vestir, e o
 * casaco que desce até ao joelho. São as três caixas a mais do {@code ModelWitchHunter}, e é por elas que um
 * caçador se reconhece de longe.
 */
public class OccultaWitchHunterClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator");
            server.runCommand("time set noon");
            server.runCommand("difficulty peaceful");

            server.runOnServer(s -> {
                var level = s.overworld();
                var player = s.getPlayerList().getPlayers().getFirst();
                BlockPos meio = player.blockPosition();

                // os três, de frente para quem olha
                for (int qual = 0; qual < 3; qual++) {
                    var caçador = OccultaEntities.WITCH_HUNTER.create(level, EntitySpawnReason.COMMAND);
                    if (caçador == null) continue;
                    caçador.snapTo(meio.getX() + 0.5 + (qual - 1) * 1.6, meio.getY(), meio.getZ() + 5.5,
                            180.0f, 0.0f);
                    caçador.finalizeSpawn(level, level.getCurrentDifficultyAt(caçador.blockPosition()),
                            EntitySpawnReason.COMMAND, null);
                    // a pele é sorteada ao nascer; aqui se escolhe, para as três aparecerem
                    caçador.pele(qual);
                    caçador.setPersistenceRequired();
                    caçador.setNoAi(true);
                    level.addFreshEntity(caçador);
                }

                player.snapTo(meio.getX() + 0.5, meio.getY() + 1.0, meio.getZ(), 0.0f, 0.0f);
                player.setDeltaMovement(Vec3.ZERO);
            });
            context.waitTicks(40);
            context.takeScreenshot("tres_cacadores_de_bruxas");
        }
    }
}
