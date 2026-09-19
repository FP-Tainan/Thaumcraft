package net.thaumcraft.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.client.fx.Sparkle;

import java.util.ArrayList;
import java.util.List;

/**
 * Os avisos do canto de baixo, à direita: o {@code PlayerNotifications} e o {@code REHNotifyHandler} da 4.2.3.5.
 *
 * <p>Cada aviso é uma linha em letra miúda (meia escala), com o símbolo do aspecto ao lado quando tem um; o mais novo
 * entra piscando com uma faísca que corre da esquerda, e os de cima vão sumindo. Os pontos de pesquisa ganhos ainda
 * voam, um símbolo por ponto, do meio da tela até o livro que aparece no canto de cima.
 */
public final class PlayerNotifications {
    /** O {@code Config.notificationDelay} e o {@code Config.notificationMax}. */
    private static final long DELAY = 5000L;
    private static final int MAX = 15;
    private static final Identifier BOOK = Thaumcraft.id("textures/item/thaumonomicon.png");

    private record Notification(String text, Identifier image, long expire, long created, int colour) {
    }

    private record AspectNotification(Aspect aspect, float startX, float startY, long created, long expire) {
    }

    private static List<Notification> notifications = new ArrayList<>();
    private static List<AspectNotification> aspects = new ArrayList<>();

    private PlayerNotifications() {
    }

    public static void init() {
        HudElementRegistry.addLast(Thaumcraft.id("notifications"), (graphics, tracker) -> draw(graphics));
    }

    private static long now() {
        return System.nanoTime() / 1000000L;
    }

    public static void add(String text) {
        add(text, null, 0xFFFFFF);
    }

    public static void add(String text, Aspect aspect) {
        add(text, aspect.image(), aspect.color());
    }

    public static void add(String text, Identifier image, int colour) {
        long time = now();
        long bonus = notifications.isEmpty() ? DELAY / 2 : 0L;
        notifications.add(new Notification(text, image, time + DELAY + bonus, time + DELAY / 4, colour));
    }

