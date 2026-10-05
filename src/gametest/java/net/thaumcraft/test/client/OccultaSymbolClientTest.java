package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A <b>Vara Mística</b> e a <b>bola de feitiço</b>.
 *
 * <p>A primeira tela é a vara no inventário, ao lado do Unguento Místico de que ela sai.
 *
 * <p>A segunda são as <b>bolas</b> dos símbolos desta fatia, paradas no ar lado a lado — porque o que
 * distingue um feitiço do outro, antes de ele bater, é só a <b>cor</b> e o <b>tamanho</b> da bola, e isso
 * tem de se ver.
 *
 * <p>As fotos ficam em {@code build/run/clientGameTest/screenshots}.
 */
public class OccultaSymbolClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            context.waitTicks(20);

            server.runCommand("give @p thaumcraft:mystic_branch");
            server.runCommand("give @p thaumcraft:mystic_unguent");
            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("simbolo_1_a_vara");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
            context.waitTicks(10);

            /*
             * Oito bolas, paradas, para as cores e os tamanhos se verem: o Accio roxo, o Aguamenti azul
             * e grande, o Incendio vermelho e o Flipendo amarelo — e as quatro das maldições: o <b>Avada
             * Kedavra verde</b>, o <b>Crucio roxo</b>, o <b>Ignianima dourado</b>, que é o maior de todos
             * com três, e o <b>Imperio lilás</b>.
             */
            context.runOnClient(minecraft -> {
                var servidor = minecraft.getSingleplayerServer();
                if (servidor == null) return;
                servidor.execute(() -> {
                    var mundo = servidor.overworld();
                    for (var quem : servidor.getPlayerList().getPlayers()) {
                        int[] quais = {1, 2, 21, 17, 4, 9, 39, 20};
                        for (int volta = 0; volta < quais.length; volta++) {
                            var qual = net.thaumcraft.occulta.symbol.Symbols.daquele(quais[volta]);
                            var bola = new net.thaumcraft.occulta.symbol.SpellEffectEntity(
                                    mundo, quem, new net.minecraft.world.phys.Vec3(0.0, 0.0, 0.0),
                                    qual, 1);
                            bola.setPos(quem.getX() - 5.6 + volta * 1.6, quem.getY() + 1.0,
                                    quem.getZ() + 9.0);
                            bola.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                            mundo.addFreshEntity(bola);
                        }
                    }
                });
            });
            context.waitTicks(10);
            context.takeScreenshot("simbolo_2_as_bolas");
        }
    }
}
