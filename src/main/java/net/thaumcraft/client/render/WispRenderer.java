package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.client.fx.Sparkle;
import net.thaumcraft.entity.WispEntity;

/**
 * O fogo-fátuo: o {@code RenderWisp} da 4.2.3.5. Dois quadrados virados para quem vê, somando luz, na altura de 0,45:
 * a chama grande ({@code wisp.png}, dezesseis quadros em quatro por quatro) na cor do aspecto — vermelha quando apanha —
 * e o miolo branco da linha cinco da folha de partículas, que pulsa.
 */
public class WispRenderer extends EntityRenderer<WispEntity, WispRenderer.State> {
    private static final Identifier WISP = Thaumcraft.id("textures/misc/wisp.png");

    public static class State extends EntityRenderState {
        int colour;
        boolean hurt;
        boolean alive;
        int age;
        float ticks;
    }

    public WispRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WispEntity wisp, State state, float partial) {
        super.extractRenderState(wisp, state, partial);
        Aspect aspect = wisp.aspect();
        state.colour = aspect == null ? 0 : aspect.color();
        state.hurt = wisp.hurtTime > 0;
        state.alive = wisp.getHealth() > 0.0f;
        state.age = wisp.tickCount;
        state.ticks = wisp.tickCount + partial;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.alive) return;
        int i = state.age % 16;
        float size = 64.0f, size4 = size * 4.0f;
        float x0 = (i % 4 * size) / size4, x1 = (i % 4 * size + size - 0.01f) / size4;
        float y0 = (i / 4 * size) / size4, y1 = (i / 4 * size + size - 0.01f) / size4;
        int r = state.colour >> 16 & 255, g = state.colour >> 8 & 255, b = state.colour & 255;
        int colour = state.hurt ? 0xFF000000 | 255 << 16 | Math.min(255, g * 255 / 300) << 8 | Math.min(255, b * 255 / 300)
                : 0xFF000000 | r << 16 | g << 8 | b;
        pose.pushPose();
        pose.translate(0.0f, 0.45f, 0.0f);
        pose.mulPose(camera.orientation);
        quad(pose, collector, WISP, 1.0f, x1, x0, y1, y0, colour);
        int q = state.age % 16;
        float pulse = 0.4f + Mth.sin(state.ticks / 10.0f) * 0.1f;
        quad(pose, collector, Sparkle.PARTICLES, pulse, (q + 1) / 16.0f, q / 16.0f, 6.0f / 16.0f, 5.0f / 16.0f, 0xFFFFFFFF);
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }

    /** Um quadrado de meio-lado {@code s}, com os cantos na ordem do original. */
    private static void quad(PoseStack pose, SubmitNodeCollector collector, Identifier texture, float s, float u1, float u0, float v1, float v0,
                             int colour) {
        collector.submitCustomGeometry(pose, AdditiveGlow.of(texture), (m, c) -> {
            corner(m, c, -s, -s, u1, v1, colour);
            corner(m, c, s, -s, u0, v1, colour);
            corner(m, c, s, s, u0, v0, colour);
            corner(m, c, -s, s, u1, v0, colour);
        });
    }

    private static void corner(PoseStack.Pose m, com.mojang.blaze3d.vertex.VertexConsumer c, float x, float y, float u, float v, int colour) {
        c.addVertex(m, x, y, 0.0f).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF0).setNormal(m, 0.0f, 0.0f, 1.0f);
    }
}
