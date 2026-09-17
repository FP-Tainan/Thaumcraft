package net.thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.crafting.ArcaneRecipe;
import net.thaumcraft.inventory.ArcaneWorkbenchMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * A tela da bancada arcana, com a folha e as medidas do Thaumcraft 4.2.3.5.
 *
 * <p>Os seis círculos em volta da grade são os aspectos primordiais: eles acendem na cor do aspecto quando
 * a receita da vez cobra aquele vis, e ficam apagados quando a varinha não tem o bastante.
 */
public class ArcaneWorkbenchScreen extends AbstractContainerScreen<ArcaneWorkbenchMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/gui_arcaneworkbench.png");
    /** Onde ficam os seis círculos na folha, na ordem dos primários do original. */
    private static final int[][] CIRCLES = {
            {88, 16}, {136, 40}, {136, 88}, {88, 112}, {40, 88}, {40, 40},
    };

    public ArcaneWorkbenchScreen(ArcaneWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 190, 234);
        // os rótulos encostados nos dois painéis da folha, que são separados
        this.titleLabelX = 8;
        this.titleLabelY = -10;
        this.inventoryLabelX = 16;
        this.inventoryLabelY = 140;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractRenderState(graphics, mouseX, mouseY, partial);
        // o que a receita da vez está cobrando, escrito nos círculos
        ArcaneRecipe recipe = this.menu.pending();
        if (recipe == null) return;
        boolean enough = this.menu.canAfford(recipe);
        List<Component> lines = new ArrayList<>();
        int index = 0;
        for (Aspect aspect : recipe.cost().getAspects()) {
            int amount = recipe.cost().getAmount(aspect);
            lines.add(Component.literal(aspect.name().getString() + " " + amount)
                    .withStyle(enough ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.RED));
            int[] where = CIRCLES[index % CIRCLES.length];
            graphics.blit(RenderPipelines.GUI_TEXTURED, aspect.image(),
                    this.leftPos + where[0] - 6, this.topPos + where[1] - 6, 0, 0, 12, 12, 12, 12,
                    (enough ? 0xFF000000 : 0x60000000) | aspect.color());
            index++;
        }
        // e, sob o cursor do resultado, a conta por extenso
        if (this.isHovering(160, 64, 16, 16, mouseX, mouseY)) {
            graphics.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
        }
    }
}
