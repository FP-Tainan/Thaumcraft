package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * As <b>duas portas do ofício</b> na tela.
 *
 * <p>A de <b>sorveira</b> tem folha própria — clara, de lascas finas, com as dobradiças escuras — e é a
 * única maneira de a reconhecer numa parede: por dentro ela é uma tranca, mas por fora tem de <b>parecer
 * uma porta diferente</b>, senão ninguém sabe qual das duas pôs.
 *
 * <p>A de <b>amieiro</b> leva a folha da porta de carvalho, como no original. Isso é de propósito lá, e aqui
 * fica igual: a porta comum do ofício é indistinguível de uma porta comum.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaDoorsClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            // as duas lado a lado, fechadas, numa parede de pedra
            for (int y = 0; y <= 2; y++) {
                server.runCommand("execute at @p run fill ~-3 ~" + y + " ~5 ~3 ~" + y + " ~5 "
                        + "minecraft:stone_bricks");
            }
            server.runCommand("execute at @p run setblock ~-1 ~ ~5 air");
            server.runCommand("execute at @p run setblock ~-1 ~1 ~5 air");
            server.runCommand("execute at @p run setblock ~1 ~ ~5 air");
            server.runCommand("execute at @p run setblock ~1 ~1 ~5 air");
            server.runCommand("execute at @p run setblock ~-1 ~ ~5 "
                    + "thaumcraft:rowan_door[facing=north,half=lower,hinge=left,open=false]");
            server.runCommand("execute at @p run setblock ~-1 ~1 ~5 "
                    + "thaumcraft:rowan_door[facing=north,half=upper,hinge=left,open=false]");
            server.runCommand("execute at @p run setblock ~1 ~ ~5 "
                    + "thaumcraft:alder_door[facing=north,half=lower,hinge=left,open=false]");
            server.runCommand("execute at @p run setblock ~1 ~1 ~5 "
                    + "thaumcraft:alder_door[facing=north,half=upper,hinge=left,open=false]");
            server.runCommand("execute at @p run tp @p ~ ~ ~ 0 0");
            context.waitTicks(20);
            context.takeScreenshot("portas_1_fechadas");

            // e abertas, que é onde a dobradiça se vê
            server.runCommand("execute at @p run setblock ~-1 ~ ~5 "
                    + "thaumcraft:rowan_door[facing=north,half=lower,hinge=left,open=true]");
            server.runCommand("execute at @p run setblock ~-1 ~1 ~5 "
                    + "thaumcraft:rowan_door[facing=north,half=upper,hinge=left,open=true]");
            server.runCommand("execute at @p run setblock ~1 ~ ~5 "
                    + "thaumcraft:alder_door[facing=north,half=lower,hinge=left,open=true]");
            server.runCommand("execute at @p run setblock ~1 ~1 ~5 "
                    + "thaumcraft:alder_door[facing=north,half=upper,hinge=left,open=true]");
            context.waitTicks(20);
            context.takeScreenshot("portas_2_abertas");

            // e as quatro peças no inventário
            server.runCommand("give @p thaumcraft:rowan_door");
            server.runCommand("give @p thaumcraft:alder_door");
            server.runCommand("give @p thaumcraft:door_key");
            server.runCommand("give @p thaumcraft:door_keyring");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("portas_3_no_inventario");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(5);
        }
    }
}
