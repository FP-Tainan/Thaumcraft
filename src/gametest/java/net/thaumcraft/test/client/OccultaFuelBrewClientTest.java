package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Os <b>dois cozimentos que não são poção</b>, no inventário e em uso.
 *
 * <p>A tela que importa é a dos <b>quatro graus lado a lado</b>: eles são o mesmo frasco, e o que muda é a
 * cor do caldo — um laranja que vai ficando mais fundo do primeiro ao quarto. É assim que se sabe qual se
 * tem na mão sem ler o nome, e é por isso que o original os pinta e não lhes dá desenhos diferentes.
 *
 * <p>Ao lado deles vai o de <b>Água sem Fim</b>, que é o mesmo frasco em azul e com a barra de goles por
 * baixo — e dois dele, um cheio e um quase gasto, para a barra se ver.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaFuelBrewClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative @a");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            server.runCommand("give @p thaumcraft:fuel_brew");
            server.runCommand("give @p thaumcraft:fuel_brew_ii");
            server.runCommand("give @p thaumcraft:fuel_brew_iii");
            server.runCommand("give @p thaumcraft:fuel_brew_iv");
            server.runCommand("give @p thaumcraft:endless_water_brew");
            server.runCommand("give @p thaumcraft:endless_water_brew[damage=95]");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("cozimentos_1_os_seis_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);

            // e o de água a trabalhar: uma caldeira do jogo vazia ao lado de uma que ele encheu
            server.runCommand("execute at @p run fill ~-4 ~-1 ~4 ~4 ~-1 ~8 minecraft:stone");
            server.runCommand("execute at @p run fill ~-4 ~ ~4 ~4 ~3 ~8 minecraft:air");
            server.runCommand("execute at @p run setblock ~-1 ~ ~6 minecraft:cauldron");
            server.runCommand("execute at @p run setblock ~1 ~ ~6 minecraft:water_cauldron[level=3]");
            server.runCommand("execute at @p run tp @p ~ ~ ~2 0 25");
            context.waitTicks(20);
            context.takeScreenshot("cozimentos_2_a_caldeira_cheia_e_a_vazia");
        }
    }
}
