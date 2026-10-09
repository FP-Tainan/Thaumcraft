package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * O calçado do ofício, calçado.
 *
 * <p>O que há para ver é a <b>cor</b> de cada par e a <b>perna inchada</b> que o original usa para eles —
 * a mesma do peito, e não a da cabeça. Três fotos de perto, uma por par, e uma quarta com os três na barra
 * para se verem as figuras.
 */
public class OccultaFootwearClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("clear @p");

            server.runCommand("give @p thaumcraft:icy_slippers");
            server.runCommand("give @p thaumcraft:seeping_shoes");
            server.runCommand("give @p thaumcraft:ruby_slippers");
            context.waitTicks(20);
            context.takeScreenshot("o_calcado_na_barra");

            // e cada par calçado, de terceira pessoa, para se ver a perna
            context.runOnClient(c -> c.options.setCameraType(
                    net.minecraft.client.CameraType.THIRD_PERSON_FRONT));
            for (var par : new String[]{"icy_slippers", "seeping_shoes", "ruby_slippers"}) {
                server.runCommand("item replace entity @p armor.feet with thaumcraft:" + par);
                context.waitTicks(20);
                context.takeScreenshot("calcado_" + par);
            }
        }
    }
}
