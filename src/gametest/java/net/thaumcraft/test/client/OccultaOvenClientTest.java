package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.OccultaItems;

/**
 * O Forno das Bruxas e os funis na tela: o feitio deles muda com o que têm ao lado, e é isso que estas fotos
 * mostram.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaOvenClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            context.waitTicks(20);

            // o forno sozinho, virado para quem o olha
            perto(server, "setblock ~ ~ ~4 thaumcraft:witches_oven[facing=south]");
            perto(server, "tp @p ~ ~ ~1 0 15");
            context.waitTicks(20);
            context.takeScreenshot("ao_forno");

            // com um funil em cima, que vira cano
            perto(server, "setblock ~ ~1 ~3 thaumcraft:fume_funnel[facing=south]");
            context.waitTicks(20);
            context.takeScreenshot("ao_forno_com_cano");

            // e com um funil de cada lado, que puxam a canalização para o forno
            perto(server, "setblock ~1 ~ ~3 thaumcraft:fume_funnel[facing=south]");
            perto(server, "setblock ~-1 ~ ~3 thaumcraft:filtered_fume_funnel[facing=south]");
            context.waitTicks(20);
            context.takeScreenshot("ao_forno_com_funis");
            perto(server, "tp @p ~ ~1 ~-2 0 25");
            context.waitTicks(20);
            context.takeScreenshot("ao_forno_de_cima");

            // um funil sozinho, que mostra o corpo largo, e o com filtro ao lado dele
            perto(server, "tp @p ~8 ~ ~ 0 5");
            context.waitTicks(10);
            perto(server, "setblock ~ ~ ~3 thaumcraft:fume_funnel[facing=south]");
            perto(server, "setblock ~2 ~ ~3 thaumcraft:filtered_fume_funnel[facing=south]");
            context.waitTicks(20);
            context.takeScreenshot("ao_funis_sozinhos");

            // a tela do forno, com um forno novo à frente de quem joga
            perto(server, "tp @p ~20 ~ ~ 0 5");
            context.waitTicks(10);
            perto(server, "setblock ~ ~ ~3 thaumcraft:witches_oven[facing=south]");
            perto(server, "data merge block ~ ~ ~3 {Items:["
                    + "{Slot:0b,id:\"minecraft:birch_sapling\",count:32},"
                    + "{Slot:1b,id:\"minecraft:coal\",count:32},"
                    + "{Slot:4b,id:\"thaumcraft:clay_jar\",count:16}]}");
            context.waitTicks(60);
            context.runOnClient(minecraft -> {
                var onde = minecraft.player.blockPosition().offset(0, 0, 3);
                var hit = new net.minecraft.world.phys.BlockHitResult(
                        net.minecraft.world.phys.Vec3.atCenterOf(onde), net.minecraft.core.Direction.UP, onde, false);
                minecraft.player.getInventory().setSelectedSlot(8);
                minecraft.gameMode.useItemOn(minecraft.player, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
            });
            context.waitTicks(30);
            context.takeScreenshot("ao_forno_tela");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);

            // e as coisas que saem dele, no inventário
            context.runOnClient(minecraft -> {
                var inv = minecraft.player.getInventory();
                int casa = 0;
                for (var item : new net.minecraft.world.item.Item[]{
                        OccultaItems.WITCHES_OVEN, OccultaItems.FUME_FUNNEL, OccultaItems.FILTERED_FUME_FUNNEL,
                        OccultaItems.SOFT_CLAY_JAR, OccultaItems.CLAY_JAR, OccultaItems.WOOD_ASH,
                        OccultaItems.FOUL_FUME, OccultaItems.EXHALE_OF_THE_HORNED_ONE,
                        OccultaItems.BREATH_OF_THE_GODDESS, OccultaItems.HINT_OF_REBIRTH,
                        OccultaItems.WHIFF_OF_MAGIC, OccultaItems.REEK_OF_MISFORTUNE,
                        OccultaItems.ODOUR_OF_PURITY, OccultaItems.FUME_FILTER}) {
                    inv.setItem(casa++, new ItemStack(item));
                }
                minecraft.setScreenAndShow(
                        new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player));
            });
            context.waitTicks(20);
            context.takeScreenshot("ao_forno_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }

    /** O comando corre no lugar de quem joga, e não na origem do mundo. */
    private static void perto(TestServerContext server, String command) {
        server.runCommand("execute at @p run " + command);
    }
}
