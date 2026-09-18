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
import net.thaumcraft.block.entity.EssentiaCrystalizerBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O cristalizador de essência: o {@code TileEssentiaCrystalizerRenderer} da 4.2.3.5.
 *
 * <p>O corpo é o {@code crystalizer.obj}, virado para a boca; em volta, quatro cristais do {@code vis_relay.obj}
 * giram e tomam aos poucos a cor do aspecto que está sendo cristalizado, cada um pulsando um pouco de brilho.
 */
public class EssentiaCrystalizerRenderer implements BlockEntityRenderer<EssentiaCrystalizerBlockEntity, EssentiaCrystalizerRenderer.State> {
    private static final Identifier BODY = Thaumcraft.id("textures/models/crystalizer.png");
    private static final Identifier CRYSTAL = Thaumcraft.id("textures/models/vis_relay.png");

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.DOWN;
        float spin;
        float ticks;
        int colour = 0xFFFFFFFF;
    }

    public EssentiaCrystalizerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EssentiaCrystalizerBlockEntity crystalizer, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(crystalizer, state, crumbling);
        state.facing = crystalizer.facing();
        state.spin = crystalizer.spin + crystalizer.spinInc * partial;
        var viewer = Minecraft.getInstance().getCameraEntity();
        state.ticks = (viewer == null ? 0 : viewer.tickCount) + partial;
        state.colour = colour(crystalizer.cr, crystalizer.cg, crystalizer.cb);
    }

    private static int colour(float r, float g, float b) {
        return 0xFF000000 | (int) (Mth.clamp(r, 0.0f, 1.0f) * 255.0f) << 16
                | (int) (Mth.clamp(g, 0.0f, 1.0f) * 255.0f) << 8 | (int) (Mth.clamp(b, 0.0f, 1.0f) * 255.0f);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        draw(pose, collector, state.facing, state.spin, state.ticks, state.colour, state.lightCoords);
    }

    static void draw(PoseStack pose, SubmitNodeCollector collector, Direction facing, float spin, float ticks,
                     int colour, int light) {
        pose.pushPose();
        // o translateFromOrientation do original
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

        float[] body = ObjModel.part("crystalizer", null);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(BODY),
                (m, v) -> ObjMesh.draw(body, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));

        float[] crystal = ObjModel.part("vis_relay", "Crystal");
        for (int q = 0; q < 4; q++) {
            pose.pushPose();
            pose.scale(0.75f, 0.75f, 0.75f);
            float glow = Mth.sin((ticks + q * 10) / 2.0f) * 0.05f + 0.95f;
            // o brilho forçado do original: luz de bloco entre 50 e 200, sem luz do céu
            int bright = 50 + (int) (150.0f * glow);
            pose.mulPose(Axis.ZP.rotationDegrees(90 * q));
            pose.translate(0.34f, 0.0f, 1.2125f);
            pose.mulPose(Axis.ZP.rotationDegrees(spin));
            collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(CRYSTAL),
                    (m, v) -> ObjMesh.draw(crystal, m, v, bright, OverlayTexture.NO_OVERLAY, colour));
            pose.popPose();
        }
        pose.popPose();
    }

    /** O cristalizador na mão e no inventário: de boca para baixo, parado e branco. */
    public static class Item implements SpecialModelRenderer<net.minecraft.util.Unit> {
        @Override
        public void submit(@Nullable net.minecraft.util.Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                           int light, int overlay, boolean foil, int tint) {
            var player = Minecraft.getInstance().player;
            draw(pose, collector, Direction.DOWN, 0.0f, player == null ? 0 : player.tickCount, 0xFFFFFFFF, light);
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
