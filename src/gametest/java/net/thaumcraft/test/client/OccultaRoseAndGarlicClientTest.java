package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A <b>Rosa de Sangue</b> e a <b>Guirlanda de Alho</b> na tela.
 *
 * <p>A rosa tem <b>dois desenhos</b> — aberta e fechada —, e a diferença entre eles é tudo o que denuncia
 * que ela comeu alguém. Se os dois forem iguais, a flor deixa de avisar e passa a ser uma armadilha perfeita,
 * que é exatamente o que ela não deve ser.
 *
 * <p>E a guirlanda são <b>vinte e nove caixas</b> penduradas de cabeça para baixo num cordel em ziguezague:
 * um número trocado não falha prova nenhuma, só põe um alho dentro da parede. Só se vê.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaRoseAndGarlicClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            // as duas rosas lado a lado: a aberta e a que já comeu
            server.runCommand("execute at @p run setblock ~-1 ~-1 ~3 minecraft:dirt");
            server.runCommand("execute at @p run setblock ~1 ~-1 ~3 minecraft:dirt");
            server.runCommand("execute at @p run setblock ~-1 ~ ~3 thaumcraft:blood_rose[cheia=false]");
            server.runCommand("execute at @p run setblock ~1 ~ ~3 thaumcraft:blood_rose[cheia=true]");
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 15");
            context.waitTicks(20);
            context.takeScreenshot("rosa_1_aberta_e_cheia");

            // a guirlanda na parede, de frente
            server.runCommand("execute at @p run setblock ~ ~1 ~5 minecraft:stone_bricks");
            server.runCommand("execute at @p run setblock ~ ~1 ~4 thaumcraft:garlic_garland[facing=south]");
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 -5");
            context.waitTicks(20);
            context.takeScreenshot("rosa_2_a_guirlanda");

            /*
             * E de lado, que é onde o ziguezague do cordel se vê. O rumo é de verdade: a guirlanda está três
             * para a esquerda e dois à frente de onde a câmara vai parar, e isso dá cinquenta e seis graus.
             * Estava a setenta negativos, que olha para o campo vazio.
             */
            server.runCommand("execute at @p run tp @p ~3 ~ ~2 56 -5");
            context.waitTicks(20);
            context.takeScreenshot("rosa_3_a_guirlanda_de_lado");

            // e as duas no inventário
            server.runCommand("give @p thaumcraft:blood_rose");
            server.runCommand("give @p thaumcraft:garlic_garland");
            server.runCommand("give @p thaumcraft:boline");
            server.runCommand("give @p thaumcraft:taglock");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("rosa_4_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
