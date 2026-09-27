package net.thaumcraft.occulta.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.spinning.SpinningWheelMenu;

/**
 * A tela da Roca: o {@code BlockSpinningWheelGUI} do Witchery, com a folha e as medidas dele.
 *
 * <p>Duas coisas por cima da folha: a <b>seta</b> que corre enquanto o fio se fia, e o <b>aviso</b> de que não há
 * altar por perto — o mesmo quadradinho que a Destilaria mostra, e pela mesma razão.
 */
public class SpinningWheelScreen extends AbstractContainerScreen<SpinningWheelMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/spinning_wheel.png");

    /** A seta, do lugar do original: entre a fibra e o que sai. */
    private static final int ARROW_X = 79, ARROW_Y = 20, ARROW_W = 24, ARROW_H = 17, ARROW_U = 176, ARROW_V = 14;

    /** E o aviso de que falta altar. */
    private static final int WARN_X = 35, WARN_Y = 20, WARN_W = 9, WARN_H = 9, WARN_U = 176, WARN_V = 0;

    public SpinningWheelScreen(SpinningWheelMenu menu, Inventory inventory, Component title) {
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

        int andou = this.menu.spinScaled(ARROW_W);
        if (andou <= 0) return;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                this.leftPos + ARROW_X, this.topPos + ARROW_Y, ARROW_U, ARROW_V, andou, ARROW_H, 256, 256);
    }
}
