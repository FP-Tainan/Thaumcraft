package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.block.MirrorBlock;
import net.thaumcraft.block.entity.LinkedMirrorBlockEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCItems;

/** Uma parede com um espelho sem par, um par ligado e um espelho de essência; um no chão; os itens na barra. */
public class MirrorClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("tp @p ~ ~ ~ 180 20");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos base = player.blockPosition().north(3);
                for (int x = -3; x <= 3; x++) {
                    for (int y = 0; y < 3; y++) level.setBlockAndUpdate(base.offset(x, y, -1), Blocks.STONE_BRICKS.defaultBlockState());
                }
                var south = TCBlocks.MIRROR.defaultBlockState().setValue(MirrorBlock.FACING, Direction.SOUTH);
                level.setBlockAndUpdate(base.offset(-2, 1, 0), south);
                level.setBlockAndUpdate(base.offset(0, 1, 0), south);
                level.setBlockAndUpdate(base.offset(0, 2, 0), south);
                level.setBlockAndUpdate(base.offset(2, 1, 0), TCBlocks.ESSENTIA_MIRROR.defaultBlockState().setValue(MirrorBlock.FACING, Direction.SOUTH));
                level.setBlockAndUpdate(base.offset(1, 0, 1), TCBlocks.MIRROR.defaultBlockState().setValue(MirrorBlock.FACING, Direction.UP));
                // o par do meio: um em cima do outro
                if (level.getBlockEntity(base.offset(0, 1, 0)) instanceof LinkedMirrorBlockEntity m1
                        && level.getBlockEntity(base.offset(0, 2, 0)) instanceof LinkedMirrorBlockEntity m2) {
                    m1.setLink(base.offset(0, 2, 0), level.dimension());
                    m2.setLink(base.offset(0, 1, 0), level.dimension());
                    m1.linked = true;
                    m2.linked = true;
                    m1.sync();
                    m2.sync();
                }
                ItemStack linked = new ItemStack(TCItems.MIRROR);
                CompoundTag tag = new CompoundTag();
                tag.putInt("linkX", 0);
                linked.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                var inv = player.getInventory();
                inv.setItem(0, new ItemStack(TCItems.MIRROR));
                inv.setItem(1, linked);
                inv.setItem(2, new ItemStack(TCItems.ESSENTIA_MIRROR));
                inv.setItem(3, new ItemStack(TCItems.HAND_MIRROR));
                inv.setSelectedSlot(0);
            });
            context.waitTicks(40);
            context.takeScreenshot("espelhos");
            server.runCommand("tp @p ~ ~ ~ 180 70");
            context.waitTicks(10);
            context.takeScreenshot("espelho_no_chao");
        }
    }
}
