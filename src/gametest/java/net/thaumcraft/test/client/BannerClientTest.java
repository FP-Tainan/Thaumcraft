package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.BannerBlock;
import net.thaumcraft.block.entity.BannerBlockEntity;
import net.thaumcraft.registry.TCBlocks;

import java.util.concurrent.atomic.AtomicReference;

/** Os estandartes (o dos cultistas, os coloridos, um pintado, um na parede) e o purificador de fluxo. */
public class BannerClientTest implements FabricClientGameTest {
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
                for (int x = -4; x <= 4; x++) for (int z = 2; z <= 7; z++) {
                    level.setBlockAndUpdate(p.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
                    for (int y = 0; y < 4; y++) level.setBlockAndUpdate(p.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
                for (int x = -4; x <= 4; x++) for (int y = 0; y < 4; y++) level.setBlockAndUpdate(p.offset(x, y, 7), Blocks.STONE_BRICKS.defaultBlockState());
                int[] colors = {-1, 14, 11, 5};
                for (int i = 0; i < colors.length; i++) {
                    BlockPos at = p.offset(-3 + i * 2, 0, 4);
                    level.setBlockAndUpdate(at, TCBlocks.BANNER.defaultBlockState());
                    var te = (BannerBlockEntity) level.getBlockEntity(at);
                    te.setColor((byte) colors[i]);
                    te.setFacing((byte) 8);
                    if (i == 2) te.setAspect(Aspect.of("aqua"));
                }
                BlockPos wall = p.offset(0, 1, 6);
                level.setBlockAndUpdate(wall, TCBlocks.BANNER.defaultBlockState());
                var te = (BannerBlockEntity) level.getBlockEntity(wall);
                te.setColor((byte) 10);
                te.setWall(true);
                te.setFacing((byte) 8);
                te.setAspect(Aspect.of("praecantatio"));
                level.setBlockAndUpdate(p.offset(3, 0, 6), TCBlocks.FLUX_SCRUBBER.defaultBlockState());
                player.getInventory().setItem(0, BannerBlock.stack(1));
                player.getInventory().setItem(1, new net.minecraft.world.item.ItemStack(net.thaumcraft.registry.TCItems.FLUX_SCRUBBER));
                player.getInventory().setSelectedSlot(0);
            });
            BlockPos p = base.get();
            server.runCommand("tp @p " + (p.getX() + 0.5) + " " + (p.getY() + 0.5) + " " + (p.getZ() + 0.5) + " 0 5");
            context.waitTicks(30);
            context.takeScreenshot("estandartes");
        }
    }
}
