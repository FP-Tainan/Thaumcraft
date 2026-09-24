package net.thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.inventory.ArcaneChestMenu;

/**
 * O Baú Arcano aberto: o {@code ArcaneChestGui} do Magia Naturalis 0.5.0. São duas telas, uma para cada baú — a de
 * madeira-grande com nove casas por seis fileiras, a de prateada com onze por sete.
 */
public class ArcaneChestScreen extends AbstractContainerScreen<ArcaneChestMenu> {
    private static final Identifier GREATWOOD = Thaumcraft.id("textures/gui/chest_greatwood.png");
    private static final Identifier SILVERWOOD = Thaumcraft.id("textures/gui/chest_silverwood.png");

    private final Identifier background;

    public ArcaneChestScreen(ArcaneChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title,
                menu.columns() == 9 ? 176 : 212,
                18 + menu.rows() * 18 + 96);
        this.background = menu.columns() == 9 ? GREATWOOD : SILVERWOOD;
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = menu.columns() == 9 ? 8 : 26;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.background, this.leftPos, this.topPos,
                0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }
}
