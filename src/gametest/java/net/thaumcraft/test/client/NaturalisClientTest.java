package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * O ramo do Magia Naturalis na tela: a aba dele no Thaumonomicon, as coisas na casa do inventário e o bicho
 * flutuando dentro do jarro.
 */
public class NaturalisClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("thaumcraft pesquisa tudo");
            context.waitTicks(20);

            // a aba do ramo no livro
            context.runOnClient(minecraft -> {
                net.thaumcraft.client.gui.ThaumonomiconScreen.select(net.thaumcraft.naturalis.Naturalis.CATEGORY);
                minecraft.setScreenAndShow(new net.thaumcraft.client.gui.ThaumonomiconScreen());
            });
            context.waitTicks(20);
            context.takeScreenshot("livro_naturalis");

            // a aba do criativo do ramo
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
            server.runCommand("give @p thaumcraft:thaumium_sickle");
            server.runCommand("give @p thaumcraft:elemental_sickle");
            server.runCommand("give @p thaumcraft:spectacles");
            server.runCommand("give @p thaumcraft:builder_focus");
            server.runCommand("give @p thaumcraft:greatwood_gold_trim 8");
            server.runCommand("give @p thaumcraft:prison_jar");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("coisas_do_naturalis");

            // e o jarro com um bicho dentro, no mundo
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
            server.runCommand("summon pig ~ ~ ~3");
            server.runCommand("setblock ~ ~1 ~3 thaumcraft:prison_jar");
            context.waitTicks(20);
            context.takeScreenshot("jarro_no_mundo");
        }
    }
}