    /** Um símbolo que voa do meio da tela para o livro. */
    public static void addAspect(Aspect aspect) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        long time = now() + level.getRandom().nextInt(1000);
        float x = 0.4f + level.getRandom().nextFloat() * 0.2f;
        float y = 0.4f + level.getRandom().nextFloat() * 0.2f;
        aspects.add(new AspectNotification(aspect, x, y, time, time + 1500L));
    }

    /** O {@code getListAndUpdate}: tira os vencidos; enquanto o primeiro está na tela, os outros não vencem. */
    private static List<Notification> update(long time) {
        List<Notification> kept = new ArrayList<>();
        boolean first = true;
        for (Notification li : notifications) {
            if (li.expire() >= time) {
                kept.add(first ? li : new Notification(li.text(), li.image(), time + DELAY, li.created(), li.colour()));
            }
            first = false;
        }
        notifications = kept;
        return kept;
    }

    private static void draw(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        long time = now();
        if (!update(time).isEmpty()) drawNotifications(graphics, minecraft, time);
        aspects.removeIf(li -> li.expire() < time);
        if (!aspects.isEmpty()) drawAspects(graphics, time);
    }

    private static void drawNotifications(GuiGraphicsExtractor graphics, Minecraft minecraft, long time) {
        int k = graphics.guiWidth();
        int l = graphics.guiHeight();
        float shift = -8.0f;
        for (int entry = 0; entry < notifications.size() && entry < MAX; entry++) {
            Notification li = notifications.get(entry);
            int size = minecraft.font.width(li.text()) / 2;
            int alpha = 255;
            boolean newest = entry == notifications.size() - 1 && li.created() > time;
            if (newest) alpha = 255 - (int) ((float) (li.created() - time) / (DELAY / 4) * 240.0f);
            if (li.expire() < time + DELAY) {
                alpha = (int) (255.0f - (float) (time + DELAY - li.expire()) / DELAY * 240.0f);
                shift = -8.0f * (alpha / 255.0f);
            }
            int colour = (alpha / 2 << 24) | 0xFFFFFF;
            var pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(k - size - 10, l - entry * 8 + shift);
            pose.scale(0.5f, 0.5f);
            graphics.text(minecraft.font, li.text(), -4, -8, colour, true);
            pose.popMatrix();
            if (li.image() != null) {
                pose.pushMatrix();
                pose.translate(k - 9, l - entry * 8 + shift - 6.0f);
                pose.scale(0.03125f, 0.03125f);
                int tint = Mth.clamp((int) (alpha / 511.0f * 255.0f), 0, 255) << 24 | li.colour() & 0xFFFFFF;
                graphics.blit(RenderPipelines.GUI_TEXTURED, li.image(), 0, 0, 0, 0, 256, 256, 256, 256, tint);
                pose.popMatrix();
            }
            if (newest) {
                float scale = (float) (li.created() - time) / (DELAY / 4);
                int a = 255 - (int) (scale * 240.0f);
                pose.pushMatrix();
                pose.translate(k - 5 - 8.0f * scale - (1.0f - scale) * (1.0f - scale) * (1.0f - scale) * size * 3.0f,
                        l - entry * 8 + shift - 2.0f - 8.0f * scale);
                pose.scale(scale, scale);
                int px = 16 * ((minecraft.player.tickCount + entry * 3) % 16);
                int tint = Mth.clamp((int) ((0.5f - a / 511.0f) * 255.0f), 0, 255) << 24 | 0xFFFFFF;
                graphics.blit(RenderPipelines.GUI_TEXTURED, Sparkle.PARTICLES, 0, 0, px, 80, 16, 16, 256, 256, tint);
                pose.popMatrix();
            }
        }
    }

    private static void drawAspects(GuiGraphicsExtractor graphics, long time) {
        int sw = graphics.guiWidth(), sh = graphics.guiHeight();
        float mainAlpha = 0.0f;
        var pose = graphics.pose();
        for (AspectNotification li : aspects) {
            if (li.created() > time) continue;
            int startX = (int) (sw * li.startX()), startY = (int) (sh * li.startY());
            int endX = sw, endY = -8;
            int bezierX = (int) (sw * (0.25f + li.startX())), bezierY = (int) (sh * li.startY());
            double t = (double) (time - li.created()) / (li.expire() - li.created());
            double x = (1.0 - t) * (1.0 - t) * startX + 2.0 * (1.0 - t) * t * bezierX + t * t * endX;
            double y = (1.0 - t) * (1.0 - t) * startY + 2.0 * (1.0 - t) * t * bezierY + t * t * endY;
            float alpha = 1.0f;
            if (t < 0.3f) alpha = (float) (t / 0.3f);
            else if (t > 0.66f) alpha = (float) (1.0 - (t - 0.66f) / 0.34f);
            mainAlpha = Math.max(mainAlpha, alpha);
            pose.pushMatrix();
            pose.translate((float) x, (float) y);
            pose.scale(0.075f * alpha, 0.075f * alpha);
            int tint = Mth.clamp((int) (alpha * 0.66f * 255.0f), 0, 255) << 24 | li.aspect().color() & 0xFFFFFF;
            graphics.blit(RenderPipelines.GUI_TEXTURED, li.aspect().image(), 0, 0, 0, 0, 256, 256, 256, 256, tint);
            pose.popMatrix();
        }
        if (mainAlpha > 0.0f) {
            pose.pushMatrix();
            pose.translate(sw - 16, 0.0f);
            pose.scale(0.0625f, 0.0625f);
            int tint = Mth.clamp((int) (mainAlpha * 255.0f), 0, 255) << 24 | 0xFFFFFF;
            graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, 0, 0, 0, 0, 256, 256, 256, 256, tint);
            pose.popMatrix();
        }
    }
}
