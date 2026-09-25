package net.thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.mortuorum.SewingMenu;

/**
 * A tela da Máquina de Costura: o {@code GuiSewing} do Necromancy, com a folha dele — a grade de quatro por
 * quatro, a agulha, a linha e a casa de onde sai a peça.
 */
public class SewingScreen extends AbstractContainerScreen<SewingMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/sewing.png");

    public SewingScreen(SewingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.titleLabelY = -1000;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight, 256, 256);
    }
}
