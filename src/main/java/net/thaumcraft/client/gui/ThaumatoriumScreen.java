package net.thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.block.entity.ThaumatoriumBlockEntity;
import net.thaumcraft.crafting.CrucibleRecipe;
import net.thaumcraft.inventory.ThaumatoriumMenu;
import net.thaumcraft.registry.TCSounds;
import org.joml.Matrix3x2fStack;

import java.util.List;

/**
 * A tela do taumatório: o {@code GuiThaumatorium} da 4.2.3.5. A casa do catalisador; à direita, a receita da vez com as
 * setas para passar entre as possíveis; embaixo, os aspectos dela (até seis, com setas se houver mais) e, se estiver
 * marcada, as barrinhas do quanto já entrou de cada um. Clicar no resultado marca ou desmarca a receita; com matrizes
 * mnemônicas, o canto mostra quantas estão marcadas de quantas cabem.
 */
public class ThaumatoriumScreen extends AbstractContainerScreen<ThaumatoriumMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/gui_thaumatorium.png");
    private int index;
    private int lastSize;
    private int startAspect;

    public ThaumatoriumScreen(ThaumatoriumMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.titleLabelY = -1000;
        this.inventoryLabelY = -1000;
        this.menu.updateRecipes();
        this.lastSize = this.menu.recipes.size();
        this.refreshIndex();
    }

    private ThaumatoriumBlockEntity tile() {
        return this.menu.thaumatorium;
    }

    private void refreshIndex() {
        ThaumatoriumBlockEntity tile = this.tile();
        if (tile != null && !this.menu.recipes.isEmpty()) {
            for (int a = 0; a < this.menu.recipes.size(); a++) {
                if (tile.recipeHash.contains(this.menu.recipes.get(a).hash())) {
                    this.index = a;
                    break;
                }
            }
        }
        this.startAspect = 0;
    }

    private void blit(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int w, int h, int colour) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, u, v, w, h, 256, 256, colour);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mx, int my, float partial) {
        super.extractBackground(graphics, mx, my, partial);
        int k = this.leftPos, l = this.topPos;
        this.blit(graphics, k, l, 0, 0, this.imageWidth, this.imageHeight, -1);
        ThaumatoriumBlockEntity tile = this.tile();
        this.menu.updateRecipes();
        List<CrucibleRecipe> recipes = this.menu.recipes;
        if (tile == null || recipes.isEmpty()) return;
        if (this.index >= recipes.size()) this.index = recipes.size() - 1;
        if (this.lastSize != recipes.size()) {
            this.lastSize = recipes.size();
            this.refreshIndex();
        }
        if (this.index < 0) this.index = 0;
        CrucibleRecipe recipe = recipes.get(this.index);
        if (recipes.size() > 1) {
            this.blit(graphics, k + 128, l + 16, this.index > 0 ? 192 : 176, 16, 16, 8, -1);
            this.blit(graphics, k + 128, l + 24, this.index < recipes.size() - 1 ? 192 : 176, 24, 16, 8, -1);
        }
        if (recipe.cost().size() > 6) {
            this.blit(graphics, k + 32, l + 40, this.startAspect > 0 ? 192 : 176, 32, 8, 16, -1);
            this.blit(graphics, k + 136, l + 40, this.startAspect < recipe.cost().size() - 1 ? 200 : 184, 32, 8, 16, -1);
        } else {
            this.startAspect = 0;
        }
        boolean marked = tile.recipeHash.contains(recipe.hash());
        if (!tile.recipeHash.isEmpty()) {
            int x = mx - (k + 112), y = my - (l + 16);
            if (x >= 0 && y >= 0 && x < 16 && y < 16 || marked) this.blit(graphics, k + 104, l + 8, 176, 96, 48, 48, -1);
            var player = this.minecraft.player;
            float alpha = 0.6f + Mth.sin(player.tickCount / 5.0f) * 0.4f + 0.4f;
            this.blit(graphics, k + 88, l + 16, 176, 56, 24, 24, Mth.clamp((int) (alpha * 255), 0, 255) << 24 | 0xFFFFFF);
        }
        this.drawAspects(graphics, k, l, recipe, marked);
        this.drawOutput(graphics, k, l, mx, my, recipe, marked);
        if (tile.maxRecipes > 1) {
            Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(k + 136, l + 33);
            pose.scale(0.5f, 0.5f);
            String text = tile.recipeHash.size() + "/" + tile.maxRecipes;
            graphics.text(this.font, text, -this.font.width(text) / 2, 0, 0xFFFFFFFF, false);
            pose.popMatrix();
        }
    }

    private void drawAspects(GuiGraphicsExtractor graphics, int k, int l, CrucibleRecipe recipe, boolean marked) {
        ThaumatoriumBlockEntity tile = this.tile();
        AspectList cost = recipe.cost();
        List<Aspect> sorted = cost.getAspectsSorted();
        if (marked) {
            int count = 0, pos = 0;
            for (Aspect aspect : sorted) {
                if (count >= this.startAspect) {
                    this.blit(graphics, k + 41 + 16 * pos, l + 57, 176, 8, 14, 6, -1);
                    int i1 = (int) ((float) tile.essentia.getAmount(aspect) / cost.getAmount(aspect) * 12.0f);
                    this.blit(graphics, k + 42 + 16 * pos, l + 58, 176, 0, i1, 4, 0xFF000000 | aspect.color());
                    pos++;
                }
                if (++count >= 6 + this.startAspect) break;
            }
        }
        int count = 0, pos = 0;
        for (Aspect aspect : sorted) {
            if (count >= this.startAspect) {
                ArcaneWorkbenchScreen.tag(graphics, k + 40 + 16 * pos, l + 40, aspect, cost.getAmount(aspect), 1.0f);
                pos++;
            }
            if (++count >= 6 + this.startAspect) break;
        }
    }

    /** O resultado; esmaecido e pulsando quando ainda dá para marcar (ou já está marcado). */
    private void drawOutput(GuiGraphicsExtractor graphics, int x, int y, int mx, int my, CrucibleRecipe recipe, boolean marked) {
        ThaumatoriumBlockEntity tile = this.tile();
        graphics.item(recipe.result(), x + 112, y + 16);
        graphics.itemDecorations(this.font, recipe.result(), x + 112, y + 16);
        if (tile.recipeHash.size() < tile.maxRecipes || marked) {
            float alpha = 0.3f + Mth.sin(this.minecraft.player.tickCount / 4.0f) * 0.3f + 0.3f;
            graphics.fill(x + 112, y + 16, x + 128, y + 32, Mth.clamp((int) ((1.0f - alpha) * 160), 0, 255) << 24 | 0x404040);
        }
        int xx = mx - (x + 112), yy = my - (y + 16);
        if (xx >= 0 && yy >= 0 && xx < 16 && yy < 16) graphics.setTooltipForNextFrame(this.font, recipe.result(), mx, my);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        ThaumatoriumBlockEntity tile = this.tile();
        List<CrucibleRecipe> recipes = this.menu.recipes;
        int mx = (int) event.x(), my = (int) event.y();
        int gx = this.leftPos, gy = this.topPos;
        if (tile == null || recipes.isEmpty() || this.index < 0 || this.index >= recipes.size()) return handled;
        int x = mx - (gx + 112), y = my - (gy + 16);
        if (x >= 0 && y >= 0 && x < 16 && y < 16
                && (tile.recipeHash.size() < tile.maxRecipes || tile.recipeHash.contains(recipes.get(this.index).hash()))) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, this.index);
            this.sound(TCSounds.HHON.value(), 0.3f);
            return true;
        }
        if (recipes.size() > 1) {
            x = mx - (gx + 128);
            if (this.index > 0 && x >= 0 && x < 16 && my - (gy + 16) >= 0 && my - (gy + 16) < 8) {
                this.index--;
                this.sound(TCSounds.CAMERA_CLACK.value(), 0.4f);
                return true;
            }
            if (this.index < recipes.size() - 1 && x >= 0 && x < 16 && my - (gy + 24) >= 0 && my - (gy + 24) < 8) {
                this.index++;
                this.sound(TCSounds.CAMERA_CLACK.value(), 0.4f);
                return true;
            }
        }
        int size = recipes.get(this.index).cost().size();
        if (size > 6) {
            if (this.startAspect > 0 && mx - (gx + 32) >= 0 && mx - (gx + 32) < 8 && my - (gy + 40) >= 0 && my - (gy + 40) < 16) {
                this.startAspect--;
                this.sound(TCSounds.CAMERA_CLACK.value(), 0.4f);
                return true;
            }
            if (this.startAspect < size - 1 && mx - (gx + 136) >= 0 && mx - (gx + 136) < 8 && my - (gy + 40) >= 0 && my - (gy + 40) < 16) {
                this.startAspect++;
                this.sound(TCSounds.CAMERA_CLACK.value(), 0.4f);
                return true;
            }
        }
        return handled;
    }

    private void sound(net.minecraft.sounds.SoundEvent sound, float volume) {
        var p = this.minecraft.player;
        p.level().playLocalSound(p.getX(), p.getY(), p.getZ(), sound, SoundSource.PLAYERS, volume, 1.0f, false);
    }
}
