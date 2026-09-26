package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.OccultaItems;

/**
 * As três árvores do ofício na tela: a sorveira pequena, o amieiro estreito e o espinheiro-alvar largo, cada uma
 * nascida da muda dela.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaTreesClientTest implements FabricClientGameTest {
    private static final String[] ÁRVORES = {"rowan", "alder", "hawthorn"};

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            context.waitTicks(20);

            for (String árvore : ÁRVORES) {
                // terreno limpo para cada uma, e a muda plantada a três passos, que é o alcance de quem joga
                perto(server, "tp @p ~ ~ ~30 0 0");
                context.waitTicks(20);
                perto(server, "fill ~-7 ~-1 ~-3 ~7 ~-1 ~10 minecraft:dirt");
                perto(server, "setblock ~ ~ ~3 thaumcraft:" + árvore + "_sapling[stage=1]");
                context.waitTicks(5);
                // farinha de osso na muda, que é como quem joga a apressa; quem a dá é o servidor, porque só o
                // que ele sabe da mão de quem joga é que vale no clique
                server.runCommand("item replace entity @p hotbar.0 with minecraft:bone_meal 64");
                context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(0));
                context.waitTicks(5);
                for (int volta = 0; volta < 8; volta++) {
                    context.runOnClient(minecraft -> {
                        var onde = minecraft.player.blockPosition().offset(0, 0, 3);
                        var hit = new net.minecraft.world.phys.BlockHitResult(
                                net.minecraft.world.phys.Vec3.atCenterOf(onde),
                                net.minecraft.core.Direction.UP, onde, false);
                        minecraft.gameMode.useItemOn(minecraft.player,
                                net.minecraft.world.InteractionHand.MAIN_HAND, hit);
                    });
                    context.waitTicks(5);
                }
                // e de longe, para caber a árvore inteira
                perto(server, "tp @p ~ ~ ~-8 0 -15");
                context.waitTicks(20);
                context.takeScreenshot("ao_arvore_" + árvore);
                perto(server, "tp @p ~ ~ ~8 0 0");
                context.waitTicks(5);
            }

            // e a madeira das três, no inventário
            context.runOnClient(minecraft -> {
                var inv = minecraft.player.getInventory();
                int casa = 0;
                for (var item : OccultaItems.WOOD.values()) inv.setItem(casa++, new ItemStack(item));
                inv.setItem(casa, new ItemStack(OccultaItems.ROWAN_BERRIES));
                minecraft.setScreenAndShow(
                        new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player));
            });
            context.waitTicks(20);
            context.takeScreenshot("ao_madeiras");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }

    /** O comando corre no lugar de quem joga, e não na origem do mundo. */
    private static void perto(TestServerContext server, String command) {
        server.runCommand("execute at @p run " + command);
    }
}
