package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.arcana.ArcanaBlocks;
import net.thaumcraft.arcana.InscriptionTableBlock;

/**
 * Os dois blocos do Ars Arcana vistos: o <b>Óculus</b> e a <b>Mesa de Inscrição</b>, com os modelos do
 * original.
 *
 * <p>Eles eram cubos até agora. As caixas vêm dos modelos Techne do Ars Magica 2, convertidas uma a uma — 28
 * no Óculus, 15 na Mesa — e esta foto existe para provar que a conversão bateu: se a conta do giro de 180° em
 * Z estiver errada, o bloco sai de cabeça para baixo ou fora do lugar, e nenhuma prova de servidor diria nada.
 */
public class ArcanaBlocksClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");

            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                var level = (ServerLevel) jogador.level();

                // um chão claro, para as silhuetas se lerem
                for (int x = -2; x <= 6; x++) {
                    for (int z = -3; z <= 3; z++) {
                        level.setBlockAndUpdate(new BlockPos(x, -60, z),
                                Blocks.SMOOTH_QUARTZ.defaultBlockState());
                    }
                }

                level.setBlockAndUpdate(new BlockPos(3, -59, -1),
                        ArcanaBlocks.OCCULUS.defaultBlockState());
                level.setBlockAndUpdate(new BlockPos(3, -59, 1),
                        ArcanaBlocks.INSCRIPTION_TABLE.defaultBlockState()
                                .setValue(InscriptionTableBlock.FACING, Direction.WEST));
            });

            // de frente, ao nível dos olhos
            server.runCommand("tp @p -1 -59 0 -90 0");
            context.waitTicks(30);
            context.takeScreenshot("aa_blocos");

            // e de cima, para se ver o feitio de cada um
            server.runCommand("tp @p 3 -54 0 -90 55");
            context.waitTicks(20);
            context.takeScreenshot("aa_blocos_de_cima");
        }
    }
}
