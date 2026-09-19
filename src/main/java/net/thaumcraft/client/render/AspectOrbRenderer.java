package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.client.fx.Sparkle;
import net.thaumcraft.entity.AspectOrbEntity;

/**
 * O orbe de aspecto: o {@code RenderAspectOrb} da 4.2.3.5. Um quadrado virado para quem vê, com um dos dezesseis
 * quadros da linha oito da folha de partículas (troca a cada 25 ms), na cor do aspecto a meia força, somando luz ou
 * misturando conforme o aspecto, e encolhendo de 0,4 a 0,1 ao longo da vida.
 */
public class AspectOrbRenderer extends EntityRenderer<AspectOrbEntity, AspectOrbRenderer.State> {
    public static class State extends EntityRenderState {
        float life;
        int colour = 0xFFFFFF;
        boolean additive = true;
    }

    public AspectOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.1f;
        this.shadowStrength = 0.5f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AspectOrbEntity entity, State state, float partial) {
        super.extractRenderState(entity, state, partial);
        state.life = entity.life(partial);
        Aspect aspect = entity.aspect();
        state.colour = aspect == null ? 0xFFFFFF : aspect.color();
        state.additive = aspect == null || aspect.blend() == 1;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        int frame = (int) (System.nanoTime() / 25000000L % 16L);
        float u0 = frame / 16.0f, u1 = (frame + 1) / 16.0f;
        float v0 = 0.5f, v1 = 0.5625f;
        float scale = 0.1f + 0.3f * (1.0f - state.life);
        int colour = 0x80 << 24 | state.colour & 0xFFFFFF;
        pose.pushPose();
        pose.mulPose(camera.orientation);
        pose.scale(scale, scale, scale);
        collector.submitCustomGeometry(pose, state.additive ? AdditiveGlow.of(Sparkle.PARTICLES) : AdditiveGlow.blended(Sparkle.PARTICLES),
                (matrix, consumer) -> {
                    corner(matrix, consumer, -0.5f, -0.25f, u0, v1, colour);
                    corner(matrix, consumer, 0.5f, -0.25f, u1, v1, colour);
                    corner(matrix, consumer, 0.5f, 0.75f, u1, v0, colour);
                    corner(matrix, consumer, -0.5f, 0.75f, u0, v0, colour);
                });
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }

    private static void corner(PoseStack.Pose matrix, com.mojang.blaze3d.vertex.VertexConsumer consumer,
                               float x, float y, float u, float v, int colour) {
        consumer.addVertex(matrix, x, y, 0.0f).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF0).setNormal(matrix, 0.0f, 1.0f, 0.0f);
    }
}
