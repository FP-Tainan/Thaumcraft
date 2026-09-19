package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.GolemEntity;
import net.thaumcraft.entity.golem.Marker;
import net.thaumcraft.item.GolemBellItem;
import net.thaumcraft.item.GolemPlacerItem;
import net.thaumcraft.registry.TCComponents;
import org.joml.Quaternionf;

import java.util.List;

/**
 * As marcas do sino no mundo: o {@code renderMarkedBlocks} do {@code RenderEventHandler} da 4.2.3.5.
 *
 * <p>Com o sino (ligado a um golem) ou um golem guardado na mão, cada face marcada ganha a runa {@code mark.png} da cor
 * dela (a sem cor pulsa em tons claros); um bloco de ar marcado fica com as seis faces acesas; a casa do golem ganha a
 * runa {@code home.png}; e, com o sino, uma linha de escrita ({@code script.png}) sai da cabeça do golem até cada marca.
 * Tudo na luz que soma, sem escrever profundidade.
 */
public final class MarkerOverlay {
    private static final Identifier MARK = Thaumcraft.id("textures/misc/mark.png");
    private static final Identifier HOME = Thaumcraft.id("textures/misc/home.png");
    private static final Identifier EMPTY = Thaumcraft.id("textures/misc/marker_empty.png");
    private static final Identifier SCRIPT = Thaumcraft.id("textures/misc/script.png");
    /** O {@code golemLinkQuality} de fábrica. */
    private static final int LINK_QUALITY = 16;

    private MarkerOverlay() {
    }

    public static void init() {
        LevelRenderEvents.COLLECT_SUBMITS.register(MarkerOverlay::draw);
    }

    private static void draw(net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;
        ItemStack held = player.getMainHandItem();
        boolean bell = held.getItem() instanceof GolemBellItem;
        if (!bell && !(held.getItem() instanceof GolemPlacerItem)) return;
        List<Marker> markers = held.get(TCComponents.GOLEM_MARKERS);
        if (markers == null) return;
        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        GolemEntity golem = null;
        GolemBellItem.Link link = null;
        if (bell) {
            link = held.get(TCComponents.GOLEM_LINK);
            if (link != null && mc.level.getEntity(link.golemId()) instanceof GolemEntity g) golem = g;
            if (golem == null) return;
        }
        Vec3 camera = context.levelState().cameraRenderState.pos;
        PoseStack pose = context.poseStack();
        SubmitNodeCollector collector = context.submitNodeCollector();
        float time = (float) (net.minecraft.util.Util.getNanos() / 30_000_000L);
        if (golem != null && link.face() > -1 && player.distanceToSqr(Vec3.atLowerCornerOf(link.home())) < 4096.0) {
            face(pose, collector, camera, link.home(), link.face(), 0.65f, pulse(time, link.face()), HOME, 1.0f);
        }
        for (Marker m : markers) {
            Direction dir = m.direction();
            BlockPos in = m.pos().relative(dir);
            if (!m.in(mc.level) || player.distanceToSqr(in.getX(), in.getY(), in.getZ()) >= 4096.0) continue;
            int colour = m.color() == -1 ? pulse(time, m.side()) : GolemBellItem.COLORS[m.color()];
            face(pose, collector, camera, in, m.side(), 0.4f, colour, MARK, 1.0f);
            if (mc.level.isEmptyBlock(m.pos())) {
                for (Direction a : Direction.values()) {
                    int c = m.color() == -1 ? pulse(time, a.get3DDataValue()) : GolemBellItem.COLORS[m.color()];
                    face(pose, collector, camera, m.pos().relative(a), a.get3DDataValue(), 0.98f, c, EMPTY, -1.0f);
                }
            }
            if (golem != null && LINK_QUALITY > 3) {
                line(pose, collector, camera, in.getX() - dir.getStepX() * 0.5, in.getY() - dir.getStepY() * 0.5,
                        in.getZ() - dir.getStepZ() * 0.5, m.side(), m.color(), golem, partial, time);
            }
        }
    }

