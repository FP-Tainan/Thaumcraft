package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.spirit.SpiritPortalBlock;

/**
 * O Portal do Espírito visto: a moldura de neve com o vão aceso, no verde de água do original.
 */
public class OccultaPortalClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set midnight");

            server.runOnServer(s -> {
                ServerLevel mundo = s.overworld();
                var jogador = s.getPlayerList().getPlayers().getFirst();
                BlockPos pé = jogador.blockPosition().offset(-1, 0, 6);

                var neve = Blocks.SNOW_BLOCK.defaultBlockState();
                for (int l = 0; l < SpiritPortalBlock.WIDTH; l++) {
                    mundo.setBlockAndUpdate(pé.offset(l, 0, 0), neve);
                    mundo.setBlockAndUpdate(pé.offset(l, SpiritPortalBlock.HEIGHT + 1, 0), neve);
                }
                for (int y = 1; y <= SpiritPortalBlock.HEIGHT; y++) {
                    mundo.setBlockAndUpdate(pé.offset(-1, y, 0), neve);
                    mundo.setBlockAndUpdate(pé.offset(SpiritPortalBlock.WIDTH, y, 0), neve);
                }
                for (int l = 0; l < SpiritPortalBlock.WIDTH; l++) {
                    for (int y = 1; y <= SpiritPortalBlock.HEIGHT; y++) {
                        mundo.setBlockAndUpdate(pé.offset(l, y, 0), Blocks.AIR.defaultBlockState());
                    }
                }
                // e se acende, como o Espírito Fluente acenderia do outro lado
                SpiritPortalBlock.tryToCreate(mundo, pé.above());
                if (!mundo.getBlockState(pé.above()).is(OccultaBlocks.SPIRIT_PORTAL)) {
                    throw new IllegalStateException("o portal devia ter acendido");
                }

                jogador.teleportTo(mundo, jogador.getX(), jogador.getY(), jogador.getZ(),
                        java.util.Set.of(), 0.0f, 0.0f, false);
            });
            context.waitTicks(40);
            context.takeScreenshot("ao_portal_do_espirito");
        }
    }
}
