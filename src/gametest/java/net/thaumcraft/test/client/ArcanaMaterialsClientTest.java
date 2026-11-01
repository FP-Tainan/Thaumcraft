package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Os materiais do Ars Arcana vistos de perto: os cinco minérios e os cinco blocos de guardar numa parede, e
 * as nove pedras e pós na barra.
 *
 * <p>Nenhuma prova de servidor vê uma textura que ficou por copiar: o item existe, o bloco existe, e o que
 * aparece é o quadriculado. Esta vê.
 */
public class ArcanaMaterialsClientTest implements FabricClientGameTest {
    private static final String[] CINCO = {"vinteum", "chimerite", "blue_topaz", "moonstone", "sunstone"};

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("gamerule doDaylightCycle false");
            server.runCommand("weather clear");

            // uma parede de dez: os cinco minérios em baixo e os cinco blocos em cima
            for (int i = 0; i < CINCO.length; i++) {
                server.runCommand("setblock ~" + (i - 2) + " ~ ~-5 thaumcraft:" + CINCO[i] + "_ore");
                server.runCommand("setblock ~" + (i - 2) + " ~1 ~-5 thaumcraft:" + CINCO[i] + "_block");
            }
            server.runCommand("tp @p ~ ~1 ~ 180 0");
            context.waitTicks(20);
            context.takeScreenshot("arcana_minerios");

            // e as nove pedras e pós na barra, cada uma no seu lugar
            String[] nove = {"vinteum_dust", "arcane_compound", "arcane_ash", "purified_vinteum", "chimerite",
                    "blue_topaz", "sunstone", "moonstone", "animal_fat"};
            for (int i = 0; i < nove.length; i++) {
                server.runCommand("item replace entity @p hotbar." + i + " with thaumcraft:" + nove[i]);
            }
            context.waitTicks(20);
            context.takeScreenshot("arcana_materiais");
        }
    }
}
