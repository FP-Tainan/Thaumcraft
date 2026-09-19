package net.thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.inventory.PechMenu;
import net.thaumcraft.registry.TCSounds;

/**
 * A tela de troca do pech: o {@code GuiPech} da 4.2.3.5. O fundo da {@code gui_pech.png}, sem rótulos; o botão dos
 * dados (25 por 25) acende quando há um item de valor sozinho na casa e as saídas estão vazias, e o clique nele faz a
 * troca, com o som dos dados.
 */
public class PechScreen extends AbstractContainerScreen<PechMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/gui_pech.png");

    public PechScreen(PechMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 175, 232);
        this.titleLabelY = -1000;
        this.inventoryLabelY = -1000;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        if (this.menu.canTrade()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos + 67, this.topPos + 24, 176, 0, 25, 25, 256, 256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        int dx = (int) event.x() - (this.leftPos + 67), dy = (int) event.y() - (this.topPos + 24);
        if (dx >= 0 && dy >= 0 && dx < 25 && dy < 25 && this.menu.canTrade()) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
            var player = this.minecraft.player;
            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), TCSounds.PECH_DICE.value(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 0.95f + player.level().getRandom().nextFloat() * 0.1f, false);
            return true;
        }
        return handled;
    }
}
