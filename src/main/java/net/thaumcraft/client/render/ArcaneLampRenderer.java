package net.thaumcraft.client.render;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.ArcaneLampBlock;

/**
 * O bocal das lâmpadas: o {@code TileArcaneLampRenderer} da 4.2.3.5, com o {@code renderNozzle} do
 * {@code ModelBoreBase} — as duas caixinhas que prendem a lâmpada ao bloco de apoio.
 */
public class ArcaneLampRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, ArcaneLampRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/bore.png");
    /** O {@code Nozzle1} e o {@code Nozzle2}, já com o ponto de giro deles (0, 8, 0). */
    private static final float[] NOZZLE = BoxMesh.join(
            BoxMesh.box(2.5f, 6.0f, -2.0f, 5, 4, 4, 106, 42, 128, 64),
            BoxMesh.box(7.0f, 5.5f, -2.5f, 1, 5, 5, 106, 51, 128, 64));

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.DOWN;
    }

    public ArcaneLampRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T lamp, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(lamp, state, crumbling);
        state.facing = lamp.getBlockState().hasProperty(ArcaneLampBlock.FACING)
                ? lamp.getBlockState().getValue(ArcaneLampBlock.FACING) : Direction.DOWN;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.5f, 0.0f, 0.5f);
        switch (state.facing) {
            case DOWN -> {
                pose.translate(-0.5f, 0.5f, 0.0f);
                pose.mulPose(Axis.ZN.rotationDegrees(90.0f));
            }
            case UP -> {
                pose.translate(0.5f, 0.5f, 0.0f);
                pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
            }
            case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(270.0f));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(180.0f));
            case EAST -> {
            }
        }
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        int light = state.lightCoords;
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                (m, v) -> MeshDrawer.draw(NOZZLE, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }
}
