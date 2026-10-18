package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.effect.MobEffectInstance;
import net.thaumcraft.occulta.OccultaEffects;

/**
 * Os <b>ícones das poções do ofício</b>, que até aqui eram quadriculado roxo.
 *
 * <p>A foto mostra um punhado deles na barra lateral do inventário: um frasco cheio da cor de cada
 * efeito, com o sinal da categoria no canto — <b>dourado</b> para o que faz bem, <b>vermelho escuro</b>
 * para o que faz mal, <b>cinza</b> para o que não é nem uma coisa nem outra.
 */
public class OccultaPotionIconsClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            var server = singleplayer.getServer();
            server.runCommand("gamemode survival");
            server.runCommand("time set noon");
            server.runCommand("difficulty peaceful");
            server.runCommand("gamerule doMobSpawning false");

            server.runOnServer(s -> {
                var jogador = s.getPlayerList().getPlayers().getFirst();
                /*
                 * Cinco, e não mais: com seis ou mais a tela encolhe a lista e os desenhos ficam do
                 * tamanho de um pixel. O que se quer ver aqui é o frasco.
                 */
                for (var qual : java.util.List.of(
                        OccultaEffects.WOLFSBANE, OccultaEffects.SWIMMING, OccultaEffects.FEATHER_FALL,
                        OccultaEffects.SUN_ALLERGY, OccultaEffects.CHILLED)) {
                    jogador.addEffect(new MobEffectInstance(qual, 20 * 600));
                }
            });

            context.waitTicks(20);
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(
                    new net.minecraft.client.gui.screens.inventory.InventoryScreen(minecraft.player)));
            context.waitTicks(20);
            context.takeScreenshot("os_frascos_do_oficio");
            context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        }
    }
}
