package net.thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.block.entity.AlchemicalFurnaceBlockEntity;
import net.thaumcraft.inventory.AlchemicalFurnaceMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * A tela do forno alquímico, com a folha do Thaumcraft 4.2.3.5.
 *
 * <p>Além da chama e da seta do forno comum, ela mostra o que o forno já tem guardado por dentro: os
 * símbolos dos aspectos em fila, cada um com o quanto há dele. Essa lista não vem pela tela — vem do
 * próprio bloco, que o servidor mantém acertado em quem está por perto.
 */
public class AlchemicalFurnaceScreen extends AbstractContainerScreen<AlchemicalFurnaceMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/gui_alchemyfurnace.png");
    /** Onde a chama fica na folha, e o tamanho dela. */
    private static final int FLAME_X = 56;
    private static final int FLAME_Y = 36;
    private static final int FLAME_W = 14;
    private static final int FLAME_H = 14;
    /** Onde a seta do cozimento fica. */
    private static final int ARROW_X = 79;
    private static final int ARROW_Y = 34;
    private static final int ARROW_W = 24;
    private static final int ARROW_H = 17;
    /** Onde a fila de aspectos começa. */
    private static final int LIST_X = 116;
    private static final int LIST_Y = 18;
    private static final int LIST_STEP = 18;
    private static final int SYMBOL = 16;

    public AlchemicalFurnaceScreen(AlchemicalFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight, 256, 256);

        // a chama, que encolhe conforme o combustível acaba
        float burnt = this.menu.burnt();
        if (burnt > 0.0f) {
            int lit = Math.max(1, Math.round(FLAME_H * burnt));
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                    this.leftPos + FLAME_X, this.topPos + FLAME_Y + FLAME_H - lit,
                    176, FLAME_H - lit, FLAME_W, lit, 256, 256);
        }
        // e a seta, que anda conforme o cozimento
        float cooked = this.menu.cooked();
        if (cooked > 0.0f) {
            int done = Math.max(1, Math.round(ARROW_W * cooked));
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                    this.leftPos + ARROW_X, this.topPos + ARROW_Y, 176, 14, done, ARROW_H, 256, 256);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractRenderState(graphics, mouseX, mouseY, partial);

        AspectList held = this.held();
        if (held == null || held.isEmpty()) return;

        int row = 0;
        for (Aspect aspect : held.getAspects()) {
            if (row >= 8) break;
            int amount = held.getAmount(aspect);
            int x = this.leftPos + LIST_X;
            int y = this.topPos + LIST_Y + row * LIST_STEP;
            graphics.blit(RenderPipelines.GUI_TEXTURED, aspect.image(), x, y, 0, 0,
                    SYMBOL, SYMBOL, SYMBOL, SYMBOL, 0xFF000000 | aspect.color());
            graphics.text(this.font, String.valueOf(amount), x + SYMBOL + 2, y + 4, -1, true);

            if (mouseX >= x && mouseX < x + SYMBOL && mouseY >= y && mouseY < y + SYMBOL) {
                List<Component> lines = new ArrayList<>();
                lines.add(aspect.name());
                lines.add(Component.literal(amount + " / " + AlchemicalFurnaceBlockEntity.MAX_VIS)
                        .withStyle(net.minecraft.ChatFormatting.GRAY));
                graphics.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
            }
            row++;
        }
    }

    /** O que o forno tem guardado, lido no próprio bloco. */
    private AspectList held() {
        if (this.minecraft == null || this.minecraft.level == null) return null;
        var found = this.minecraft.level.getBlockEntity(this.menu.where());
        return found instanceof AlchemicalFurnaceBlockEntity furnace ? furnace.getAspects() : null;
    }
}
