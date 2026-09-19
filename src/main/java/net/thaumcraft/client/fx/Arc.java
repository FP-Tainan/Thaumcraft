package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.AdditiveGlow;

import java.util.ArrayList;
import java.util.List;

/**
 * O arco de faíscas: o {@code FXArc} da 4.2.3.5. Uma parábola de um ponto a outro, sacudida a cada passo, desenhada por um
 * tique com a textura {@code beamh.png} em duas fitas cruzadas, somando luz. Onde chega, uma estrelinha da mesma cor (o
 * {@code arcLightning} do {@code ClientProxy}).
 */
public final class Arc implements ThaumFx.Effect {
    private static final Identifier BEAM = Thaumcraft.id("textures/misc/beamh.png");
    private final Vec3 origin;
    private final List<Vec3> points = new ArrayList<>();
    private final float red, green, blue;
    private final float length;
    private int age;

    private Arc(RandomSource random, double x, double y, double z, double tx, double ty, double tz, float r, float g, float b, double hg) {
        this.origin = new Vec3(x, y, z);
        this.red = r;
        this.green = g;
        this.blue = b;
        double gravity = 0.115, noise = 0.25;
        Vec3 vs = Vec3.ZERO, ve = new Vec3(tx - x, ty - y, tz - z), vc = Vec3.ZERO;
        this.length = (float) ve.length();
        Vec3 vv = velocity(vs, ve, hg, gravity);
        double l = vv.lengthSqr();
        this.points.add(vs);
        for (int c = 0; ve.distanceToSqr(vc) > l && c < 50; c++) {
            Vec3 vt = vc.add(vv);
            vc = vt;
            vt = vt.add((random.nextDouble() - random.nextDouble()) * noise, (random.nextDouble() - random.nextDouble()) * noise,
                    (random.nextDouble() - random.nextDouble()) * noise);
            this.points.add(vt);
            vv = vv.add(0.0, -gravity / 1.9, 0.0);
        }
        this.points.add(ve);
    }

    /** O {@code arcLightning}: a estrelinha na chegada e o arco. */
    public static void spawn(RandomSource random, double x, double y, double z, double tx, double ty, double tz, float r, float g, float b, float h) {
        Sparkle.coloured(random, tx, ty, tz, 3.0f, 2, r, g, b);
        ThaumFx.add(new Arc(random, x, y, z, tx, ty, tz, r, g, b, h));
    }

    /** O {@code Utils.calculateVelocity}: a velocidade de lançamento que faz a parábola subir {@code heightGain} e chegar. */
    private static Vec3 velocity(Vec3 from, Vec3 to, double heightGain, double gravity) {
        double endGain = to.y - from.y;
        double horizDist = Math.sqrt((to.x - from.x) * (to.x - from.x) + (to.z - from.z) * (to.z - from.z));
        double maxGain = heightGain > endGain + heightGain ? heightGain : endGain + heightGain;
        double a = -horizDist * horizDist / (4.0 * maxGain);
        double b = horizDist;
        double c = -endGain;
        double slope = -b / (2.0 * a) - Math.sqrt(b * b - 4.0 * a * c) / (2.0 * a);
        double vy = Math.sqrt(maxGain * gravity);
        double vh = vy / slope;
        double dx = to.x - from.x, dz = to.z - from.z;
        double mag = Math.sqrt(dx * dx + dz * dz);
        return new Vec3(vh * dx / mag, vy, vh * dz / mag);
    }

    @Override
    public boolean tick() {
        return this.age++ < 1;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        Vec3 cam = view.camera();
        float ox = (float) (this.origin.x - cam.x), oy = (float) (this.origin.y - cam.y), oz = (float) (this.origin.z - cam.z);
        int colour = (int) (0.8f * 255) << 24 | (int) (this.red * 255) << 16 | (int) (this.green * 255) << 8 | (int) (this.blue * 255);
        float size = 0.25f;
        collector.submitCustomGeometry(pose, AdditiveGlow.of(BEAM), (m, c) -> {
            for (int pass = 0; pass < 2; pass++) {
                for (int i = 0; i + 1 < this.points.size(); i++) {
                    Vec3 p = this.points.get(i), q = this.points.get(i + 1);
                    float u0 = i / this.length, u1 = (i + 1) / this.length;
                    float wx = pass == 0 ? 0.0f : size, wy = pass == 0 ? size : 0.0f;
                    vertex(m, c, ox + p.x - wx, oy + p.y - wy, oz + p.z - wx, u0, 1.0f, colour);
                    vertex(m, c, ox + p.x + wx, oy + p.y + wy, oz + p.z + wx, u0, 0.0f, colour);
                    vertex(m, c, ox + q.x + wx, oy + q.y + wy, oz + q.z + wx, u1, 0.0f, colour);
                    vertex(m, c, ox + q.x - wx, oy + q.y - wy, oz + q.z - wx, u1, 1.0f, colour);
                    vertex(m, c, ox + q.x - wx, oy + q.y - wy, oz + q.z - wx, u1, 1.0f, colour);
                    vertex(m, c, ox + q.x + wx, oy + q.y + wy, oz + q.z + wx, u1, 0.0f, colour);
                    vertex(m, c, ox + p.x + wx, oy + p.y + wy, oz + p.z + wx, u0, 0.0f, colour);
                    vertex(m, c, ox + p.x - wx, oy + p.y - wy, oz + p.z - wx, u0, 1.0f, colour);
                }
            }
        });
    }

    private static void vertex(PoseStack.Pose m, com.mojang.blaze3d.vertex.VertexConsumer c, double x, double y, double z, float u, float v, int colour) {
        c.addVertex(m, (float) x, (float) y, (float) z).setColor(colour).setUv(u, v)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(200).setNormal(m, 0.0f, 1.0f, 0.0f);
    }
}
