package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.AdditiveGlow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * O raio do Thaumcraft: o {@code FXLightningBoltCommon} e o {@code FXLightningBolt} da 4.2.3.5, descompilados.
 *
 * <p>O raio nasce como um segmento reto da ponta até o alvo e é quebrado ao meio sete vezes. Em cada quebra
 * o ponto do meio é empurrado para um lado ao acaso — cada vez menos —, e às vezes do ponto sai um galho, que
 * é um segmento a mais com metade da luz. No fim, os segmentos são ordenados por luz, e o raio é desenhado
 * crescendo da ponta para o alvo, três segmentos de comprimento por tique, em duas passadas: uma larga e
 * fraca ({@code p_large}) e uma fina e forte ({@code p_small}), as duas somando luz.
 */
public final class LightningBolt implements ThaumFx.Effect {
    private static final Identifier LARGE = Thaumcraft.id("textures/misc/p_large.png");
    private static final Identifier SMALL = Thaumcraft.id("textures/misc/p_small.png");

    private List<Segment> segments = new ArrayList<>();
    private final V start;
    private final V end;
    private final Map<Integer, Integer> splitParents = new HashMap<>();
    private float multiplier;
    private final float length;
    private int numSegments0;
    private int increment;
    private int type;
    private int numSplits;
    private boolean finalized;
    private final Random rand;
    private int particleAge;
    private int particleMaxAge;
    private float width = 0.03f;

