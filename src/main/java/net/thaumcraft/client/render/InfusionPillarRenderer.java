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
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.InfusionPillarBlockEntity;
import net.thaumcraft.client.render.model.PillarModel;

/**
 * O pilar do altar de infusão: o {@code TileInfusionPillarRenderer} da 4.2.3.5, com o {@code pillar.obj} do mod.
 *
 * <p>O modelo vem deitado no arquivo; o original o põe de pé girando noventa graus em X e depois o vira para o
 * canto do altar em que ele está — nada, noventa, duzentos e setenta ou cento e oitenta graus.
 */
public class InfusionPillarRenderer implements BlockEntityRenderer<InfusionPillarBlockEntity, InfusionPillarRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/pillar.png");

    public static class State extends BlockEntityRenderState {
        byte orientation;
    }

    public InfusionPillarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(InfusionPillarBlockEntity pillar, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(pillar, state, crumbling);
        state.orientation = pillar.orientation();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.5, 0.0, 0.5);
        pose.mulPose(Axis.XN.rotationDegrees(90.0f));
        if (state.orientation == 3) {
            pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
        } else if (state.orientation == 4) {
            pose.mulPose(Axis.ZP.rotationDegrees(270.0f));
        } else if (state.orientation == 5) {
            pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        }
        int light = state.lightCoords;
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE), (matrix, consumer) ->
                ObjMesh.draw(PillarModel.PILLAR, matrix, consumer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }

    /** O modelo sobe dois blocos: o pilar não some quando só a base sai da vista. */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
