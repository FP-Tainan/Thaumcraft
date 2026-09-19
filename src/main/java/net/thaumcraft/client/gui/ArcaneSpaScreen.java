package net.thaumcraft.client.gui;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.ArcaneSpaBlockEntity;
import net.thaumcraft.inventory.ArcaneSpaMenu;
import net.thaumcraft.registry.TCSounds;

import java.util.List;

/**
 * A tela do spa arcano: o {@code GuiSpa} da 4.2.3.5. A casa dos sais, o botão de misturar ou só o fluido e o tanque
 * com o fluido desenhado dentro, cheio na proporção do que tem.
 */
public class ArcaneSpaScreen extends AbstractContainerScreen<ArcaneSpaMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/gui_spa.png");

    public ArcaneSpaScreen(ArcaneSpaMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.titleLabelY = -1000;
        this.inventoryLabelY = -1000;
    }

    private void blit(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int w, int h) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, u, v, w, h, 256, 256);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mx, int my, float partial) {
        super.extractBackground(graphics, mx, my, partial);
        int k = this.leftPos, l = this.topPos;
        this.blit(graphics, k, l, 0, 0, this.imageWidth, this.imageHeight);
        ArcaneSpaBlockEntity spa = this.menu.spa;
        if (spa == null) return;
        this.blit(graphics, k + 89, l + 35, 208, spa.mix() ? 16 : 32, 8, 8);
        if (spa.tank.amount > 0 && !spa.tank.isResourceBlank()) {
            var fluid = spa.tank.variant.getFluid();
            TextureAtlasSprite sprite = this.minecraft.getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState())
                    .stillMaterial().sprite();
            int colour = 0xFF000000 | FluidVariantRendering.getColor(spa.tank.variant);
            float bar = (float) spa.tank.amount / ArcaneSpaBlockEntity.CAPACITY;
            // o renderFluid do original: seis quadros de oito, de cima para baixo, na coluna do tanque
            for (int a = 0; a < 6; a++) graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, k + 107, l + 15 + a * 8, 8, 8, colour);
            this.blit(graphics, k + 107, l + 15, 107, 15, 10, (int) (48.0f - 48.0f * bar));
        }
        this.blit(graphics, k + 106, l + 11, 232, 0, 10, 55);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractRenderState(graphics, mouseX, mouseY, partial);
        ArcaneSpaBlockEntity spa = this.menu.spa;
        if (spa == null) return;
        int x = mouseX - (this.leftPos + 104), y = mouseY - (this.topPos + 10);
        if (x >= 0 && y >= 0 && x < 10 && y < 55 && !spa.tank.isResourceBlank()) {
            graphics.setComponentTooltipForNextFrame(this.font, List.of(FluidVariantAttributes.getName(spa.tank.variant),
                    Component.literal(spa.fluidMb() + " mb")), mouseX, mouseY);
        }
        x = mouseX - (this.leftPos + 88);
        y = mouseY - (this.topPos + 34);
        if (x >= 0 && y >= 0 && x < 10 && y < 10) {
            graphics.setComponentTooltipForNextFrame(this.font,
                    List.of(Component.translatable(spa.mix() ? "text.spa.mix.true" : "text.spa.mix.false")), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int x = (int) event.x() - (this.leftPos + 89), y = (int) event.y() - (this.topPos + 35);
        if (x >= 0 && y >= 0 && x < 8 && y < 8) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
            var p = this.minecraft.player;
            p.level().playLocalSound(p.getX(), p.getY(), p.getZ(), TCSounds.CAMERA_CLACK.value(), SoundSource.PLAYERS, 0.4f, 1.0f, false);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