    /**
     * O construtor de {@code (x1, y1, z1, x, y, z, seed, duration, multi, speed)} do original.
     */
    public LightningBolt(double x1, double y1, double z1, double x, double y, double z, long seed, int duration,
                         float multi, int speed) {
        this.start = new V(x1, y1, z1);
        this.end = new V(x, y, z);
        this.rand = new Random(seed);
        this.numSegments0 = 1;
        this.increment = 1;
        this.length = this.end.copy().sub(this.start).length();
        this.particleMaxAge = 3 + this.rand.nextInt(3) - 1;
        this.multiplier = 1.0f;
        this.particleAge = -((int) (this.length * 3.0f));
        this.segments.add(new Segment(new Point(this.start, new V(0, 0, 0)), new Point(this.end, new V(0, 0, 0)),
                1.0f, 0, 0));
        this.particleMaxAge = duration + this.rand.nextInt(duration) - duration / 2;
        this.multiplier = multi;
        this.increment = speed;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void fractal(int splits, float amount, float splitChance, float splitLength, float splitAngle) {
        if (this.finalized) return;
        List<Segment> old = this.segments;
        this.segments = new ArrayList<>();
        Segment prev;
        for (Segment segment : old) {
            prev = segment.prev;
            V sub = segment.diff.copy().scale(1.0f / splits);
            Point[] points = new Point[splits + 1];
            V startPoint = segment.start.point;
            points[0] = segment.start;
            points[splits] = segment.end;
            for (int i = 1; i < splits; i++) {
                V off = V.perpendicular(segment.diff).rotate(this.rand.nextFloat() * 360.0f, segment.diff);
                off.scale((this.rand.nextFloat() - 0.5f) * amount);
                V base = startPoint.copy().add(sub.copy().scale(i));
                points[i] = new Point(base, off);
            }
            for (int i = 0; i < splits; i++) {
                Segment next = new Segment(points[i], points[i + 1], segment.light, segment.segmentNo * splits + i,
                        segment.splitNo);
                next.prev = prev;
                if (prev != null) prev.next = next;
                if (i != 0 && this.rand.nextFloat() < splitChance) {
                    V splitRot = V.xCross(next.diff).rotate(this.rand.nextFloat() * 360.0f, next.diff);
                    V diff = next.diff.copy().rotate((this.rand.nextFloat() * 0.66f + 0.33f) * splitAngle, splitRot)
                            .scale(splitLength);
                    this.numSplits++;
                    this.splitParents.put(this.numSplits, next.splitNo);
                    Segment split = new Segment(points[i],
                            new Point(points[i + 1].base, points[i + 1].offset.copy().add(diff)),
                            segment.light / 2.0f, next.segmentNo, this.numSplits);
                    split.prev = prev;
                    this.segments.add(split);
                }
                prev = next;
                this.segments.add(next);
            }
            if (segment.next != null) segment.next.prev = prev;
        }
        this.numSegments0 *= splits;
    }

    public void defaultFractal() {
        this.fractal(2, this.length * this.multiplier / 8.0f, 0.7f, 0.1f, 45.0f);
        this.fractal(2, this.length * this.multiplier / 12.0f, 0.5f, 0.1f, 50.0f);
        this.fractal(2, this.length * this.multiplier / 17.0f, 0.5f, 0.1f, 55.0f);
        this.fractal(2, this.length * this.multiplier / 23.0f, 0.5f, 0.1f, 60.0f);
        this.fractal(2, this.length * this.multiplier / 30.0f, 0.0f, 0.0f, 0.0f);
        this.fractal(2, this.length * this.multiplier / 34.0f, 0.0f, 0.0f, 0.0f);
        this.fractal(2, this.length * this.multiplier / 40.0f, 0.0f, 0.0f, 0.0f);
    }

    private void calculateCollisionAndDiffs() {
        Map<Integer, Integer> lastActive = new HashMap<>();
        this.segments.sort((a, b) -> {
            int comp = Integer.compare(a.splitNo, b.splitNo);
            return comp == 0 ? Integer.compare(a.segmentNo, b.segmentNo) : comp;
        });
        int lastSplit = 0;
        int lastSeg = 0;
        for (Segment segment : this.segments) {
            if (segment.splitNo > lastSplit) {
                lastActive.put(lastSplit, lastSeg);
                lastSplit = segment.splitNo;
                lastSeg = lastActive.get(this.splitParents.get(segment.splitNo));
            }
            lastSeg = segment.segmentNo;
        }
        lastActive.put(lastSplit, lastSeg);
        lastSplit = 0;
        lastSeg = lastActive.get(0);
        Iterator<Segment> iterator = this.segments.iterator();
        while (iterator.hasNext()) {
            Segment segment = iterator.next();
            if (lastSplit != segment.splitNo) {
                lastSplit = segment.splitNo;
                lastSeg = lastActive.get(segment.splitNo);
            }
            if (segment.segmentNo > lastSeg) iterator.remove();
            segment.calcEndDiffs();
        }
    }

    /** Fecha o raio e o põe na lista do que se desenha. */
    public void finalizeBolt() {
        if (this.finalized) return;
        this.finalized = true;
        this.calculateCollisionAndDiffs();
        Collections.sort(this.segments, (a, b) -> Float.compare(b.light, a.light));
        ThaumFx.add(this);
    }

    @Override
    public boolean tick() {
        this.particleAge += this.increment;
        if (this.particleAge > this.particleMaxAge) this.particleAge = this.particleMaxAge;
        return this.particleAge < this.particleMaxAge;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        if (view.distanceTo(this.start.x, this.start.y, this.start.z) > 100.0) return;
        // as cores de cada tipo, primeiro a da passada larga e depois a da fina
        float[] wide = switch (this.type) {
            case 1 -> new float[]{0.6f, 0.6f, 0.1f};
            case 2 -> new float[]{0.1f, 0.1f, 0.6f};
            case 3 -> new float[]{0.1f, 1.0f, 0.1f};
            case 4 -> new float[]{0.6f, 0.1f, 0.1f};
            default -> new float[]{0.6f, 0.3f, 0.6f};
        };
        float[] thin = switch (this.type) {
            case 1 -> new float[]{1.0f, 1.0f, 0.1f};
            case 2 -> new float[]{0.1f, 0.1f, 1.0f};
            case 3 -> new float[]{0.1f, 0.6f, 0.1f};
            case 4 -> new float[]{1.0f, 0.1f, 0.1f};
            default -> new float[]{1.0f, 0.6f, 1.0f};
        };
        collector.submitCustomGeometry(pose, AdditiveGlow.of(LARGE),
                (matrix, consumer) -> this.renderBolt(matrix, consumer, view, partial, 0, wide));
        collector.submitCustomGeometry(pose, AdditiveGlow.of(SMALL),
                (matrix, consumer) -> this.renderBolt(matrix, consumer, view, partial, 1, thin));
    }

    private void renderBolt(PoseStack.Pose matrix, VertexConsumer consumer, ThaumFx.View view, float partial,
                            int pass, float[] rgb) {
        V playerVec = new V(view.sinYaw() * -view.cosPitch(), -view.cosSinPitch() / view.cosYaw(), view.cosYaw() * view.cosPitch());
        float age = this.particleAge >= 0 ? (float) this.particleAge / this.particleMaxAge : 0.0f;
        float mainAlpha = pass == 0 ? (1.0f - age) * 0.4f : 1.0f - age * 0.5f;
        int steps = (int) (this.length * 3.0f);
        int renderLength = steps == 0 ? Integer.MAX_VALUE
                : (int) ((this.particleAge + partial + steps) / steps * this.numSegments0);
        Vec3 cam = view.camera();

        for (Segment s : this.segments) {
            if (s.segmentNo > renderLength) continue;
            V rel = new V(view.playerX() - s.start.point.x, view.playerY() - s.start.point.y, view.playerZ() - s.start.point.z);
            float w = this.width * (rel.length() / 5.0f + 1.0f) * (1.0f + s.light) * 0.5f;
            V d1 = V.cross(playerVec, s.prevDiff).scale(w / s.sinPrev);
            V d2 = V.cross(playerVec, s.nextDiff).scale(w / s.sinNext);
            V a = s.start.point, b = s.end.point;
            float x1 = (float) (a.x - cam.x), y1 = (float) (a.y - cam.y), z1 = (float) (a.z - cam.z);
            float x2 = (float) (b.x - cam.x), y2 = (float) (b.y - cam.y), z2 = (float) (b.z - cam.z);
            int colour = argb(mainAlpha * s.light, rgb);
            quad(matrix, consumer, colour,
                    x2 - d2.x, y2 - d2.y, z2 - d2.z, 0.5f, 0.0f,
                    x1 - d1.x, y1 - d1.y, z1 - d1.z, 0.5f, 0.0f,
                    x1 + d1.x, y1 + d1.y, z1 + d1.z, 0.5f, 1.0f,
                    x2 + d2.x, y2 + d2.y, z2 + d2.z, 0.5f, 1.0f);
            if (s.next == null) {
                V round = b.copy().add(s.diff.copy().normalize().scale(w));
                float x3 = (float) (round.x - cam.x), y3 = (float) (round.y - cam.y), z3 = (float) (round.z - cam.z);
                quad(matrix, consumer, colour,
                        x3 - d2.x, y3 - d2.y, z3 - d2.z, 0.0f, 0.0f,
                        x2 - d2.x, y2 - d2.y, z2 - d2.z, 0.5f, 0.0f,
                        x2 + d2.x, y2 + d2.y, z2 + d2.z, 0.5f, 1.0f,
                        x3 + d2.x, y3 + d2.y, z3 + d2.z, 0.0f, 1.0f);
            }
            if (s.prev == null) {
                V round = a.copy().sub(s.diff.copy().normalize().scale(w));
                float x3 = (float) (round.x - cam.x), y3 = (float) (round.y - cam.y), z3 = (float) (round.z - cam.z);
                quad(matrix, consumer, colour,
                        x1 - d1.x, y1 - d1.y, z1 - d1.z, 0.5f, 0.0f,
                        x3 - d1.x, y3 - d1.y, z3 - d1.z, 0.0f, 0.0f,
                        x3 + d1.x, y3 + d1.y, z3 + d1.z, 0.0f, 1.0f,
                        x1 + d1.x, y1 + d1.y, z1 + d1.z, 0.5f, 1.0f);
            }
        }
    }

    private static int argb(float alpha, float[] rgb) {
        int a = Math.max(0, Math.min(255, (int) (alpha * 255.0f)));
        return a << 24 | (int) (rgb[0] * 255.0f) << 16 | (int) (rgb[1] * 255.0f) << 8 | (int) (rgb[2] * 255.0f);
    }

    /** Um quadrilátero dos dois lados: o raio é fita, e o original desenha sem esconder as costas. */
    static void quad(PoseStack.Pose m, VertexConsumer c, int colour,
                     float ax, float ay, float az, float au, float av,
                     float bx, float by, float bz, float bu, float bv,
                     float cx, float cy, float cz, float cu, float cv,
                     float dx, float dy, float dz, float du, float dv) {
        vertex(m, c, colour, ax, ay, az, au, av);
        vertex(m, c, colour, bx, by, bz, bu, bv);
        vertex(m, c, colour, cx, cy, cz, cu, cv);
        vertex(m, c, colour, dx, dy, dz, du, dv);
        vertex(m, c, colour, dx, dy, dz, du, dv);
        vertex(m, c, colour, cx, cy, cz, cu, cv);
        vertex(m, c, colour, bx, by, bz, bu, bv);
        vertex(m, c, colour, ax, ay, az, au, av);
    }

    static void vertex(PoseStack.Pose m, VertexConsumer c, int colour, float x, float y, float z, float u, float v) {
        c.addVertex(m, x, y, z).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0).setNormal(m, 0.0f, 1.0f, 0.0f);
    }

