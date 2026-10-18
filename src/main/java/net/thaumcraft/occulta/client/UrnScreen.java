package net.thaumcraft.occulta.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.leonard.UrnMenu;

/**
 * A tela da <b>Urna do Leonard</b>: o {@code ItemLeonardsUrnGUI} do Witchery.
 *
 * <p>A folha dela é um <b>desenho da urna</b> inteira, com as faíscas em volta, e não tem casa nenhuma
 * marcada. As casas desenham-se <b>por cima</b>, uma a uma, com o quadradinho que mora ao lado da folha —
 * e é por isso que a tela muda de cara conforme o tamanho da urna: com uma casa há um quadradinho, com
 * quatro há o losango.
 *
 * <p>É a única tela do mod em que o número de casas não é fixo.
 */
public class UrnScreen extends AbstractContainerScreen<UrnMenu> {
    private static final Identifier FOLHA = Thaumcraft.id("textures/gui/urn.png");

    /** As medidas do original: cento e setenta e seis por cento e oitenta e quatro. */
    public static final int LARGURA = 176;
    public static final int ALTURA = 184;

    /** E o quadradinho de casa, que mora logo ao lado da folha. */
    public static final int CASA = 18;

    public UrnScreen(UrnMenu menu, Inventory inventário, Component título) {
        super(menu, inventário, título, LARGURA, ALTURA);
        this.inventoryLabelY = ALTURA - 96 + 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor desenha, int rato, int ratoY, float parcial) {
        super.extractBackground(desenha, rato, ratoY, parcial);
        desenha.blit(RenderPipelines.GUI_TEXTURED, FOLHA, this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight, 256, 256);

        for (int casa = 0; casa < this.menu.casas(); casa++) {
            desenha.blit(RenderPipelines.GUI_TEXTURED, FOLHA,
                    this.leftPos + UrnMenu.LOSANGO[casa][0] - 1, this.topPos + UrnMenu.LOSANGO[casa][1] - 1,
                    LARGURA, 0, CASA, CASA, 256, 256);
        }
    }
}
