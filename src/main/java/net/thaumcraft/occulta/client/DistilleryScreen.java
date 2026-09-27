package net.thaumcraft.occulta.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.DistilleryBlockEntity;
import net.thaumcraft.occulta.DistilleryMenu;

/**
 * A tela da Destilaria: o {@code BlockDistilleryGUI} do Witchery, com a folha e as medidas dele.
 *
 * <p>São três coisas desenhadas por cima da folha: a <b>seta</b> que corre para a direita conforme se destila, a
 * <b>gota</b> que pinga na esquerda em sete passos, e o <b>aviso</b> de que não há altar por perto — que no
 * original é um quadradinho no canto do pote.
 */
public class DistilleryScreen extends AbstractContainerScreen<DistilleryMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/distillery.png");

    /** A seta: trinta e oito por trinta e cinco, da esquerda para a direita. */
    private static final int ARROW_X = 68, ARROW_Y = 14, ARROW_W = 38, ARROW_H = 35, ARROW_U = 176, ARROW_V = 29;

    /** A gota que pinga, em sete passos: os do {@code switch} do original. */
    private static final int[] DROP = {29, 24, 20, 16, 11, 6, 0};
    private static final int DROP_X = 33, DROP_Y = 20, DROP_W = 12, DROP_U = 185, DROP_V = 29;

    /** O aviso de que falta altar: nove por nove, ao lado dos potes. */
    private static final int WARN_X = 35, WARN_Y = 58, WARN_W = 9, WARN_H = 9, WARN_U = 197, WARN_V = 0;

    public DistilleryScreen(DistilleryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight, 256, 256);

        if (!this.menu.powered()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                    this.leftPos + WARN_X, this.topPos + WARN_Y, WARN_U, WARN_V, WARN_W, WARN_H, 256, 256);
        }

        int andou = this.menu.cookScaled(ARROW_W);
        if (andou <= 0) return;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                this.leftPos + ARROW_X, this.topPos + ARROW_Y, ARROW_U, ARROW_V, andou, ARROW_H, 256, 256);

        // a gota: o passo sai do tempo que falta, de dois em dois tiques, em sete
        int passo = (DistilleryBlockEntity.COOK_TIME - this.menu.cookTime()) / 2 % DROP.length;
        int altura = DROP[passo];
        if (altura <= 0) return;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                this.leftPos + DROP_X, this.topPos + DROP_Y + 29 - altura,
                DROP_U, DROP_V - altura, DROP_W, altura, 256, 256);
    }
}
