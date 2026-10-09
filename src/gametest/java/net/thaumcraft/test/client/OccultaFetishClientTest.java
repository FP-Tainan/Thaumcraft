package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.fetish.FetishBlockEntity;

/**
 * Os três <b>fetiches</b> em fila, e o Espantalho em quatro cores.
 *
 * <p>O que importa nesta tela são três coisas:
 *
 * <ul>
 *   <li>a <b>silhueta</b> de cada um: o Espantalho é uma estaca com travessa e um saco na cabeça; o
 *       Ídolo é um toco de madeira com três galhos atrás; e a Escada é uma folha cruzada de penas, que
 *       não tem boneco nenhum;</li>
 *   <li>a <b>tinta</b>, que no Espantalho pega na cabeça, no corpo e nos braços — e <b>não</b> na estaca
 *       nem na cara, que ficam de madeira e de palha seja qual for a cor;</li>
 *   <li>e a <b>cópia espectral</b>, a seis décimos, que é o que o mundo dos sonhos põe no mundo de cima.</li>
 * </ul>
 */
public class OccultaFetishClientTest implements FabricClientGameTest {
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

                for (int dx = -4; dx <= 4; dx++) {
                    for (int dz = 2; dz <= 8; dz++) {
                        level.setBlockAndUpdate(meio.offset(dx, -1, dz),
                                Blocks.SMOOTH_STONE.defaultBlockState());
                    }
                }

                // a fila da frente: os três fetiches, cada um com a cara dele
                põe(level, meio.offset(-3, 0, 5), OccultaBlocks.SCARECROW, 9, false);
                põe(level, meio.offset(-1, 0, 5), OccultaBlocks.WITCHS_LADDER, 9, false);
                põe(level, meio.offset(1, 0, 5), OccultaBlocks.TREANT_IDOL, 9, false);
                // e um espantalho espectral, para se ver que está ali e que não está
                põe(level, meio.offset(3, 0, 5), OccultaBlocks.SCARECROW, 9, true);

                // a fila de trás: quatro espantalhos pintados
                int[] cores = {14, 4, 5, 11};
                for (int i = 0; i < cores.length; i++) {
                    põe(level, meio.offset(-3 + i * 2, 0, 8), OccultaBlocks.SCARECROW, cores[i], false);
                }

                player.snapTo(meio.getX() + 0.5, meio.getY() + 1.6, meio.getZ(), 0.0f, 2.0f);
                player.setDeltaMovement(Vec3.ZERO);
            });
            context.waitTicks(40);
            context.takeScreenshot("os_fetiches");
        }
    }

    private static void põe(net.minecraft.server.level.ServerLevel level, BlockPos onde,
                            net.minecraft.world.level.block.Block qual, int cor, boolean espectral) {
        level.setBlockAndUpdate(onde, qual.defaultBlockState());
        if (!(level.getBlockEntity(onde) instanceof FetishBlockEntity alma)) return;
        alma.color(cor);
        if (espectral) alma.spectral(true);
    }
}
