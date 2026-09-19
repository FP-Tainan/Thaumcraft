package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.block.eldritch.EldritchInsetBlock;
import net.thaumcraft.block.entity.eldritch.EldritchAltarBlockEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.world.RuinsFeature;

import java.util.concurrent.atomic.AtomicReference;

/** As ruínas: o anel eldritch com o obelisco, um totem, as urnas e os caixotes, e as pedras antigas. */
public class RuinsClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            AtomicReference<BlockPos> base = new AtomicReference<>();
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos p = player.blockPosition();
                base.set(p);
                for (int x = -8; x <= 8; x++) for (int z = -2; z <= 16; z++) {
                    level.setBlockAndUpdate(p.offset(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState());
                    for (int y = 0; y < 12; y++) level.setBlockAndUpdate(p.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
                BlockPos ring = p.offset(0, -1, 10);
                RuinsFeature.eldritchRing(level, level.getRandom(), ring.getX(), ring.getY(), ring.getZ(), 0, 0, 11, 11);
                if (level.getBlockEntity(ring.above()) instanceof EldritchAltarBlockEntity altar) {
                    altar.setEyes((byte) 3);
                    altar.setChanged();
                    level.sendBlockUpdated(ring.above(), level.getBlockState(ring.above()), level.getBlockState(ring.above()), 3);
                }
                RuinsFeature.totem(level, level.getRandom(), p.getX() - 6, p.getZ() + 5);
                for (int r = 0; r < 3; r++) {
                    level.setBlockAndUpdate(p.offset(3 + r, 0, 4), TCBlocks.LOOT_URNS.get(r).defaultBlockState());
                    level.setBlockAndUpdate(p.offset(3 + r, 0, 6), TCBlocks.LOOT_CRATES.get(r).defaultBlockState());
                }
                BlockPos wall = p.offset(-4, 0, 3);
                var blocks = new net.minecraft.world.level.block.Block[]{TCBlocks.ANCIENT_STONE, TCBlocks.ANCIENT_ROCK, TCBlocks.CRUSTED_STONE,
                        TCBlocks.ANCIENT_STONE_PEDESTAL, TCBlocks.OBSIDIAN_TILE};
                for (int i = 0; i < blocks.length; i++) level.setBlockAndUpdate(wall.offset(-i, 0, 0), blocks[i].defaultBlockState());
                for (int i = 0; i < 3; i++) level.setBlockAndUpdate(wall.offset(-i, 1, 0), TCBlocks.ANCIENT_ROCK.defaultBlockState());
                BlockPos inset = wall.offset(0, 2, 0);
                level.setBlockAndUpdate(inset, TCBlocks.GLOWING_CRUSTED_STONE.defaultBlockState());
                level.setBlockAndUpdate(inset.west(), TCBlocks.GLYPHED_STONE.defaultBlockState());
                for (BlockPos at : new BlockPos[]{inset, inset.west()}) {
                    level.setBlockAndUpdate(at, EldritchInsetBlock.shape(level.getBlockState(at), level, at));
                }
            });
            BlockPos p = base.get();
            server.runCommand("tp @p " + (p.getX() + 0.5) + " " + (p.getY() + 1) + " " + (p.getZ() - 1.5) + " 0 12");
            context.waitTicks(40);
            context.takeScreenshot("ruinas-anel");
            server.runCommand("tp @p " + (p.getX() + 3.5) + " " + (p.getY() + 1) + " " + (p.getZ() + 10.5) + " 90 15");
            context.waitTicks(20);
            context.takeScreenshot("ruinas-obelisco");
            server.runCommand("tp @p " + (p.getX() + 4.5) + " " + (p.getY() + 1.5) + " " + (p.getZ() + 1.5) + " 0 40");
            context.waitTicks(20);
            context.takeScreenshot("ruinas-urnas");
            server.runCommand("tp @p " + (p.getX() - 5.5) + " " + (p.getY() + 1) + " " + (p.getZ() + 0.5) + " 0 10");
            context.waitTicks(20);
            context.takeScreenshot("ruinas-pedras");
            server.runCommand("tp @p " + (p.getX() + 0.5) + " " + (p.getY() + 3) + " " + (p.getZ() + 6.5) + " 0 -10");
            context.waitTicks(20);
            context.takeScreenshot("ruinas-obelisco-perto");
            server.runCommand("tp @p " + (p.getX() + 5.5) + " " + (p.getY() + 3.5) + " " + (p.getZ() + 10.5) + " 90 0");
            context.waitTicks(20);
            context.takeScreenshot("ruinas-obelisco-lado");
            server.runCommand("tp @p " + (p.getX() - 4.0) + " " + (p.getY() + 1.0) + " " + (p.getZ() + 1.2) + " 0 0");
            context.waitTicks(20);
            context.takeScreenshot("ruinas-engastadas");
        }
    }
}
