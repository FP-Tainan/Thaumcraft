package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.AdditiveGlow;

/**
 * A faísca: o {@code FXSpark} da 4.2.3.5. Um dos três estouros da {@code particles2.png} (oito quadros cada, numa
 * folha de oito por oito), às vezes espelhado, parado, somando luz, de cinco a nove tiques. É a que sai dos
 * aglomerados de cristal.
 */
public final class Spark implements ThaumFx.Effect {
    private static final Identifier SHEET = Thaumcraft.id("textures/misc/particles2.png");

    private final Vec3 at;
    private final float size;
    private final int colour;
    private final int start;
    private final boolean flip;
    private final int maxAge;
    private int age;

    private Spark(Vec3 at, float size, int colour, RandomSource random) {
        this.at = at;
        this.size = size;
        this.colour = colour;
        this.maxAge = 5 + random.nextInt(5);
        this.start = random.nextInt(3) * 8;
        this.flip = random.nextBoolean();
    }

    /** @param argb a cor, com a força no alfa */
    public static void spawn(Vec3 at, float size, int argb, RandomSource random) {
        ThaumFx.add(new Spark(at, size, argb, random));
    }

    @Override
    public boolean tick() {
        return this.age++ < this.maxAge;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        int part = this.start + (int) ((float) this.age / this.maxAge * 7.0f);
        float u0 = part % 8 / 8.0f, u1 = u0 + 0.125f;
        float v0 = part / 8 / 8.0f, v1 = v0 + 0.125f;
        if (this.flip) {
            float t = u0;
            u0 = u1;
            u1 = t;
        }
        Vec3 cam = view.camera();
        float x = (float) (this.at.x - cam.x), y = (float) (this.at.y - cam.y), z = (float) (this.at.z - cam.z);
        float a = u0, b = u1;
        collector.submitCustomGeometry(pose, AdditiveGlow.of(SHEET), (matrix, consumer) ->
                Sparkle.billboard(matrix, consumer, view, x, y, z, this.size, a, b, v0, v1, this.colour));
    }
}
