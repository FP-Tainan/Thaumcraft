package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.block.InfernalFurnaceStructure;

/** A fornalha infernal formada, vista de frente e de cima, de dia (como a imagem da wiki). */
public class InfernalFurnaceClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("tp @p ~ ~ ~ 200 25");
            java.util.concurrent.atomic.AtomicReference<BlockPos> where = new java.util.concurrent.atomic.AtomicReference<>();
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                // o canto noroeste de baixo, cinco blocos ao norte e um a oeste; a boca olha para o sul, para quem vê
                BlockPos origin = player.blockPosition().offset(-2, 0, -7);
                for (int y = 0; y < 3; y++) {
                    for (int x = 0; x < 3; x++) {
                        for (int z = 0; z < 3; z++) {
                            boolean corner = x != 1 && z != 1;
                            BlockState state = corner ? Blocks.NETHER_BRICKS.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState();
                            if (x == 1 && z == 1 && y == 1) state = Blocks.LAVA.defaultBlockState();
                            if (x == 1 && z == 1 && y == 2) state = Blocks.AIR.defaultBlockState();
                            if (x == 1 && z == 2 && y == 1) state = Blocks.IRON_BARS.defaultBlockState();
                            level.setBlockAndUpdate(origin.offset(x, y, z), state);
                        }
                    }
                }
                InfernalFurnaceStructure.replace(level, origin);
                where.set(origin);
            });
            BlockPos o = where.get();
            server.runCommand("tp @p " + (o.getX() + 1.5) + " " + (o.getY() + 0.5) + " " + (o.getZ() + 7.5) + " 180 5");
            context.waitTicks(40);
            context.takeScreenshot("fornalha_infernal");
            server.runCommand("tp @p " + (o.getX() + 4.5) + " " + (o.getY() + 5.5) + " " + (o.getZ() + 5.5) + " 135 45");
            context.waitTicks(10);
            context.takeScreenshot("fornalha_infernal_cima");
        }
    }
}
