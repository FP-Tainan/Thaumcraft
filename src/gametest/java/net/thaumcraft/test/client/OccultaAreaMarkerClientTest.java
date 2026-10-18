package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.rite.AreaMarkerBlock;

/**
 * As <b>duas estatuetas</b> vistas: o lobo sentado no degrau e a mandrágora de braços abertos.
 *
 * <p>São de <b>meio tamanho</b> — o desenhista as encolhe à metade —, e é isso que as fotos provam: duas
 * coisas pequenas no chão, ao lado uma da outra, do tamanho de um gato.
 */
public class OccultaAreaMarkerClientTest implements FabricClientGameTest {
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

                for (int x = -2; x <= 2; x++) {
                    for (int z = 2; z <= 6; z++) {
                        mundo.setBlockAndUpdate(meio.offset(x, -1, z), Blocks.STONE.defaultBlockState());
                    }
                }
                mundo.setBlockAndUpdate(meio.offset(-1, 0, 4),
                        OccultaBlocks.WOLF_MARKER.defaultBlockState()
                                .setValue(AreaMarkerBlock.FACING, Direction.SOUTH));
                mundo.setBlockAndUpdate(meio.offset(1, 0, 4),
                        OccultaBlocks.MANDRAKE_MARKER.defaultBlockState()
                                .setValue(AreaMarkerBlock.FACING, Direction.SOUTH));

                jogador.setDeltaMovement(Vec3.ZERO);
                return new double[]{meio.getX() + 0.5, meio.getY(), meio.getZ() + 4.5};
            });

            olha(server, onde[0] - 3.0, onde[1] + 1.0, onde[2], 270.0f, 18.0f);
            context.waitTicks(40);
            context.takeScreenshot("o_lobo_de_perto");

            olha(server, onde[0] + 3.0, onde[1] + 1.0, onde[2], 90.0f, 18.0f);
            context.waitTicks(20);
            context.takeScreenshot("a_mandragora_de_perto");

            // e as duas na mão de quem as leva
            server.runCommand("gamemode survival");
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                jogador.getInventory().setItem(0, new ItemStack(OccultaItems.WOLF_MARKER));
                jogador.getInventory().setItem(1, new ItemStack(OccultaItems.MANDRAKE_MARKER));
            });
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("os_dois_marcadores_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }

    /** Põe a câmara naquele ponto, olhando para aquele rumo. */
    private static void olha(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
                             double x, double y, double z, float guinada, float passo) {
        server.runCommand(String.format(java.util.Locale.ROOT, "tp @p %.1f %.1f %.1f %.1f %.1f",
                x, y, z, guinada, passo));
    }
}
