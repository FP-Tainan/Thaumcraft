package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.GoddessStatueBlock;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;

/**
 * A <b>Estátua da Deusa</b> vista: de pé no mundo e na mão de quem a leva.
 *
 * <p>São as duas fotos que provam o desenho: a senhora de <b>braços abertos</b> e <b>duas saias</b>, com
 * dois blocos de alto, e a mesma encolhida a seis décimos numa casa de inventário.
 */
public class OccultaGoddessClientTest implements FabricClientGameTest {
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
                mundo.setBlockAndUpdate(meio.offset(0, 0, 4),
                        OccultaBlocks.GODDESS_STATUE.defaultBlockState()
                                .setValue(GoddessStatueBlock.FACING, Direction.SOUTH));

                jogador.setDeltaMovement(Vec3.ZERO);
                return new double[]{meio.getX() + 0.5, meio.getY(), meio.getZ() + 4.5};
            });

            olha(server, onde[0], onde[1] + 1.8, onde[2] - 3.6, 0.0f, 2.0f);
            context.waitTicks(40);
            context.takeScreenshot("a_deusa_de_frente");

            olha(server, onde[0] + 5.5, onde[1] + 2.4, onde[2], 90.0f, 6.0f);
            context.waitTicks(20);
            context.takeScreenshot("a_deusa_de_lado");

            // e a mesma na mão de quem a leva
            server.runCommand("gamemode survival");
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                jogador.getInventory().setItem(0, new ItemStack(OccultaItems.GODDESS_STATUE));
            });
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("a_deusa_no_inventario");
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
