package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.nodes.NodeType;
import net.thaumcraft.block.entity.NodeBlockEntity;
import net.thaumcraft.block.entity.WandPedestalBlockEntity;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

/** O pedestal de recarga com o foco composto em cima e uma varinha bebendo de um nó ao lado; e os dois soltos. */
public class WandPedestalClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 180 15");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos at = player.blockPosition().offset(0, 0, -4);
                level.setBlockAndUpdate(at, TCBlocks.WAND_PEDESTAL.defaultBlockState());
                level.setBlockAndUpdate(at.above(), TCBlocks.RECHARGE_FOCUS.defaultBlockState());
                level.setBlockAndUpdate(at.offset(-2, 0, 0), TCBlocks.WAND_PEDESTAL.defaultBlockState());
                level.setBlockAndUpdate(at.offset(2, 0, 0), TCBlocks.RECHARGE_FOCUS.defaultBlockState());
                level.setBlockAndUpdate(at.offset(3, 2, -2), TCBlocks.NODE.defaultBlockState());
                if (level.getBlockEntity(at.offset(3, 2, -2)) instanceof NodeBlockEntity node) {
                    node.setup(new AspectList().add(Aspects.FIRE, 200).add(Aspects.MOTION, 200), NodeType.NORMAL, null);
                }
                if (level.getBlockEntity(at.offset(-2, 0, 0)) instanceof WandPedestalBlockEntity ped) {
                    ItemStack wand = new ItemStack(TCItems.WAND);
                    wand.set(TCComponents.WAND_ROD, "greatwood");
                    wand.set(TCComponents.WAND_CAP, "gold");
                    ped.setItem(0, wand);
                }
            });
            context.waitTicks(60);
            context.takeScreenshot("pedestal_recarga");
        }
    }
}
