package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.thaumcraft.occulta.OccultaItems;

import java.util.List;

/**
 * As caras das duas <b>bússolas</b>.
 *
 * <p>É o que há para conferir: que as <b>trinta e três</b> da Bússola de Gente são todas diferentes — uma
 * agulha por rumo — e que as <b>seis</b> da Bússola da Prateleira vão esquentando.
 */
public class OccultaCompassClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("clear @p");

            // as trinta e três da Bússola de Gente, que enchem a mochila inteira
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                var mochila = jogador.getInventory();
                for (int cara = 0; cara < 33; cara++) {
                    mochila.setItem(cara, cara(OccultaItems.PLAYER_COMPASS, cara));
                }
                jogador.containerMenu.broadcastChanges();
            });
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("as_trinta_e_tres_caras");

            // e as seis da outra, com a picareta ao lado
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            server.runCommand("clear @p");
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                var mochila = jogador.getInventory();
                for (int cara = 0; cara < 6; cara++) {
                    mochila.setItem(cara, cara(OccultaItems.SHELF_COMPASS, cara));
                }
                mochila.setItem(7, new ItemStack(OccultaItems.KOBOLDITE_PICKAXE));
                jogador.containerMenu.broadcastChanges();
            });
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("as_seis_caras_e_a_picareta");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }

    /** Uma bússola daquela cara. */
    private static ItemStack cara(net.minecraft.world.item.Item qual, int cara) {
        ItemStack bússola = new ItemStack(qual);
        if (cara > 0) {
            bússola.set(DataComponents.CUSTOM_MODEL_DATA,
                    new CustomModelData(List.of((float) cara), List.of(), List.of(), List.of()));
        }
        return bússola;
    }
}
