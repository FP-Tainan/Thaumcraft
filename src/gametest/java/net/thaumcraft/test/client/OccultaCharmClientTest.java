package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.OccultaItems;

/**
 * Os <b>amuletos</b>, e o que o da Polinésia faz na tela.
 *
 * <p>É o que há para ver: que clicar numa vaca com ele <b>abre uma tela de trocas com a vaca</b>, com o nome
 * dela em cima e o que ela por acaso tenha dentro. É a única coisa no ramo em que a tela de aldeão aparece
 * sem aldeão nenhum, e é por isso que ela merece uma foto.
 */
public class OccultaCharmClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("clear @p");
            server.runCommand("gamerule doMobSpawning false");

            // de frente para o norte do mundo, e uma vaca parada três passos à frente
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 10");
            server.runCommand("execute at @p run summon minecraft:cow ~ ~ ~3 {NoAI:1b,Silent:1b}");
            context.waitTicks(20);

            // os quatro na barra, e o amuleto na mão
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                var mochila = jogador.getInventory();
                int casa = 0;
                for (Item cada : new Item[]{OccultaItems.POLYNESIA_CHARM, OccultaItems.DEVILS_TONGUE_CHARM,
                        OccultaItems.CHARM_OF_FANCIFUL_THINKING, OccultaItems.WOLF_TOKEN}) {
                    mochila.setItem(casa++, new ItemStack(cada));
                }
                mochila.setSelectedSlot(0);
                jogador.containerMenu.broadcastChanges();
            });
            context.waitTicks(20);
            context.takeScreenshot("os_amuletos");

            // e o clique que faz da vaca uma vendedora
            context.runOnClient(minecraft -> minecraft.gameMode.useItem(
                    minecraft.player, net.minecraft.world.InteractionHand.MAIN_HAND));
            context.waitTicks(40);
            context.takeScreenshot("a_vaca_vendedora");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }
}
