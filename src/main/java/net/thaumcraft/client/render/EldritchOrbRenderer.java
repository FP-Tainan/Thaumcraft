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
import net.thaumcraft.entity.eldritch.EldritchOrbEntity;

import java.util.Random;

/**
 * O orbe eldritch: o {@code RenderEldritchOrb} da 4.2.3.5. Os doze leques de raios do orbe primordial, todos na cor da
 * ordem ({@code BlockCustomOreItem.colors[5]}), e no meio o brilho da terceira linha da folha de partículas em mistura
 * comum, do tamanho de um bloco.
 */
public class EldritchOrbRenderer extends EntityRenderer<EldritchOrbEntity, EldritchOrbRenderer.State> {
    private static final int OUTER = 0xEECCFF;

    public static class State extends EntityRenderState {
        int id;
        int age;
    }

    public EldritchOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EldritchOrbEntity entity, State state, float partial) {
        super.extractRenderState(entity, state, partial);
        state.id = entity.getId();
        state.age = entity.tickCount;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float spin = state.age / 80.0f;
        float grow = Math.min(state.age, 10) / 10.0f;
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
            collector.submitCustomGeometry(pose, RenderTypes.dragonRays(), (matrix, buffer) -> {
                float[] left = {-0.866f * width, length, -0.5f * width};
                float[] right = {0.866f * width, length, -0.5f * width};
                float[] bottom = {0.0f, length, width};
                ray(matrix, buffer, left, right);
                ray(matrix, buffer, right, bottom);
                ray(matrix, buffer, bottom, left);
            });
        }
        pose.popPose();

        float u0 = state.age % 13 / 16.0f, u1 = u0 + 0.0624375f;
        float v0 = 0.1875f, v1 = v0 + 0.0624375f;
        pose.pushPose();
        pose.mulPose(camera.orientation);
        collector.submitCustomGeometry(pose, AdditiveGlow.blended(Sparkle.PARTICLES), (matrix, consumer) -> {
            corner(matrix, consumer, -0.5f, -0.5f, u0, v1);
            corner(matrix, consumer, 0.5f, -0.5f, u1, v1);
            corner(matrix, consumer, 0.5f, 0.5f, u1, v0);
            corner(matrix, consumer, -0.5f, 0.5f, u0, v0);
        });
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }

    private static void ray(PoseStack.Pose matrix, com.mojang.blaze3d.vertex.VertexConsumer buffer, float[] a, float[] b) {
        buffer.addVertex(matrix, 0.0f, 0.0f, 0.0f).setColor(0xFFFFFFFF);
        buffer.addVertex(matrix, a[0], a[1], a[2]).setColor(OUTER);
        buffer.addVertex(matrix, b[0], b[1], b[2]).setColor(OUTER);
    }

    private static void corner(PoseStack.Pose matrix, com.mojang.blaze3d.vertex.VertexConsumer consumer, float x, float y, float u, float v) {
        consumer.addVertex(matrix, x, y, 0.0f).setColor(0xFFFFFFFF).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, 1.0f);
    }
}
