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
 * A tela do forno alquímico, com a folha e as medidas do Thaumcraft 4.2.3.5.
 *
 * <p>Tudo aqui saiu do {@code GuiAlchemyFurnace} do original, chamada por chamada: a chama no meio, o
 * tubo de vidro da esquerda mostrando quanta essência o forno já guardou, e o da direita mostrando o
 * cozimento. Os dois enchem de baixo para cima.
 *
 * <p>O original não escreve rótulo nenhum nesta tela — não há um {@code drawString} na classe inteira —,
 * e por isso aqui também não há. Quem quiser saber <em>qual</em> essência está lá dentro põe os Óculos da
 * Revelação, que é como se lê isso no mod.
 */
public class AlchemicalFurnaceScreen extends AbstractContainerScreen<AlchemicalFurnaceMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/gui_alchemyfurnace.png");

    /** A chama, entre as duas casas. */
    private static final int FLAME_X = 80;
    private static final int FLAME_Y = 26;
    private static final int FLAME_W = 16;
    private static final int FLAME_H = 20;
    private static final int FLAME_U = 176;

    /** O tubo da esquerda: a essência que o forno já tem guardada. */
    private static final int VIS_X = 61;
    private static final int VIS_Y = 12;
    private static final int VIS_W = 8;
    private static final int VIS_H = 48;
    private static final int VIS_U = 200;

    /** O tubo da direita: o cozimento. */
    private static final int COOK_X = 106;
    private static final int COOK_Y = 13;
    private static final int COOK_W = 9;
    private static final int COOK_H = 46;
    private static final int COOK_U = 216;

    /** A moldura de vidro que vai por cima do tubo da esquerda. */
    private static final int GLASS_X = 60;
    private static final int GLASS_Y = 8;
    private static final int GLASS_W = 10;
    private static final int GLASS_H = 55;
    private static final int GLASS_U = 232;

    public AlchemicalFurnaceScreen(AlchemicalFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        // o original não escreve rótulo nenhum: os dois saem da tela
        this.titleLabelY = -1000;
        this.inventoryLabelY = -1000;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight, 256, 256);

        // a chama, que encolhe conforme o combustível acaba
        int lit = Math.round(FLAME_H * this.menu.burnt());
        if (lit > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                    this.leftPos + FLAME_X, this.topPos + FLAME_Y + FLAME_H - lit,
                    FLAME_U, FLAME_H - lit, FLAME_W, lit, 256, 256);
        }

        // o tubo da esquerda enche com a essência guardada, e a moldura de vidro vai por cima
        int held = Math.round(VIS_H * this.menu.filled());
        if (held > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                    this.leftPos + VIS_X, this.topPos + VIS_Y + VIS_H - held,
                    VIS_U, VIS_H - held, VIS_W, held, 256, 256);
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                this.leftPos + GLASS_X, this.topPos + GLASS_Y, GLASS_U, 0, GLASS_W, GLASS_H, 256, 256);

        // e o tubo da direita anda com o cozimento
        int done = Math.round(COOK_H * this.menu.cooked());
        if (done > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND,
                    this.leftPos + COOK_X, this.topPos + COOK_Y + COOK_H - done,
                    COOK_U, COOK_H - done, COOK_W, done, 256, 256);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractRenderState(graphics, mouseX, mouseY, partial);

        // sob o cursor do tubo da esquerda, o que há lá dentro por extenso
        if (!this.isHovering(GLASS_X, GLASS_Y, GLASS_W, GLASS_H, mouseX, mouseY)) return;
        AspectList held = this.held();
        List<Component> lines = new ArrayList<>();
        if (held == null || held.isEmpty()) {
            lines.add(Component.translatable("tc.furnace.empty"));
        } else {
            for (Aspect aspect : held.getAspects()) {
                lines.add(Component.translatable("tc.aspect.amount", aspect.name(), held.getAmount(aspect))
                        .withStyle(style -> style.withColor(aspect.color())));
            }
        }
        graphics.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
    }

    /** O que o forno tem guardado, lido no próprio bloco. */
    private AspectList held() {
        if (this.minecraft == null || this.minecraft.level == null) return null;
        var found = this.minecraft.level.getBlockEntity(this.menu.where());
        return found instanceof AlchemicalFurnaceBlockEntity furnace ? furnace.getAspects() : null;
    }
}
