package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.thaumcraft.client.render.AdditiveGlow;

/**
 * As runas que acendem na face de um bloco: o {@code FXBlockRunes} da 4.2.3.5, descompilado.
 *
 * <p>Uma das dezesseis runas da sétima linha da folha de partículas, colada numa das quatro faces de lado do
 * bloco, um pouco para cima ou para baixo, que acende rápido e apaga devagar. É o que a pedra de proteção mostra.
 */
public final class BlockRunes implements ThaumFx.Effect {
    private double x, y, z, prevY;
    private final float red, green, blue;
    private final float rotation;
    private final int rune;
    private final double ofx, ofy;
    private final float scale;
    private final int maxAge;
    private float gravity;
    private double motionY;
    private float alpha;
    private int age;

    private BlockRunes(RandomSource random, double x, double y, double z, float r, float g, float b, int m) {
        this.x = x;
        this.y = this.prevY = y;
        this.z = z;
        this.red = r == 0.0f ? 1.0f : r;
        this.green = g;
        this.blue = b;
        this.rotation = random.nextInt(4) * 90;
        this.maxAge = 3 * m;
        this.rune = (int) (Math.random() * 16.0 + 224.0);
        this.ofx = random.nextFloat() * 0.2;
        this.ofy = -0.3 + random.nextFloat() * 0.6;
        this.scale = (float) (1.0 + random.nextGaussian() * 0.1f);
    }

    /** O {@code blockRunes} do {@code ClientProxy}: no meio do bloco, com a cor e a vida dadas. */
    public static void spawn(double x, double y, double z, float r, float g, float b, int duration, float gravity) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        BlockRunes runes = new BlockRunes(level.getRandom(), x + 0.5, y + 0.5, z + 0.5, r, g, b, duration);
        runes.gravity = gravity;
        ThaumFx.add(runes);
    }

    @Override
    public boolean tick() {
        this.prevY = this.y;
        float threshold = this.maxAge / 5.0f;
        this.alpha = this.age <= threshold ? this.age / threshold : (float) (this.maxAge - this.age) / this.maxAge;
        if (this.age++ >= this.maxAge) return false;
        this.motionY -= 0.04 * this.gravity;
        this.y += this.motionY;
        return true;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        float u0 = this.rune % 16 / 16.0f, u1 = u0 + 0.0624375f;
        float v0 = 0.375f, v1 = v0 + 0.0624375f;
        float h = 0.3f * this.scale * 0.5f;
        int colour = (int) (Mth.clamp(this.alpha / 2.0f, 0.0f, 1.0f) * 255) << 24
                | (int) (Mth.clamp(this.red, 0, 1) * 255) << 16 | (int) (Mth.clamp(this.green, 0, 1) * 255) << 8
                | (int) (Mth.clamp(this.blue, 0, 1) * 255);
        pose.pushPose();
        pose.translate(this.x - view.camera().x, this.prevY + (this.y - this.prevY) * partial - view.camera().y,
                this.z - view.camera().z);
        pose.mulPose(Axis.YP.rotationDegrees(this.rotation));
        pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
        pose.translate(this.ofx, this.ofy, -0.51);
        collector.submitCustomGeometry(pose, AdditiveGlow.of(Sparkle.PARTICLES), (m, c) -> {
            float[][] k = {{-h, h, u1, v1}, {h, h, u1, v0}, {h, -h, u0, v0}, {-h, -h, u0, v1}};
            for (int i = 0; i < 4; i++) vertex(m, c, k[i], colour);
            for (int i = 3; i >= 0; i--) vertex(m, c, k[i], colour);
        });
        pose.popPose();
    }

    private static void vertex(PoseStack.Pose m, com.mojang.blaze3d.vertex.VertexConsumer c, float[] k, int colour) {
        c.addVertex(m, k[0], k[1], 0.0f).setColor(colour).setUv(k[2], k[3]).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0).setNormal(m, 0.0f, 0.0f, 1.0f);
    }
}
