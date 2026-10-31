package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.brew.BrewBagItem;
import net.thaumcraft.occulta.brew.BrewBagMenu;
import net.thaumcraft.occulta.brew.BrewItem;

import java.util.List;

/**
 * A <b>Bolsa de Cozimentos</b> vista — e nela isso não é um detalhe, é o item.
 *
 * <p>São quatro fotos, e cada uma mostra uma coisa que só existe desenhada:
 *
 * <ol>
 *   <li>a <b>roda</b> no meio da tela, com os oito frascos à volta, que é a única interface que a bolsa
 *       tem;</li>
 *   <li>o que se vê <b>depois do gesto</b>: o anel sai da frente e fica só o frasco escolhido;</li>
 *   <li>a bolsa <b>aberta</b>, com a fila de oito casas e não de nove;</li>
 *   <li>e as <b>cores</b>, que se tingem como uma armadura de couro.</li>
 * </ol>
 */
public class OccultaBrewBagClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative @a");
            server.runCommand("time set noon");
            server.runCommand("clear @p");

            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                jogador.getInventory().setSelectedSlot(0);
                jogador.getInventory().setItem(0, cheia());
            });
            context.waitTicks(10);

            // a roda: segura-se o botão e ela aparece no meio da tela
            context.getInput().holdKey(options -> options.keyUse);
            context.waitTicks(10);
            context.takeScreenshot("a_roda_da_bolsa");

            /*
             * E o gesto: a cabeça sobe dez graus, que passa dos sete, e o traço de cima escolhe a casa de
             * cima. Dali em diante só se vê o frasco escolhido.
             */
            context.runOnClient(minecraft -> minecraft.player.setXRot(minecraft.player.getXRot() - 10.0f));
            context.waitTicks(10);
            context.takeScreenshot("a_bolsa_escolheu");
            context.getInput().releaseKey(options -> options.keyUse);
            context.waitTicks(10);

            // a bolsa aberta: oito casas numa fila
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                BrewBagMenu.abre(jogador, jogador.getInventory().getItem(0));
            });
            context.waitTicks(20);
            context.takeScreenshot("a_bolsa_aberta");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);

            // e as cores dela, no inventário
            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                int[] cores = {BrewBagItem.CACAU, 0xB02E26, 0x3C44AA, 0x5E7C16,
                        0xF9C62C, 0x8932B8, 0xF9FFFE, 0x1D1D21};
                for (int qual = 0; qual < cores.length; qual++) {
                    ItemStack bolsa = new ItemStack(OccultaItems.BREW_BAG);
                    bolsa.set(DataComponents.DYED_COLOR, new DyedItemColor(0xFF000000 | cores[qual]));
                    jogador.getInventory().setItem(qual, bolsa);
                }
            });
            // de sobrevivência, para a tela ser a do inventário e não a do criativo
            server.runCommand("gamemode survival @a");
            context.waitTicks(10);
            context.getInput().setCursorPos(0, 0);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("as_bolsas_tingidas");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }

    /** Uma bolsa com um frasco em cada uma das oito casas, cada um de outra cor. */
    private static ItemStack cheia() {
        net.minecraft.world.item.Item[] temperos = {
                Items.SUGAR, Items.REDSTONE, Items.GLOWSTONE_DUST, Items.SPIDER_EYE,
                Items.BLAZE_POWDER, Items.GHAST_TEAR, Items.MAGMA_CREAM, Items.RABBIT_FOOT};
        ItemStack bolsa = new ItemStack(OccultaItems.BREW_BAG);
        NonNullList<ItemStack> dentro = NonNullList.withSize(BrewBagItem.CASAS, ItemStack.EMPTY);
        for (int casa = 0; casa < BrewBagItem.CASAS; casa++) {
            dentro.set(casa, BrewItem.of(OccultaItems.BREW,
                    List.of(Items.NETHER_WART, temperos[casa], Items.GUNPOWDER)));
        }
        BrewBagItem.guarda(bolsa, dentro);
        return bolsa;
    }
}
