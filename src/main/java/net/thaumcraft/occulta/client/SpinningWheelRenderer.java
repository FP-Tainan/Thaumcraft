package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.BoxMesh;
import net.thaumcraft.client.render.MeshDrawer;
import net.thaumcraft.occulta.spinning.SpinningWheelBlock;
import net.thaumcraft.occulta.spinning.SpinningWheelBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A Roca no mundo: o {@code RenderSpinningWheel} e o {@code ModelSpinningWheel} do Witchery.
 *
 * <p>O banco inclinado, as quatro pernas abertas, os dois braços, o novelo no seu poste — e a <b>roda</b>, de
 * cinco peças, que gira quando a roca está fiando. O novelo gira com ela, no outro sentido e mais devagar, que é
 * o que o original faz.
 */
public class SpinningWheelRenderer
        implements BlockEntityRenderer<SpinningWheelBlockEntity, SpinningWheelRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/spinning_wheel.png");

    private static final float[] SEAT = BoxMesh.box(-2f, -1f, -7f, 4, 1, 14, 0, 0, 64, 64);
    private static final float[] LEG_LONG = BoxMesh.box(-1f, 0f, 0f, 1, 9, 1, 32, 0, 64, 64);
    private static final float[] LEG_LONG_L = BoxMesh.box(0f, 0f, 0f, 1, 9, 1, 32, 0, 64, 64);
    private static final float[] LEG_SHORT = BoxMesh.box(-1f, 0f, 0f, 1, 6, 1, 0, 6, 64, 64);
    private static final float[] LEG_SHORT_L = BoxMesh.box(0f, 0f, 0f, 1, 6, 1, 0, 6, 64, 64);
    private static final float[] THREAD = BoxMesh.box(-1f, -3f, -1f, 2, 3, 2, 23, 0, 64, 64);
    private static final float[] THREAD_POLE = BoxMesh.box(-0.5f, 0f, -0.5f, 1, 4, 1, 9, 7, 64, 64);
    private static final float[] ARM = BoxMesh.box(-0.5f, -7f, -0.5f, 1, 7, 1, 28, 6, 64, 64);

    /** A roda, de cinco peças, que gira toda junta. */
    private static final float[] WHEEL = BoxMesh.join(
            BoxMesh.box(0f, -3f, -3f, 0, 6, 6, 0, 0, 64, 64),
            BoxMesh.box(-0.5f, -4f, -3f, 1, 1, 6, 0, 7, 64, 64),
            BoxMesh.box(-0.5f, 3f, -3f, 1, 1, 6, 0, 7, 64, 64),
            BoxMesh.box(-0.5f, -4f, 3f, 1, 8, 1, 23, 5, 64, 64),
            BoxMesh.box(-0.5f, -4f, -4f, 1, 8, 1, 23, 5, 64, 64));

    /** O relógio da roda: o original conta as voltas pelo relógio da máquina, e não pelo do mundo. */
    public static final long SPIN_STEP = 25L;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        boolean spinning;
        long clock;
    }

    public SpinningWheelRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SpinningWheelBlockEntity roca, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(roca, state, partial, camera, crumbling);
        var feitio = roca.getBlockState();
        state.facing = feitio.hasProperty(SpinningWheelBlock.FACING)
                ? feitio.getValue(SpinningWheelBlock.FACING) : Direction.NORTH;
        state.spinning = roca.spinTime > 0 && roca.spinTime < SpinningWheelBlockEntity.SPIN_TIME
                && roca.powerLevel > 0;
        state.clock = System.currentTimeMillis() / SPIN_STEP;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);
        pose.mulPose(Axis.YP.rotationDegrees(angle(state.facing)));

        int luz = state.lightCoords;
        part(pose, collector, SEAT, 0f, 18f, 0f, 0.26025f, 0f, 0f, luz);
        part(pose, collector, LEG_LONG, -1f, 16f, 5f, 0.17453f, 0f, 0.17453f, luz);
        part(pose, collector, LEG_LONG_L, 1f, 16f, 5f, 0.17453f, 0f, -0.17453f, luz);
        part(pose, collector, LEG_SHORT, -1f, 19f, -6f, -0.17453f, 0f, 0.17453f, luz);
        part(pose, collector, LEG_SHORT_L, 1f, 19f, -6f, -0.17453f, 0f, -0.17453f, luz);

        float giro = state.spinning ? (float) (state.clock / 2L % 360L) : 0.0f;
        part(pose, collector, THREAD, 0f, 12f, 5f, 0f, (float) Math.toRadians(giro), 0f, luz);
        part(pose, collector, THREAD_POLE, 0f, 12f, 5f, 0f, 0f, 0f, luz);
        part(pose, collector, ARM, -1f, 18f, -2f, 0.22689f, 0f, 0f, luz);
        part(pose, collector, ARM, 1f, 18f, -2f, 0.22689f, 0f, 0f, luz);

        float roda = state.spinning ? (float) (-(state.clock / 3L) % 360L) : 0.0f;
        part(pose, collector, WHEEL, 0f, 12f, -3.5f, (float) Math.toRadians(roda), 0f, 0f, luz);
        pose.popPose();
    }

    /** O giro de cada marca: os quatro do original. */
    static float angle(Direction facing) {
        return switch (facing) {
            case SOUTH -> 180.0f;
            case WEST -> 270.0f;
            case EAST -> 90.0f;
            default -> 0.0f;
        };
    }

    /** Uma peça no ponto de giro dela, com as três voltas na ordem do {@code ModelRenderer}: Z, Y e X. */
    static void part(PoseStack pose, SubmitNodeCollector collector, float[] mesh, float px, float py, float pz,
                     float rx, float ry, float rz, int light) {
        part(pose, collector, mesh, px, py, pz, rx, ry, rz, light, TEXTURE);
    }

    static void part(PoseStack pose, SubmitNodeCollector collector, float[] mesh, float px, float py, float pz,
                     float rx, float ry, float rz, int light, Identifier folha) {
        pose.pushPose();
        pose.translate(px / 16.0f, py / 16.0f, pz / 16.0f);
        if (rz != 0.0f) pose.mulPose(Axis.ZP.rotation(rz));
        if (ry != 0.0f) pose.mulPose(Axis.YP.rotation(ry));
        if (rx != 0.0f) pose.mulPose(Axis.XP.rotation(rx));
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(folha),
                (m, v) -> MeshDrawer.draw(mesh, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }
}
