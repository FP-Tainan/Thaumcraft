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
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.BellowsBlock;
import net.thaumcraft.block.entity.BellowsBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O fole: o {@code TileBellowsRenderer} e o {@code ModelBellows} da 4.2.3.5.
 *
 * <p>Três tábuas — a de cima e a de baixo andam com o saco, a do meio fica — e o saco de couro, que estica e
 * encolhe com o sopro; o bico sai da tábua do meio para o lado em que o fole sopra. As caixas e a sequência de
 * giros e deslocamentos são as do original, passo a passo; na mão, o fole respira sozinho, virado para o norte.
 */
public class BellowsRenderer implements BlockEntityRenderer<BellowsBlockEntity, BellowsRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/bellows.png");
    private static final float UNIT = 1.0f / 16.0f;
    // as caixas do ModelBellows, já com o ponto de giro de cada uma somado
    private static final float[] TOP = BoxMesh.box(-6, 8, -6, 12, 2, 12, 0, 0, 128, 64);
    private static final float[] BOTTOM = BoxMesh.box(-6, 22, -6, 12, 2, 12, 0, 0, 128, 64);
    private static final float[] MIDDLE = BoxMesh.box(-6, 15, -6, 12, 2, 12, 0, 0, 128, 64);
    private static final float[] NOZZLE = BoxMesh.box(-2, 14, 6, 4, 4, 2, 0, 36, 128, 64);
    /** O saco, com o ponto de giro que o desenhista põe a meio pixel. */
    private static final float[] BAG = BoxMesh.box(-10, -12.03333f + 0.5f, -10, 20, 24, 20, 48, 0, 128, 64);

    public static class State extends BlockEntityRenderState {
        float inflation;
        int orientation;
    }

    public BellowsRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BellowsBlockEntity bellows, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(bellows, state, crumbling);
        state.inflation = bellows.inflation;
        state.orientation = bellows.getBlockState().getValue(BellowsBlock.FACING).get3DDataValue();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        draw(pose, collector, state.inflation, state.orientation, state.lightCoords, OverlayTexture.NO_OVERLAY);
    }

    /** O {@code renderEntityAt} do original, com o fole na origem do bloco. */
    static void draw(PoseStack pose, SubmitNodeCollector collector, float scale, int orientation, int light, int overlay) {
        float tscale = 0.125f + scale * 0.875f;
        pose.pushPose();
        pose.translate(0.5f, -0.5f, 0.5f);
        switch (orientation) {
            case 0 -> {
                pose.translate(0.0f, 1.0f, -1.0f);
                pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            }
            case 1 -> {
                pose.translate(0.0f, 1.0f, 1.0f);
                pose.mulPose(Axis.XP.rotationDegrees(270.0f));
            }
            case 2 -> pose.mulPose(Axis.YP.rotationDegrees(180.0f));
            case 4 -> pose.mulPose(Axis.YP.rotationDegrees(270.0f));
            case 5 -> pose.mulPose(Axis.YP.rotationDegrees(90.0f));
            default -> {
            }
        }
        pose.translate(0.0f, 1.0f, 0.0f);
        pose.pushPose();
        pose.scale(0.5f, (scale + 0.1f) / 2.0f, 0.5f);
        mesh(pose, collector, BAG, light, overlay);
        pose.popPose();
        pose.translate(0.0f, -1.0f, 0.0f);
        pose.pushPose();
        pose.translate(0.0f, -tscale / 2.0f + 0.5f, 0.0f);
        mesh(pose, collector, TOP, light, overlay);
        pose.popPose();
        pose.pushPose();
        pose.translate(0.0f, tscale / 2.0f - 0.5f, 0.0f);
        mesh(pose, collector, BOTTOM, light, overlay);
        pose.popPose();
        mesh(pose, collector, MIDDLE, light, overlay);
        mesh(pose, collector, NOZZLE, light, overlay);
        pose.popPose();
    }

    private static void mesh(PoseStack pose, SubmitNodeCollector collector, float[] mesh, int light, int overlay) {
        pose.pushPose();
        pose.scale(UNIT, UNIT, UNIT);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                (matrix, consumer) -> MeshDrawer.draw(mesh, matrix, consumer, light, overlay, 0xFFFFFFFF));
        pose.popPose();
    }

    /** O fole na mão e no inventário: o original, sem mundo, faz o saco respirar pelo relógio de quem joga. */
    public static class Item implements SpecialModelRenderer<net.minecraft.util.Unit> {
        @Override
        public void submit(@Nullable net.minecraft.util.Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                           int light, int overlay, boolean foil, int tint) {
            var player = Minecraft.getInstance().player;
            float scale = player == null ? 1.0f : Mth.sin(player.tickCount / 8.0f) * 0.3f + 0.7f;
            draw(pose, collector, scale, Direction.NORTH.get3DDataValue(), light, overlay);
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

    /** O que o arquivo do item declara para pedir este desenhista. */
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
