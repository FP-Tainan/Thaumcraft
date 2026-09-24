package net.thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.inventory.TranscribingTableMenu;

/**
 * A tela da Mesa de Transcrição: o {@code GuiTranscribingTable} do Magia Naturalis 0.5.0, que empresta a tela da
 * mesa de desconstrução do Thaumcraft — e a barra ao lado da casa desce enquanto a próxima colheita não vem.
 */
public class TranscribingTableScreen extends AbstractContainerScreen<TranscribingTableMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/gui_decontable.png");

    public TranscribingTableScreen(TranscribingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        // como a da mesa de desconstrução, esta tela não escreve rótulo nenhum
        this.titleLabelY = -1000;
        this.inventoryLabelY = -1000;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        int k = this.leftPos, l = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, k, l, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        int timer = this.menu.timer();
        if (timer > 0) {
            int bar = timer * 46 / net.thaumcraft.naturalis.TranscribingTableBlockEntity.PERIOD;
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, k + 93, l + 15 + 46 - bar, 176, 46 - bar, 9, bar, 256, 256);
        }
    }
}
