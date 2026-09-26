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
 * <p>A água entra pelo caminho de sempre — balde na mão, clique no caldeirão —, que é o que também prova que o
 * miolo conta ao cliente o que tem dentro.
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

            // o caldeirão vazio, a três passos de quem joga — que é o alcance da mão
            perto(server, "setblock ~ ~ ~3 thaumcraft:witches_cauldron");
            perto(server, "tp @p ~ ~ ~ 0 25");
            context.waitTicks(20);
            context.takeScreenshot("ao_caldeirao_vazio");

            // três baldes de água, um clique de cada vez
            for (int volta = 0; volta < 3; volta++) {
                server.runCommand("item replace entity @p hotbar.0 with minecraft:water_bucket");
                context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(0));
                context.waitTicks(5);
                clica(context, 3);
                context.waitTicks(5);
            }
            context.waitTicks(20);
            context.takeScreenshot("ao_caldeirao_cheio");

            // fogo embaixo: em cinco segundos ferve
            perto(server, "setblock ~ ~-2 ~3 minecraft:netherrack");
            perto(server, "setblock ~ ~-1 ~3 minecraft:fire");
            context.waitTicks(140);
            context.takeScreenshot("ao_caldeirao_fervendo");

            // e o que se joga dentro tinge a água
            perto(server, "summon item ~ ~1 ~3 {Item:{id:\"thaumcraft:mandrake_root\",count:1}}");
            context.waitTicks(30);
            perto(server, "summon item ~ ~1 ~3 {Item:{id:\"thaumcraft:exhale_of_the_horned_one\",count:1}}");
            context.waitTicks(40);
            context.takeScreenshot("ao_caldeirao_tingido");
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

    /** Um clique no bloco que está tantos passos à frente de quem joga. */
    private static void clica(ClientGameTestContext context, int passos) {
        context.runOnClient(minecraft -> {
            var onde = minecraft.player.blockPosition().offset(0, 0, passos);
            var hit = new net.minecraft.world.phys.BlockHitResult(
                    net.minecraft.world.phys.Vec3.atCenterOf(onde), net.minecraft.core.Direction.UP, onde, false);
            minecraft.gameMode.useItemOn(minecraft.player, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
        });
    }

    /** O comando corre no lugar de quem joga, e não na origem do mundo. */
    private static void perto(TestServerContext server, String command) {
        server.runCommand("execute at @p run " + command);
    }
}
