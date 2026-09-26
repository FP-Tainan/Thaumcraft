package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.thaumcraft.shattered.ShatteredBlocks;

/**
 * A Porta Antiga dentro da ombreira dela: vazia de cabeça descoberta, com porta para quem tem os Óculos do Véu.
 *
 * <p>É o retrato do que quem manda mostrou numa foto, e a única maneira de conferir que a folha desenhada pelo
 * miolo assenta onde a do bloco assentaria.
 */
public class AncientDoorClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative @p");
            server.runCommand("time set noon");

            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = (net.minecraft.server.level.ServerLevel) player.level();
                BlockPos meio = player.blockPosition().north(5);
                // a ombreira: duas colunas e a verga, como a que nasce no mundo
                for (int altura = 0; altura < 2; altura++) {
                    for (int qual = -1; qual <= 1; qual += 2) {
                        level.setBlockAndUpdate(meio.east(qual).above(altura),
                                Blocks.STONE_BRICKS.defaultBlockState());
                    }
                }
                for (int a = -1; a <= 1; a++) {
                    level.setBlockAndUpdate(meio.east(a).above(2), Blocks.STONE_BRICKS.defaultBlockState());
                }
                var porta = ShatteredBlocks.ANCIENT_DIMENSIONAL_DOOR.defaultBlockState()
                        .setValue(DoorBlock.FACING, Direction.SOUTH)
                        .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
                level.setBlockAndUpdate(meio, porta);
                level.setBlockAndUpdate(meio.above(), porta.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
            });
            server.runCommand("tp @p ~ ~ ~ 180 0");
            context.waitTicks(40);
            context.takeScreenshot("porta_antiga_sem_oculos");

            server.runCommand("item replace entity @p armor.head with thaumcraft:veil_goggles");
            context.waitTicks(40);
            context.takeScreenshot("porta_antiga_com_oculos");
        }
    }
}
