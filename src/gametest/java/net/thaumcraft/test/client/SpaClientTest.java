package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.thaumcraft.block.entity.ArcaneSpaBlockEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/** Uma poça de fluido purificante e uma de morte líquida, o spa arcano; depois a tela do spa com água e sais. */
public class SpaClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 180 40");
            java.util.concurrent.atomic.AtomicReference<BlockPos> where = new java.util.concurrent.atomic.AtomicReference<>();
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos at = player.blockPosition().offset(0, -1, -4);
                for (int x = -4; x <= 4; x++) for (int z = -2; z <= 2; z++) level.setBlockAndUpdate(at.offset(x, 0, z), Blocks.AIR.defaultBlockState());
                for (int x = -4; x <= 4; x++) for (int z = -2; z <= 2; z++) level.setBlockAndUpdate(at.offset(x, -1, z), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(at.offset(-2, 0, 0), TCBlocks.PURIFYING_FLUID.defaultBlockState());
                level.setBlockAndUpdate(at.offset(2, 0, 0), TCBlocks.LIQUID_DEATH.defaultBlockState());
                level.setBlockAndUpdate(at.offset(0, 1, -3), TCBlocks.ARCANE_SPA.defaultBlockState());
                if (level.getBlockEntity(at.offset(0, 1, -3)) instanceof ArcaneSpaBlockEntity spa) {
                    try (Transaction t = Transaction.openOuter()) {
                        spa.tank.insert(FluidVariant.of(Fluids.WATER), 3 * FluidConstants.BUCKET, t);
                        t.commit();
                    }
                    spa.setItem(0, new ItemStack(TCItems.BATH_SALTS, 5));
                    level.setBlockAndUpdate(at.offset(0, 0, -3), Blocks.REDSTONE_BLOCK.defaultBlockState());
                }
                player.getInventory().setItem(0, new ItemStack(TCItems.BUCKET_PURE));
                player.getInventory().setItem(1, new ItemStack(TCItems.BUCKET_DEATH));
                player.getInventory().setItem(2, new ItemStack(TCItems.BATH_SALTS));
                where.set(at.offset(0, 1, -3));
            });
            context.waitTicks(60);
            context.takeScreenshot("fluidos");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                if (player.level().getBlockEntity(where.get()) instanceof ArcaneSpaBlockEntity spa) player.openMenu(spa);
            });
            context.waitTicks(20);
            context.takeScreenshot("spa_tela");
        }
    }
}
