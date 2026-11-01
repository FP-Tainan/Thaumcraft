package net.thaumcraft.arcana.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.arcana.machine.ObeliskMenu;

/**
 * A tela do Obelisco: o {@code GuiObelisk} da 1.4.0.009.
 *
 * <p>Uma casa e um mostrador, e mais nada. O mostrador tem <b>catorze pixels</b> e desce conforme o
 * combustível se gasta — ao contrário da chama de um forno, que encolhe por baixo, este <b>esvazia-se por
 * cima</b>.
 *
 * <p>A folha tem 176 por <b>165</b>, que é um pixel a menos do que a do jogo: é a do original.
 */
public class ObeliskScreen extends AbstractContainerScreen<ObeliskMenu> {
    private static final Identifier FOLHA = Thaumcraft.id("textures/gui/gui_obelisk.png");

    /** Onde o mostrador fica e qual é o tamanho dele. */
    private static final int BAR_X = 79;
    private static final int BAR_Y = 30;
    private static final int BAR_W = 14;
    private static final int BAR_H = 14;
    private static final int BAR_U = 176;

    public ObeliskScreen(ObeliskMenu menu, Inventory mochila, Component título) {
        super(menu, mochila, título, 176, 165);
        // o original não escreve rótulo nenhum nesta tela
        this.titleLabelY = -1000;
        this.inventoryLabelY = -1000;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int rato, int ratoY, float parcial) {
        super.extractBackground(graphics, rato, ratoY, parcial);
        graphics.blit(RenderPipelines.GUI_TEXTURED, FOLHA, this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight, 256, 256);

        int quanto = Math.round(BAR_H * this.menu.burning());
        if (quanto <= 0) return;
        // o original desenha dois pixels a mais de altura, e é de propósito: a ponta do mostrador
        graphics.blit(RenderPipelines.GUI_TEXTURED, FOLHA,
                this.leftPos + BAR_X, this.topPos + BAR_Y + BAR_H - quanto,
                BAR_U, BAR_H - quanto, BAR_W, quanto + 2, 256, 256);
    }
}
