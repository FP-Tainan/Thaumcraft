package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.RandomSource;
import net.thaumcraft.client.render.AdditiveGlow;

/**
 * A bolha do {@code FXBubble} da 4.2.3.5: sobe devagar, balançando, e estoura nos últimos dois tiques (os quadros 16 a
 * 18 da {@code particles.png}).
 */
public final class Bubble implements ThaumFx.Effect {
    private double x, y, z, prevX, prevY, prevZ, mx, my, mz;
    private final float red, green, blue, alpha;
    private final float scale;
    private final double speed = 0.002;
    private int maxAge;
    private int particle = 16;
    private final RandomSource random;

    private Bubble(double x, double y, double z, float red, float green, float blue, float alpha, int age, RandomSource random) {
        this.x = this.prevX = x;
        this.y = this.prevY = y;
        this.z = this.prevZ = z;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.random = random;
        // a escala sorteada do EntityFX, vezes a da bolha
        this.scale = (random.nextFloat() * 0.5f + 0.5f) * 2.0f * (random.nextFloat() * 0.3f + 0.2f);
        this.mx = (Math.random() * 2.0 - 1.0) * 0.02f;
        this.my = Math.random() * 0.02f;
        this.mz = (Math.random() * 2.0 - 1.0) * 0.02f;
        this.maxAge = (int) (age + 2 + 8.0 / (Math.random() * 0.8 + 0.2));
        var viewer = Minecraft.getInstance().getCameraEntity();
        if (viewer != null && viewer.distanceToSqr(x, y, z) > 50.0 * 50.0) this.maxAge = 0;
    }

    public static void spawn(double x, double y, double z, float red, float green, float blue, float alpha, int age, RandomSource random) {
        ThaumFx.add(new Bubble(x, y, z, red, green, blue, alpha, age, random));
    }

    @Override
    public boolean tick() {
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.my += this.speed;
        this.mx += (this.random.nextFloat() - this.random.nextFloat()) * 0.01f;
        this.mz += (this.random.nextFloat() - this.random.nextFloat()) * 0.01f;
        this.x += this.mx;
        this.y += this.my;
        this.z += this.mz;
        this.mx *= 0.85f;
        this.my *= 0.85f;
        this.mz *= 0.85f;
        if (this.maxAge-- <= 0) return false;
        if (this.maxAge <= 2) this.particle++;
        return true;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        float u0 = this.particle % 16 / 16.0f, u1 = u0 + 0.0624375f;
        float v0 = this.particle / 16 / 16.0f, v1 = v0 + 0.0624375f;
        float s = 0.1f * this.scale;
        float px = (float) (this.prevX + (this.x - this.prevX) * partial - view.camera().x);
        float py = (float) (this.prevY + (this.y - this.prevY) * partial - view.camera().y);
        float pz = (float) (this.prevZ + (this.z - this.prevZ) * partial - view.camera().z);
        int colour = (int) (this.alpha * 255) << 24 | (int) (this.red * 255) << 16 | (int) (this.green * 255) << 8 | (int) (this.blue * 255);
        collector.submitCustomGeometry(pose, AdditiveGlow.blended(Sparkle.PARTICLES),
                (m, c) -> Sparkle.billboard(m, c, view, px, py, pz, s, u0, u1, v0, v1, colour));
    }
}
