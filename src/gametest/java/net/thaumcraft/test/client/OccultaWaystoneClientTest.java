package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.OccultaComponents;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.waystone.Waystones;

/**
 * As três Pedras de Caminho na mão, e o que a pedra presa diz de si.
 *
 * <p>É a única coisa desta fatia que mora do lado de quem joga: uma pedra presa que não dissesse para onde
 * aponta seria uma pedra lisa com outra figura.
 */
public class OccultaWaystoneClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runOnServer(s -> {
                var player = s.getPlayerList().getPlayers().getFirst();
                var level = player.level();

                ItemStack presa = new ItemStack(OccultaItems.BOUND_WAYSTONE, 3);
                presa.set(OccultaComponents.WAYSTONE,
                        new Waystones.Lugar(level.dimension(), new BlockPos(128, 64, -256)));

                ItemStack sangrada = Waystones.sangrada(player);

                player.getInventory().setItem(0, new ItemStack(OccultaItems.WAYSTONE, 8));
                player.getInventory().setItem(1, presa);
                player.getInventory().setItem(2, sangrada);
            });
            context.waitTicks(20);
            context.takeScreenshot("pedra_de_caminho_na_barra");

            // e a dica da pedra presa, que diz o mundo e as três contas dele
            context.runOnClient(minecraft ->
                    minecraft.setScreenAndShow(
                            new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(10);
            double[] onde = context.computeOnClient(minecraft -> {
                double escala = minecraft.getWindow().getGuiScale();
                int esquerda = (minecraft.getWindow().getGuiScaledWidth() - 176) / 2;
                int alto = (minecraft.getWindow().getGuiScaledHeight() - 166) / 2;
                // a segunda casa da barra rápida, que é onde está a pedra presa
                return new double[]{(esquerda + 8 + 18 + 8) * escala, (alto + 142 + 8) * escala};
            });
            context.getInput().setCursorPos(onde[0], onde[1]);
            context.waitTicks(5);
            context.takeScreenshot("pedra_de_caminho_presa_diz_o_lugar");
        }
    }
}
