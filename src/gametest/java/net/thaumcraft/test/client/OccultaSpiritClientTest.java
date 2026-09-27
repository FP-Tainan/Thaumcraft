package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaEntities;
import net.thaumcraft.occulta.spirit.CorpseEntity;
import net.thaumcraft.occulta.spirit.DreamCatcherBlock;
import net.thaumcraft.occulta.spirit.DreamCatcherBlockEntity;
import net.thaumcraft.occulta.spirit.DreamWeaveItem;
import net.thaumcraft.occulta.spirit.NightmareEntity;
import net.thaumcraft.occulta.spirit.SpiritWorld;

/**
 * O outro lado visto: o quarto de sonho com os apanhadores na parede, o corpo caído no chão, o Pesadelo de pé e o
 * Mundo dos Espíritos.
 *
 * <p><b>Quem olha fica a nove de distância dos apanhadores, e é de propósito.</b> Eles pegam a cinco, e a teia
 * dos pesadelos dá <b>Cegueira</b> a quem estiver dentro desse alcance: de perto, a foto sai preta — o que é o
 * porte funcionando, e não falhando.
 */
public class OccultaSpiritClientTest implements FabricClientGameTest {
    /** A que distância a parede fica de quem olha: mais do que o alcance do apanhador. */
    private static final int LONGE = 9;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");

            java.util.concurrent.atomic.AtomicReference<BlockPos> chão = new java.util.concurrent.atomic.AtomicReference<>();
            server.runOnServer(s -> {
                ServerLevel mundo = s.overworld();
                var jogador = s.getPlayerList().getPlayers().getFirst();
                BlockPos pé = jogador.blockPosition();
                chão.set(pé);
                BlockPos parede = pé.offset(0, 0, LONGE);

                for (int x = -3; x <= 3; x++) {
                    for (int y = 0; y <= 3; y++) {
                        mundo.setBlockAndUpdate(parede.offset(x, y, 0), Blocks.STONE_BRICKS.defaultBlockState());
                    }
                }
                prega(mundo, parede.offset(-2, 1, -1), DreamWeaveItem.Weave.MOVE);
                prega(mundo, parede.offset(0, 1, -1), DreamWeaveItem.Weave.NIGHTMARE);
                prega(mundo, parede.offset(2, 1, -1), DreamWeaveItem.Weave.INTENSITY);
                mundo.setBlockAndUpdate(parede.offset(-3, 0, -2), OccultaBlocks.WISPY_COTTON.defaultBlockState());
                mundo.setBlockAndUpdate(parede.offset(3, 0, -2), OccultaBlocks.GLINT_WEED.defaultBlockState());

                jogador.teleportTo(mundo, pé.getX() + 0.5, pé.getY(), pé.getZ() + 0.5,
                        java.util.Set.of(), 0.0f, 5.0f, false);
            });
            context.waitTicks(30);
            context.takeScreenshot("ao_quarto_de_sonho");

            // o corpo caído, que é o que fica de cá quando o espírito passa
            server.runOnServer(s -> {
                ServerLevel mundo = s.overworld();
                var jogador = s.getPlayerList().getPlayers().getFirst();
                BlockPos pé = chão.get();
                // o corpo fica exatamente onde o espírito estava: por isso quem o deixa tem de estar no chão
                jogador.teleportTo(mundo, pé.getX() + 0.5, pé.getY(), pé.getZ() + 0.5,
                        java.util.Set.of(), 0.0f, 0.0f, false);
                CorpseEntity.lay(mundo, jogador);
                jogador.teleportTo(mundo, pé.getX() + 0.5, pé.getY() + 1.0, pé.getZ() - 4.5,
                        java.util.Set.of(), 0.0f, 25.0f, false);
            });
            context.waitTicks(40);
            context.takeScreenshot("ao_corpo_adormecido");

            // e o outro lado, visto de dentro, com um Pesadelo — que é o único lugar onde ele fica de pé
            server.runOnServer(s -> {
                ServerLevel lá = SpiritWorld.level(s);
                if (lá == null) throw new IllegalStateException("o mundo dos espíritos devia abrir");
                // o pedaço tem de estar feito antes de se lhe perguntar a altura
                lá.getChunk(0, 0);
                int alto = lá.getHeight(Heightmap.Types.MOTION_BLOCKING, 8, 8);

                NightmareEntity pesadelo = OccultaEntities.NIGHTMARE.create(lá, SpiritWorld.reason());
                if (pesadelo != null) {
                    pesadelo.snapTo(8.5, alto + 1.0, 13.5, 180.0f, 0.0f);
                    pesadelo.setNoAi(true);
                    lá.addFreshEntity(pesadelo);
                }
                s.getPlayerList().getPlayers().getFirst().teleportTo(lá, 8.5, alto + 1.0, 8.5,
                        java.util.Set.of(), 0.0f, 5.0f, false);
            });
            context.waitTicks(100);
            context.takeScreenshot("ao_mundo_dos_espiritos");

            // as teias e o que se bebe, na barra
            server.runCommand("item replace entity @p hotbar.0 with thaumcraft:brew_of_sleeping");
            server.runCommand("item replace entity @p hotbar.1 with thaumcraft:brew_of_flowing_spirit");
            server.runCommand("item replace entity @p hotbar.2 with thaumcraft:sleeping_apple");
            server.runCommand("item replace entity @p hotbar.3 with thaumcraft:dream_weave_move");
            server.runCommand("item replace entity @p hotbar.4 with thaumcraft:dream_weave_dig");
            server.runCommand("item replace entity @p hotbar.5 with thaumcraft:dream_weave_eat");
            server.runCommand("item replace entity @p hotbar.6 with thaumcraft:dream_weave_nightmare");
            server.runCommand("item replace entity @p hotbar.7 with thaumcraft:dream_weave_intensity");
            server.runCommand("item replace entity @p hotbar.8 with thaumcraft:mellifluous_hunger");
            context.waitTicks(10);
            context.takeScreenshot("ao_sonho_itens");
        }
    }

    /** Prega um apanhador naquele lugar, virado para o sul, com aquela teia dentro. */
    private static void prega(ServerLevel mundo, BlockPos onde, DreamWeaveItem.Weave qual) {
        mundo.setBlockAndUpdate(onde, OccultaBlocks.DREAM_CATCHER.defaultBlockState()
                .setValue(DreamCatcherBlock.FACING, Direction.SOUTH));
        if (mundo.getBlockEntity(onde) instanceof DreamCatcherBlockEntity alma) alma.setWeave(qual);
    }
}
