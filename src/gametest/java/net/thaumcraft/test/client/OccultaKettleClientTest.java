package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.thaumcraft.occulta.OccultaBlocks;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.kettle.KettleBlockEntity;
import net.thaumcraft.occulta.kettle.KettleRecipes;

import java.util.concurrent.atomic.AtomicReference;

/**
 * O Caldeirão de Pote visto: o pote de ferro nas correntes, a barra de cima, a tampa de líquido pintada da cor do
 * que estiver dentro e os frascos na borda.
 */
public class OccultaKettleClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");

            AtomicReference<BlockPos> onde = new AtomicReference<>();
            server.runOnServer(s -> {
                var mundo = s.overworld();
                var jogador = s.getPlayerList().getPlayers().getFirst();
                BlockPos pé = jogador.blockPosition();
                BlockPos pote = pé.offset(0, 1, 5);
                onde.set(pote);
                // o lume só fica de pé sobre o netherrack
                mundo.setBlockAndUpdate(pote.below(2), Blocks.NETHERRACK.defaultBlockState());
                mundo.setBlockAndUpdate(pote.below(), Blocks.FIRE.defaultBlockState());
                mundo.setBlockAndUpdate(pote, OccultaBlocks.WITCHES_KETTLE.defaultBlockState());
                // e quem olha fica um pouco acima, de frente, como quem o vai encher
                jogador.teleportTo(mundo, pé.getX() + 0.5, pé.getY() + 2.0, pé.getZ() + 0.5,
                        java.util.Set.of(), 0.0f, 25.0f, false);
            });
            context.waitTicks(30);
            context.takeScreenshot("ao_pote_vazio");

            // cheio de água, com frascos na borda e a cor da Sopa de Redstone
            server.runOnServer(s -> {
                if (!(s.overworld().getBlockEntity(onde.get()) instanceof KettleBlockEntity pote)) {
                    throw new IllegalStateException("o pote devia ter alma");
                }
                pote.fill();
                pote.throwIn(new ItemStack(Items.GLASS_BOTTLE, 2));
                var sopa = KettleRecipes.of(OccultaItems.REDSTONE_SOUP);
                if (sopa != null) {
                    for (var coisa : sopa.inputs()) pote.throwIn(new ItemStack(coisa));
                }
            });
            context.waitTicks(40);
            context.takeScreenshot("ao_pote_cheio");

            // e o pote na mão, com o que os bichos deixam
            server.runCommand("item replace entity @p weapon.mainhand with thaumcraft:witches_kettle");
            server.runCommand("item replace entity @p hotbar.1 with thaumcraft:dog_tongue");
            server.runCommand("item replace entity @p hotbar.2 with thaumcraft:creeper_heart");
            server.runCommand("item replace entity @p hotbar.3 with thaumcraft:toe_of_frog");
            server.runCommand("item replace entity @p hotbar.4 with thaumcraft:redstone_soup");
            context.waitTicks(10);
            context.takeScreenshot("ao_pote_item");
        }
    }
}
