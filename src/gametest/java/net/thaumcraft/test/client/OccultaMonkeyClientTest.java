package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaEntities;

/**
 * O <b>Macaco de Asas</b> visto: as asas abertas no ar, as asas fechadas pousado, e a tinta.
 *
 * <p>São três fotos e cada uma prova uma coisa que só se vê: que as <b>asas chatas de doze blocos</b>
 * batem quando ele voa e se fecham para trás quando ele pousa, que <b>sentado</b> ele dobra as pernas e
 * levanta o rabo, e que a <b>tinta</b> do original não o pinta de azul — ela mistura um sopro de cor no
 * castanho que a folha dele já tem.
 */
public class OccultaMonkeyClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator");
            server.runCommand("time set noon");
            server.runCommand("difficulty peaceful");
            server.runCommand("gamerule doMobSpawning false");

            double[] onde = server.computeOnServer(s -> {
                var mundo = s.overworld();
                var jogador = s.getPlayerList().getPlayers().getFirst();
                BlockPos meio = jogador.blockPosition();

                // uma lage de pedra para o pousado ficar pousado
                for (int x = -2; x <= 2; x++) {
                    for (int z = 2; z <= 6; z++) {
                        mundo.setBlockAndUpdate(meio.offset(x, -1, z), Blocks.STONE.defaultBlockState());
                    }
                }

                põe(mundo, meio.offset(-2, 2, 4), 0, false);   // no ar, asas a bater
                põe(mundo, meio.offset(0, 0, 4), 0, true);     // pousado e sentado
                põe(mundo, meio.offset(2, 0, 4), 5, true);     // e pintado de verde-claro

                jogador.setDeltaMovement(Vec3.ZERO);
                return new double[]{meio.getX() + 0.5, meio.getY(), meio.getZ() + 4.5};
            });

            olha(server, onde[0], onde[1] + 1.0, onde[2] - 3.2, 0.0f, 5.0f);
            context.waitTicks(40);
            context.takeScreenshot("os_tres_macacos_de_frente");

            olha(server, onde[0] + 4.0, onde[1] + 1.8, onde[2] + 4.0, 135.0f, 14.0f);
            context.waitTicks(20);
            context.takeScreenshot("os_dois_pousados_por_tras");

            olha(server, onde[0] - 2.0, onde[1] + 2.2, onde[2] - 2.2, 0.0f, 0.0f);
            context.waitTicks(20);
            context.takeScreenshot("o_macaco_no_ar_de_perto");
        }
    }

    /** Põe um macaco quieto naquele lugar, com aquela cor, sentado ou não. */
    private static void põe(net.minecraft.server.level.ServerLevel mundo, BlockPos onde,
                            int cor, boolean sentado) {
        var macaco = OccultaEntities.WINGED_MONKEY.create(mundo, EntitySpawnReason.COMMAND);
        if (macaco == null) return;
        macaco.snapTo(onde.getX() + 0.5, onde.getY(), onde.getZ() + 0.5, 180.0f, 0.0f);
        macaco.setYBodyRot(180.0f);
        macaco.setYHeadRot(180.0f);
        macaco.cor(cor);
        macaco.setPersistenceRequired();
        macaco.setNoAi(true);
        macaco.setSilent(true);
        macaco.setOrderedToSit(sentado);
        mundo.addFreshEntity(macaco);
    }

    /** Põe a câmara naquele ponto, olhando para aquele rumo. */
    private static void olha(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
                             double x, double y, double z, float guinada, float passo) {
        server.runCommand(String.format(java.util.Locale.ROOT, "tp @p %.1f %.1f %.1f %.1f %.1f",
                x, y, z, guinada, passo));
    }
}
