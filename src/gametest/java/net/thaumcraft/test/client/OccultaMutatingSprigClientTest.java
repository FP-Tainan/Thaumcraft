package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.thaumcraft.occulta.MutatingSprigItem;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;

/**
 * O <b>Galho Mutante</b> visto, e o desenho que ele lê.
 *
 * <p>Duas fotos, e as duas são do mesmo lugar: a <b>figura montada</b> — trigo maduro cercado de Algodão
 * Sonhador, com água nas quatro quinas de baixo — e o que fica dela <b>depois do toque</b>, que é um pé de
 * losna sozinho no meio de um quadrado vazio.
 *
 * <p>É a única maneira de ver uma mutação: elas não têm tela, não têm receita desenhada em lugar nenhum, e
 * o que diz que funcionaram é o chão mudar.
 *
 * <p>E uma terceira do <b>galho na mão</b>, novo e quase gasto, porque a barra de desgaste é a única coisa
 * que ele mostra.
 */
public class OccultaMutatingSprigClientTest implements FabricClientGameTest {
    /** Onde a figura se monta, em relação a quem olha. */
    private static final int LONGE = 4;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative @a");
            server.runCommand("time set noon");
            server.runCommand("clear @p");

            // a figura: trigo maduro, quatro algodões e a água nas quinas
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                var level = jogador.level();
                BlockPos meio = jogador.blockPosition().relative(jogador.getDirection(), LONGE);
                monta(level, meio);
                jogador.getInventory().setSelectedSlot(0);
                jogador.getInventory().setItem(0, new ItemStack(OccultaItems.MUTATING_SPRIG));
            });
            context.waitTicks(20);
            context.takeScreenshot("o_desenho_da_losna");

            // e o toque, que deixa um pé de losna sozinho
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                var level = jogador.level();
                BlockPos meio = jogador.blockPosition().relative(jogador.getDirection(), LONGE);
                for (Direction lado : Direction.Plane.HORIZONTAL) level.removeBlock(meio.relative(lado), false);
                level.setBlock(meio, OccultaBlocks.WORMWOOD.defaultBlockState(), 3);
            });
            context.waitTicks(20);
            context.takeScreenshot("a_losna_que_ficou");

            // e o galho na mão, novo e quase gasto
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                ItemStack novo = new ItemStack(OccultaItems.MUTATING_SPRIG);
                ItemStack gasto = new ItemStack(OccultaItems.MUTATING_SPRIG);
                gasto.set(DataComponents.DAMAGE, MutatingSprigItem.TOQUES - 2);
                jogador.getInventory().setItem(0, novo);
                jogador.getInventory().setItem(1, gasto);
            });
            server.runCommand("gamemode survival @a");
            context.waitTicks(10);
            context.getInput().setCursorPos(0, 0);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("o_galho_novo_e_o_gasto");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }

    /** Trigo maduro cercado de Algodão Sonhador, com a água nas quatro quinas de baixo. */
    private static void monta(net.minecraft.world.level.Level level, BlockPos meio) {
        level.setBlock(meio.below(), Blocks.FARMLAND.defaultBlockState(), 3);
        level.setBlock(meio, Blocks.WHEAT.defaultBlockState()
                .setValue(BlockStateProperties.AGE_7, 7), 3);
        for (Direction lado : Direction.Plane.HORIZONTAL) {
            level.setBlock(meio.relative(lado).below(), Blocks.DIRT.defaultBlockState(), 3);
            level.setBlock(meio.relative(lado), OccultaBlocks.WISPY_COTTON.defaultBlockState(), 3);
        }
        for (BlockPos quina : net.thaumcraft.occulta.Mutations.quinas(meio)) {
            level.setBlock(quina.below(), Blocks.WATER.defaultBlockState(), 3);
        }
    }
}
