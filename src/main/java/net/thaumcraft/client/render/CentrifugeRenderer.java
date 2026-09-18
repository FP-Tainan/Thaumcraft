package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.CentrifugeBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * A centrífuga alquímica: o {@code TileCentrifugeRenderer} e o {@code ModelCentrifuge} da 4.2.3.5.
 *
 * <p>As duas tampas ficam paradas; o eixo, a travessa e os dois pesos giram no ângulo que a centrífuga guarda.
 */
public class CentrifugeRenderer implements BlockEntityRenderer<CentrifugeBlockEntity, CentrifugeRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/centrifuge.png");
    private static final float[] BOXES = BoxMesh.join(
            BoxMesh.mirror(BoxMesh.box(-4, -8, -4, 8, 4, 8, 20, 16, 64, 32)),
            BoxMesh.mirror(BoxMesh.box(-4, 4, -4, 8, 4, 8, 20, 16, 64, 32)));
    private static final float[] SPINNY = BoxMesh.join(
            BoxMesh.mirror(BoxMesh.box(-4, -1, -1, 8, 2, 2, 16, 0, 64, 32)),
            BoxMesh.mirror(BoxMesh.box(4, -3, -2, 4, 6, 4, 0, 16, 64, 32)),
            BoxMesh.mirror(BoxMesh.box(-8, -3, -2, 4, 6, 4, 0, 16, 64, 32)),
            BoxMesh.mirror(BoxMesh.box(-1.5f, -4, -1.5f, 3, 8, 3, 0, 0, 64, 32)));

    public static class State extends BlockEntityRenderState {
        float rotation;
    }

    public CentrifugeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CentrifugeBlockEntity centrifuge, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(centrifuge, state, crumbling);
        state.rotation = centrifuge.rotation;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        draw(pose, collector, state.rotation, state.lightCoords);
    }

    static void draw(PoseStack pose, SubmitNodeCollector collector, float rotation, int light) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                (m, v) -> MeshDrawer.draw(BOXES, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.mulPose(Axis.YP.rotationDegrees(rotation));
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                (m, v) -> MeshDrawer.draw(SPINNY, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }

    /** A centrífuga na mão e no inventário, parada. */
    public static class Item implements SpecialModelRenderer<net.minecraft.util.Unit> {
        @Override
        public void submit(@Nullable net.minecraft.util.Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                           int light, int overlay, boolean foil, int tint) {
            draw(pose, collector, 0.0f, light);
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
