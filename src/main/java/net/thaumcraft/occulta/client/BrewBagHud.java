package net.thaumcraft.occulta.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.brew.BrewBagItem;

/**
 * A <b>roda da Bolsa de Cozimentos</b>: o pedaço do {@code PlayerRender} do Witchery que a desenha.
 *
 * <p>Esta roda <b>é</b> a bolsa. Não há lista, não há número de casa, não há botão: segurando a bolsa
 * aparece um anel no meio da tela, da cor dela, com os oito frascos à volta — cada um no rumo em que se
 * tem de mexer a cabeça para o atirar. Quem olha para a roda já sabe o gesto.
 *
 * <p>E no instante em que o gesto pega, os outros sete <b>somem</b>: fica só o que vai sair. É a
 * confirmação, e é a única que há.
 *
 * <p>As oito posições são as do original, e não são um círculo certo: os rumos retos ficam a <b>trinta e
 * dois</b> do meio e as diagonais a <b>vinte e quatro</b> em cada eixo — que dá trinta e quatro de
 * distância. O desencontro é de propósito e deixa os cantos respirar.
 *
 * <p><b>Desvio minúsculo, declarado:</b> o original divide cada canal da cor por <b>256</b> e não por 255
 * — um engano dele —, de modo que o anel sai meio por cento mais escuro do que a cor da bolsa. Aqui ele
 * sai na cor certa.
 */
public final class BrewBagHud {
    /** O anel: uma folha de duzentos e cinquenta e seis, desenhada a um terço. */
    private static final Identifier ANEL = Thaumcraft.id("textures/gui/radial.png");
    public static final int FOLHA = 256;
    public static final float TERÇO = 1.0f / 3.0f;

    /** Onde cada frasco fica, do meio da tela: a roda do relógio a partir do cima. */
    public static final int[][] RODA = {
            {0, -32}, {24, -24}, {32, 0}, {24, 24}, {0, 32}, {-24, 24}, {-32, 0}, {-24, -24}};

    /** E onde o nome de cada um fica, com o jeito de o encostar: 0 à esquerda, 1 ao meio, 2 à direita. */
    public static final int[][] NOMES = {
            {0, -11, 1}, {23, 6, 0}, {23, 6, 0}, {23, 6, 0},
            {0, 19, 1}, {-5, 6, 2}, {-5, 6, 2}, {-5, 6, 2}};

    /** A cor do nome: branco a quase metade, como no original. */
    public static final int COR_DO_NOME = 0x77FFFFFF;

    private BrewBagHud() {
    }

    public static void init() {
        HudElementRegistry.addLast(Thaumcraft.id("brew_bag_wheel"), (graphics, tracker) -> desenha(graphics));
    }

    private static void desenha(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player quem = minecraft.player;
        if (quem == null || minecraft.gui.screen() != null) return;
        if (!quem.isUsingItem() || quem.isShiftKeyDown()) return;
        ItemStack bolsa = quem.getUseItem();
        if (!(bolsa.getItem() instanceof BrewBagItem)) return;

        int escolhida = BrewBagReader.escolhida();
        int x = graphics.guiWidth() / 2 - 8;
        int y = graphics.guiHeight() / 2 - 8;

        /*
         * O anel só se desenha enquanto <b>nada</b> foi escolhido: feito o gesto, ele sai da frente e
         * deixa o frasco escolhido sozinho na tela.
         */
        if (escolhida < 0) {
            var pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(x - 42.0f + 5.0f, y - 42.0f + 5.0f);
            pose.scale(TERÇO, TERÇO);
            graphics.blit(RenderPipelines.GUI_TEXTURED, ANEL, 8, 8, 0.0f, 0.0f, FOLHA, FOLHA,
                    FOLHA, FOLHA, ARGB.opaque(BrewBagItem.cor(bolsa)));
            pose.popMatrix();
        }

        for (int casa = 0; casa < BrewBagItem.CASAS; casa++) {
            if (escolhida >= 0 && escolhida != casa) continue;
            ItemStack frasco = BrewBagItem.naCasa(bolsa, casa);
            if (frasco.isEmpty()) continue;

            int frascoX = x + RODA[casa][0];
            int frascoY = y + RODA[casa][1];
            graphics.item(frasco, frascoX, frascoY);
            graphics.itemDecorations(minecraft.font, frasco, frascoX, frascoY);

            var nome = frasco.getHoverName();
            int largura = minecraft.font.width(nome);
            int nomeX = frascoX + NOMES[casa][0];
            if (NOMES[casa][2] == 1) nomeX -= largura / 2;
            else if (NOMES[casa][2] == 2) nomeX -= largura;
            graphics.text(minecraft.font, nome, nomeX, frascoY + NOMES[casa][1], COR_DO_NOME);
        }
    }
}
