package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.SilverVatBlockEntity;

/**
 * Três <b>Tinas de Prata</b> lado a lado, para as duas coisas que o corpo dela conta se verem.
 *
 * <p>A primeira está <b>sozinha</b>: sem máquina ao lado, sem bico nenhum. A segunda tem uma <b>fornalha</b>
 * de cada lado, e por isso dois bicos. A terceira está <b>cheia</b>, com as oito camadas de prata à vista.
 *
 * <p>É o melhor exemplo do mod de um bloco que conta o seu estado pelo corpo, e não por um número numa
 * tela — e é a única maneira de o conferir.
 */
public class OccultaSilverVatClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode spectator @p");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            server.runOnServer(s -> {
                var level = s.overworld();
                var player = s.getPlayerList().getPlayers().getFirst();
                BlockPos meio = player.blockPosition();

                for (int x = -6; x <= 6; x++) {
                    for (int z = 0; z <= 8; z++) {
                        level.setBlockAndUpdate(meio.offset(x, -1, z),
                                Blocks.STONE_BRICKS.defaultBlockState());
                    }
                }

                // a sozinha
                BlockPos só = meio.offset(-3, 0, 4);
                level.setBlockAndUpdate(só, OccultaBlocks.SILVER_VAT.defaultBlockState());

                // a de dois bicos, com uma fornalha de cada lado
                BlockPos entre = meio.offset(0, 0, 4);
                level.setBlockAndUpdate(entre, OccultaBlocks.SILVER_VAT.defaultBlockState());
                level.setBlockAndUpdate(entre.north(), Blocks.FURNACE.defaultBlockState());
                level.setBlockAndUpdate(entre.south(), Blocks.FURNACE.defaultBlockState());

                // e a cheia
                BlockPos cheia = meio.offset(3, 0, 4);
                level.setBlockAndUpdate(cheia, OccultaBlocks.SILVER_VAT.defaultBlockState());
                if (level.getBlockEntity(cheia) instanceof SilverVatBlockEntity tina) {
                    tina.prata(new ItemStack(OccultaItems.SILVER_DUST, 64));
                }

                player.snapTo(meio.getX() + 0.5, meio.getY() + 3.0, meio.getZ() + 1.0, 0.0f, 55.0f);
                player.setDeltaMovement(Vec3.ZERO);
            });
            context.waitTicks(40);
            context.takeScreenshot("a_tina_de_prata");
        }
    }
}
