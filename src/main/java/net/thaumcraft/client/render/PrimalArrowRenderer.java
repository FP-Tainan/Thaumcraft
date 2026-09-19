package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.projectile.ArrowModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.PrimalArrowEntity;

/**
 * O {@code RenderPrimalArrow} da 4.2.3.5: a flecha comum desenhada somando luz, com o fogo-fátuo da cor do primário
 * (o {@code BlockCustomOreItem.colors}) girando em volta; cravada, some aos poucos em cinco segundos.
 */
public class PrimalArrowRenderer extends ArrowRenderer<PrimalArrowEntity, PrimalArrowRenderer.State> {
    private static final Identifier ARROW = Identifier.withDefaultNamespace("textures/entity/projectiles/arrow.png");
    private static final Identifier WISP = Thaumcraft.id("textures/misc/wisp.png");
    /** As cores do minério infundido, de onde a flecha tira a dela (a primeira é a da pedra comum). */
    private static final int[] COLORS = {16777215, 16777086, 16727041, 37119, 40960, 15650047, 5592439};
    private final ArrowModel model;

    public static class State extends ArrowRenderState {
        int type;
        float fade;
        int ticks;
    }

    public PrimalArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ArrowModel(context.bakeLayer(ModelLayers.ARROW));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PrimalArrowEntity arrow, State state, float partial) {
        super.extractRenderState(arrow, state, partial);
        state.type = Mth.clamp(arrow.arrowType(), 0, 5);
        state.fade = (100.0f - arrow.inGroundTicks()) / 100.0f;
        state.ticks = arrow.tickCount;
    }

    @Override
    protected Identifier getTextureLocation(State state) {
        return ARROW;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float fade = Mth.clamp(state.fade, 0.0f, 1.0f);
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0f));
        pose.mulPose(Axis.ZP.rotationDegrees(state.xRot));
        int tint = Mth.clamp((int) (fade * 255.0f), 0, 255) << 24 | 0xFFFFFF;
        collector.submitModel(this.model, state, pose, AdditiveGlow.of(ARROW), state.lightCoords, OverlayTexture.NO_OVERLAY, tint, null,
                state.outlineColor, null);
        pose.popPose();
        // o fogo-fátuo: dezesseis quadros de quatro por quatro, sempre virado para quem olha
        int rgb = COLORS[state.type + 1];
        int colour = Mth.clamp((int) (fade * 255.0f), 0, 255) << 24 | rgb;
        int i = state.ticks % 16;
        float x0 = (i % 4) / 4.0f, x1 = x0 + 0.24975f, y0 = (i / 4) / 4.0f, y1 = y0 + 0.24975f;
        pose.pushPose();
        pose.mulPose(camera.orientation);
        // desenhado dos dois lados: o original não descartava a face de trás
        collector.submitCustomGeometry(pose, state.type < 5 ? AdditiveGlow.of(WISP) : AdditiveGlow.blended(WISP), (m, c) -> {
            vertex(m, c, -0.5f, -0.5f, x1, y1, colour);
            vertex(m, c, -0.5f, 0.5f, x1, y0, colour);
            vertex(m, c, 0.5f, 0.5f, x0, y0, colour);
            vertex(m, c, 0.5f, -0.5f, x0, y1, colour);
            vertex(m, c, 0.5f, -0.5f, x0, y1, colour);
            vertex(m, c, 0.5f, 0.5f, x0, y0, colour);
            vertex(m, c, -0.5f, 0.5f, x1, y0, colour);
            vertex(m, c, -0.5f, -0.5f, x1, y1, colour);
        });
        pose.popPose();
    }

    private static void vertex(PoseStack.Pose m, VertexConsumer c, float x, float y, float u, float v, int colour) {
        c.addVertex(m, x, y, 0.0f).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(m, 0.0f, 0.0f, 1.0f);
    }
}