    // ----------------------------------------------------------------- as peças do raio

    private static final class Point {
        final V point;
        final V base;
        final V offset;

        Point(V base, V offset) {
            this.point = base.copy().add(offset);
            this.base = base;
            this.offset = offset;
        }
    }

    private static final class Segment {
        final Point start;
        final Point end;
        V diff;
        Segment prev;
        Segment next;
        V nextDiff;
        V prevDiff;
        float sinPrev;
        float sinNext;
        final float light;
        final int segmentNo;
        final int splitNo;

        Segment(Point start, Point end, float light, int segmentNo, int splitNo) {
            this.start = start;
            this.end = end;
            this.light = light;
            this.segmentNo = segmentNo;
            this.splitNo = splitNo;
            this.diff = end.point.copy().sub(start.point);
        }

        void calcEndDiffs() {
            if (this.prev != null) {
                V prevNorm = this.prev.diff.copy().normalize();
                V thisNorm = this.diff.copy().normalize();
                this.prevDiff = thisNorm.add(prevNorm).normalize();
                this.sinPrev = (float) Math.sin(V.anglePreNorm(thisNorm, prevNorm.scale(-1.0f)) / 2.0f);
            } else {
                this.prevDiff = this.diff.copy().normalize();
                this.sinPrev = 1.0f;
            }
            if (this.next != null) {
                V nextNorm = this.next.diff.copy().normalize();
                V thisNorm = this.diff.copy().normalize();
                this.nextDiff = thisNorm.add(nextNorm).normalize();
                this.sinNext = (float) Math.sin(V.anglePreNorm(thisNorm, nextNorm.scale(-1.0f)) / 2.0f);
            } else {
                this.nextDiff = this.diff.copy().normalize();
                this.sinNext = 1.0f;
            }
        }
    }

