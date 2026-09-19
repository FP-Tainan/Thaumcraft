package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.block.CrystalClusterBlock;
import net.thaumcraft.registry.TCBlocks;

/** Os minérios (cinábrio, âmbar e as seis pedras infundidas) numa parede, e os sete aglomerados de cristal. */
public class OresClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode survival");
            server.runCommand("time set midnight");
            server.runCommand("tp @p ~ ~ ~ 180 10");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos base = player.blockPosition().north(4);
                var ores = new java.util.ArrayList<net.minecraft.world.level.block.Block>();
                ores.add(TCBlocks.CINNABAR_ORE);
                ores.add(TCBlocks.AMBER_ORE);
                ores.addAll(TCBlocks.INFUSED_STONE.values());
                for (int i = 0; i < ores.size(); i++) {
                    level.setBlockAndUpdate(base.east(i - 4).above(2), ores.get(i).defaultBlockState());
                    level.setBlockAndUpdate(base.east(i - 4).above(1), Blocks.STONE.defaultBlockState());
                }
                int i = 0;
                for (var crystal : TCBlocks.CRYSTAL_CLUSTERS.values()) {
                    level.setBlockAndUpdate(base.east(i - 3).south(1), crystal.defaultBlockState().setValue(CrystalClusterBlock.FACING, Direction.UP));
                    i++;
                }
                // um preso na parede, virado para quem olha
                level.setBlockAndUpdate(base.east(4).above(3), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(base.east(4).above(3).south(), TCBlocks.CRYSTAL_CLUSTERS.get("fire").defaultBlockState()
                        .setValue(CrystalClusterBlock.FACING, Direction.SOUTH));
                var inv = player.getInventory();
                int slot = 0;
                for (var ore : ores) inv.setItem(slot++, new ItemStack(ore));
                for (var crystal : TCBlocks.CRYSTAL_CLUSTERS.values()) inv.setItem(slot++, new ItemStack(crystal));
            });
            context.waitTicks(60);
            context.takeScreenshot("minerios_cristais");
            context.getInput().pressKey(options -> options.keyInventory);
            context.waitTicks(10);
            context.takeScreenshot("minerios_cristais_inventario");
        }
    }
}
