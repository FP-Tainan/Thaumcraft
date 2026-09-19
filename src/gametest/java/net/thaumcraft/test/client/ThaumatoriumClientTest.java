package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.block.ItemGrateBlock;
import net.thaumcraft.block.MnemonicMatrixBlock;
import net.thaumcraft.block.ThaumatoriumStructure;
import net.thaumcraft.block.entity.ThaumatoriumBlockEntity;
import net.thaumcraft.crafting.CrucibleRecipes;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/** O taumatório sobre o crisol com uma matriz mnemônica ao lado e duas grades; depois, a tela dele com carvão. */
public class ThaumatoriumClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("tp @p ~ ~1 ~ 180 15");
            java.util.concurrent.atomic.AtomicReference<BlockPos> where = new java.util.concurrent.atomic.AtomicReference<>();
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos bottom = player.blockPosition().offset(0, 0, -4);
                level.setBlockAndUpdate(bottom.below(2), TCBlocks.NITOR.defaultBlockState());
                level.setBlockAndUpdate(bottom.below(), TCBlocks.CRUCIBLE.defaultBlockState());
                level.setBlockAndUpdate(bottom, TCBlocks.ALCHEMICAL_CONSTRUCT.defaultBlockState());
                level.setBlockAndUpdate(bottom.above(), TCBlocks.ALCHEMICAL_CONSTRUCT.defaultBlockState());
                ThaumatoriumStructure.build(level, bottom, Direction.SOUTH);
                level.setBlockAndUpdate(bottom.west(2), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(bottom.west(), TCBlocks.MNEMONIC_MATRIX.defaultBlockState().setValue(MnemonicMatrixBlock.FACING, Direction.WEST));
                level.setBlockAndUpdate(bottom.east(2).below(), TCBlocks.ITEM_GRATE.defaultBlockState());
                level.setBlockAndUpdate(bottom.east(3).below(), TCBlocks.ITEM_GRATE.defaultBlockState().setValue(ItemGrateBlock.CLOSED, true));
                if (level.getBlockEntity(bottom) instanceof ThaumatoriumBlockEntity tile) {
                    tile.getUpgrades();
                    CrucibleRecipes.ALL.stream().filter(r -> r.result().is(TCItems.ALUMENTUM)).findFirst().ifPresent(r -> tile.toggle(r, player));
                    tile.setItem(0, new ItemStack(Items.COAL, 5));
                }
                var inv = player.getInventory();
                inv.setItem(0, new ItemStack(TCItems.MNEMONIC_MATRIX));
                inv.setItem(1, new ItemStack(TCItems.ITEM_GRATE));
                where.set(bottom);
            });
            context.waitTicks(40);
            context.takeScreenshot("taumatorio");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                if (player.level().getBlockEntity(where.get()) instanceof ThaumatoriumBlockEntity tile) player.openMenu(tile);
            });
            context.waitTicks(20);
            context.takeScreenshot("taumatorio_tela");
        }
    }
}