    /** O {@code WRVector3} do original: um vetor de floats que muda no lugar. */
    static final class V {
        float x, y, z;

        V(double x, double y, double z) {
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
        }

        V add(V v) {
            this.x += v.x;
            this.y += v.y;
            this.z += v.z;
            return this;
        }

        V sub(V v) {
            this.x -= v.x;
            this.y -= v.y;
            this.z -= v.z;
            return this;
        }

        V scale(float s) {
            this.x *= s;
            this.y *= s;
            this.z *= s;
            return this;
        }

        V normalize() {
            float l = this.length();
            this.x /= l;
            this.y /= l;
            this.z /= l;
            return this;
        }

        float length() {
            return (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        }

        V copy() {
            return new V(this.x, this.y, this.z);
        }

        static V cross(V a, V b) {
            return new V(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
        }

        static V xCross(V v) {
            return new V(0.0, v.z, -v.y);
        }

        static V zCross(V v) {
            return new V(-v.y, v.x, 0.0);
        }

        static V perpendicular(V v) {
            return v.z == 0.0f ? zCross(v) : xCross(v);
        }

        static float anglePreNorm(V a, V b) {
            return (float) Math.acos(a.x * b.x + a.y * b.y + a.z * b.z);
        }

        /** O {@code WRMat4.rotationMat(angle, axis).translate(this)} do original, em graus. */
        V rotate(float angle, V axis) {
            V n = axis.copy().normalize();
            float ax = n.x, ay = n.y, az = n.z;
            double rad = angle * 0.0174532925;
            float cos = (float) Math.cos(rad), ocos = 1.0f - cos, sin = (float) Math.sin(rad);
            float m0 = ax * ax * ocos + cos, m1 = ay * ax * ocos + az * sin, m2 = ax * az * ocos - ay * sin;
            float m4 = ax * ay * ocos - az * sin, m5 = ay * ay * ocos + cos, m6 = ay * az * ocos + ax * sin;
            float m8 = ax * az * ocos + ay * sin, m9 = ay * az * ocos - ax * sin, m10 = az * az * ocos + cos;
            float nx = this.x * m0 + this.y * m1 + this.z * m2;
            float ny = this.x * m4 + this.y * m5 + this.z * m6;
            float nz = this.x * m8 + this.y * m9 + this.z * m10;
            this.x = nx;
            this.y = ny;
            this.z = nz;
            return this;
        }
    }
}
