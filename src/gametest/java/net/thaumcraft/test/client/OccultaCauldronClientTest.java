package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.OccultaItems;

/**
 * O Caldeirão da Bruxa na tela: vazio, cheio e a ferver, com a cor do que tem dentro.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaCauldronClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            context.waitTicks(20);

            // o caldeirão vazio, de frente
            perto(server, "setblock ~ ~ ~3 thaumcraft:witches_cauldron");
            perto(server, "tp @p ~ ~ ~ 0 25");
            context.waitTicks(20);
            context.takeScreenshot("ao_caldeirao_vazio");

            // com água dentro
            perto(server, "data merge block ~ ~ ~3 {Water:3000}");
            context.waitTicks(20);
            context.takeScreenshot("ao_caldeirao_cheio");

            // e ao fogo, fervendo, com o que foi jogado dentro a tingir a água
            perto(server, "setblock ~ ~-1 ~3 minecraft:netherrack");
            perto(server, "setblock ~ ~-1 ~3 minecraft:netherrack");
            perto(server, "fill ~ ~-1 ~3 ~ ~-1 ~3 minecraft:netherrack");
            perto(server, "data merge block ~ ~ ~3 {Water:3000,Heated:100,"
                    + "Inside:[\"thaumcraft:mandrake_root\",\"thaumcraft:exhale_of_the_horned_one\"]}");
            context.waitTicks(30);
            context.takeScreenshot("ao_caldeirao_fervendo");
            perto(server, "tp @p ~ ~1 ~1 0 45");
            context.waitTicks(20);
            context.takeScreenshot("ao_caldeirao_de_cima");

            // e o que anda com ele, no inventário
            context.runOnClient(minecraft -> {
                var inv = minecraft.player.getInventory();
                int casa = 0;
                for (var item : new net.minecraft.world.item.Item[]{
                        OccultaItems.WITCHES_CAULDRON, OccultaItems.ANOINTING_PASTE,
                        OccultaItems.MUTANDIS, OccultaItems.MUTANDIS_EXTREMIS}) {
                    inv.setItem(casa++, new ItemStack(item));
                }
                minecraft.setScreenAndShow(
                        new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player));
            });
            context.waitTicks(20);
            context.takeScreenshot("ao_caldeirao_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }

    /** O comando corre no lugar de quem joga, e não na origem do mundo. */
    private static void perto(TestServerContext server, String command) {
        server.runCommand("execute at @p run " + command);
    }
}
