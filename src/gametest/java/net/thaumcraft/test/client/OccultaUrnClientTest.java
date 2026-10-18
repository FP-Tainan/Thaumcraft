package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.brew.BrewItem;
import net.thaumcraft.occulta.leonard.LeonardsUrnItem;
import net.thaumcraft.occulta.leonard.UrnMenu;

import java.util.List;

/**
 * A <b>Urna do Leonard</b> vista: os quatro tamanhos na barra e a tela de cada um.
 *
 * <p>São duas coisas que só se veem: os <b>quatro desenhos</b> do item, que mudam com o tamanho, e a
 * <b>tela</b>, em que as casas aparecem <b>uma a uma</b> por cima do desenho da urna — com uma casa há um
 * quadradinho ao meio, com quatro há o <b>losango</b>.
 *
 * <p>É a única tela do mod cujo número de casas não é fixo, e é por isso que valem duas fotos dela.
 */
public class OccultaUrnClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("clear @p");

            // os quatro tamanhos na barra, com um frasco de atirar ao lado
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                for (int tamanho = 0; tamanho < LeonardsUrnItem.TAMANHOS; tamanho++) {
                    jogador.getInventory().setItem(tamanho, LeonardsUrnItem.daquele(tamanho));
                }
                jogador.getInventory().setItem(5, frasco());
            });
            context.waitTicks(10);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("as_quatro_urnas");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));

            // a tela da pequena: uma casa só
            abre(server, 0);
            context.waitTicks(20);
            context.takeScreenshot("a_urna_de_uma_casa");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);

            // e a da grande: o losango, com um frasco numa das casas
            abre(server, 3);
            context.waitTicks(20);
            context.takeScreenshot("a_urna_de_quatro_casas");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }

    /** Um cozimento de atirar, que é o único que cabe na urna. */
    private static ItemStack frasco() {
        return BrewItem.of(OccultaItems.BREW, List.of(Items.NETHER_WART, Items.SUGAR, Items.GUNPOWDER));
    }

    /** Põe na mão a urna daquele tamanho, com um frasco na primeira casa, e abre-a. */
    private static void abre(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
                             int tamanho) {
        server.runOnServer(s -> {
            var jogador = s.getPlayerList().getPlayers().getFirst();
            ItemStack urna = LeonardsUrnItem.daquele(tamanho);
            NonNullList<ItemStack> dentro = NonNullList.withSize(tamanho + 1, ItemStack.EMPTY);
            dentro.set(0, frasco());
            LeonardsUrnItem.guarda(urna, dentro);
            jogador.getInventory().setSelectedSlot(0);
            jogador.getInventory().setItem(0, urna);
            UrnMenu.abre(jogador, urna);
        });
    }
}
