package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.block.entity.FocalManipulatorBlockEntity;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.FocusUpgradeTable;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/** O manipulador focal com um foco de fogo de duas melhorias, trabalhando na terceira; depois, a tela dele. */
public class FocalManipulatorClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("tp @p ~ ~ ~ 180 35");
            java.util.concurrent.atomic.AtomicReference<BlockPos> where = new java.util.concurrent.atomic.AtomicReference<>();
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos pos = player.blockPosition().offset(0, 0, -2);
                level.setBlockAndUpdate(pos, TCBlocks.FOCAL_MANIPULATOR.defaultBlockState());
                ItemStack fire = new ItemStack(TCItems.FOCI.get("fire"));
                FocusItem.apply(fire, FocusUpgradeTable.POTENCY, 1);
                FocusItem.apply(fire, FocusUpgradeTable.FRUGAL, 2);
                if (level.getBlockEntity(pos) instanceof FocalManipulatorBlockEntity table) {
                    table.setItem(0, fire);
                    player.giveExperienceLevels(50);
                    table.startCraft(FocusUpgradeTable.FIREBEAM.id(), player);
                }
                where.set(pos);
            });
            context.waitTicks(40);
            context.takeScreenshot("manipulador_focal");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                if (player.level().getBlockEntity(where.get()) instanceof FocalManipulatorBlockEntity table) player.openMenu(table);
            });
            context.waitTicks(30);
            context.takeScreenshot("manipulador_focal_tela");
        }
    }
}
