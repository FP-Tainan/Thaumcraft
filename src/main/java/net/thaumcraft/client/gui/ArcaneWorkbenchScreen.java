package net.thaumcraft.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.crafting.ArcaneRecipe;
import net.thaumcraft.inventory.ArcaneWorkbenchMenu;
import net.thaumcraft.item.WandItem;
import org.joml.Matrix3x2fStack;

import java.text.DecimalFormat;

/**
 * A tela da bancada arcana: o {@code GuiArcaneWorkbench} da 4.2.3.5, descompilado.
 *
 * <p>Os seis círculos em volta da grade não são casas: são onde aparece o vis que a receita da vez cobra,
 * cada um sempre do mesmo primário — ar no alto, depois terra, fogo, água, ordem e entropia, dando a volta
 * no sentido anti-horário. O número já vem com o desconto das pontas da varinha. Enquanto a varinha não tem
 * aquele tanto, o símbolo pulsa meio apagado; quando tem, acende de vez.
 *
 * <p>Se a varinha não pagar a receita inteira, o resultado aparece escurecido na casa de saída, e em cima
 * dela um aviso miúdo em vermelho.
 */
public class ArcaneWorkbenchScreen extends AbstractContainerScreen<ArcaneWorkbenchMenu> {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/gui_arcaneworkbench.png");
    /** O centro de cada círculo, na ordem dos primários do original. */
    private static final int[][] ASPECT_LOCS = {
            {72, 21}, {24, 43}, {24, 102}, {72, 124}, {120, 102}, {120, 43},
    };
    private static final DecimalFormat FORMAT = new DecimalFormat("#######.##");
    /** O 15625838 do original. */
    private static final int INSUFFICIENT = 0xFFEE6E6E;
    private static final int[][] OUTLINE = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    public ArcaneWorkbenchScreen(ArcaneWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 190, 234);
    }

    /** O original não escreve nada por cima da folha: nem o nome da bancada, nem o do inventário. */
    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight, 256, 256);

        ArcaneRecipe recipe = this.menu.pending();
        if (recipe == null) return;
        ItemStack wand = this.menu.wand();
        boolean hasWand = wand.getItem() instanceof WandItem;
        AspectList cost = recipe.cost();
        int ticks = this.minecraft.player == null ? 0 : this.minecraft.player.tickCount;

        int count = 0;
        for (Aspect primal : Aspects.primals()) {
            float amount = cost.getAmount(primal);
            if (amount > 0) {
                float alpha = 0.5f + (Mth.sin((ticks + count * 10) / 2.0f) * 0.2f - 0.2f);
                if (hasWand) {
                    amount *= WandItem.cap(wand).discount(primal);
                    if (amount * WandItem.VIS_UNIT <= WandItem.vis(wand, primal)) alpha = 1.0f;
                }
                tag(graphics, this.leftPos + ASPECT_LOCS[count][0] - 8, this.topPos + ASPECT_LOCS[count][1] - 8,
                        primal, amount, alpha);
            }
            if (++count > 5) break;
        }

        if (hasWand && !WandItem.consume(wand, cost, false)) {
            // o resultado escurecido: o original pinta o item com um terço da cor e dois terços de opacidade
            int x = this.leftPos + 160, y = this.topPos + 64;
            graphics.item(recipe.result(), x, y);
            graphics.itemDecorations(this.font, recipe.result(), x, y);
            graphics.fill(x, y, x + 16, y + 16, 0xA81C1C1C);

            Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(this.leftPos + 168, this.topPos + 46);
            pose.scale(0.5f, 0.5f);
            String text = Component.translatable("tc.workbench.insufficient").getString();
            graphics.text(this.font, text, -this.font.width(text) / 2, 0, INSUFFICIENT, false);
            pose.popMatrix();
        }
    }

    /**
     * O {@code UtilsFX.drawTag} do original: o símbolo do aspecto, dezesseis por dezesseis na cor dele, e o
     * número em letra de metade do tamanho encostado no canto de baixo à direita, com contorno preto.
     */
    static void tag(GuiGraphicsExtractor graphics, int x, int y, Aspect aspect, float amount, float alpha) {
        int a = Mth.clamp((int) (alpha * 255.0f), 0, 255) << 24;
        graphics.blit(RenderPipelines.GUI_TEXTURED, aspect.image(), x, y, 0, 0, 16, 16, 16, 16, 16, 16,
                a | aspect.color());
        if (amount <= 0) return;
        var font = net.minecraft.client.Minecraft.getInstance().font;
        String text = FORMAT.format(amount);
        int width = font.width(text);
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(0.5f, 0.5f);
        int tx = 32 - width + x * 2, ty = 32 - font.lineHeight + y * 2;
        for (int[] d : OUTLINE) {
            graphics.text(font, text, tx + d[0], ty + d[1], 0xFF000000, false);
        }
        graphics.text(font, text, tx, ty, 0xFFFFFFFF, false);
        pose.popMatrix();
    }
}
