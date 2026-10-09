package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.effect.MobEffectInstance;
import net.thaumcraft.occulta.OccultaEffects;

/**
 * A <b>Insanidade</b> mentindo sobre o que é: o {@code renderInventoryEffect} do {@code PotionInsanity}.
 *
 * <p>Na lista de efeitos do inventário ela não diz o próprio nome. Mostra uma de <b>sete piadas</b> do
 * original — «Com Sabor de Queijo», «Inclinada ao Waffle», «Piu-Piu» — e troca de piada <b>a cada três
 * segundos</b>.
 *
 * <p>Por isso são <b>duas fotos</b>, e não uma: a mesma poção, no mesmo inventário, com <b>nomes
 * diferentes</b> em cada. É a única maneira de ver que ela troca, e é o que esta tela guarda.
 */
public class OccultaInsanityClientTest implements FabricClientGameTest {
    /** A duração escolhida dá 6000/60 % 7 = 2 na primeira foto, e 1 na segunda. */
    private static final int DURA = 6000;

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode creative");
            server.runCommand("time set noon");
            server.runCommand("difficulty peaceful");
            server.runCommand("gamerule doMobSpawning false");

            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                jogador.addEffect(new MobEffectInstance(OccultaEffects.INSANITY, DURA, 0, false, false));
            });
            context.waitTicks(10);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(10);
            context.takeScreenshot("a_insanidade_com_um_nome");

            // três segundos depois a piada é outra, e é a mesma poção
            context.waitTicks(70);
            context.takeScreenshot("a_insanidade_com_outro");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }
}
