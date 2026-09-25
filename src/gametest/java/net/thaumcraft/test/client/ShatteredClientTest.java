package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.thaumcraft.shattered.FabricBlocks;

/** Os tecidos dos Reinos Fragmentados, lado a lado, para se verem as cores. */
public class ShatteredClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set midnight");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = (net.minecraft.server.level.ServerLevel) player.level();
                BlockPos base = player.blockPosition().north(8).west(8);
                DyeColor[] cores = DyeColor.values();
                for (int i = 0; i < cores.length; i++) {
                    BlockPos onde = base.east(i);
                    level.setBlockAndUpdate(onde, FabricBlocks.FABRIC.get(cores[i]).defaultBlockState());
                    level.setBlockAndUpdate(onde.north(2), FabricBlocks.ANCIENT.get(cores[i]).defaultBlockState());
                }
                level.setBlockAndUpdate(base.north(4), FabricBlocks.ETERNAL.defaultBlockState());
                level.setBlockAndUpdate(base.north(4).east(2), FabricBlocks.UNRAVELLED.defaultBlockState());
            });
            server.runCommand("tp @p ~-1 ~2 ~ 170 20");
            context.waitTicks(40);
            context.takeScreenshot("tecidos");

            // e a aba do ramo no livro
            server.runCommand("thaumcraft pesquisa tudo");
            context.waitTicks(20);
            context.runOnClient(minecraft -> {
                net.thaumcraft.client.gui.ThaumonomiconScreen.select(net.thaumcraft.shattered.ShatteredRealms.CATEGORY);
                minecraft.setScreenAndShow(new net.thaumcraft.client.gui.ThaumonomiconScreen());
            });
            context.waitTicks(20);
            context.takeScreenshot("livro_shattered");
        }
    }
}
