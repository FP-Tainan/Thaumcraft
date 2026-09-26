package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.thaumcraft.shattered.ShatteredBlocks;

/**
 * Uma fenda solta no mundo, a comer a pedra em volta — e o que os Óculos do Véu fazem por ela.
 *
 * <p>São quatro retratos: a fenda sem os óculos, que é para não se ver nada; a mesma com eles no rosto, recém
 * nascida; ela crescida; e a de quem a fez, que aparece de cabeça descoberta.
 */
public class RiftWorldClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = (net.minecraft.server.level.ServerLevel) player.level();
                BlockPos onde = player.blockPosition().north(5).above(1);
                // uma parede de pedra atrás, para se ver o que a fenda come
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dy = -1; dy <= 4; dy++) {
                        level.setBlockAndUpdate(onde.offset(dx, dy, -2),
                                net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                    }
                }
                level.setBlockAndUpdate(onde, ShatteredBlocks.RIFT.defaultBlockState());
                // e ela cresce logo, senão o rasgão é pequeno demais para se ver que falta
                if (level.getBlockEntity(onde) instanceof net.thaumcraft.shattered.RiftBlockEntity fenda) {
                    for (int i = 0; i < 2000; i++) fenda.grow();
                }
            });
            server.runCommand("tp @p ~ ~ ~ 180 0");
            context.waitTicks(40);
            // esta fenda nasceu com o mundo: de cabeça descoberta ela é ar
            context.takeScreenshot("fenda_sem_oculos");

            server.runCommand("item replace entity @p armor.head with thaumcraft:veil_goggles");
            context.waitTicks(40);
            context.takeScreenshot("fenda_com_oculos");

            // e uma fenda que o próprio thaumaturgo rasgou aparece sem óculos nenhuns
            server.runCommand("item replace entity @p armor.head with air");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = (net.minecraft.server.level.ServerLevel) player.level();
                BlockPos onde = player.blockPosition().north(5).above(1);
                if (level.getBlockEntity(onde) instanceof net.thaumcraft.shattered.RiftBlockEntity fenda) {
                    fenda.setNatural(false);
                }
            });
            context.waitTicks(40);
            context.takeScreenshot("fenda_nossa_sem_oculos");

            // e a mesma crescida de todo, que é o tamanho a que ela para
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = (net.minecraft.server.level.ServerLevel) player.level();
                BlockPos onde = player.blockPosition().north(5).above(1);
                if (level.getBlockEntity(onde) instanceof net.thaumcraft.shattered.RiftBlockEntity fenda) {
                    for (int i = 0; i < 40000; i++) fenda.grow();
                }
            });
            context.waitTicks(40);
            context.takeScreenshot("fenda_crescida_de_todo");
        }
    }
}
