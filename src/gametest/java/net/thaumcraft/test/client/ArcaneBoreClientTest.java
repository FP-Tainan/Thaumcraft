package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.block.ArcaneBoreBaseBlock;
import net.thaumcraft.block.ArcaneBoreBlock;
import net.thaumcraft.block.entity.ArcaneBoreBlockEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/** A broca arcana cavando uma parede de pedra, vista de lado; depois, parada, e a tela dela. */
public class ArcaneBoreClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            java.util.concurrent.atomic.AtomicReference<BlockPos> where = new java.util.concurrent.atomic.AtomicReference<>();
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos at = player.blockPosition().offset(-4, 0, 0);
                for (int x = -10; x <= -5; x++) for (int y = 0; y < 5; y++) for (int z = -3; z <= 3; z++) {
                    level.setBlockAndUpdate(at.offset(x, y, z), Blocks.STONE.defaultBlockState());
                }
                level.setBlockAndUpdate(at, TCBlocks.ARCANE_BORE_BASE.defaultBlockState().setValue(ArcaneBoreBaseBlock.FACING, Direction.SOUTH));
                level.setBlockAndUpdate(at.above(), TCBlocks.ARCANE_BORE.defaultBlockState()
                        .setValue(ArcaneBoreBlock.FACING, Direction.WEST).setValue(ArcaneBoreBlock.BASE, Direction.UP));
                if (level.getBlockEntity(at.above()) instanceof ArcaneBoreBlockEntity bore) {
                    bore.setItem(0, new ItemStack(TCItems.FOCI.get("excavation")));
                    bore.setItem(1, new ItemStack(Items.DIAMOND_PICKAXE));
                }
                level.setBlockAndUpdate(at.below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
                where.set(at.above());
            });
            BlockPos w = where.get();
            server.runCommand("tp @p " + (w.getX() - 2.5) + " " + (w.getY() - 0.5) + " " + (w.getZ() + 6.5) + " 160 10");
            context.waitTicks(90);
            context.takeScreenshot("broca_trabalhando");

            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                player.level().setBlockAndUpdate(where.get().below(2), Blocks.GRASS_BLOCK.defaultBlockState());
            });
            context.waitTicks(40);
            context.takeScreenshot("broca_parada");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                if (player.level().getBlockEntity(where.get()) instanceof ArcaneBoreBlockEntity bore) player.openMenu(bore);
            });
            context.waitTicks(20);
            context.takeScreenshot("broca_tela");
        }
    }
}
