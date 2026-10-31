package net.thaumcraft.occulta.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.brew.BrewBagMenu;

/**
 * A <b>Bolsa de Cozimentos</b> aberta: o {@code ItemBrewBagGUI} do Witchery.
 *
 * <p>A folha dela é a <b>arca de oito colunas</b> do original — uma folha feita para até seis filas, de
 * que a bolsa usa <b>uma</b>. Por isso a tela se desenha em dois pedaços: o de cima, do topo da folha,
 * com o título e a fila de casas; e o de baixo, tirado de bem mais abaixo na folha, com o inventário de
 * quem joga.
 *
 * <p>É de oito e não de nove porque os rumos da cabeça são oito.
 */
public class BrewBagScreen extends AbstractContainerScreen<BrewBagMenu> {
    private static final Identifier FOLHA = Thaumcraft.id("textures/gui/brew_bag.png");

    /** As medidas: a largura de sempre, e cento e trinta e dois de altura para uma fila só. */
    public static final int LARGURA = 176;
    public static final int ALTURA = 132;

    /** Onde o pedaço de baixo da folha começa, e quanto ele mede. */
    public static final int EM_BAIXO = 126;
    public static final int ALTO_DE_BAIXO = 96;

    /** E quanto mede o pedaço de cima: uma fila de casas mais o cabeçalho. */
    public static final int ALTO_DE_CIMA = 18 + 17;

    public BrewBagScreen(BrewBagMenu menu, Inventory inventário, Component título) {
        super(menu, inventário, título, LARGURA, ALTURA);
        this.inventoryLabelY = ALTURA - 96 + 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor desenha, int rato, int ratoY, float parcial) {
        super.extractBackground(desenha, rato, ratoY, parcial);
        desenha.blit(RenderPipelines.GUI_TEXTURED, FOLHA, this.leftPos, this.topPos, 0, 0,
                LARGURA, ALTO_DE_CIMA, 256, 256);
        desenha.blit(RenderPipelines.GUI_TEXTURED, FOLHA, this.leftPos, this.topPos + ALTO_DE_CIMA,
                0, EM_BAIXO, LARGURA, ALTO_DE_BAIXO, 256, 256);
    }
}
