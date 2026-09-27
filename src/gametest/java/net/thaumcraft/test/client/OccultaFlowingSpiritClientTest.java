package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaBlocks;

/**
 * Os dois líquidos do outro lado, vistos: o Espírito Fluente e as Lágrimas Ocas, cada um na sua bacia, e a poça
 * endurecida depois de levar um Cozimento Sólido.
 */
public class OccultaFlowingSpiritClientTest implements FabricClientGameTest {
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
                BlockPos pé = jogador.blockPosition();

                // duas bacias de três por três, uma de cada líquido
                bacia(mundo, pé.offset(-3, 0, 6), OccultaBlocks.FLOWING_SPIRIT);
                bacia(mundo, pé.offset(3, 0, 6), OccultaBlocks.HOLLOW_TEARS);

                jogador.teleportTo(mundo, pé.getX() + 0.5, pé.getY() + 1.0, pé.getZ() + 0.5,
                        java.util.Set.of(), 0.0f, 30.0f, false);
            });
            context.waitTicks(40);
            context.takeScreenshot("ao_dois_liquidos");

            // as coisas novas na barra
            server.runCommand("item replace entity @p hotbar.0 with thaumcraft:brew_of_flowing_spirit");
            server.runCommand("item replace entity @p hotbar.1 with thaumcraft:brew_of_hollow_tears");
            server.runCommand("item replace entity @p hotbar.2 with thaumcraft:focused_will");
            server.runCommand("item replace entity @p hotbar.3 with thaumcraft:condensed_fear");
            server.runCommand("item replace entity @p hotbar.4 with thaumcraft:brew_of_solid_rock");
            server.runCommand("item replace entity @p hotbar.5 with thaumcraft:brew_of_solid_dirt");
            server.runCommand("item replace entity @p hotbar.6 with thaumcraft:brew_of_solid_sand");
            server.runCommand("item replace entity @p hotbar.7 with thaumcraft:brew_of_solid_erosion");
            server.runCommand("item replace entity @p hotbar.8 with thaumcraft:bucket_flowing_spirit");
            context.waitTicks(10);
            context.takeScreenshot("ao_espirito_itens");
        }
    }

    /** Uma bacia de três por três, cavada no chão e cheia daquele líquido. */
    private static void bacia(ServerLevel mundo, BlockPos meio, net.minecraft.world.level.block.Block líquido) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                mundo.setBlockAndUpdate(meio.offset(x, -1, z), Blocks.STONE_BRICKS.defaultBlockState());
                mundo.setBlockAndUpdate(meio.offset(x, 0, z), líquido.defaultBlockState());
            }
        }
        // e a borda, para o líquido não escorrer
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) != 2 && Math.abs(z) != 2) continue;
                mundo.setBlockAndUpdate(meio.offset(x, 0, z), Blocks.STONE_BRICKS.defaultBlockState());
                mundo.setBlockAndUpdate(meio.offset(x, -1, z), Blocks.STONE_BRICKS.defaultBlockState());
            }
        }
    }
}
