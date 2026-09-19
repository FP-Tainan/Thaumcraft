package net.thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.ArcaneBoreBlockEntity;
import net.thaumcraft.inventory.ArcaneBoreMenu;
import org.joml.Matrix3x2fStack;

/**
 * A tela da broca arcana: o {@code GuiArcaneBore} da 4.2.3.5. A casa do foco, a da picareta (com o aviso quando ela
 * está por quebrar) e, à direita, em letra pequena, a largura, a velocidade e o que mais a broca faz.
 */
public class ArcaneBoreScreen extends AbstractContainerScreen<ArcaneBoreMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/gui_arcanebore.png");

    public ArcaneBoreScreen(ArcaneBoreMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 141);
        this.titleLabelY = -1000;
        this.inventoryLabelY = -1000;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mx, int my, float partial) {
        super.extractBackground(graphics, mx, my, partial);
        int k = this.leftPos, l = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, k, l, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        ArcaneBoreBlockEntity bore = this.menu.bore;
        if (bore == null) return;
        ItemStack pick = bore.pickaxe();
        if (!pick.isEmpty() && pick.getDamageValue() + 1 >= pick.getMaxDamage()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, k + 74, l + 18, 184, 0, 16, 16, 256, 256);
        }
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(k + 112, l + 8);
        pose.scale(0.5f, 0.5f);
        graphics.text(this.font, Component.translatable("tc.bore.width", 1 + (bore.area + bore.maxRadius) * 2), 0, 0, 0xFFFFFFFF, true);
        graphics.text(this.font, Component.translatable("tc.bore.speed", bore.speed), 0, 10, 0xFFFFFFFF, true);
        graphics.text(this.font, Component.translatable("tc.bore.other"), 0, 24, 0xFFFFFFFF, true);
        int base = 0;
        if (bore.dowsing()) {
            graphics.text(this.font, Component.translatable("tc.bore.clusters"), 4, 34 + base, 0xFFC0C0C0, true);
            base += 9;
        }
        if (bore.fortune > 0) {
            graphics.text(this.font, Component.translatable("tc.bore.fortune", bore.fortune), 4, 34 + base, 0xFFEEC64A, true);
            base += 9;
        }
        if (bore.silkTouch()) {
            graphics.text(this.font, Component.translatable("tc.bore.silk"), 4, 34 + base, 0xFF8080FF, true);
        }
        pose.popMatrix();
    }
}
