package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.thaumcraft.entity.DartEntity;

/**
 * O dardo do golem: o {@code RenderDart} da 4.2.3.5 — a flecha do jogo de então, cinza-azulada ({@code 0.5, 0.5, 0.6})
 * e mais fina (três quartos da largura).
 */
public class DartRenderer extends EntityRenderer<DartEntity, DartRenderer.State> {
    private static final Identifier ARROW = Identifier.withDefaultNamespace("textures/entity/projectiles/arrow.png");
    private static final int TINT = 0xFF000000 | (int) (0.5f * 255) << 16 | (int) (0.5f * 255) << 8 | (int) (0.6f * 255);

    public static class State extends EntityRenderState {
        public float yRot;
        public float xRot;
        public float shake;
    }

    public DartRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DartEntity dart, State s, float partial) {
        super.extractRenderState(dart, s, partial);
        s.yRot = dart.getYRot(partial);
        s.xRot = dart.getXRot(partial);
        s.shake = dart.shakeTime - partial;
    }

    @Override
    public void submit(State s, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(s.yRot - 90.0f));
        pose.mulPose(Axis.ZP.rotationDegrees(s.xRot));
        if (s.shake > 0.0f) pose.mulPose(Axis.ZP.rotationDegrees(-Mth.sin(s.shake * 3.0f) * s.shake));
        float w = 0.025625f;
        pose.mulPose(Axis.XP.rotationDegrees(45.0f));
        pose.scale(w * 0.75f, w, w);
        pose.translate(-4.0f, 0.0f, 0.0f);
        int light = s.lightCoords;
        float v16 = 0.0f, v17 = 0.15625f, v18 = 5 / 32.0f, v19 = 10 / 32.0f;
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(ARROW), (m, c) -> {
            v(c, m, -7, -2, -2, v16, v18, light, 1, 0, 0);
            v(c, m, -7, -2, 2, v17, v18, light, 1, 0, 0);
            v(c, m, -7, 2, 2, v17, v19, light, 1, 0, 0);
            v(c, m, -7, 2, -2, v16, v19, light, 1, 0, 0);
            v(c, m, -7, 2, -2, v16, v18, light, -1, 0, 0);
            v(c, m, -7, 2, 2, v17, v18, light, -1, 0, 0);
            v(c, m, -7, -2, 2, v17, v19, light, -1, 0, 0);
            v(c, m, -7, -2, -2, v16, v19, light, -1, 0, 0);
        });
        for (int i = 0; i < 4; i++) {
            pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            collector.submitCustomGeometry(pose, RenderTypes.entityCutout(ARROW), (m, c) -> {
                v(c, m, -8, -2, 0, 0.0f, 0.0f, light, 0, 0, 1);
                v(c, m, 8, -2, 0, 0.5f, 0.0f, light, 0, 0, 1);
                v(c, m, 8, 2, 0, 0.5f, 5 / 32.0f, light, 0, 0, 1);
                v(c, m, -8, 2, 0, 0.0f, 5 / 32.0f, light, 0, 0, 1);
            });
        }
        pose.popPose();
        super.submit(s, pose, collector, camera);
    }

    private static void v(VertexConsumer c, PoseStack.Pose m, float x, float y, float z, float u, float vv, int light, float nx, float ny, float nz) {
        c.addVertex(m, x, y, z).setColor(TINT).setUv(u, vv).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, nx, ny, nz);
    }
}
