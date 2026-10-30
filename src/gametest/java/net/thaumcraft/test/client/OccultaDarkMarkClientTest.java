package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A <b>Marca Negra</b> no céu, que é o que o Morsmordre deixa.
 *
 * <p>A tela que importa é a de <b>longe</b>: ela é uma caveira de quatro blocos de altura, preta e
 * fumegando, e existe para ser vista do outro lado do vale. É o único feitiço do mod que não serve para nada
 * a não ser isso.
 *
 * <p>De perto vê-se o que ela é: duas caixas, a caveira e a mandíbula, ampliadas ao dobro — e a cara dela
 * <b>acompanha quem olha</b>, que é o que a torna desconfortável.
 *
 * <p>E ao lado dela vai uma <b>Porta de Sorveira</b> onde estava uma de carvalho, que é o que o Colloportus
 * faz: a porta troca e quem a pôs ali não é avisado.
 *
 * <p>As telas são todas tiradas <b>do chão</b>, olhando para cima: em criativo quem é posto no ar cai, e a
 * Marca está a cinco casas de altura.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaDarkMarkClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative @a");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            server.runCommand("execute at @p run fill ~-10 ~-1 ~-2 ~10 ~-1 ~20 minecraft:stone");
            server.runCommand("execute at @p run fill ~-10 ~ ~-2 ~10 ~10 ~20 minecraft:air");
            server.runCommand("execute at @p run summon thaumcraft:dark_mark ~ ~5 ~14");
            context.waitTicks(30);

            // catorze casas à frente e cinco acima: vinte graus de inclinação
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 -20");
            context.waitTicks(20);
            context.takeScreenshot("marca_1_de_longe");

            // cinco e cinco: quarenta e cinco graus, e a cara dela virada para cá
            server.runCommand("execute at @p run tp @p ~ ~ ~9 0 -45");
            context.waitTicks(30);
            context.takeScreenshot("marca_2_de_perto");

            server.runCommand("time set midnight");
            context.waitTicks(30);
            context.takeScreenshot("marca_3_de_noite");
            server.runCommand("time set noon");

            // e a porta que o Colloportus troca, ao lado de uma que ele não tocou
            server.runCommand("execute at @p run setblock ~-2 ~ ~4 minecraft:oak_door[half=lower]");
            server.runCommand("execute at @p run setblock ~-2 ~1 ~4 minecraft:oak_door[half=upper]");
            server.runCommand("execute at @p run setblock ~2 ~ ~4 thaumcraft:rowan_door[half=lower]");
            server.runCommand("execute at @p run setblock ~2 ~1 ~4 thaumcraft:rowan_door[half=upper]");
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 0");
            context.waitTicks(20);
            context.takeScreenshot("marca_4_a_porta_trocada");
        }
    }
}
