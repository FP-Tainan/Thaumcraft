package net.thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.inventory.FocusPouchMenu;

/**
 * A tela da bolsa de focos: o {@code GuiFocusPouch} da 4.2.3.5 — o fundo inteiro da {@code gui_focuspouch.png}, sem
 * rótulo nenhum, e a marca sobre a casa da barra onde está a própria bolsa.
 */
public class FocusPouchScreen extends AbstractContainerScreen<FocusPouchMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/gui_focuspouch.png");

    public FocusPouchScreen(FocusPouchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 175, 232);
        this.titleLabelY = -1000;
        this.inventoryLabelY = -1000;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        // sem a bolsa na mão, a tela fecha, como no original
        if (this.minecraft.player.getInventory().getItem(this.menu.blockedHotbarSlot()).isEmpty()) this.onClose();
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        // a marca sobre a casa travada
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, 8 + this.menu.blockedHotbarSlot() * 18, 209, 240, 0, 16, 16, 256, 256);
    }
}
