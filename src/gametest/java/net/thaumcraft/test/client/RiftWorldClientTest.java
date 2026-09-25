package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.thaumcraft.shattered.ShatteredBlocks;

/** Uma fenda solta no mundo, a comer a pedra em volta. */
public class RiftWorldClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set midnight");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = (net.minecraft.server.level.ServerLevel) player.level();
                BlockPos onde = player.blockPosition().north(5).above(1);
                // uma parede de pedra atrás, para se ver o que a fenda come
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dy = -1; dy <= 4; dy++) {
                        level.setBlockAndUpdate(onde.offset(dx, dy, -2),
                                net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                    }
                }
                level.setBlockAndUpdate(onde, ShatteredBlocks.RIFT.defaultBlockState());
            });
            server.runCommand("tp @p ~ ~ ~ 180 0");
            context.waitTicks(60);
            context.takeScreenshot("fenda_no_mundo");
        }
    }
}
