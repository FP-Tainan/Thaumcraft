package net.thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.mortuorum.SummoningAltarMenu;

/**
 * A tela do Altar de Invocação: o {@code GuiAltar} do Necromancy, com a folha dele — o corpo desmontado no meio,
 * o sangue de um lado e a alma do outro, com os dois nomes escritos por baixo das casas.
 */
public class SummoningAltarScreen extends AbstractContainerScreen<SummoningAltarMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/altar.png");

    public SummoningAltarScreen(SummoningAltarMenu menu, Inventory inventory, Component title) {
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

    /** Os dois nomes que o {@code GuiAltar} escreve por baixo das casas do sangue e da alma. */
    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, Component.translatable("gui.thaumcraft.altar.blood"), 21, 60, 0xFFFFFFFF, false);
        graphics.text(this.font, Component.translatable("gui.thaumcraft.altar.soul"), 132, 60, 0xFFFFFFFF, false);
    }
}
