package net.thaumcraft.test.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biomes;
import net.thaumcraft.occulta.OccultaItems;
import net.thaumcraft.occulta.biome.BiomeBookItem;
import net.thaumcraft.occulta.client.BiomeBookScreen;

/**
 * O <b>Livro de Biomas</b> aberto.
 *
 * <p>Ele é o único livro do mod que <b>se usa</b> em vez de se ler: a folha em que ele fica é o que se
 * copia para a nota. Uma prova de servidor diz que todo bioma tem folha; só uma foto diz se a folha
 * <b>cabe</b> — e são quinze marcadores encostados na beira direita, que é o lugar mais apertado de
 * qualquer folha deste porte.
 *
 * <p>São duas: a <b>primeira folha</b>, onde o marcador de cima está aceso e a seta de voltar apagada; e
 * uma folha do <b>meio do livro</b>, onde as duas setas acendem e o marcador aceso é outro.
 */
public class OccultaBiomeBookClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            singleplayer.getServer().runCommand("time set noon");

            foto(context, null, "o_livro_de_biomas_na_primeira");
            foto(context, "jungle", "o_livro_de_biomas_na_selva");
        }
    }

    /** Abre o livro naquele bioma e tira-lhe a foto. */
    private static void foto(ClientGameTestContext context, String bioma, String nome) {
        context.runOnClient(minecraft -> {
            ItemStack livro = new ItemStack(OccultaItems.BIOME_BOOK);
            if (bioma != null) BiomeBookItem.abre(livro, Biomes.JUNGLE);
            minecraft.setScreenAndShow(new BiomeBookScreen(livro));
        });
        context.waitTicks(20);
        context.takeScreenshot(nome);
        context.runOnClient(minecraft -> minecraft.setScreenAndShow(null));
        context.waitTicks(5);
    }
}
