package net.thaumcraft.arcana.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.arcana.InscriptionMenu;
import net.thaumcraft.arcana.InscriptionTableBlockEntity;
import net.thaumcraft.arcana.SpellValidator;

/**
 * A Mesa de Inscrição vista: a {@code GuiInscriptionTable} do Ars Magica 2.
 *
 * <p>Nove casas em fila para a frase e uma para o feitiço. E, entre elas, a coisa que faz a mesa valer a pena:
 * <b>ela diz o que está errado</b>. Uma frase que não fecha não sai em silêncio — sai com o motivo escrito, em
 * vermelho, e com o nome da peça que a estragou.
 *
 * <p>É onde a gramática do ramo deixa de ser uma regra escondida no código e vira uma coisa que se aprende
 * jogando.
 *
 * <p><b>Desvio declarado:</b> o fundo é desenhado com retângulos, e não com uma folha de figura. A folha do
 * original é de uma tela de arrastar e soltar que este porte não tem — aqui as peças são itens em casas, e as
 * casas se desenham sozinhas.
 */
public class InscriptionScreen extends AbstractContainerScreen<InscriptionMenu> {
    /** O vermelho do aviso, o verde de quando fecha e o cinza de quando ainda não há frase. */
    private static final int ERRO = 0xFFFF5555;
    private static final int BOM = 0xFF55FF55;
    private static final int CALADO = 0xFF808080;

    /** As cores do painel do jogo. */
    private static final int BORDA_CLARA = 0xFFFFFFFF;
    private static final int BORDA_ESCURA = 0xFF373737;
    private static final int FUNDO = 0xFFC6C6C6;
    private static final int CASA = 0xFF8B8B8B;

    public InscriptionScreen(InscriptionMenu menu, Inventory mochila, Component título) {
        super(menu, mochila, título, 176, 166);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        super.extractBackground(g, mouseX, mouseY, partial);
        int x = this.leftPos;
        int y = this.topPos;

        g.fill(x, y, x + this.imageWidth, y + this.imageHeight, BORDA_ESCURA);
        g.fill(x + 1, y + 1, x + this.imageWidth - 1, y + this.imageHeight - 1, BORDA_CLARA);
        g.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, FUNDO);

        for (int i = 0; i < InscriptionTableBlockEntity.RECIPE_SIZE; i++) {
            casa(g, x + InscriptionMenu.RECIPE_X + i * 18 - 1, y + InscriptionMenu.RECIPE_Y - 1);
        }
        casa(g, x + InscriptionMenu.RESULT_X - 1, y + InscriptionMenu.RESULT_Y - 1);

        for (int linha = 0; linha < 3; linha++) {
            for (int coluna = 0; coluna < 9; coluna++) {
                casa(g, x + 8 + coluna * 18 - 1, y + 84 + linha * 18 - 1);
            }
        }
        for (int coluna = 0; coluna < 9; coluna++) {
            casa(g, x + 8 + coluna * 18 - 1, y + 142 - 1);
        }
    }

    /** Uma casa de dezoito por dezoito, com a sombra do jogo. */
    private static void casa(GuiGraphicsExtractor g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, BORDA_ESCURA);
        g.fill(x + 1, y + 1, x + 18, y + 18, BORDA_CLARA);
        g.fill(x + 1, y + 1, x + 17, y + 17, CASA);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);

        SpellValidator.Result leitura = this.menu.reading();
        Component fala;
        int cor;

        if (leitura.why() != null) {
            fala = leitura.why();
            cor = ERRO;
        } else if (leitura.ok()) {
            fala = Component.translatable("tc.spell.validate.good");
            cor = BOM;
        } else {
            fala = Component.translatable("tc.spell.validate.nothing");
            cor = CALADO;
        }

        // no meio, por baixo das casas da frase
        int largura = this.font.width(fala);
        g.text(this.font, fala, (this.imageWidth - largura) / 2, 42, cor, false);
    }
}
