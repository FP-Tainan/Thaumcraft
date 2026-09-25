package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.thaumcraft.shattered.RiftBlockEntity;
import net.thaumcraft.shattered.ShatteredBlocks;

/** A porta dimensional de pé no mundo, e o bolso do outro lado dela. */
public class PocketClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");

            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = (net.minecraft.server.level.ServerLevel) player.level();
                BlockPos onde = player.blockPosition().north(4);
                var estado = ShatteredBlocks.OAK_DIMENSIONAL_DOOR.defaultBlockState()
                        .setValue(DoorBlock.FACING, Direction.SOUTH)
                        .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
                level.setBlockAndUpdate(onde, estado);
                level.setBlockAndUpdate(onde.above(), estado.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
            });
            server.runCommand("tp @p ~ ~ ~ 180 0");
            context.waitTicks(40);
            context.takeScreenshot("porta_dimensional");

            // e do outro lado dela
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = (net.minecraft.server.level.ServerLevel) player.level();
                BlockPos onde = player.blockPosition().north(4);
                if (level.getBlockEntity(onde) instanceof RiftBlockEntity fenda) fenda.teleport(player);
            });
            context.waitTicks(60);
            context.takeScreenshot("dentro_do_bolso");
        }
    }
}
