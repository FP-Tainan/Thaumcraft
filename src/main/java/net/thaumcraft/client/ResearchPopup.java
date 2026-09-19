package net.thaumcraft.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.gui.ThaumonomiconScreen;
import net.thaumcraft.research.Research;

import java.util.ArrayList;
import java.util.List;

/**
 * O {@code GuiResearchPopup}: a faixa "Research Completed!" que desce no canto de cima à esquerda por três segundos
 * quando uma pesquisa se completa, com o ícone e o nome dela — uma de cada vez, em fila. A moldura é a das conquistas
 * (a de então, {@code achievement_background}, é hoje o {@code toast/advancement}). Aparece por cima de tudo, também
 * com uma tela aberta.
 */
public final class ResearchPopup {
    private static final Identifier FRAME = Identifier.withDefaultNamespace("toast/advancement");
    private static final List<Research> QUEUE = new ArrayList<>();
    private static long researchTime;

    private ResearchPopup() {
    }

    public static void init() {
        HudElementRegistry.addLast(Thaumcraft.id("research_popup"), (graphics, tracker) -> {
            if (Minecraft.getInstance().gui.screen() == null) draw(graphics);
        });
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) ->
                ScreenEvents.afterExtract(screen).register((s, graphics, mouseX, mouseY, partial) -> draw(graphics)));
    }

    /** O {@code queueResearchInformation}: entra na fila, e o livro passa a abrir na casa dela. */
    public static void queue(Research research) {
        if (researchTime == 0L) researchTime = Util.getMillis();
        QUEUE.add(research);
        ThaumonomiconScreen.lastX = research.column();
        ThaumonomiconScreen.lastY = research.row();
    }

    private static void draw(GuiGraphicsExtractor graphics) {
        if (QUEUE.isEmpty() || researchTime == 0L) return;
        double var1 = (Util.getMillis() - researchTime) / 3000.0;
        if (var1 < 0.0 || var1 > 1.0) {
            QUEUE.removeFirst();
            researchTime = QUEUE.isEmpty() ? 0L : Util.getMillis();
            return;
        }
        double var3 = var1 * 2.0;
        if (var3 > 1.0) var3 = 2.0 - var3;
        var3 *= 4.0;
        var3 = 1.0 - var3;
        if (var3 < 0.0) var3 = 0.0;
        var3 *= var3;
        var3 *= var3;
        int x = 0;
        int y = -(int) (var3 * 36.0);
        var font = Minecraft.getInstance().font;
        Research research = QUEUE.getFirst();
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FRAME, x, y, 160, 32);
        graphics.text(font, "Research Completed!", x + 30, y + 7, -256, false);
        Component name = research.name();
        int offset = font.width(name);
        if (offset <= 125) {
            graphics.text(font, name, x + 30, y + 18, -1, false);
        } else {
            float vv = 125.0f / offset;
            var pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(x + 30, y + 16 + 2.0f / vv);
            pose.scale(vv, vv);
            graphics.text(font, name, 0, 0, -1, false);
            pose.popMatrix();
        }
        if (research.iconStack() != null) {
            graphics.item(research.iconStack().get(), x + 8, y + 8);
        } else if (research.icon() != null) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, research.icon(), x + 8, y + 8, 0, 0, 16, 16, 16, 16);
        }
    }
}
