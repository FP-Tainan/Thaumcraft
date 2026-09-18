package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.world.GreatwoodTree;
import net.thaumcraft.world.SilverwoodTree;

/** A grande-madeira e o pinheiro-de-prata lado a lado, de dia e de noite. */
public class MagicalTreeClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            singleplayer.getServer().runCommand("gamemode creative");
            singleplayer.getServer().runCommand("time set noon");
            singleplayer.getServer().runCommand("weather clear");
            singleplayer.getServer().runCommand("execute as @p at @s run tp @s ~ ~ ~ 0 -10");
            singleplayer.getServer().runOnServer(server -> {
                var level = server.overworld();
                var player = server.getPlayerList().getPlayers().get(0);
                BlockPos here = player.blockPosition();
                BlockPos great = here.offset(-7, 0, 22);
                BlockPos silver = here.offset(7, 0, 14);
                for (BlockPos base : new BlockPos[]{great, silver}) {
                    for (int x = -1; x <= 2; x++) {
                        for (int z = -1; z <= 2; z++) level.setBlock(base.offset(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                    }
                }
                boolean g = false, s = false;
                for (int i = 0; i < 20 && !g; i++) g = GreatwoodTree.generate(level, level.getRandom(), great, false, false);
                for (int i = 0; i < 20 && !s; i++) s = SilverwoodTree.generate(level, level.getRandom(), silver, 7, 4, true);
                // um pouco de tudo na mão e no chão, para ver os itens
                player.getInventory().add(new net.minecraft.world.item.ItemStack(net.thaumcraft.registry.TCItems.GREATWOOD_LEAVES));
                player.getInventory().add(new net.minecraft.world.item.ItemStack(net.thaumcraft.registry.TCItems.SILVERWOOD_LEAVES));
                player.getInventory().add(new net.minecraft.world.item.ItemStack(net.thaumcraft.registry.TCItems.GREATWOOD_SAPLING));
                player.getInventory().add(new net.minecraft.world.item.ItemStack(net.thaumcraft.registry.TCItems.SILVERWOOD_SAPLING));
                player.getInventory().add(new net.minecraft.world.item.ItemStack(net.thaumcraft.registry.TCItems.GREATWOOD_LOG));
                player.getInventory().add(new net.minecraft.world.item.ItemStack(net.thaumcraft.registry.TCItems.SILVERWOOD_LOG));
                player.getInventory().add(new net.minecraft.world.item.ItemStack(net.thaumcraft.registry.TCItems.GREATWOOD_PLANKS));
                player.getInventory().add(new net.minecraft.world.item.ItemStack(net.thaumcraft.registry.TCItems.SILVERWOOD_PLANKS));
                player.getInventory().add(new net.minecraft.world.item.ItemStack(net.thaumcraft.registry.TCItems.SILVERWOOD_STAIRS));
            });
            context.waitTicks(60);
            context.takeScreenshot("arvores_magicas_dia");
            singleplayer.getServer().runCommand("time set midnight");
            context.waitTicks(20);
            context.takeScreenshot("arvores_magicas_noite");
        }
    }
}
