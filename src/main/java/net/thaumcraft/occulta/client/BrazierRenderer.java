package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.BoxMesh;
import net.thaumcraft.occulta.brazier.BrazierBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O Braseiro no mundo: o {@code RenderBrazier} e o {@code ModelBrazier} do Witchery.
 *
 * <p>Quatro pernas com os pés abertos a quarenta e cinco graus, a bacia de quatro lados sobre o seu fundo — e a
 * <b>cinza</b>, uma chapa lisa no fundo dela, que só se desenha quando ele está aceso.
 */
public class BrazierRenderer implements BlockEntityRenderer<BrazierBlockEntity, BrazierRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/brazier.png");

    private static final float[] LEG = BoxMesh.box(-0.5f, 0f, -0.5f, 1, 11, 1, 0, 0, 64, 64);
    private static final float[] FOOT = BoxMesh.box(-0.5f, 0f, -0.5f, 1, 5, 1, 0, 13, 64, 64);
    private static final float[] ASH = BoxMesh.box(-2.5f, 0f, -2.5f, 5, 0, 5, 0, 20, 64, 64);
    private static final float[] PAN_Z = BoxMesh.box(-0.5f, -0.5f, -3f, 1, 1, 6, 5, 12, 64, 64);
    private static final float[] PAN_X = BoxMesh.box(-3f, -0.5f, -0.5f, 6, 1, 1, 4, 26, 64, 64);
    private static final float[] FOOT_BASE = BoxMesh.box(-1.5f, -0.5f, -1.5f, 3, 1, 3, 6, 0, 64, 64);
    private static final float[] PAN_BASE = BoxMesh.box(-3f, 0f, -3f, 6, 1, 6, 6, 5, 64, 64);

    /** Quanto os pés se abrem: os quarenta e cinco graus do original. */
    private static final float SPLAY = (float) (Math.PI / 4.0);

    /** Onde cada perna fica, e como o pé dela se abre. */
    private static final float[][] LEGS = {{0.7f, -0.74f}, {-0.7f, -0.7f}, {-0.7f, 0.7f}, {0.7f, 0.7f}};
    private static final float[][] FEET = {
            {-0.7f, 0.7f, SPLAY, SPLAY}, {-0.7f, -0.7f, -SPLAY, SPLAY},
            {0.7f, -0.7f, -SPLAY, -SPLAY}, {0.7f, 0.7f, SPLAY, -SPLAY}};

    public static class State extends BlockEntityRenderState {
        boolean burning;
    }

    public BrazierRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BrazierBlockEntity braseiro, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(braseiro, state, partial, camera, crumbling);
        state.burning = braseiro.burning();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        draw(pose, collector, state.lightCoords, state.burning);
    }

    /** O braseiro inteiro. */
    static void draw(PoseStack pose, SubmitNodeCollector collector, int light, boolean aceso) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);

        for (float[] perna : LEGS) {
            SpinningWheelRenderer.part(pose, collector, LEG, perna[0], 10f, perna[1], 0f, 0f, 0f, light, TEXTURE);
        }
        for (float[] pé : FEET) {
            SpinningWheelRenderer.part(pose, collector, FOOT, pé[0], 21f, pé[1], pé[2], 0f, pé[3], light, TEXTURE);
        }
        SpinningWheelRenderer.part(pose, collector, FOOT_BASE, 0f, 21f, 0f, 0f, 0f, 0f, light, TEXTURE);
        SpinningWheelRenderer.part(pose, collector, PAN_BASE, 0f, 9.95f, 0f, 0f, 0f, 0f, light, TEXTURE);
        SpinningWheelRenderer.part(pose, collector, PAN_Z, 3f, 9.5f, 0f, 0f, 0f, 0f, light, TEXTURE);
        SpinningWheelRenderer.part(pose, collector, PAN_Z, -3f, 9.5f, 0f, 0f, 0f, 0f, light, TEXTURE);
        SpinningWheelRenderer.part(pose, collector, PAN_X, 0f, 9.5f, 3f, 0f, 0f, 0f, light, TEXTURE);
        SpinningWheelRenderer.part(pose, collector, PAN_X, 0f, 9.5f, -3f, 0f, 0f, 0f, light, TEXTURE);
        if (aceso) {
            SpinningWheelRenderer.part(pose, collector, ASH, 0f, 9.7f, 0f, 0f, 0f, 0f, light, TEXTURE);
        }
        pose.popPose();
    }
}
