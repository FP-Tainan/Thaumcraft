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
import net.thaumcraft.block.NodeStabilizerBlock;
import net.thaumcraft.block.entity.NodeStabilizerBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O estabilizador de nó: o {@code TileNodeStabilizerRenderer} da 4.2.3.5, com o {@code node_stabilizer.obj}.
 *
 * <p>A base ({@code lock}) fica parada; os quatro pistões se abrem conforme o estabilizador segura um nó, com o
 * brilho da {@code node_stabilizer_over.png} pulsando — vermelho no avançado. Segurando, uma bolha
 * ({@code node_bubble.png}) envolve o nó de cima, branca no comum e vermelha no avançado.
 */
public class NodeStabilizerRenderer implements BlockEntityRenderer<NodeStabilizerBlockEntity, NodeStabilizerRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/node_stabilizer.png");
    private static final Identifier OVER = Thaumcraft.id("textures/models/node_stabilizer_over.png");
    private static final Identifier BUBBLE = Thaumcraft.id("textures/misc/node_bubble.png");

    public static class State extends BlockEntityRenderState {
        int count;
        int lock = 1;
        float ticks;
    }

    public NodeStabilizerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(NodeStabilizerBlockEntity stabilizer, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(stabilizer, state, crumbling);
        state.count = stabilizer.count;
        state.lock = stabilizer.getBlockState().getBlock() instanceof NodeStabilizerBlock block ? block.lock() : 1;
        var viewer = Minecraft.getInstance().getCameraEntity();
        state.ticks = viewer == null ? 0 : viewer.tickCount + partial;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        draw(pose, collector, state.count, state.lock, state.ticks, state.lightCoords, true);
        if (state.count <= 0) return;
        float alpha = Mth.sin(state.ticks / 8.0f) * 0.1f + 0.5f;
        FacingQuad.draw(pose, collector, camera, AdditiveGlow.of(BUBBLE), new Vec3(0.5, 1.5, 0.5), 0.9f,
                state.count / 37.0f * alpha, 1, 0, state.lock == 1 ? 0xFFFFFF : 0xFF4444);
    }

    /** A base e os pistões. No inventário ({@code world = false}) os pistões ficam fechados e sem brilho próprio. */
    static void draw(PoseStack pose, SubmitNodeCollector collector, int count, int lock, float ticks, int light, boolean world) {
        float[] base = ObjModel.part("node_stabilizer", "lock");
        float[] piston = ObjModel.part("node_stabilizer", "piston");
        pose.pushPose();
        pose.translate(0.5f, 0.0f, 0.5f);
        pose.mulPose(Axis.XP.rotationDegrees(-90.0f));
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE), (m, c) -> ObjMesh.draw(base, m, c, light, OverlayTexture.NO_OVERLAY, -1));
        for (int a = 0; a < 4; a++) {
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(90 * a));
            pose.mulPose(Axis.YP.rotationDegrees(45.0f));
            pose.translate(0.0f, 0.0f, count / 100.0f);
            collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE), (m, c) -> ObjMesh.draw(piston, m, c, light, OverlayTexture.NO_OVERLAY, -1));
            int glow = light;
            if (world) {
                float scale = Mth.sin((ticks + a * 5) / 3.0f) * 0.1f + 0.9f;
                glow = 50 + (int) (170.0f * (count / 37.0f * scale));
            }
            int tint = lock == 2 ? 0xFFFF3333 : 0xFFFFFFFF; // o glColor4f(1, 0.2, 0.2) do avançado
            int packed = glow;
            collector.submitCustomGeometry(pose, RenderTypes.entityCutout(OVER), (m, c) -> ObjMesh.draw(piston, m, c, packed, OverlayTexture.NO_OVERLAY, tint));
            pose.popPose();
        }
        pose.popPose();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    /** O estabilizador na mão e no inventário. */
    public static class Item implements SpecialModelRenderer<Integer> {
        private final int lock;

        Item(int lock) {
            this.lock = lock;
        }

        @Override
        public void submit(@Nullable Integer ignored, PoseStack pose, SubmitNodeCollector collector, int light, int overlay, boolean foil, int tint) {
            draw(pose, collector, 0, this.lock, 0, light, false);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> extents) {
            extents.accept(new Vector3f(0.0f, 0.0f, 0.0f));
            extents.accept(new Vector3f(1.0f, 1.0f, 1.0f));
        }

        @Override
        public Integer extractArgument(ItemStack stack) {
            return this.lock;
        }
    }

    public record Unbaked(int lock) implements SpecialModelRenderer.Unbaked<Integer> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                com.mojang.serialization.Codec.INT.optionalFieldOf("lock", 1).forGetter(Unbaked::lock)).apply(instance, Unbaked::new));

        @Override
        public SpecialModelRenderer<Integer> bake(SpecialModelRenderer.BakingContext context) {
            return new Item(this.lock);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
