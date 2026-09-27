package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.occulta.BloodCrucibleBlockEntity;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.brazier.BrazierBlockEntity;
import net.thaumcraft.occulta.brazier.BrazierRecipes;

/**
 * As três máquinas vistas: a Roca, o Braseiro aceso e o Crisol com sangue dentro.
 */
public class OccultaMachinesClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");

            server.runOnServer(s -> {
                var mundo = s.overworld();
                var jogador = s.getPlayerList().getPlayers().getFirst();
                BlockPos pé = jogador.blockPosition();

                mundo.setBlockAndUpdate(pé.offset(-2, 0, 4), OccultaBlocks.SPINNING_WHEEL.defaultBlockState());
                mundo.setBlockAndUpdate(pé.offset(0, 0, 4), OccultaBlocks.BRAZIER.defaultBlockState());
                mundo.setBlockAndUpdate(pé.offset(2, 0, 4), OccultaBlocks.BLOOD_CRUCIBLE.defaultBlockState());

                // o braseiro aceso, com o Sinal de Fumaça dentro
                if (mundo.getBlockEntity(pé.offset(0, 0, 4)) instanceof BrazierBlockEntity braseiro) {
                    var fumaça = BrazierRecipes.of("tc.brazier.smoke");
                    if (fumaça != null) {
                        for (var coisa : fumaça.inputs()) braseiro.add(new ItemStack(coisa));
                    }
                    braseiro.light();
                }
                // e o crisol com três goles dentro
                if (mundo.getBlockEntity(pé.offset(2, 0, 4)) instanceof BloodCrucibleBlockEntity crisol) {
                    crisol.feed();
                    crisol.feed();
                    crisol.feed();
                }
                jogador.teleportTo(mundo, pé.getX() + 0.5, pé.getY() + 1.0, pé.getZ() + 0.5,
                        java.util.Set.of(), 0.0f, 20.0f, false);
            });
            context.waitTicks(30);
            context.takeScreenshot("ao_maquinas");

            // e a tela da Roca
            server.runCommand("item replace entity @p hotbar.0 with thaumcraft:attuned_stone");
            server.runCommand("item replace entity @p hotbar.1 with thaumcraft:golden_thread");
            server.runCommand("item replace entity @p hotbar.2 with minecraft:hay_block");
            server.runCommand("item replace entity @p hotbar.3 with thaumcraft:whiff_of_magic");
            context.waitTicks(10);
            context.takeScreenshot("ao_maquinas_itens");
        }
    }
}
