package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * O {@code UtilsFX.renderFacingQuad} da 4.2.3.5: um quadrado de meio-lado {@code scale} sempre virado para quem vê,
 * com o quadro {@code frame} de uma tira de {@code frames} quadros lado a lado, brilho 220.
 */
public final class FacingQuad {
    private FacingQuad() {
    }

    /**
     * @param at onde fica o centro, na conta do {@code pose} que chega (num desenhista de bloco, a partir do canto do
     *           bloco)
     */
    public static void draw(PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, RenderType type, Vec3 at,
                            float scale, float alpha, int frames, int frame, int colour) {
        int argb = (int) (Mth.clamp(alpha, 0.0f, 1.0f) * 255.0f) << 24 | colour & 0xFFFFFF;
        float u0 = (float) frame / frames, u1 = (float) (frame + 1) / frames;
        pose.pushPose();
        pose.translate(at.x, at.y, at.z);
        pose.mulPose(camera.orientation);
        pose.scale(scale, scale, scale);
        collector.submitCustomGeometry(pose, type, (m, c) -> {
            corner(m, c, -1.0f, -1.0f, u0, 1.0f, argb);
            corner(m, c, 1.0f, -1.0f, u1, 1.0f, argb);
            corner(m, c, 1.0f, 1.0f, u1, 0.0f, argb);
            corner(m, c, -1.0f, 1.0f, u0, 0.0f, argb);
        });
        pose.popPose();
    }

    private static void corner(PoseStack.Pose m, com.mojang.blaze3d.vertex.VertexConsumer c, float x, float y, float u, float v, int argb) {
        c.addVertex(m, x, y, 0.0f).setColor(argb).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(220).setNormal(m, 0.0f, 0.0f, 1.0f);
    }
}
