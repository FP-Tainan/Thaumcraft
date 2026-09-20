package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.AdditiveGlow;

/**
 * A faísca: o {@code FXSparkle} da 4.2.3.5, descompilado.
 *
 * <p>Uma estrelinha de quatro quadros da linha cinco da folha de partículas do mod, que encolhe até sumir
 * em três vezes o seu multiplicador de tiques, somando luz. É o que pipoca onde o raio bate.
 */
public final class Sparkle implements ThaumFx.Effect {
    public static final Identifier PARTICLES = Thaumcraft.id("textures/misc/particles.png");

    private double x, y, z, prevX, prevY, prevZ;
    private double motionX, motionY, motionZ;
    private float red, green, blue;
    private final float scale;
    private final int maxAge;
    private final int multiplier;
    private float gravity;
    private int age;

    /**
     * O {@code sparkle(x, y, z, size, color, gravity)} do {@code ClientProxy}: sorteia se nasce, como o
     * original faz com a quantidade de partículas no máximo.
     */
    public static void spawn(RandomSource random, double x, double y, double z, float size, int colour, float gravity) {
        if (random.nextInt(6) >= 4) return;
        Sparkle sparkle = new Sparkle(random, x, y, z, size, colour, 6);
        sparkle.gravity = gravity;
        ThaumFx.add(sparkle);
    }

    /**
     * A faísca com tudo escolhido: o construtor do {@code FXSparkle} do original, com o empurrão e a gravidade
     * postos à mão. É por aqui que um mod de fora faz as faíscas dele.
     */
    public static void custom(RandomSource random, double x, double y, double z, float size, int type, int multiplier,
                              float gravity, double motionX, double motionY, double motionZ) {
        Sparkle sparkle = new Sparkle(random, x, y, z, size, type, multiplier);
        sparkle.gravity = gravity;
        sparkle.motionX = motionX;
        sparkle.motionY = motionY;
        sparkle.motionZ = motionZ;
        ThaumFx.add(sparkle);
    }

    /** A faísca com a cor dada ({@code setRBGColorF}), sem sorteio de nascer: a que fecha o arco de faíscas. */
    public static void coloured(RandomSource random, double x, double y, double z, float size, int m, float r, float g, float b) {
        ThaumFx.add(new Sparkle(random, x, y, z, size, -1, m, r, g, b));
    }

    private Sparkle(RandomSource random, double x, double y, double z, float size, int type, int m, float r, float g, float b) {
        this(random, x, y, z, size, type, m);
        this.red = r;
        this.green = g;
        this.blue = b;
    }

    private Sparkle(RandomSource random, double x, double y, double z, float size, int type, int m) {
        this.x = this.prevX = x;
        this.y = this.prevY = y;
        this.z = this.prevZ = z;
        // o EntityFX sorteia a escala entre um e dois; a faísca multiplica pelo tamanho pedido
        this.scale = (random.nextFloat() * 0.5f + 0.5f) * 2.0f * size;
        this.maxAge = 3 * m;
        this.multiplier = m;
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
            case 6 -> {
                r = 0.8f + random.nextFloat() * 0.2f;
                g = 0.8f + random.nextFloat() * 0.2f;
                b = 0.8f + random.nextFloat() * 0.2f;
            }
            case 7 -> {
                r = 0.2f;
                g = 0.5f + random.nextFloat() * 0.3f;
                b = 0.6f + random.nextFloat() * 0.3f;
            }
            default -> {
            }
        }
        this.red = r;
        this.green = g;
        this.blue = b;
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
        this.motionX *= 0.908;
        this.motionY *= 0.908;
        this.motionZ *= 0.908;
        return true;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        int part = 16 + this.age / this.multiplier;
        float u0 = part % 4 / 16.0f, u1 = u0 + 0.0624375f;
        float v0 = 0.25f, v1 = v0 + 0.0624375f;
        float size = 0.1f * this.scale * (float) (this.maxAge - this.age + 1) / this.maxAge;
        float px = (float) (this.prevX + (this.x - this.prevX) * partial - view.camera().x);
        float py = (float) (this.prevY + (this.y - this.prevY) * partial - view.camera().y);
        float pz = (float) (this.prevZ + (this.z - this.prevZ) * partial - view.camera().z);
        int colour = 0xFF000000 | (int) (this.red * 255) << 16 | (int) (this.green * 255) << 8 | (int) (this.blue * 255);
        collector.submitCustomGeometry(pose, AdditiveGlow.of(PARTICLES),
                (matrix, consumer) -> billboard(matrix, consumer, view, px, py, pz, size, u0, u1, v0, v1, colour));
    }

    /** O quadrado virado para quem vê, com os cantos do {@code renderParticle} do 1.7.10. */
    static void billboard(PoseStack.Pose m, com.mojang.blaze3d.vertex.VertexConsumer c, ThaumFx.View v,
                          float x, float y, float z, float s, float u0, float u1, float v0, float v1, int colour) {
        float f1 = v.cosYaw(), f2 = v.cosPitch(), f3 = v.sinYaw(), f4 = v.sinSinPitch(), f5 = v.cosSinPitch();
        LightningBolt.quad(m, c, colour,
                x - f1 * s - f4 * s, y - f2 * s, z - f3 * s - f5 * s, u1, v1,
                x - f1 * s + f4 * s, y + f2 * s, z - f3 * s + f5 * s, u1, v0,
                x + f1 * s + f4 * s, y + f2 * s, z + f3 * s + f5 * s, u0, v0,
                x + f1 * s - f4 * s, y - f2 * s, z + f3 * s - f5 * s, u0, v1);
    }
}
