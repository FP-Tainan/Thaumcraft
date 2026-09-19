package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.EssentiaReservoirBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O reservatório de essência: o {@code TileEssentiaReservoirRenderer} da 4.2.3.5. O bocal ({@code reservoir.obj}) virado
 * para o lado do cano, e o líquido dentro da caixa (de 3/16 a 13/16, subindo até 10/16 com o quanto está cheio), na
 * cor que vai passando pelos aspectos guardados, quase opaco e aceso.
 */
public class EssentiaReservoirRenderer implements BlockEntityRenderer<EssentiaReservoirBlockEntity, EssentiaReservoirRenderer.State> {
    private static final Identifier BODY = Thaumcraft.id("textures/models/reservoir.png");
    private static final Identifier LIQUID = Thaumcraft.id("textures/misc/essentia.png");
    private static final float W3 = 3.0f / 16.0f, W10 = 10.0f / 16.0f, W13 = 13.0f / 16.0f;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.DOWN;
        float level;
        int colour;
        boolean liquid;
    }

    public EssentiaReservoirRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EssentiaReservoirBlockEntity te, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(te, state, crumbling);
        state.facing = te.facing();
        state.liquid = te.displayAspect != null && te.essentia.visSize() != 0;
        state.level = (float) te.essentia.visSize() / te.maxAmount;
        state.colour = (int) (0.9f * 255) << 24 | channel(te.cr) << 16 | channel(te.cg) << 8 | channel(te.cb);
    }

    private static int channel(float f) {
        return Math.max(0, Math.min(255, (int) (f * 255)));
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        drawBody(pose, collector, state.facing, state.lightCoords);
        if (state.liquid) drawLiquid(pose, collector, state.level, state.colour);
    }

    /** O {@code translateFromOrientation} e o modelo inteiro. */
    static void drawBody(PoseStack pose, SubmitNodeCollector collector, Direction facing, int light) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        switch (facing) {
            case DOWN -> pose.mulPose(Axis.XP.rotationDegrees(-90.0f));
            case UP -> pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(180.0f));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            case EAST -> pose.mulPose(Axis.YP.rotationDegrees(-90.0f));
            default -> {
            }
        }
        pose.translate(0.0f, 0.0f, -0.5f);
        float[] body = ObjModel.part("reservoir", null);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(BODY),
                (m, v) -> ObjMesh.draw(body, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }

    /** O {@code renderLiquid}: as seis faces da caixa do líquido, com brilho 200. */
    private static void drawLiquid(PoseStack pose, SubmitNodeCollector collector, float level, int colour) {
        float lo = W3, hi = W13, top = W3 + W10 * level;
        int light = 200;
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(LIQUID), (m, c) -> {
            // em cima e embaixo
            face(m, c, colour, light, 0, 1, 0, lo, top, lo, lo, top, hi, hi, top, hi, hi, top, lo);
            face(m, c, colour, light, 0, -1, 0, lo, lo, lo, hi, lo, lo, hi, lo, hi, lo, lo, hi);
            // os quatro lados
            face(m, c, colour, light, 0, 0, -1, lo, lo, lo, lo, top, lo, hi, top, lo, hi, lo, lo);
            face(m, c, colour, light, 0, 0, 1, hi, lo, hi, hi, top, hi, lo, top, hi, lo, lo, hi);
            face(m, c, colour, light, -1, 0, 0, lo, lo, hi, lo, top, hi, lo, top, lo, lo, lo, lo);
            face(m, c, colour, light, 1, 0, 0, hi, lo, lo, hi, top, lo, hi, top, hi, hi, lo, hi);
        });
    }

    private static void face(PoseStack.Pose m, VertexConsumer c, int colour, int light, float nx, float ny, float nz,
                             float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3) {
        c.addVertex(m, x0, y0, z0).setColor(colour).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, nx, ny, nz);
        c.addVertex(m, x1, y1, z1).setColor(colour).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, nx, ny, nz);
        c.addVertex(m, x2, y2, z2).setColor(colour).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, nx, ny, nz);
        c.addVertex(m, x3, y3, z3).setColor(colour).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, nx, ny, nz);
    }

    /** No inventário: o bocal para baixo, como o {@code renderInventoryBlock} com um reservatório novo. */
    public static class Item implements SpecialModelRenderer<net.minecraft.util.Unit> {
        @Override
        public void submit(@Nullable net.minecraft.util.Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                           int light, int overlay, boolean foil, int tint) {
            drawBody(pose, collector, Direction.DOWN, light);
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
