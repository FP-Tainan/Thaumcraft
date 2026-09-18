package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.thaumcraft.client.render.AdditiveGlow;

/**
 * A partícula de uso geral do Thaumcraft: o {@code FXGeneric} da 4.2.3.5, descompilado.
 *
 * <p>Um quadrado virado para quem vê, com um dos quadros da folha de partículas do mod — que pode andar de um
 * quadro ao seguinte ao longo da vida, ou em laço —, somando luz, com a metade do brilho no primeiro e no
 * último tique. Pode esperar um tanto antes de aparecer.
 */
public final class GenericFx implements ThaumFx.Effect {
    private double x, y, z, prevX, prevY, prevZ;
    private double motionX, motionY, motionZ;
    private final float red, green, blue, alpha;
    private final float scale;
    private final int maxAge;
    private final int delay;
    private final int start, count, increment;
    private final boolean loop;
    private int frame;
    private int age;

    public GenericFx(double x, double y, double z, double mx, double my, double mz, float red, float green,
                     float blue, float alpha, boolean loop, int start, int count, int increment, int age, int delay,
                     float scale) {
        this.x = this.prevX = x;
        this.y = this.prevY = y;
        this.z = this.prevZ = z;
        this.motionX = mx;
        this.motionY = my;
        this.motionZ = mz;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.loop = loop;
        this.start = start;
        this.count = count;
        this.increment = increment;
        this.maxAge = age + delay;
        this.delay = delay;
        this.scale = scale;
        this.frame = start;
    }

    /**
     * O {@code blockSparkle} do {@code ClientProxy}: estrelinhas em volta de um bloco, na cor dada (variando um
     * pouco), com os quadros 112 a 120 da folha.
     */
    public static void blockSparkle(double bx, double by, double bz, int colour, int count) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        RandomSource random = level.getRandom();
        float r = (colour >> 16 & 255) / 255.0f, g = (colour >> 8 & 255) / 255.0f, b = (colour & 255) / 255.0f;
        // o particleCount do original, com as partículas no máximo
        for (int a = 0; a < count * 2; a++) {
            ThaumFx.add(new GenericFx(bx - 0.1f + random.nextFloat() * 1.2f, by - 0.1f + random.nextFloat() * 1.2f,
                    bz - 0.1f + random.nextFloat() * 1.2f, 0.0, random.nextFloat() * 0.02, 0.0,
                    r - 0.2f + random.nextFloat() * 0.4f, g - 0.2f + random.nextFloat() * 0.4f,
                    b - 0.2f + random.nextFloat() * 0.4f, 0.9f, false, 112, 9, 1, 5 + random.nextInt(8),
                    random.nextInt(10), 0.7f + random.nextFloat() * 0.4f));
        }
    }

    @Override
    public boolean tick() {
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        if (this.age++ >= this.maxAge) return false;
        this.x += this.motionX;
        this.y += this.motionY;
        this.z += this.motionZ;
        this.motionX *= 0.98;
        this.motionY *= 0.98;
        this.motionZ *= 0.98;
        if (this.loop) {
            this.frame = this.start + this.age / this.increment % this.count;
        } else {
            float fs = (float) this.age / this.maxAge;
            this.frame = (int) (this.start + Math.min(this.count * fs, this.count - 1));
        }
        return true;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        if (this.age < this.delay) return;
        float a = this.alpha;
        if (this.age <= 1 || this.age >= this.maxAge - 1) a /= 2.0f;
        float u0 = (this.frame % 16) / 16.0f, u1 = u0 + 0.0624375f;
        float v0 = (this.frame / 16) / 16.0f, v1 = v0 + 0.0624375f;
        float size = 0.1f * this.scale;
        float px = (float) (this.prevX + (this.x - this.prevX) * partial - view.camera().x);
        float py = (float) (this.prevY + (this.y - this.prevY) * partial - view.camera().y);
        float pz = (float) (this.prevZ + (this.z - this.prevZ) * partial - view.camera().z);
        int colour = Mth.clamp((int) (a * 255), 0, 255) << 24 | (int) (Mth.clamp(this.red, 0, 1) * 255) << 16
                | (int) (Mth.clamp(this.green, 0, 1) * 255) << 8 | (int) (Mth.clamp(this.blue, 0, 1) * 255);
        collector.submitCustomGeometry(pose, AdditiveGlow.of(Sparkle.PARTICLES),
                (matrix, consumer) -> Sparkle.billboard(matrix, consumer, view, px, py, pz, size, u0, u1, v0, v1, colour));
    }
}
