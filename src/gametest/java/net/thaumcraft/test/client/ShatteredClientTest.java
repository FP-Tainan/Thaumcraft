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

            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(10);

            // as portas comuns do ramo e o alçapão dimensional
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = (net.minecraft.server.level.ServerLevel) player.level();
                BlockPos base = player.blockPosition().north(5);
                // duas ombreiras de pedra para as portas, e um pilar baixo para o alçapão
                for (int i = 0; i < 2; i++) {
                    for (int y = 0; y < 4; y++) {
                        level.setBlockAndUpdate(base.east(i * 3 - 3).below().above(y),
                                net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState());
                    }
                }
                level.setBlockAndUpdate(base.east(3).below(),
                        net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState());
                porta(level, base.west(3), net.thaumcraft.shattered.ShatteredBlocks.GOLD_DOOR);
                porta(level, base, net.thaumcraft.shattered.ShatteredBlocks.QUARTZ_DOOR);
                level.setBlockAndUpdate(base.east(3),
                        net.thaumcraft.shattered.ShatteredBlocks.DIMENSIONAL_TRAPDOOR.defaultBlockState());
            });
            server.runCommand("time set noon");
            // os Óculos do Véu no rosto: sem eles o vão de uma porta que ninguém assentou não se vê
            server.runCommand("item replace entity @p armor.head with thaumcraft:veil_goggles");
            server.runCommand("tp @p ~ ~1 ~ 180 0");
            context.waitTicks(40);
            context.takeScreenshot("portas_do_ramo");

            // e os óculos, no rosto e na mão
            context.runOnClient(minecraft -> {
                minecraft.player.getInventory().setItem(0,
                        new net.minecraft.world.item.ItemStack(net.thaumcraft.shattered.ShatteredItems.VEIL_GOGGLES));
                minecraft.player.getInventory().setSelectedSlot(0);
                minecraft.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);
            });
            context.waitTicks(20);
            context.takeScreenshot("oculos_do_veu");
            context.runOnClient(minecraft -> minecraft.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
        }
    }

    /** Põe uma porta de duas metades, que é como o jogo a assenta. */
    private static void porta(net.minecraft.server.level.ServerLevel level, BlockPos baixo,
                              net.minecraft.world.level.block.Block bloco) {
        var estado = bloco.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.FACING, net.minecraft.core.Direction.SOUTH);
        level.setBlockAndUpdate(baixo, estado.setValue(net.minecraft.world.level.block.DoorBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER));
        level.setBlockAndUpdate(baixo.above(), estado.setValue(net.minecraft.world.level.block.DoorBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
    }
}
