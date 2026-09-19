package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.eldritch.StrangeCrystalBlock;
import net.thaumcraft.block.entity.eldritch.StrangeCrystalBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Os cristais estranhos: o {@code TileEldritchCrystalRenderer} da 4.2.3.5. O {@code vcrystal.obj}: a base com a textura da
 * pedra incrustada, e os cristais por cima a setenta por cento, acesos e pulsando devagar; virados para o lado em que
 * crescem, e cada um girado de um quarto de volta ao acaso.
 */
public class StrangeCrystalRenderer implements BlockEntityRenderer<StrangeCrystalBlockEntity, StrangeCrystalRenderer.State> {
    private static final Identifier CRUST = Thaumcraft.id("textures/block/crust.png");
    private static final Identifier CRYSTAL = Thaumcraft.id("textures/models/vcrystal.png");

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.UP;
        int turn;
    }

    public StrangeCrystalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(StrangeCrystalBlockEntity te, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(te, state, partial, camera, crumbling);
        state.facing = te.getBlockState().getValue(StrangeCrystalBlock.FACING);
        state.turn = Math.floorMod(te.getBlockPos().hashCode(), 4);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
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
        pose.translate(0.0f, 0.0f, -0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(90.0f * state.turn));
        int light = state.lightCoords;
        float[] base = ObjModel.part("vcrystal", "Base");
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(CRUST),
                (m, v) -> ObjMesh.draw(base, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        var player = Minecraft.getInstance().player;
        float shade = Mth.sin((player == null ? 0 : player.tickCount) / 6.0f) * 0.075f + 0.925f;
        int glow = (int) (210.0f * shade);
        float[] crystal = ObjModel.part("vcrystal", "Crystal");
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(CRYSTAL),
                (m, v) -> ObjMesh.draw(crystal, m, v, glow, OverlayTexture.NO_OVERLAY, 0xB3FFFFFF));
        pose.popPose();
    }
}
