package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.thaumcraft.client.render.AdditiveGlow;

/**
 * O {@code FXSmokeSpiral} da 4.2.3.5: a fumaça que dá duas voltas em espiral em volta de um ponto, descendo da cabeça aos
 * pés (sem passar do chão, {@code miny}), com os quadros 1 a 5 da fumaça da {@code particles.png}, sumindo aos poucos.
 */
public final class SmokeSpiral implements ThaumFx.Effect {
    private final double x, y, z;
    private final float radius, r, g, b, scale;
    private final int start, miny, maxAge;
    private int age;

    private SmokeSpiral(double x, double y, double z, float radius, int start, int miny, int colour, RandomSource random) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.start = start;
        this.miny = miny;
        this.maxAge = 20 + random.nextInt(10);
        this.scale = (random.nextFloat() * 0.5f + 0.5f) * 2.0f;
        this.r = (colour >> 16 & 255) / 255.0f;
        this.g = (colour >> 8 & 255) / 255.0f;
        this.b = (colour & 255) / 255.0f;
    }

    /** O {@code smokeSpiral} do {@code ClientProxy}. */
    public static void spawn(double x, double y, double z, float radius, int start, int miny, int colour, RandomSource random) {
        ThaumFx.add(new SmokeSpiral(x, y, z, radius, start, miny, colour, random));
    }

    @Override
    public boolean tick() {
        return this.age++ < this.maxAge;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        float alpha = (float) (this.maxAge - this.age) / this.maxAge;
        int particle = (int) (1.0f + (float) this.age / this.maxAge * 4.0f);
        float t = (this.age + partial) / this.maxAge;
        float r1 = this.start + 720.0f * t;
        float r2 = 90.0f - 180.0f * t;
        float mX = -Mth.sin(r1 / 180.0f * (float) Math.PI) * Mth.cos(r2 / 180.0f * (float) Math.PI) * this.radius;
        float mZ = Mth.cos(r1 / 180.0f * (float) Math.PI) * Mth.cos(r2 / 180.0f * (float) Math.PI) * this.radius;
        float mY = -Mth.sin(r2 / 180.0f * (float) Math.PI) * this.radius;
        float u0 = particle % 16 / 16.0f, u1 = u0 + 0.0624375f;
        float v0 = particle / 16 / 16.0f, v1 = v0 + 0.0624375f;
        float size = 0.15f * this.scale;
        float px = (float) (this.x + mX - view.camera().x);
        float py = (float) (Math.max(this.y + mY, this.miny + 0.1f) - view.camera().y);
        float pz = (float) (this.z + mZ - view.camera().z);
        int colour = Mth.clamp((int) (0.66f * alpha * 255.0f), 0, 255) << 24 | (int) (this.r * 255) << 16 | (int) (this.g * 255) << 8
                | (int) (this.b * 255);
        collector.submitCustomGeometry(pose, AdditiveGlow.blended(Sparkle.PARTICLES),
                (m, c) -> Sparkle.billboard(m, c, view, px, py, pz, size, u1, u0, v0, v1, colour));
    }
}
