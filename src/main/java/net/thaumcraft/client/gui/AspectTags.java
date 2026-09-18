package net.thaumcraft.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import org.joml.Matrix3x2fStack;

import java.text.DecimalFormat;

/**
 * O {@code UtilsFX.drawTag} da 4.2.3.5: o símbolo de um aspecto nas telas do mod.
 *
 * <p>O símbolo vai em dezesseis por dezesseis, na cor do aspecto — ou quase preto, apagado. Com quantidade, o
 * número sai em letra de metade do tamanho no canto de baixo à direita, com contorno. Com bônus, uma
 * estrelinha que troca de quadro a cada tique brilha por trás, e se o bônus passa de um, o número dele sai em
 * cima.
 */
public final class AspectTags {
    private static final Identifier PARTICLES = Thaumcraft.id("textures/misc/particles.png");
    private static final DecimalFormat FORMAT = new DecimalFormat("#######.##");
    private static final int[][] OUTLINE = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    private AspectTags() {
    }

    /**
     * @param bw    apagado: quase preto, com oito décimos do alfa
     * @param ticks o relógio de quem vê, para a estrelinha do bônus
     */
    public static void draw(GuiGraphicsExtractor graphics, Font font, int x, int y, Aspect aspect, float amount,
                            int bonus, float alpha, boolean bw, int ticks) {
        if (aspect == null) return;
        int rgb = bw ? 0x1A1A1A : aspect.color();
        float a = bw ? alpha * 0.8f : alpha;
        int colour = Mth.clamp((int) (a * 255.0f), 0, 255) << 24 | rgb;
        graphics.blit(RenderPipelines.GUI_TEXTURED, aspect.image(), x, y, 0, 0, 16, 16, 16, 16, 16, 16, colour);

        Matrix3x2fStack pose = graphics.pose();
        if (amount > 0) {
            String text = FORMAT.format(amount);
            int width = font.width(text);
            pose.pushMatrix();
            pose.scale(0.5f, 0.5f);
            int tx = 32 - width + x * 2, ty = 32 - font.lineHeight + y * 2;
            for (int[] d : OUTLINE) graphics.text(font, text, tx + d[0], ty + d[1], 0xFF000000, false);
            graphics.text(font, text, tx, ty, 0xFFFFFFFF, false);
            pose.popMatrix();
        }
        if (bonus > 0) {
            int px = 16 * (ticks % 16);
            graphics.blit(RenderPipelines.GUI_TEXTURED, PARTICLES, x - 4, y - 4, px, 80, 16, 16, 256, 256);
            if (bonus > 1) {
                String text = Integer.toString(bonus);
                int sw = font.width(text) / 2;
                pose.pushMatrix();
                pose.scale(0.5f, 0.5f);
                int tx = 8 - sw + x * 2, ty = 15 - font.lineHeight + y * 2;
                for (int[] d : OUTLINE) graphics.text(font, text, tx + d[0], ty + d[1], 0xFF000000, false);
                graphics.text(font, text, tx, ty, 0xFFFFFFFF, false);
                pose.popMatrix();
            }
        }
    }
}
