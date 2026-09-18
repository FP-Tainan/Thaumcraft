package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import org.joml.Quaternionf;

/**
 * Os anéis que o tubo de mão única e o tampão desenham: a haste do {@code ModelTubeValve} achatada e tingida,
 * posta de través no cano. É a conta de giros do {@code TileTubeOnewayRenderer} e do
 * {@code TileTubeBufferRenderer} da 4.2.3.5, tal e qual.
 */
public final class TubeRingsRenderer {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/valve.png");
    /** A única caixa do ModelTubeValve. */
    private static final float[] ROD = BoxMesh.box(-1.0f, 2.0f, -1.0f, 2.0f, 2.0f, 2.0f, 0.0f, 10.0f, 64.0f, 32.0f);
    private static final float UNIT = 1.0f / 16.0f;

    private TubeRingsRenderer() {
    }

    /** O giro que os dois desenhistas do original fazem para deitar o anel no rumo do cano. */
    static void turnTowards(PoseStack pose, Direction fd) {
        if (fd.getStepY() == 0) {
            pose.mulPose(Axis.YP.rotationDegrees(90.0f));
        } else {
            pose.mulPose(Axis.XN.rotationDegrees(90.0f));
            pose.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(90.0), fd.getStepY(), 0.0f, 0.0f));
        }
        pose.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(90.0),
                fd.getStepX(), fd.getStepY(), fd.getStepZ()));
    }

    static void ring(PoseStack pose, SubmitNodeCollector collector, int light, int colour) {
        pose.pushPose();
        pose.scale(UNIT, UNIT, UNIT);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE), (matrix, consumer) ->
                MeshDrawer.draw(ROD, matrix, consumer, light, OverlayTexture.NO_OVERLAY, colour));
        pose.popPose();
    }

    /** Os três anéis azulados do tubo de mão única, apontando para onde a essência vai. */
    public static void oneway(PoseStack pose, SubmitNodeCollector collector, Direction facing, int light) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        turnTowards(pose, facing);
        pose.scale(1.1f, 0.5f, 1.1f);
        pose.translate(0.0f, -0.5f, 0.0f);
        int colour = 0xFF000000 | (int) (0.45f * 255) << 16 | (int) (0.5f * 255) << 8 | 255;
        ring(pose, collector, light, colour);
        pose.translate(0.0f, -0.25f, 0.0f);
        ring(pose, collector, light, colour);
        pose.translate(0.0f, -0.25f, 0.0f);
        ring(pose, collector, light, colour);
        pose.popPose();
    }

    /** O anel de um lado estrangulado do tampão: azul quando só puxa um, vermelho quando não puxa nada. */
    public static void choke(PoseStack pose, SubmitNodeCollector collector, Direction dir, int level, int light) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        turnTowards(pose, dir.getOpposite());
        int colour = level == 2 ? 0xFFFF4D4D : 0xFF4D4DFF;
        pose.scale(1.2f, 1.0f, 1.2f);
        pose.translate(0.0f, -0.5f, 0.0f);
        ring(pose, collector, light, colour);
        pose.popPose();
    }
}
