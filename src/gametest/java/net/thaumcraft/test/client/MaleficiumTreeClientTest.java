package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.maleficium.MaleficiumBlocks;
import net.thaumcraft.maleficium.WarpwoodSaplingBlock;

/** A árvore distorcida no mundo, com o Lumos aceso ao pé dela. */
public class MaleficiumTreeClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set midnight");
            server.runCommand("gamerule doDaylightCycle false");
            server.runCommand("weather clear");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos at = player.blockPosition().offset(0, 0, 9);
                // chão limpo e céu livre para a árvore caber
                for (int x = -9; x <= 9; x++) {
                    for (int z = -9; z <= 9; z++) {
                        level.setBlockAndUpdate(at.offset(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState());
                        for (int y = 0; y < 22; y++) level.setBlockAndUpdate(at.offset(x, y, z), Blocks.AIR.defaultBlockState());
                    }
                }
                var sapling = (WarpwoodSaplingBlock) MaleficiumBlocks.WARPWOOD_SAPLING;
                boolean grew = false;
                for (int tries = 0; tries < 20 && !grew; tries++) {
                    grew = sapling.grow((net.minecraft.server.level.ServerLevel) level, at,
                            sapling.defaultBlockState(), level.getRandom());
                }
                if (!grew) throw new AssertionError("a árvore distorcida não nasceu");
                level.setBlockAndUpdate(at.offset(3, 1, -5), MaleficiumBlocks.LUMOS.defaultBlockState());
                level.setBlockAndUpdate(at.offset(-3, 1, -5), MaleficiumBlocks.LUMOS.defaultBlockState());
            });
            server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 -10");
            context.waitTicks(40);
            context.takeScreenshot("arvore_distorcida");
        }
    }
}
