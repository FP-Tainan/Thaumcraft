package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.FluxScrubberBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O {@code TileFluxScrubberRenderer} da 4.2.3.5: o topo do obelisco ({@code obelisk_cap.obj}, peças Cap e Tip) na
 * textura do purificador, virado pela face, com a ponta subindo e descendo devagar.
 */
public class FluxScrubberRenderer implements BlockEntityRenderer<FluxScrubberBlockEntity, FluxScrubberRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/fluxscrubber.png");

    public static class State extends BlockEntityRenderState {
        int facing;
        float q;
    }

    public FluxScrubberRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FluxScrubberBlockEntity te, State state, float partial, Vec3 camera,
                                   @Nullable ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(te, state, partial, camera, crumbling);
        state.facing = te.facing.get3DDataValue();
        var viewer = Minecraft.getInstance().getCameraEntity();
        state.q = (viewer == null ? 0 : viewer.tickCount) + partial + te.count;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        draw(state, pose, collector, state.lightCoords);
    }

    /** O item: o mesmo topo de obelisco, de pé. */
    public static class Item implements net.minecraft.client.renderer.special.SpecialModelRenderer<net.minecraft.util.Unit> {
        @Override
        public void submit(@Nullable net.minecraft.util.Unit ignored, PoseStack pose, SubmitNodeCollector collector, int light, int overlay,
                           boolean foil, int tint) {
            State state = new State();
            state.facing = 1;
            draw(state, pose, collector, light);
        }

        @Override
        public void getExtents(java.util.function.Consumer<org.joml.Vector3fc> extents) {
            extents.accept(new org.joml.Vector3f(0.0f, 0.0f, 0.0f));
            extents.accept(new org.joml.Vector3f(1.0f, 1.0f, 1.0f));
        }

        @Override
        public net.minecraft.util.Unit extractArgument(net.minecraft.world.item.ItemStack stack) {
            return net.minecraft.util.Unit.INSTANCE;
        }
    }

    public record Unbaked() implements net.minecraft.client.renderer.special.SpecialModelRenderer.Unbaked<net.minecraft.util.Unit> {
        public static final com.mojang.serialization.MapCodec<Unbaked> CODEC =
                com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(instance -> instance.point(new Unbaked()));

        @Override
        public net.minecraft.client.renderer.special.SpecialModelRenderer<net.minecraft.util.Unit> bake(
                net.minecraft.client.renderer.special.SpecialModelRenderer.BakingContext context) {
            return new Item();
        }

        @Override
        public com.mojang.serialization.MapCodec<Unbaked> type() {
            return CODEC;
        }
    }

    static void draw(State state, PoseStack pose, SubmitNodeCollector collector, int light) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        switch (state.facing) {
            case 0 -> pose.mulPose(Axis.XP.rotationDegrees(-90.0f));
            case 1 -> pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            case 3 -> pose.mulPose(Axis.YP.rotationDegrees(180.0f));
            case 4 -> pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            case 5 -> pose.mulPose(Axis.YP.rotationDegrees(-90.0f));
            default -> {
            }
        }
        pose.translate(0.0f, 0.0f, -0.5f);
        float[] cap = ObjModel.part("obelisk_cap", "Cap");
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                (m, v) -> ObjMesh.draw(cap, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        float bob = Mth.sin(state.q / 8.0f) * 0.075f + 0.075f;
        pose.translate(0.0f, 0.0f, -bob);
        float[] tip = ObjModel.part("obelisk_cap", "Tip");
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                (m, v) -> ObjMesh.draw(tip, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }
}
