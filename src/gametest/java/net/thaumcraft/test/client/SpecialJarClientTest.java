package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.nodes.NodeType;
import net.thaumcraft.block.entity.NodeJarBlockEntity;
import net.thaumcraft.item.JarredNode;
import net.thaumcraft.registry.TCBlocks;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCItems;

/** O jarro de cérebro e o nó no jarro, com óculos para ver o nó; e os dois na barra. */
public class SpecialJarClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("execute as @p at @s run tp @s ~ ~ ~ 180 30");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();
                BlockPos at = player.blockPosition().offset(0, 0, -2);
                level.setBlockAndUpdate(at.west(), TCBlocks.BRAIN_JAR.defaultBlockState());
                level.setBlockAndUpdate(at.east(), TCBlocks.NODE_JAR.defaultBlockState());
                AspectList aura = new AspectList().add(Aspects.AIR, 30).add(Aspects.FIRE, 20).add(Aspects.ORDER, 15);
                if (level.getBlockEntity(at.east()) instanceof NodeJarBlockEntity jar) jar.setup(aura, NodeType.NORMAL, null);
                player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new ItemStack(TCItems.GOGGLES));
                player.getInventory().setItem(0, new ItemStack(TCItems.BRAIN_JAR));
                ItemStack nodeJar = new ItemStack(TCItems.NODE_JAR);
                nodeJar.set(TCComponents.JARRED_NODE, JarredNode.of(aura, NodeType.NORMAL, null, ""));
                player.getInventory().setItem(1, nodeJar);
            });
            context.waitTicks(40);
            context.takeScreenshot("jarros_especiais");
        }
    }
}
