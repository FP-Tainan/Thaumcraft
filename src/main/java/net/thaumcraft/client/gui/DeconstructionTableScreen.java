package net.thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.inventory.DeconstructionTableMenu;
import net.thaumcraft.registry.TCSounds;

import java.util.List;

/**
 * A tela da mesa de desconstrução: o {@code GuiDeconstructionTable} da 4.2.3.5.
 *
 * <p>A barra de quebra desce ao lado da casa conforme a coisa é desfeita; o primário que sobrar aparece embaixo
 * da casa, com o nome e a descrição ao passar o mouse, e um clique nele o recolhe como ponto de pesquisa.
 */
public class DeconstructionTableScreen extends AbstractContainerScreen<DeconstructionTableMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/gui_decontable.png");

    public DeconstructionTableScreen(DeconstructionTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        // o original não escreve rótulo nenhum
        this.titleLabelY = -1000;
        this.inventoryLabelY = -1000;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        int k = this.leftPos, l = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, k, l, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        int breaktime = this.menu.breaktime();
        if (breaktime > 0) {
            int bar = breaktime * 46 / 40;
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, k + 93, l + 15 + 46 - bar, 176, 46 - bar, 9, bar, 256, 256);
        }
        Aspect aspect = this.menu.aspect();
        if (aspect != null) {
            AspectTags.draw(graphics, this.font, k + 64, l + 48, aspect, 0, 0, 1.0f, false, 0);
            int dx = mouseX - (k + 64), dy = mouseY - (l + 48);
            if (dx >= 0 && dy >= 0 && dx < 16 && dy < 16) {
                graphics.setComponentTooltipForNextFrame(this.font, List.of(aspect.name(),
                        Component.translatable("tc.aspect.help." + aspect.tag())), mouseX, mouseY - 8);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        int dx = (int) event.x() - (this.leftPos + 64), dy = (int) event.y() - (this.topPos + 48);
        if (dx >= 0 && dy >= 0 && dx < 16 && dy < 16 && this.menu.aspect() != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, DeconstructionTableMenu.COLLECT);
            var player = this.minecraft.player;
            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), TCSounds.HHOFF.value(),
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.2f, 1.0f + player.level().getRandom().nextFloat() * 0.1f, false);
            return true;
        }
        return handled;
    }
}
