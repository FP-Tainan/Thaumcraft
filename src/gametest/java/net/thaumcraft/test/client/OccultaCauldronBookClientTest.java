package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.thaumcraft.occulta.CauldronBookItem;
import net.thaumcraft.occulta.client.MarkupBookScreen;

/**
 * Os <b>Cozimentos das Bruxas</b> abertos.
 *
 * <p>Este livro é a única coisa do mod que <b>só existe desenhada</b>: ele não faz nada, não muda nada no
 * mundo, e o que ele vale é o que se lê nele. Uma prova de servidor pode dizer que os atalhos levam a
 * algum lado; só uma foto diz se a página <b>cabe na folha</b>.
 *
 * <p>São quatro, escolhidas pelo que cada uma põe à prova na folha: o <b>índice</b>, que é uma coluna de
 * atalhos; a <b>receita do caldeirão</b>, que é uma bancada de três por três desenhada com coisas; a
 * <b>lista de temperos de cor</b>, que é coisa-e-texto em dezasseis linhas; e um <b>capítulo de texto
 * corrido</b>, que é onde a quebra de linha se vê.
 */
public class OccultaCauldronBookClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            singleplayer.getServer().runCommand("time set noon");

            foto(context, CauldronBookItem.PRIMEIRA, "o_indice");
            foto(context, "cauldron", "a_receita_do_caldeirao");
            foto(context, "modifiers3", "os_temperos_de_cor");
            foto(context, "rituals5", "um_capitulo_corrido");
        }
    }

    /** Abre um capítulo e tira-lhe a foto. */
    private static void foto(ClientGameTestContext context, String capítulo, String nome) {
        context.runOnClient(minecraft -> minecraft.setScreenAndShow(new MarkupBookScreen(
                CauldronBookItem.RAIZ, CauldronBookItem.PÁGINAS, capítulo)));
        context.waitTicks(20);
        context.takeScreenshot(nome);
        context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        context.waitTicks(5);
    }
}
