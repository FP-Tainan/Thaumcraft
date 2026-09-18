package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.thaumcraft.client.fx.Sparkle;
import net.thaumcraft.entity.PrimalOrbEntity;

import java.util.Random;

/**
 * A esfera do foco Primordial: o {@code RenderPrimalOrb} da 4.2.3.5, descompilado.
 *
 * <p>Doze leques de raios, cada par da cor de um dos seis primários (as cores do {@code BlockCustomOreItem}),
 * girando devagar e crescendo nos primeiros dez tiques, somando luz e sumindo para as pontas. No meio, um
 * brilho virado para quem vê, com os treze quadros da segunda linha da folha de partículas.
 */
public class PrimalOrbRenderer extends EntityRenderer<PrimalOrbEntity, PrimalOrbRenderer.State> {
    /** As cores do {@code BlockCustomOreItem.colors}: a primeira é o branco do cinábrio, que os raios pulam. */
    private static final int[] COLORS = {0xFFFFFF, 0xFFFF7E, 0xFF3C01, 0x0090FF, 0x00A000, 0xEECCFF, 0x555577};

    public static class State extends EntityRenderState {
        int id;
        int age;
    }

    public PrimalOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PrimalOrbEntity entity, State state, float partial) {
        super.extractRenderState(entity, state, partial);
        state.id = entity.getId();
        state.age = entity.tickCount;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float spin = state.age / 80.0f;
        float grow = Math.min(state.age, 10) / 10.0f;

        // as rotações se acumulam de um leque para o outro, como no original
        pose.pushPose();
        Random rays = new Random(state.id);
        for (int i = 0; i < 12; i++) {
            pose.mulPose(Axis.XP.rotationDegrees(rays.nextFloat() * 360.0f));
            pose.mulPose(Axis.YP.rotationDegrees(rays.nextFloat() * 360.0f));
            pose.mulPose(Axis.ZP.rotationDegrees(rays.nextFloat() * 360.0f));
            pose.mulPose(Axis.XP.rotationDegrees(rays.nextFloat() * 360.0f));
            pose.mulPose(Axis.YP.rotationDegrees(rays.nextFloat() * 360.0f));
            pose.mulPose(Axis.ZP.rotationDegrees(rays.nextFloat() * 360.0f + spin * 360.0f));
            float length = (rays.nextFloat() * 20.0f + 5.0f) / (30.0f / grow);
            float width = (rays.nextFloat() * 2.0f + 1.0f) / (30.0f / grow);
            int outer = COLORS[i / 2 + 1];
            collector.submitCustomGeometry(pose, RenderTypes.dragonRays(), (matrix, buffer) -> {
                float l = length, w = width;
                float[] left = {-0.866f * w, l, -0.5f * w};
                float[] right = {0.866f * w, l, -0.5f * w};
                float[] bottom = {0.0f, l, w};
                ray(matrix, buffer, left, right, outer);
                ray(matrix, buffer, right, bottom, outer);
                ray(matrix, buffer, bottom, left, outer);
            });
        }
        pose.popPose();

        // o brilho do meio, com o quadro do tique
        float u0 = state.age % 13 / 16.0f, u1 = u0 + 0.0624375f;
        float v0 = 0.125f, v1 = v0 + 0.0624375f;
        pose.pushPose();
        pose.mulPose(camera.orientation);
        pose.scale(0.5f, 0.5f, 0.5f);
        collector.submitCustomGeometry(pose, AdditiveGlow.of(Sparkle.PARTICLES), (matrix, consumer) -> {
            int colour = 0xCCFFFFFF;
            corner(matrix, consumer, -0.5f, -0.5f, u0, v1, colour);
            corner(matrix, consumer, 0.5f, -0.5f, u1, v1, colour);
            corner(matrix, consumer, 0.5f, 0.5f, u1, v0, colour);
            corner(matrix, consumer, -0.5f, 0.5f, u0, v0, colour);
        });
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }

    /** Um triângulo do leque: branco cheio no centro, a cor do primário sem nada nas pontas. */
    private static void ray(PoseStack.Pose matrix, com.mojang.blaze3d.vertex.VertexConsumer buffer,
                            float[] a, float[] b, int outer) {
        buffer.addVertex(matrix, 0.0f, 0.0f, 0.0f).setColor(0xFFFFFFFF);
        buffer.addVertex(matrix, a[0], a[1], a[2]).setColor(outer);
        buffer.addVertex(matrix, b[0], b[1], b[2]).setColor(outer);
    }

    private static void corner(PoseStack.Pose matrix, com.mojang.blaze3d.vertex.VertexConsumer consumer,
                               float x, float y, float u, float v, int colour) {
        consumer.addVertex(matrix, x, y, 0.0f).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, 1.0f);
    }
}
