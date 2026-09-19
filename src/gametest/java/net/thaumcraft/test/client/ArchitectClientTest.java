package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.item.FocusUpgradeTable;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

/** A prévia do arquiteto: a varinha de troca com arquiteto mirando um chão de terra, e depois uma parede. */
public class ArchitectClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 180 60");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos at = player.blockPosition();
                for (int x = -4; x <= 4; x++) for (int z = -8; z <= 0; z++) level.setBlockAndUpdate(at.offset(x, -1, z), Blocks.STONE.defaultBlockState());
                for (int x = -3; x <= 3; x++) for (int y = 0; y < 4; y++) level.setBlockAndUpdate(at.offset(x, y, -6), Blocks.STONE_BRICKS.defaultBlockState());
                ItemStack wand = new ItemStack(TCItems.WAND);
                wand.set(TCComponents.WAND_FOCUS, "trade");
                wand.set(TCComponents.FOCUS_UPGRADES, java.util.List.of(FocusUpgradeTable.FRUGAL.id(), FocusUpgradeTable.FRUGAL.id(),
                        FocusUpgradeTable.ARCHITECT.id(), (short) -1, (short) -1));
                wand.set(TCComponents.WAND_AREA, java.util.List.of(2, 2, 1, 0));
                player.getInventory().setItem(0, wand);
                player.getInventory().setSelectedSlot(0);
            });
            context.waitTicks(30);
            context.takeScreenshot("arquiteto_chao");
            server.runCommand("execute as @p at @s run tp @s ~ ~ ~-3 180 5");
            context.waitTicks(20);
            context.takeScreenshot("arquiteto_parede");
        }
    }
}
