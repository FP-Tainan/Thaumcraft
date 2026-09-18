package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.thaumcraft.client.render.AdditiveGlow;

/**
 * O fogo-fátuo: o {@code FXWisp} da 4.2.3.5, descompilado.
 *
 * <p>Uma bolota de luz — o brilho branco grande do canto de baixo da folha de partículas — tingida, que cresce
 * até a metade da vida e encolhe depois (ou só encolhe). Pode seguir uma criatura, puxada para o meio dela, ou
 * andar em linha reta até um ponto, chegando lá no fim da vida.
 */
public final class Wisp implements ThaumFx.Effect {
    private double x, y, z, prevX, prevY, prevZ;
    private double motionX, motionY, motionZ;
    private float red, green, blue;
    private final float baseScale;
    private final int maxAge;
    private final int halfLife;
    private float gravity;
    private boolean shrink;
    private Entity target;
    private int age;

    private Wisp(RandomSource random, double x, double y, double z, float size, int type) {
        this.x = this.prevX = x;
        this.y = this.prevY = y;
        this.z = this.prevZ = z;
        this.baseScale = (random.nextFloat() * 0.5f + 0.5f) * 2.0f * size;
        int life = (int) (36.0 / (Math.random() * 0.3 + 0.7));
        var camera = Minecraft.getInstance().getCameraEntity();
        if (camera != null && camera.distanceToSqr(x, y, z) > 50 * 50) life = 0;
        this.maxAge = life;
        this.halfLife = Math.max(1, life / 2);
        float r = 1.0f, g = 0.0f, b = 0.0f;
        switch (type) {
            case 0 -> {
                r = 0.75f + random.nextFloat() * 0.25f;
                g = 0.25f + random.nextFloat() * 0.25f;
                b = 0.75f + random.nextFloat() * 0.25f;
            }
            case 1 -> {
                r = 0.5f + random.nextFloat() * 0.3f;
                g = 0.5f + random.nextFloat() * 0.3f;
                b = 0.2f;
            }
            case 2 -> {
                r = 0.2f;
                g = 0.2f;
                b = 0.7f + random.nextFloat() * 0.3f;
            }
            case 3 -> {
                r = 0.2f;
                g = 0.7f + random.nextFloat() * 0.3f;
                b = 0.2f;
            }
            case 4 -> {
                r = 0.7f + random.nextFloat() * 0.3f;
                g = 0.2f;
                b = 0.2f;
            }
            case 5 -> {
                r = random.nextFloat() * 0.1f;
                g = random.nextFloat() * 0.1f;
                b = random.nextFloat() * 0.1f;
            }
            case 6 -> {
                r = 0.8f + random.nextFloat() * 0.2f;
                g = 0.8f + random.nextFloat() * 0.2f;
                b = 0.8f + random.nextFloat() * 0.2f;
            }
            case 7 -> {
                r = 0.7f + random.nextFloat() * 0.3f;
                g = 0.5f + random.nextFloat() * 0.2f;
                b = 0.3f + random.nextFloat() * 0.1f;
            }
            default -> {
            }
        }
        this.red = r;
        this.green = g;
        this.blue = b;
    }

    private static RandomSource random() {
        return Minecraft.getInstance().level.getRandom();
    }

    /** O {@code wispFX2}: parado, com gravidade. */
    public static void fx2(double x, double y, double z, float size, int type, boolean shrink, float gravity) {
        Wisp wisp = new Wisp(random(), x, y, z, size, type);
        wisp.gravity = gravity;
        wisp.shrink = shrink;
        ThaumFx.add(wisp);
    }

    /** O {@code wispFX3}: anda até o ponto dado, chegando no fim da vida. */
    public static void fx3(double x, double y, double z, double tx, double ty, double tz, float size, int type,
                           boolean shrink, float gravity) {
        Wisp wisp = new Wisp(random(), x, y, z, size, type);
        if (wisp.maxAge > 0) {
            wisp.motionX = (tx - x) / wisp.maxAge;
            wisp.motionY = (ty - y) / wisp.maxAge;
            wisp.motionZ = (tz - z) / wisp.maxAge;
        }
        wisp.gravity = gravity;
        wisp.shrink = shrink;
        ThaumFx.add(wisp);
    }

    /** O {@code wispFX4}: nasce perto de uma criatura e é puxado para o meio dela. */
    public static void fx4(double x, double y, double z, Entity target, int type, boolean shrink, float gravity) {
        Wisp wisp = new Wisp(random(), x, y, z, 0.4f, type);
        wisp.target = target;
        wisp.gravity = gravity;
        wisp.shrink = shrink;
        ThaumFx.add(wisp);
    }

    @Override
    public boolean tick() {
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        if (this.age++ >= this.maxAge) return false;
        this.motionY -= 0.04 * this.gravity;
        this.x += this.motionX;
        this.y += this.motionY;
        this.z += this.motionZ;
        if (this.target != null) {
            this.motionX *= 0.985;
            this.motionY *= 0.985;
            this.motionZ *= 0.985;
            double dx = this.target.getX() - this.x;
            double dy = this.target.getY() + this.target.getBbHeight() / 2.0f - this.y;
            double dz = this.target.getZ() - this.z;
            double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (d > 0) {
                this.motionX = Mth.clamp(this.motionX + dx / d * 0.2, -0.2, 0.2);
                this.motionY = Mth.clamp(this.motionY + dy / d * 0.2, -0.2, 0.2);
                this.motionZ = Mth.clamp(this.motionZ + dz / d * 0.2, -0.2, 0.2);
            }
        } else {
            this.motionX *= 0.98;
            this.motionY *= 0.98;
            this.motionZ *= 0.98;
        }
        return true;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        float agescale;
        if (this.shrink) {
            agescale = (float) (this.maxAge - this.age) / this.maxAge;
        } else {
            agescale = (float) this.age / this.halfLife;
            if (agescale > 1.0f) agescale = 2.0f - agescale;
        }
        float size = 0.5f * this.baseScale * agescale;
        if (size <= 0) return;
        float px = (float) (this.prevX + (this.x - this.prevX) * partial - view.camera().x);
        float py = (float) (this.prevY + (this.y - this.prevY) * partial - view.camera().y);
        float pz = (float) (this.prevZ + (this.z - this.prevZ) * partial - view.camera().z);
        int colour = 0x80000000 | (int) (Mth.clamp(this.red, 0, 1) * 255) << 16
                | (int) (Mth.clamp(this.green, 0, 1) * 255) << 8 | (int) (Mth.clamp(this.blue, 0, 1) * 255);
        collector.submitCustomGeometry(pose, AdditiveGlow.of(Sparkle.PARTICLES),
                (matrix, consumer) -> Sparkle.billboard(matrix, consumer, view, px, py, pz, size,
                        0.0f, 0.125f, 0.875f, 1.0f, colour));
    }
}
