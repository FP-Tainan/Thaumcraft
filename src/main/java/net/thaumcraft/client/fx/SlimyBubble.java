package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.thaumcraft.client.render.AdditiveGlow;

/**
 * A bolha: o {@code FXSlimyBubble} da 4.2.3.5. Quadros 144 a 150 da {@code particles.png}: nasce, estoura para cima
 * no quinto tique, sobe devagar tremendo entre dois quadros e murcha no fim. Mistura comum (a camada 1 do original).
 */
public final class SlimyBubble implements ThaumFx.Effect {
    private double x, y, z, prevX, prevY, prevZ;
    private double motionY;
    private final float size;
    private final int colour;
    private final int maxAge;
    private int age;
    private int particle = 144;

    private SlimyBubble(double x, double y, double z, float size, int colour, RandomSource random) {
        this.x = this.prevX = x;
        this.y = this.prevY = y;
        this.z = this.prevZ = z;
        this.size = size;
        this.colour = colour;
        this.maxAge = 15 + random.nextInt(5);
    }

    /** @param argb a cor, com o alfa */
    public static void spawn(double x, double y, double z, float size, int argb, RandomSource random) {
        ThaumFx.add(new SlimyBubble(x, y, z, size, argb, random));
    }

    /** A cor do original para as bolhas da fornalha avançada: um roxo que varia um pouco. */
    public static int purple(RandomSource random) {
        float r = 0.6f - random.nextFloat() * 0.2f, b = 0.6f + random.nextFloat() * 0.2f;
        return (int) (0.8f * 255) << 24 | (int) (r * 255) << 16 | Mth.clamp((int) (b * 255), 0, 255);
    }

    @Override
    public boolean tick() {
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        if (this.age++ >= this.maxAge) return false;
        if (this.age - 1 < 6) {
            this.particle = 144 + this.age / 2;
            if (this.age == 5) this.y += 0.1;
        } else if (this.age < this.maxAge - 4) {
            this.motionY += 0.005;
            this.particle = 147 + this.age % 4 / 2;
        } else {
            this.motionY /= 2.0;
            this.particle = 150 - (this.maxAge - this.age) / 2;
        }
        this.y += this.motionY;
        return true;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        float u0 = this.particle % 16 / 16.0f, u1 = u0 + 0.0625f;
        float v0 = this.particle / 16 / 16.0f, v1 = v0 + 0.0625f;
        float px = (float) (this.prevX + (this.x - this.prevX) * partial - view.camera().x);
        float py = (float) (this.prevY + (this.y - this.prevY) * partial - view.camera().y);
        float pz = (float) (this.prevZ + (this.z - this.prevZ) * partial - view.camera().z);
        collector.submitCustomGeometry(pose, AdditiveGlow.blended(Sparkle.PARTICLES),
                (matrix, consumer) -> Sparkle.billboard(matrix, consumer, view, px, py, pz, this.size, u0, u1, v0, v1, this.colour));
    }
}
