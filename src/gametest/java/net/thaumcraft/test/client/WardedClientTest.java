package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.thaumcraft.block.ArcaneEarBlock;
import net.thaumcraft.block.ArcanePressurePlateBlock;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/** A porta arcana, as três placas, o ouvido (desligado e ligado) e uma parede de vidro protegido com um buraco. */
public class WardedClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("tp @p ~ ~ ~ 180 15");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos base = player.blockPosition().north(5);
                // a parede de vidro, 5 de largura por 3 de altura, com um buraco no meio de baixo
                for (int x = -2; x <= 2; x++) {
                    for (int y = 0; y < 3; y++) {
                        if (x == 0 && y == 0) continue;
                        level.setBlockAndUpdate(base.offset(x, y, -1), TCBlocks.WARDED_GLASS.defaultBlockState());
                    }
                }
                // a porta, à esquerda
                BlockPos door = base.offset(-4, 0, 0);
                level.setBlock(door, TCBlocks.ARCANE_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 2);
                level.setBlock(door.above(), TCBlocks.ARCANE_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 2);
                // as três placas e o ouvido, na frente
                for (int i = 0; i < 3; i++) {
                    level.setBlockAndUpdate(base.offset(i - 1, 0, 2), TCBlocks.ARCANE_PRESSURE_PLATE.defaultBlockState()
                            .setValue(ArcanePressurePlateBlock.SETTING, i));
                }
                level.setBlockAndUpdate(base.offset(3, 0, 1), TCBlocks.ARCANE_EAR.defaultBlockState());
                level.setBlockAndUpdate(base.offset(4, 0, 1), TCBlocks.ARCANE_EAR.defaultBlockState().setValue(ArcaneEarBlock.POWERED, true));
                var inv = player.getInventory();
                inv.setItem(0, new ItemStack(TCItems.ARCANE_DOOR));
                inv.setItem(1, new ItemStack(TCItems.IRON_KEY));
                inv.setItem(2, new ItemStack(TCItems.GOLD_KEY));
                inv.setItem(3, new ItemStack(TCItems.ARCANE_PRESSURE_PLATE));
                inv.setItem(4, new ItemStack(TCItems.ARCANE_EAR));
                inv.setItem(5, new ItemStack(TCItems.WARDED_GLASS));
                inv.setSelectedSlot(0);
            });
            context.waitTicks(40);
            context.takeScreenshot("porta_placa_ouvido_vidro");
        }
    }
}
