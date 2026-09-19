package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.thaumcraft.client.fx.Sparkle;
import net.thaumcraft.entity.EmberEntity;

/**
 * A brasa: o {@code RenderEmber} da 4.2.3.5, descompilado.
 *
 * <p>Um quadrado virado para quem vê, com um dos quadros de fogo da linha dez da folha de partículas do mod —
 * do sétimo ao décimo quinto, conforme a brasa envelhece —, somando luz. Ela cresce de um quarto até um bloco
 * e um quarto ao longo da vida.
 */
public class EmberRenderer extends EntityRenderer<EmberEntity, EmberRenderer.State> {
    public static class State extends EntityRenderState {
        /** Que fração da vida a brasa já viveu. */
        float life;
    }

    public EmberRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EmberEntity entity, State state, float partial) {
        super.extractRenderState(entity, state, partial);
        state.life = (float) entity.tickCount / entity.duration();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        int frame = (int) (8.0f * state.life);
        float u0 = (7 + frame) / 16.0f, u1 = u0 + 0.0625f;
        float v0 = 0.5625f, v1 = v0 + 0.0625f;
        float scale = 0.25f + state.life;

        pose.pushPose();
        pose.scale(scale, scale, scale);
        pose.mulPose(camera.orientation);
        collector.submitCustomGeometry(pose, AdditiveGlow.of(Sparkle.PARTICLES), (matrix, consumer) -> {
            int colour = 0xE6FFFFFF;
            corner(matrix, consumer, -0.5f, -0.5f, u0, v1, colour);
            corner(matrix, consumer, 0.5f, -0.5f, u1, v1, colour);
            corner(matrix, consumer, 0.5f, 0.5f, u1, v0, colour);
            corner(matrix, consumer, -0.5f, 0.5f, u0, v0, colour);
        });
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }

    private static void corner(PoseStack.Pose matrix, com.mojang.blaze3d.vertex.VertexConsumer consumer,
                               float x, float y, float u, float v, int colour) {
        consumer.addVertex(matrix, x, y, 0.0f).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xDC).setNormal(matrix, 0.0f, 0.0f, 1.0f);
    }
}
