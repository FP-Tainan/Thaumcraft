package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Os cinco contratos que o Diabrete lança, na mochila e na mão.
 *
 * <p>O que há para ver é pouco e é o que importa: as <b>cinco figuras</b> do original, que são o mesmo
 * papel enrolado com um selo de cor diferente, e a <b>dica</b> de cada um — que é o que diz a quem os acha
 * que eles não se usam sozinhos, dão-se a um diabrete.
 */
public class OccultaDemonContractClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("clear @p");

            server.runCommand("give @p thaumcraft:contract");
            server.runCommand("give @p thaumcraft:contract_blaze");
            server.runCommand("give @p thaumcraft:contract_resist_fire");
            server.runCommand("give @p thaumcraft:contract_evaporate");
            server.runCommand("give @p thaumcraft:contract_fiery_touch");
            server.runCommand("give @p thaumcraft:contract_smelting");
            server.runCommand("give @p thaumcraft:contract_torment");
            context.waitTicks(20);
            context.takeScreenshot("os_contratos_na_barra");

            // e um deles na mão, que é como se leem
            server.runCommand("item replace entity @p hotbar.0 with thaumcraft:contract_smelting");
            context.waitTicks(20);
            context.takeScreenshot("o_contrato_na_mao");
        }
    }
}
