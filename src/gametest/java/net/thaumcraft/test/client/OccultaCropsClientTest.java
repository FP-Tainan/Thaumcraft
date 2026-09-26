package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.occulta.OccultaItems;

/**
 * As oito plantas do ofício, cada uma com as suas idades em fila, e as quatorze coisas que saem delas.
 *
 * <p>Não há o que conferir por conta própria numa tela: as fotos ficam em
 * {@code build/run/clientGameTest/screenshots} para quem estiver de olho. O que elas mostram é se cada idade tem
 * a folha certa, e se as três que usam o feitio de flor — a campainha-de-neve, a acônito e a losna — se
 * distinguem das cinco que usam o de plantação.
 */
public class OccultaCropsClientTest implements FabricClientGameTest {
    /** Cada planta e quantas idades ela tem. */
    private static final String[][] AS_OITO = {
            {"belladonna", "4"}, {"mandrake", "4"}, {"water_artichoke", "4"}, {"snowbell", "4"},
            {"wormwood", "4"}, {"mindrake", "4"}, {"wolfsbane", "7"}, {"garlic", "5"},
    };

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            context.waitTicks(20);

            for (String[] planta : AS_OITO) {
                String nome = planta[0];
                int idades = Integer.parseInt(planta[1]);
                boolean água = nome.equals("water_artichoke");
                // o chão da fila: água parada para a alcachofra, terra arada para as outras
                perto(server, "fill ~-1 ~-1 ~4 ~" + (idades + 1) + " ~-1 ~4 minecraft:"
                        + (água ? "water" : "farmland"));
                for (int idade = 0; idade <= idades; idade++) {
                    perto(server, "setblock ~" + idade + " ~ ~4 thaumcraft:" + nome + "[age=" + idade + "]");
                }
                int meio = idades / 2;
                perto(server, "tp @p ~" + meio + " ~ ~1 0 10");
                context.waitTicks(20);
                context.takeScreenshot("ao_" + nome);
                // e limpa a fila antes da próxima, contando da onde quem joga ficou, e volta ao lugar de antes
                int esquerda = -meio - 1;
                int direita = idades + 1 - meio;
                perto(server, "fill ~" + esquerda + " ~ ~3 ~" + direita + " ~ ~3 minecraft:air");
                perto(server, "fill ~" + esquerda + " ~-1 ~3 ~" + direita + " ~-1 ~3 minecraft:dirt");
                perto(server, "tp @p ~" + (-meio) + " ~ ~-1 0 10");
            }

            // e as quatorze coisas do ramo, no inventário
            context.runOnClient(minecraft -> {
                var inv = minecraft.player.getInventory();
                int casa = 0;
                for (var item : OccultaItems.shown()) inv.setItem(casa++, new ItemStack(item));
                minecraft.setScreenAndShow(
                        new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player));
            });
            context.waitTicks(20);
            context.takeScreenshot("ao_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }

    /** O comando corre no lugar de quem joga, e não na origem do mundo. */
    private static void perto(TestServerContext server, String command) {
        server.runCommand("execute at @p run " + command);
    }
}
