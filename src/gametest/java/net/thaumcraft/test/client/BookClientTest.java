package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Abre o Thaumonomicon e tira retrato de cada aba, para se ver o mapa do jeito que ele ficou.
 */
public class BookClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            singleplayer.getServer().runCommand("gamemode creative");
            singleplayer.getServer().runCommand("time set noon");
            singleplayer.getServer().runCommand("weather clear");
            singleplayer.getServer().runCommand("give @p thaumcraft:thaumonomicon");
            context.runOnClient(minecraft -> minecraft.player.getInventory().setSelectedSlot(0));
            context.waitTicks(20);
            context.takeScreenshot("livro_mao");

            context.runOnClient(minecraft ->
                    minecraft.setScreenAndShow(new net.thaumcraft.client.gui.ThaumonomiconScreen()));
            context.waitTicks(20);
            context.takeScreenshot("livro_basico");

            // cada aba tem o seu mapa; vale ver todas
            String[] abas = {"THAUMATURGY", "ALCHEMY", "ARTIFICE", "GOLEMANCY"};
            for (int index = 0; index < abas.length; index++) {
                final int tab = index + 1;
                // as abas ficam encostadas na lombada, uma a cada vinte e quatro pontos
                context.runOnClient(minecraft -> {
                    int left = (minecraft.getWindow().getGuiScaledWidth() - 256) / 2 - 12;
                    int top = (minecraft.getWindow().getGuiScaledHeight() - 230) / 2 + tab * 24 + 12;
                    click(minecraft, left, top);
                });
                context.waitTicks(10);
                context.takeScreenshot("livro_" + abas[index].toLowerCase(java.util.Locale.ROOT));
            }

            // e uma pesquisa aberta, para ver a folha com o texto
            context.runOnClient(minecraft -> {
                int left = (minecraft.getWindow().getGuiScaledWidth() - 256) / 2 - 12;
                int top = (minecraft.getWindow().getGuiScaledHeight() - 230) / 2 + 12;
                click(minecraft, left, top);
            });
            context.waitTicks(10);
            context.runOnClient(minecraft -> {
                if (minecraft.gui.screen() instanceof net.thaumcraft.client.gui.ThaumonomiconScreen book) {
                    minecraft.setScreenAndShow(new net.thaumcraft.client.gui.ResearchPageScreen(book,
                            net.thaumcraft.research.Researches.get("ASPECTS")));
                }
            });
            context.waitTicks(15);
            context.takeScreenshot("livro_pagina");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }

    /** Um clique na tela aberta, no ponto pedido. */
    private static void click(net.minecraft.client.Minecraft minecraft, int x, int y) {
        if (minecraft.gui.screen() == null) return;
        net.minecraft.client.input.MouseButtonEvent event = new net.minecraft.client.input.MouseButtonEvent(
                x, y, new net.minecraft.client.input.MouseButtonInfo(0, 0));
        minecraft.gui.screen().mouseClicked(event, false);
    }
}
