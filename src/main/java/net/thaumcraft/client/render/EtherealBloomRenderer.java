package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.EtherealBloomBlockEntity;

/**
 * A Flor Etérea: o {@code TileEtherealBloomRenderer} da 4.2.3.5.
 *
 * <p>Tudo cresce com o {@code growthCounter}: o caule sobe primeiro, depois abrem as folhas de baixo e, por
 * último, as de cima com o cristal. No alto fica um brilho azulado, um quadro da sexta fileira da folha dos
 * nós virado para quem olha, e o cristal no meio dele — os dois somando luz, como no original.
 */
public class EtherealBloomRenderer implements BlockEntityRenderer<EtherealBloomBlockEntity, EtherealBloomRenderer.State> {
    private static final Identifier NODES = Thaumcraft.id("textures/misc/nodes.png");
    private static final Identifier CRYSTAL = Thaumcraft.id("textures/models/crystalcapacitor.png");
    private static final Identifier LEAVES = Thaumcraft.id("textures/block/purifier_leaves.png");
    private static final Identifier STALK = Thaumcraft.id("textures/block/purifier_stalk.png");
    /** O {@code ModelCube} de sempre: a caixa de 16 com a textura de 64 por 32, espelhada. */
    private static final float[] CUBE = BoxMesh.mirror(BoxMesh.box(0, 0, 0, 16, 16, 16, 0, 0, 64, 32));
    /** O {@code 200} que o original passa ao {@code setBrightness} das folhas e do caule. */
    private static final int PLANT_LIGHT = 200;
    private static final int GLOW = 0xFFAADBFF;

    public static class State extends BlockEntityRenderState {
        float growth;
        int frame;
    }

    public EtherealBloomRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EtherealBloomBlockEntity bloom, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(bloom, state, crumbling);
        state.growth = bloom.growthCounter + partial;
        state.frame = bloom.counter % 32;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float rc1 = Math.min(state.growth, 100.0f);
        float rc2 = Math.min(state.growth, 50.0f);
        float rc3 = Math.clamp(state.growth - 33.0f, 0.0f, 33.0f);
        float rc4 = Math.clamp(state.growth - 66.0f, 0.0f, 33.0f);
        float scale1 = rc1 / 100.0f;
        float scale2 = rc2 / 60.0f + 0.1666666f;
        float scale3 = rc3 / 33.0f;
        float scale4 = rc4 / 33.0f * 0.7f;

        // o brilho no alto, sempre de frente para quem olha
        pose.pushPose();
        pose.translate(0.5f, scale1, 0.5f);
        pose.mulPose(camera.orientation);
        float u0 = state.frame / 32.0f, u1 = (state.frame + 1) / 32.0f, v0 = 6 / 32.0f, v1 = 7 / 32.0f;
        float s = scale1;
        collector.submitCustomGeometry(pose, AdditiveGlow.of(NODES), (m, c) -> {
            vertex(m, c, -s, -s, 0, u1, v1, GLOW, 0xF000F0);
            vertex(m, c, s, -s, 0, u0, v1, GLOW, 0xF000F0);
            vertex(m, c, s, s, 0, u0, v0, GLOW, 0xF000F0);
            vertex(m, c, -s, s, 0, u1, v0, GLOW, 0xF000F0);
        });
        pose.popPose();

        // o cristal, somando luz também
        if (scale4 > 0.0f) {
            pose.pushPose();
            pose.translate(0.5f - scale4 / 8.0f, scale1 - scale4 / 6.0f, 0.5f - scale4 / 8.0f);
            pose.scale(scale4 / 4.0f / 16.0f, scale4 / 3.0f / 16.0f, scale4 / 4.0f / 16.0f);
            collector.submitCustomGeometry(pose, AdditiveGlow.of(CRYSTAL),
                    (m, c) -> MeshDrawer.draw(CUBE, m, c, 0xF000F0, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
            pose.popPose();
        }

        // as folhas de baixo
        pose.pushPose();
        pose.translate(0.5f, 0.25f, 0.5f);
        pose.mulPose(Axis.XP.rotationDegrees(180.0f));
        for (int a = 0; a < 4; a++) {
            pose.pushPose();
            pose.scale(scale3, scale1, scale3);
            pose.mulPose(Axis.YP.rotationDegrees(90 * a));
            quad(pose, collector, LEAVES);
            pose.popPose();
        }
        pose.popPose();

        // as de cima, viradas de quarenta e cinco graus
        pose.pushPose();
        pose.translate(0.5f, 0.6f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(45.0f));
        pose.mulPose(Axis.XP.rotationDegrees(180.0f));
        for (int a = 0; a < 4; a++) {
            pose.pushPose();
            pose.scale(scale4, scale1 * 0.7f, scale4);
            pose.mulPose(Axis.YP.rotationDegrees(90 * a));
            quad(pose, collector, LEAVES);
            pose.popPose();
        }
        pose.popPose();

        // o caule, que cresce do chão
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.XP.rotationDegrees(180.0f));
        for (int a = 0; a < 4; a++) {
            pose.pushPose();
            pose.translate(0.0f, (1.0f - scale1) / 2.0f, 0.0f);
            pose.scale(scale2, scale1, scale2);
            pose.mulPose(Axis.YP.rotationDegrees(90 * a));
            quad(pose, collector, STALK);
            pose.popPose();
        }
        pose.popPose();
    }

    /** O {@code renderQuadCenteredFromIcon}: o ícone inteiro num quadrado de lado um, centrado na origem. */
    private static void quad(PoseStack pose, SubmitNodeCollector collector, Identifier texture) {
        if (pose.last().pose().determinant() == 0.0f) return;
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(texture), (m, c) -> {
            vertex(m, c, -0.5f, 0.5f, 1.0f, 1.0f, 1.0f, 0xFFFFFFFF, PLANT_LIGHT);
            vertex(m, c, 0.5f, 0.5f, 1.0f, 0.0f, 1.0f, 0xFFFFFFFF, PLANT_LIGHT);
            vertex(m, c, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0xFFFFFFFF, PLANT_LIGHT);
            vertex(m, c, -0.5f, -0.5f, 1.0f, 1.0f, 0.0f, 0xFFFFFFFF, PLANT_LIGHT);
        });
    }

    private static void vertex(PoseStack.Pose m, VertexConsumer c, float x, float y, float nz, float u, float v,
                               int colour, int light) {
        c.addVertex(m, x, y, 0.0f).setColor(colour).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(m, 0.0f, 0.0f, nz == 0 ? 1.0f : nz);
    }
}
