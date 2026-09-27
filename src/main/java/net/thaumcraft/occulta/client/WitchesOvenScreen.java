package net.thaumcraft.occulta.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.WitchesOvenMenu;

/**
 * A tela do Forno das Bruxas: o {@code BlockWitchesOvenGUI} do Witchery, com a folha e as medidas dele.
 *
 * <p>A chama fica entre as duas casas da esquerda e encolhe conforme o combustível acaba; a seta do cozimento
 * corre da casa do meio para a de cima da direita. Os dois números saem da folha em (176, 12) e (176, 14), como
 * no original.
 */
public class WitchesOvenScreen extends AbstractContainerScreen<WitchesOvenMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/witches_oven.png");

    /** A chama: catorze por treze, desenhada de baixo para cima. */
    private static final int FLAME_X = 56, FLAME_Y = 36, FLAME_W = 14, FLAME_H = 12, FLAME_U = 176, FLAME_V = 0;

    /** A seta do cozimento: vinte e quatro por dezesseis, da esquerda para a direita. */
    private static final int COOK_X = 79, COOK_Y = 20, COOK_W = 24, COOK_H = 16, COOK_U = 176, COOK_V = 14;

    public WitchesOvenScreen(WitchesOvenMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight, 256, 256);

        int fogo = this.menu.burnScaled(FLAME_H);
        if (fogo > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                    this.leftPos + FLAME_X, this.topPos + FLAME_Y + FLAME_H - fogo,
                    FLAME_U, FLAME_V + FLAME_H - fogo, FLAME_W, fogo + 2, 256, 256);
        }

        int cozido = this.menu.cookScaled(COOK_W);
        if (cozido > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                    this.leftPos + COOK_X, this.topPos + COOK_Y, COOK_U, COOK_V, cozido + 1, COOK_H, 256, 256);
        }
    }
}
