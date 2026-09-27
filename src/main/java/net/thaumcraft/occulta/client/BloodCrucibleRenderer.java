package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.BoxMesh;
import net.thaumcraft.client.render.MeshDrawer;
import net.thaumcraft.occulta.BloodCrucibleBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O Crisol de Sangue no mundo: o {@code RenderBloodCrucible} e o {@code ModelBloodCrucible} do Witchery.
 *
 * <p>Nove peças de pedra — o fundo, o beiral de dentro e o de fora nos quatro lados — e, dentro dele, a
 * <b>chapa de sangue</b>, que sobe conforme o crisol enche.
 */
public class BloodCrucibleRenderer
        implements BlockEntityRenderer<BloodCrucibleBlockEntity, BloodCrucibleRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/blood_crucible.png");

    /** As nove peças, na ordem do original. */
    private static final float[] STONE = BoxMesh.join(
            BoxMesh.box(2f, -2f, -2f, 1, 1, 4, 17, 11, 32, 32),
            BoxMesh.box(-3f, -2f, -2f, 1, 1, 4, 17, 11, 32, 32),
            BoxMesh.box(-3f, -2f, -3f, 6, 1, 1, 17, 19, 32, 32),
            BoxMesh.box(-3.5f, -5f, -4f, 7, 3, 1, 0, 17, 32, 32),
            BoxMesh.box(-4f, -5f, -3.7f, 1, 3, 7, 0, 6, 32, 32),
            BoxMesh.box(-2f, -1f, -2f, 4, 1, 4, 0, 0, 32, 32),
            BoxMesh.box(-3f, -2f, 2f, 6, 1, 1, 17, 19, 32, 32),
            BoxMesh.box(-3.5f, -5f, 3f, 7, 3, 1, 0, 17, 32, 32),
            BoxMesh.box(3f, -5f, -3.5f, 1, 3, 7, 0, 6, 32, 32));

    /** A chapa de sangue: seis por seis, lisa, que sobe conforme ele enche. */
    private static final float[] BLOOD = BoxMesh.box(-3f, 0f, -3f, 6, 0, 6, 0, 0, 32, 32);

    /** A cor dele, e a altura que a chapa sobe: os números do original. */
    public static final int BLOOD_COLOR = 0xFFFF0000;
    public static final float SURFACE = -0.1f;
    public static final float DEPTH = -0.2f;

    public static class State extends BlockEntityRenderState {
        float filled;
    }

    public BloodCrucibleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BloodCrucibleBlockEntity crisol, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(crisol, state, partial, camera, crumbling);
        state.filled = crisol.filled();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        draw(pose, collector, state.lightCoords, state.filled);
    }

    /** O crisol inteiro, com o sangue à altura que ele tiver. */
    static void draw(PoseStack pose, SubmitNodeCollector collector, int light, float cheio) {
        pose.pushPose();
        // o caminho do original: meio do bloco, meia casa abaixo, e de cabeça para baixo
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.translate(0.0f, -0.5f, 0.0f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));

        pose.pushPose();
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                (m, v) -> MeshDrawer.draw(STONE, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();

        if (cheio > 0.0f) {
            pose.pushPose();
            pose.translate(0.0f, SURFACE + DEPTH * cheio, 0.0f);
            pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
            collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(TEXTURE),
                    (m, v) -> MeshDrawer.draw(BLOOD, m, v, light, OverlayTexture.NO_OVERLAY, BLOOD_COLOR));
            pose.popPose();
        }
        pose.popPose();
    }
}
