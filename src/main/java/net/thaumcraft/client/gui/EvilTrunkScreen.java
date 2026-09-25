package net.thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.inventory.EvilTrunkMenu;
import net.thaumcraft.naturalis.EvilTrunkEntity;
import org.joml.Matrix3x2fStack;

/**
 * A tela do Baú Maligno: o {@code EvilTrunkGui} do Magia Naturalis 0.5.0 — o mesmo fundo do baú itinerante do
 * Thaumcraft, com a quarta fileira sempre à mostra, o nome do dono, a barrinha da vida e o botão de ficar.
 */
public class EvilTrunkScreen extends AbstractContainerScreen<EvilTrunkMenu> {
    private static final Identifier GUI = Thaumcraft.id("textures/gui/guitrunkbase.png");

    public EvilTrunkScreen(EvilTrunkMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 200);
        this.titleLabelY = -1000;
        this.inventoryLabelY = -1000;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        EvilTrunkEntity trunk = this.menu.trunk;
        if (trunk == null) return;
        Matrix3x2fStack pose = g.pose();
        pose.pushMatrix();
        pose.scale(0.5f, 0.5f);
        g.text(this.font, Component.literal(trunk.ownerName()).append(Component.translatable("entity.trunk.guiname")),
                8, 4, 0xFFC0A0F0, false);
        pose.popMatrix();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partial) {
        super.extractBackground(g, mouseX, mouseY, partial);
        EvilTrunkEntity trunk = this.menu.trunk;
        if (trunk != null && trunk.isRemoved()) this.onClose();
        int j = this.leftPos, k = this.topPos;
        g.blit(RenderPipelines.GUI_TEXTURED, GUI, j, k, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        if (trunk == null) return;
        int hp = Math.round(trunk.getHealth() / trunk.getMaxHealth() * 39.0f);
        g.blit(RenderPipelines.GUI_TEXTURED, GUI, j + 134, k + 2, 176, 16, hp, 6, 256, 256);
        // ele tem quatro fileiras sempre, e o original desenhava a quarta por cima
        g.blit(RenderPipelines.GUI_TEXTURED, GUI, j, k + 80, 0, 206, this.imageWidth, 27, 256, 256);
        if (trunk.isWaiting()) g.blit(RenderPipelines.GUI_TEXTURED, GUI, j + 112, k, 176, 0, 10, 10, 256, 256);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        EvilTrunkEntity trunk = this.menu.trunk;
        if (trunk == null) return handled;
        int x = (int) event.x() - (this.leftPos + 112), y = (int) event.y() - this.topPos;
        if (x >= 0 && y >= 0 && x < 10 && y <= 10) {
            trunk.level().playLocalSound(trunk.getX(), trunk.getY(), trunk.getZ(), SoundEvents.UI_BUTTON_CLICK.value(),
                    net.minecraft.sounds.SoundSource.NEUTRAL, 0.3f, 0.6f + (trunk.isWaiting() ? 0.0f : 0.2f), false);
            this.minecraft.player.sendSystemMessage(
                    Component.translatable(trunk.isWaiting() ? "entity.trunk.move" : "entity.trunk.stay"));
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
            return true;
        }
        return handled;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
