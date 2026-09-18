package net.thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.inventory.HoverHarnessMenu;

/**
 * A tela do arreio: o {@code GuiHoverHarness} da 4.2.3.5 — o fundo da {@code guihoverharness.png} sem rótulo nenhum e
 * a marca sobre a casa da barra onde está o próprio arreio.
 */
public class HoverHarnessScreen extends AbstractContainerScreen<HoverHarnessMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/guihoverharness.png");

    public HoverHarnessScreen(HoverHarnessMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.titleLabelY = -1000;
        this.inventoryLabelY = -1000;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        // sem o arreio na mão, a tela fecha, como no original
        if (this.minecraft.player.getInventory().getItem(this.menu.blockedHotbarSlot()).isEmpty()) this.onClose();
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        // a marca sobre a casa travada
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, 8 + this.menu.blockedHotbarSlot() * 18, 142, 240, 0, 16, 16, 256, 256);
    }
}
