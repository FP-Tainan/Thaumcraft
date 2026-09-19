package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.NodeConverterBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O transdutor de nó: o {@code TileNodeConverterRenderer} da 4.2.3.5 — o {@code node_stabilizer.obj} de cabeça para
 * baixo, com a {@code node_converter.png}. O brilho da {@code node_converter_over.png} é verde parado, laranja
 * energizando e vermelho segurando um nó energizado, e os pistões descem conforme o trabalho avança.
 */
public class NodeConverterRenderer implements BlockEntityRenderer<NodeConverterBlockEntity, NodeConverterRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/node_converter.png");
    private static final Identifier OVER = Thaumcraft.id("textures/models/node_converter_over.png");

    public static class State extends BlockEntityRenderState {
        int count;
        int status;
        float ticks;
    }

    public NodeConverterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(NodeConverterBlockEntity converter, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(converter, state, crumbling);
        state.count = Math.max(0, converter.count);
        state.status = converter.status;
        var viewer = Minecraft.getInstance().getCameraEntity();
        state.ticks = viewer == null ? 0 : viewer.tickCount + partial;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        draw(pose, collector, state.count, state.status, state.ticks, state.lightCoords, true);
    }

    static void draw(PoseStack pose, SubmitNodeCollector collector, int count, int status, float ticks, int light, boolean world) {
        float[] base = ObjModel.part("node_stabilizer", "lock");
        float[] piston = ObjModel.part("node_stabilizer", "piston");
        int tint = status == 2 ? 0xFFFF004C : status == 1 ? 0xFFFF9919 : 0xFF7FFF7F;
        float v = Math.min(50, count) / 137.0f;
        pose.pushPose();
        pose.translate(0.5f, 1.0f, 0.5f);
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE), (m, c) -> ObjMesh.draw(base, m, c, light, OverlayTexture.NO_OVERLAY, -1));
        int glow = world ? 50 + (int) (170.0f * (v * 2.5f * (Mth.sin(ticks / 3.0f) * 0.1f + 0.9f))) : light;
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(OVER), (m, c) -> ObjMesh.draw(base, m, c, glow, OverlayTexture.NO_OVERLAY, tint));
        for (int a = 0; a < 4; a++) {
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(90 * a));
            pose.mulPose(Axis.YP.rotationDegrees(45.0f));
            pose.translate(0.0f, 0.0f, v);
            collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE), (m, c) -> ObjMesh.draw(piston, m, c, light, OverlayTexture.NO_OVERLAY, -1));
            int pistonGlow = world ? 50 + (int) (170.0f * (v * 2.5f * (Mth.sin((ticks + a * 5) / 3.0f) * 0.1f + 0.9f))) : light;
            collector.submitCustomGeometry(pose, RenderTypes.entityCutout(OVER), (m, c) -> ObjMesh.draw(piston, m, c, pistonGlow, OverlayTexture.NO_OVERLAY, tint));
            pose.popPose();
        }
        pose.popPose();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    /** O transdutor na mão e no inventário. */
    public static class Item implements SpecialModelRenderer<net.minecraft.util.Unit> {
        @Override
        public void submit(@Nullable net.minecraft.util.Unit ignored, PoseStack pose, SubmitNodeCollector collector, int light, int overlay,
                           boolean foil, int tint) {
            draw(pose, collector, 0, 0, 0, light, false);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> extents) {
            extents.accept(new Vector3f(0.0f, 0.0f, 0.0f));
            extents.accept(new Vector3f(1.0f, 1.0f, 1.0f));
        }

        @Override
        public net.minecraft.util.Unit extractArgument(ItemStack stack) {
            return net.minecraft.util.Unit.INSTANCE;
        }
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<net.minecraft.util.Unit> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.point(new Unbaked()));

        @Override
        public SpecialModelRenderer<net.minecraft.util.Unit> bake(SpecialModelRenderer.BakingContext context) {
            return new Item();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