    /** A cor que pulsa das marcas sem cor: três senos em compassos diferentes, um por canal. */
    private static int pulse(float time, int side) {
        float r = Mth.sin(time % 32767.0f / 12.0f + side) * 0.2f + 0.8f;
        float g = Mth.sin(time % 32767.0f / 14.0f + side) * 0.2f + 0.8f;
        float b = Mth.sin(time % 32767.0f / 16.0f + side) * 0.2f + 0.8f;
        return (int) (r * 255) << 16 | (int) (g * 255) << 8 | (int) (b * 255);
    }

    /**
     * O {@code drawMarkerOverlay} (e o da casa, e o do ar): o quadrado no meio do bloco, virado para a face e levado até
     * ela, um centésimo para fora ({@code nudge} 1) ou para dentro (−1).
     */
    private static void face(PoseStack pose, SubmitNodeCollector collector, Vec3 camera, BlockPos at, int side, float scale,
                             int rgb, Identifier texture, float nudge) {
        Direction dir = Direction.from3DDataValue(side);
        pose.pushPose();
        pose.translate(at.getX() + 0.5 + dir.getStepX() * 0.01f * nudge - camera.x, at.getY() + 0.5 + dir.getStepY() * 0.01f * nudge - camera.y,
                at.getZ() + 0.5 + dir.getStepZ() * 0.01f * nudge - camera.z);
        pose.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(90.0), -dir.getStepY(), dir.getStepX(), -dir.getStepZ()));
        pose.translate(0.0, 0.0, dir.getStepZ() < 0 ? 0.5 : -0.5);
        pose.scale(scale, scale, scale);
        int colour = 0xFF000000 | rgb;
        collector.submitCustomGeometry(pose, AdditiveGlow.twoSided(texture), (m, c) -> {
            vertex(m, c, -0.5f, 0.5f, 0.0f, 1.0f, colour);
            vertex(m, c, 0.5f, 0.5f, 1.0f, 1.0f, colour);
            vertex(m, c, 0.5f, -0.5f, 1.0f, 0.0f, colour);
            vertex(m, c, -0.5f, -0.5f, 0.0f, 0.0f, colour);
        });
        pose.popPose();
    }

    private static void vertex(PoseStack.Pose m, VertexConsumer c, float x, float y, float u, float v, int colour) {
        c.addVertex(m, x, y, 0.0f).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(200).setNormal(m, 0.0f, 0.0f, 1.0f);
    }

    /** O {@code drawMarkerLine}: a fita de escrita ondulando da cabeça do golem até a marca. */
    private static void line(PoseStack pose, SubmitNodeCollector collector, Vec3 camera, double x, double y, double z, int side, int color,
                             Entity golem, float partial, float time) {
        double ePX = Mth.lerp(partial, golem.xo, golem.getX());
        double ePY = Mth.lerp(partial, golem.yo, golem.getY());
        double ePZ = Mth.lerp(partial, golem.zo, golem.getZ());
        Direction dir = Direction.from3DDataValue(side);
        double ds1x = ePX, ds1y = ePY + golem.getBbHeight(), ds1z = ePZ;
        double dc1x = (float) (x + 0.5 + dir.getStepX() * 0.5 - ds1x);
        double dc1y = (float) (y + 0.5 + dir.getStepY() * 0.5 - ds1y);
        double dc1z = (float) (z + 0.5 + dir.getStepZ() * 0.5 - ds1z);
        double dc22x = (float) (x + 0.5 - ds1x), dc22y = (float) (y + 0.5 - ds1y), dc22z = (float) (z + 0.5 - ds1z);
        double d3 = x - ePX, d4 = y - ePY, d5 = z - ePZ;
        float dist = (float) Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
        float blocks = Math.round(dist);
        float length = blocks * LINK_QUALITY;
        if (length <= 0) return;
        int n = (int) length + 1;
        double[] px = new double[n], py = new double[n], pz = new double[n];
        float[] us = new float[n];
        int[] cols = new int[n];
        float r = 1.0f, g = 1.0f, b = 1.0f;
        if (color > -1) {
            int c = GolemBellItem.COLORS[color];
            r = (c >> 16 & 255) / 255.0f;
            g = (c >> 8 & 255) / 255.0f;
            b = (c & 255) / 255.0f;
        }
        for (int i = 0; i <= length && i < n; i++) {
            float f2 = i / length;
            float f2a = Math.min(0.75f, i * 1.5f / length);
            float f3 = 1.0f - Math.abs(i - length / 2.0f) / (length / 2.0f);
            float f4 = 0.0f;
            if (color == -1) {
                r = Mth.sin(time % 32767.0f / 12.0f + side + i) * 0.2f + 0.8f;
                g = Mth.sin(time % 32767.0f / 14.0f + side + i) * 0.2f + 0.8f;
                b = Mth.sin(time % 32767.0f / 16.0f + side + i) * 0.2f + 0.8f;
            }
            double wz = Math.sin((side * 20 + z % 16.0 + dist * (1.0f - f2) * LINK_QUALITY - time % 32767.0f / 5.0f) / 4.0) * 0.5f;
            double wx = Math.sin((side * 20 + x % 16.0 + dist * (1.0f - f2) * LINK_QUALITY - time % 32767.0f / 5.0f) / 3.0) * 0.5f;
            double wy = Math.sin((side * 20 + y % 16.0 + dist * (1.0f - f2) * LINK_QUALITY - time % 32767.0f / 5.0f) / 2.0) * 0.5f;
            double dx = dc1x + wz * f3, dy = dc1y + wx * f3, dz = dc1z + wy * f3;
            if (i > length - LINK_QUALITY / 2.0f) {
                double dx2 = dc22x + wz * f3, dy2 = dc22y + wx * f3, dz2 = dc22z + wy * f3;
                f3 = (length - i) / (LINK_QUALITY / 2.0f);
                f4 = 1.0f - f3;
                dx = dx * f3 + dx2 * f4;
                dy = dy * f3 + dy2 * f4;
                dz = dz * f3 + dz2 * f4;
            }
            float alpha = f2a * (1.0f - f4);
            cols[i] = (int) (Mth.clamp(alpha, 0, 1) * 255) << 24 | (int) (r * 255) << 16 | (int) (g * 255) << 8 | (int) (b * 255);
            px[i] = dx * f2;
            py[i] = dy * f2;
            pz[i] = dz * f2;
            us[i] = (1.0f - f2) * dist - time * 0.005f;
        }
        pose.pushPose();
        pose.translate(ePX - camera.x, ePY + golem.getBbHeight() - camera.y, ePZ - camera.z);
        collector.submitCustomGeometry(pose, AdditiveGlow.twoSided(SCRIPT), (m, c) -> {
            // a tira de triângulos do original, em quadrados entre cada par de pontos
            for (int i = 0; i + 1 < n; i++) {
                strip(m, c, px[i], py[i] - 0.05, pz[i], us[i], 1.0f, cols[i]);
                strip(m, c, px[i], py[i] + 0.05, pz[i], us[i], 0.0f, cols[i]);
                strip(m, c, px[i + 1], py[i + 1] + 0.05, pz[i + 1], us[i + 1], 0.0f, cols[i + 1]);
                strip(m, c, px[i + 1], py[i + 1] - 0.05, pz[i + 1], us[i + 1], 1.0f, cols[i + 1]);
            }
        });
        pose.popPose();
    }

    private static void strip(PoseStack.Pose m, VertexConsumer c, double x, double y, double z, float u, float v, int colour) {
        c.addVertex(m, (float) x, (float) y, (float) z).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0).setNormal(m, 0.0f, 1.0f, 0.0f);
    }
}
