package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * <b>Lilith</b> e <b>Elle</b> na tela, e as coisas da porta de entrada na mão.
 *
 * <p>Lilith é o modelo mais estranho deste porte — chifres virados para trás, duas asas chatas presas aos
 * braços, uma saia de duas peças que se abre quando ela anda — e um número trocado não falha prova nenhuma:
 * só faz uma asa sair do sítio errado. Só se vê.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaLilithClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            context.waitTicks(20);

            // o marco, que é de onde tudo se mede
            server.runCommand("execute at @p run summon minecraft:marker ~ ~ ~ {Tags:[\"lilith\"]}");

            // Lilith, de frente
            põe(server, "thaumcraft:lilith ~ ~ ~5 {NoAI:1b,Invul:0,Rotation:[180f,0f]}");
            olha(server, "~ ~ ~ 0 5");
            context.waitTicks(30);
            context.takeScreenshot("lilith_1_de_frente");

            // e de perto, para os chifres, os dentes e a saia
            olha(server, "~ ~ ~2.5 0 -5");
            context.waitTicks(20);
            context.takeScreenshot("lilith_2_de_perto");

            // de lado, que é onde as asas se vêem
            olha(server, "~4 ~ ~5 90 0");
            context.waitTicks(20);
            context.takeScreenshot("lilith_3_de_lado");

            /*
             * Elle, que por fora é gente — noutro lugar, porque Lilith não sai daqui: o /kill também não a
             * mata. Ele chama o mesmo morrer que o combate chama, e ela responde do mesmo jeito.
             */
            põe(server, "thaumcraft:follower ~12 ~ ~5 {NoAI:1b,Rotation:[180f,0f]}");
            olha(server, "~12 ~ ~2 0 0");
            context.waitTicks(30);
            context.takeScreenshot("lilith_4_elle");

            // e as coisas da porta na mão e no inventário
            olha(server, "~30 ~ ~30 0 0");
            server.runCommand("item replace entity @p weapon.mainhand with thaumcraft:boline");
            server.runCommand("give @p thaumcraft:goblet");
            server.runCommand(
                    "give @p thaumcraft:goblet[thaumcraft:goblet={fonte:\"lilith\"}]");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("lilith_5_a_porta");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }

    private static void põe(TestServerContext server, String resto) {
        server.runCommand("execute at @e[tag=lilith,limit=1] run summon " + resto);
    }

    private static void olha(TestServerContext server, String resto) {
        server.runCommand("execute at @e[tag=lilith,limit=1] run tp @p " + resto);
    }
}
