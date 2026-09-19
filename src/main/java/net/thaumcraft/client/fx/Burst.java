package net.thaumcraft.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.AdditiveGlow;

/**
 * O estouro de um nó: o {@code FXBurst} da 4.2.3.5. A última fileira da folha de nós ({@code nodes.png}), quadro a
 * quadro, somando luz, por 31 tiques. Aparece quando um nó é energizado, desfeito ou quebrado.
 */
public final class Burst implements ThaumFx.Effect {
    private static final net.minecraft.resources.Identifier NODES = Thaumcraft.id("textures/misc/nodes.png");

    private final Vec3 at;
    private final float size;
    private int age;

    private Burst(Vec3 at, float size) {
        this.at = at;
        this.size = size;
    }

    /** O {@code burst}: o tamanho é o do {@code particleScale} de partida do jogo (um a dois) vezes {@code scale}. */
    public static void spawn(Vec3 at, float scale, RandomSource random) {
        ThaumFx.add(new Burst(at, (random.nextFloat() * 0.5f + 0.5f) * 2.0f * scale));
    }

    @Override
    public boolean tick() {
        return this.age++ < 31;
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, ThaumFx.View view, float partial) {
        float u0 = this.age % 32 / 32.0f, u1 = u0 + 0.03125f;
        float v0 = 0.96875f, v1 = 1.0f;
        Vec3 cam = view.camera();
        float x = (float) (this.at.x - cam.x), y = (float) (this.at.y - cam.y), z = (float) (this.at.z - cam.z);
        collector.submitCustomGeometry(pose, AdditiveGlow.of(NODES), (matrix, consumer) ->
                Sparkle.billboard(matrix, consumer, view, x, y, z, this.size, u0, u1, v0, v1, 0xFFFFFFFF));
    }
}
