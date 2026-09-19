package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.eldritch.CrabSpawnerBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O respiradouro da abertura incrustada: o {@code TileEldritchCrabSpawnerRenderer} da 4.2.3.5. O {@code crabvent.obj}
 * inteiro, com a {@code crabvent.png}, na face para onde a abertura está virada (o modelo nasce na face sul).
 */
public class CrabVentRenderer implements BlockEntityRenderer<CrabSpawnerBlockEntity, CrabVentRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/crabvent.png");

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.DOWN;
    }

    public CrabVentRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CrabSpawnerBlockEntity te, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(te, state, partial, camera, crumbling);
        state.facing = te.getFacing();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        switch (state.facing) {
            case DOWN -> pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            case UP -> pose.mulPose(Axis.XP.rotationDegrees(-90.0f));
            case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(180.0f));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(-90.0f));
            case EAST -> pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            default -> {
            }
        }
        float[] vent = ObjModel.part("crabvent", null);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                (m, v) -> ObjMesh.draw(vent, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }
}
