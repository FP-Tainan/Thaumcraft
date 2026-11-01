package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Os <b>três frascos da infusão</b> na mão e na mala.
 *
 * <p>Eles não têm nada que se veja acontecer: o que fazem é uma condição de duas horas que não aparece em
 * lista nenhuma. O que uma foto prova é o que sobra — que os três <b>se distinguem</b> no inventário, e
 * que a dica diz quanto duram.
 *
 * <p>Vale a foto porque os três desenhos do original são frascos quase iguais, e um deles trocado por
 * outro não acusa em prova nenhuma.
 */
public class OccultaInfusedBrewClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            context.waitTicks(20);
            server.runCommand("time set noon");
            context.waitTicks(10);

            // a pedra primeiro, que é a que vai na mão: o give enche da casa zero para a frente
            server.runCommand("give @p thaumcraft:necrotic_stone");
            server.runCommand("give @p thaumcraft:infusion_base");
            server.runCommand("give @p thaumcraft:brew_of_soaring");
            server.runCommand("give @p thaumcraft:brew_of_the_grave");
            context.waitTicks(20);

            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("os_tres_frascos_da_infusao");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
