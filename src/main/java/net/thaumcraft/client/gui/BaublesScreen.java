package net.thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.inventory.BaublesMenu;

/**
 * O inventário expandido do Baubles 1.0.1.10: o {@code GuiPlayerExpanded}. O fundo {@code expanded_inventory.png},
 * o jogador olhando para o mouse, a grade de craft com o rótulo dela, e a marca escura por trás das casas de uma peça
 * só (armadura e amuletos) quando estão ocupadas.
 */
public class BaublesScreen extends AbstractContainerScreen<BaublesMenu> {
    public static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/expanded_inventory.png");

    public BaublesScreen(BaublesMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.titleLabelY = -1000;
        this.inventoryLabelY = -1000;
    }

    @Override
    protected void init() {
        super.init();
        // o botão de voltar ao inventário de sempre, no mesmo lugar do botão que abre este
        this.addRenderableWidget(new ToggleButton(this.leftPos + 66, this.topPos + 9, false));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        int k = this.leftPos, l = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, k, l, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        for (Slot slot : this.menu.slots) {
            if (slot.hasItem() && slot.getMaxStackSize() == 1) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, k + slot.x, l + slot.y, 200, 0, 16, 16, 256, 256);
            }
        }
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, k + 26, l + 8, k + 76, l + 78, 30, 0.0625f,
                mouseX, mouseY, this.minecraft.player);
    }

    /** A tecla do Baubles também fecha a tela, como no original. */
    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (net.thaumcraft.client.BaublesClient.KEY.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, Component.translatable("container.crafting"), 106, 16, 0xFF404040, false);
    }

    /**
     * O {@code GuiBaublesButton}: o botãozinho de dez por dez que troca entre o inventário de sempre e o expandido,
     * com o nome escrito embaixo quando o mouse passa.
     */
    public static class ToggleButton extends AbstractButton {
        private final boolean toBaubles;

        public ToggleButton(int x, int y, boolean toBaubles) {
            super(x, y, 10, 10, Component.translatable(toBaubles ? "button.thaumcraft.baubles" : "button.thaumcraft.normal"));
            this.toBaubles = toBaubles;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            net.thaumcraft.client.BaublesClient.toggle(this.toBaubles);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
            if (this.isHovered()) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.getX(), this.getY(), 210, 48, 10, 10, 256, 256);
                graphics.centeredText(net.minecraft.client.Minecraft.getInstance().font, this.getMessage(), this.getX() + 5,
                        this.getY() + this.getHeight(), 0xFFFFFFFF);
            } else {
                graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.getX(), this.getY(), 200, 48, 10, 10, 256, 256);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
