package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.eldritch.EldritchObeliskBlockEntity;
import net.thaumcraft.world.OuterLands;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * O obelisco eldritch: o {@code TileEldritchObeliskRenderer} da 4.2.3.5. Três blocos de altura boiando um palmo acima
 * do pé, subindo e descendo devagar: por dentro, em cada lado, o céu de estrelas do portal (o mesmo do buraco portátil,
 * {@link HoleRenderer#HOLE}); por fora, a casca rendada ({@code obelisk_side.png}), e em cima e embaixo a peça
 * {@code Cap} do {@code obelisk_cap.obj}. De longe (mais de vinte e dois blocos) o céu vira o campo parado de
 * {@code particlefield32.png}, meio apagado. A luz é a do bloco cinco acima do pé. Nas Terras de Fora, as texturas
 * escuras.
 */
public class EldritchObeliskRenderer implements BlockEntityRenderer<EldritchObeliskBlockEntity, EldritchObeliskRenderer.State> {
    private static final Identifier SIDE = Thaumcraft.id("textures/models/obelisk_side.png");
    private static final Identifier SIDE_2 = Thaumcraft.id("textures/models/obelisk_side_2.png");
    private static final Identifier CAP = Thaumcraft.id("textures/models/obelisk_cap.png");
    private static final Identifier CAP_2 = Thaumcraft.id("textures/models/obelisk_cap_2.png");
    private static final Identifier FIELD_FAR = Thaumcraft.id("textures/misc/particlefield32.png");
    private static final int HEIGHT = 3;

    public static class State extends BlockEntityRenderState {
        float bob;
        boolean near;
        boolean outer;
        int light;
    }

    public EldritchObeliskRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EldritchObeliskBlockEntity te, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(te, state, partial, camera, crumbling);
        var viewer = Minecraft.getInstance().getCameraEntity();
        float count = (viewer == null ? 0 : viewer.tickCount) + partial;
        state.bob = Mth.sin(count / 10.0f) * 0.1f + 0.1f;
        var p = te.getBlockPos();
        // o original mede do meio do pé, um pouco para dentro do lado sul
        state.near = viewer == null || viewer.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5) < 512.0;
        state.outer = te.getLevel() != null && OuterLands.is(te.getLevel());
        state.light = te.getLevel() == null ? state.lightCoords : LightCoordsUtil.getLightCoords(te.getLevel(), p.above(5));
    }

    /** O obelisco sobe cinco blocos acima do pé: não pode sumir quando o pé sai da tela. */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    /** O {@code getMaxRenderDistanceSquared} de 9216: noventa e seis blocos. */
    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float y0 = 1.0f + state.bob;
        // o céu, por dentro de cada lado
        pose.pushPose();
        if (state.near) {
            collector.submitCustomGeometry(pose, HoleRenderer.HOLE, (m, v) -> planes(m.pose(), v, y0, false, 0));
        } else {
            collector.submitCustomGeometry(pose, RenderTypes.entitySolid(FIELD_FAR), (m, v) -> planes(m.pose(), v, y0, true, LightCoordsUtil.pack(11, 0)));
        }
        pose.popPose();

        int light = state.light;
        Identifier side = state.outer ? SIDE_2 : SIDE;
        Identifier cap = state.outer ? CAP_2 : CAP;
        // a casca rendada, nos quatro lados
        pose.pushPose();
        pose.translate(0.5f, y0, 0.5f);
        for (int a = 0; a < 4; a++) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(a * 90.0f));
            pose.translate(0.0f, 0.0f, -0.5f);
            collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(side), (m, v) -> {
                vertex(m, v, -0.5f, HEIGHT, 0.0f, 0.0f, 1.0f, light);
                vertex(m, v, 0.5f, HEIGHT, 0.0f, 1.0f, 1.0f, light);
                vertex(m, v, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, light);
                vertex(m, v, -0.5f, 0.0f, 0.0f, 0.0f, 0.0f, light);
            });
            pose.popPose();
        }
        pose.popPose();

        // as duas pontas: a de baixo virada para baixo, a de cima para cima
        float[] mesh = ObjModel.part("obelisk_cap", "Cap");
        pose.pushPose();
        pose.translate(0.5f, y0, 0.5f);
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(cap),
                (m, v) -> ObjMesh.draw(mesh, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
        pose.pushPose();
        pose.translate(0.5f, 4.0f + state.bob, 0.5f);
        pose.mulPose(Axis.XP.rotationDegrees(-90.0f));
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(cap),
                (m, v) -> ObjMesh.draw(mesh, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }

    private static void vertex(PoseStack.Pose m, VertexConsumer v, float x, float y, float z, float u, float vv, int light) {
        v.addVertex(m, x, y, z).setColor(0xFFFFFFFF).setUv(u, vv).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, 0.0f, 0.0f, -1.0f);
    }

    /**
     * Os quatro planos do céu ({@code drawPlaneZNeg/ZPos/XNeg/XPos}), a um centésimo das faces, três blocos de altura.
     * De perto, só a posição (o shader faz o resto); de longe, a figura parada com metade do brilho.
     */
    private static void planes(Matrix4f m, VertexConsumer v, float y0, boolean far, int light) {
        float lo = 0.01f, hi = 0.99f, y1 = y0 + HEIGHT;
        float[][][] quads = {
                {{0, y1, lo}, {1, y1, lo}, {1, y0, lo}, {0, y0, lo}},
                {{0, y1, hi}, {0, y0, hi}, {1, y0, hi}, {1, y1, hi}},
                {{lo, y1, 0}, {lo, y0, 0}, {lo, y0, 1}, {lo, y1, 1}},
                {{hi, y1, 0}, {hi, y1, 1}, {hi, y0, 1}, {hi, y0, 0}},
        };
        float[][] uv = {{1, 1}, {1, 0}, {0, 0}, {0, 1}};
        for (float[][] q : quads) {
            for (int pass = 0; pass < 2; pass++) {
                for (int i = 0; i < 4; i++) {
                    int k = pass == 0 ? i : 3 - i;
                    if (far) {
                        v.addVertex(m, q[k][0], q[k][1], q[k][2]).setColor(0.5f, 0.5f, 0.5f, 1.0f).setUv(uv[k][0], uv[k][1])
                                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0f, 1.0f, 0.0f);
                    } else {
                        v.addVertex(m, q[k][0], q[k][1], q[k][2]);
                    }
                }
            }
        }
    }
}
