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
import net.thaumcraft.block.entity.VisRelayBlockEntity;
import net.thaumcraft.block.entity.WorkbenchChargerBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * O relé de vis e o carregador da bancada: o {@code TileVisRelayRenderer} e o {@code TileMagicWorkbenchChargerRenderer}
 * da 4.2.3.5, com o {@code vis_relay.obj}.
 *
 * <p>O relé: a base presa no apoio, o anel solto e o cristal, que pulsa de leve e brilha mais ligado à rede, na cor
 * da afinação. O carregador: o anel com quatro pés virados para baixo, sobre a bancada, e o cristal.
 */
public class VisRelayRenderer<T extends VisRelayBlockEntity> implements BlockEntityRenderer<T, VisRelayRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/vis_relay.png");

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.UP;
        boolean charger;
        boolean connected;
        int colour = -1;
        float ticks;
    }

    public VisRelayRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T relay, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(relay, state, crumbling);
        state.charger = relay instanceof WorkbenchChargerBlockEntity;
        state.facing = state.charger ? Direction.UP : relay.orientation();
        state.connected = relay.parent() != null;
        state.colour = relay.colour;
        var viewer = Minecraft.getInstance().getCameraEntity();
        state.ticks = viewer == null ? partial : viewer.tickCount + partial;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float scale = Mth.sin(state.ticks / 2.0f) * 0.05f + 0.95f;
        int glow = (state.connected ? 50 : 0) + (int) (150.0f * scale);
        if (state.charger) charger(pose, collector, state.colour, glow, state.lightCoords);
        else relay(pose, collector, state.facing, state.colour, glow, state.lightCoords);
    }

    /** O {@code translateFromOrientation} e o relé. */
    static void relay(PoseStack pose, SubmitNodeCollector collector, Direction facing, int colour, int glow, int light) {
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
        pose.mulPose(Axis.XP.rotationDegrees(180.0f));
        pose.mulPose(Axis.ZP.rotationDegrees(45.0f));
        pose.pushPose();
        pose.scale(0.75f, 0.75f, 0.75f);
        pose.translate(0.0f, 0.0f, -0.16f);
        part(pose, collector, "RingBase", light);
        pose.popPose();
        part(pose, collector, "RingFloat", light);
        crystal(pose, collector, colour, glow);
        pose.popPose();
    }

    /** O carregador: o anel, os quatro pés e o cristal. */
    static void charger(PoseStack pose, SubmitNodeCollector collector, int colour, int glow, int light) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.XP.rotationDegrees(-90.0f));
        pose.mulPose(Axis.XP.rotationDegrees(180.0f));
        pose.mulPose(Axis.ZP.rotationDegrees(45.0f));
        part(pose, collector, "RingFloat", light);
        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(180.0f));
        pose.translate(0.0f, 0.0f, 0.5f);
        for (int a = 0; a < 4; a++) {
            part(pose, collector, "Support", light);
            pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
        }
        pose.popPose();
        crystal(pose, collector, colour, glow);
        pose.popPose();
    }

    private static void part(PoseStack pose, SubmitNodeCollector collector, String name, int light) {
        float[] mesh = ObjModel.part("vis_relay", name);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE), (m, c) -> ObjMesh.draw(mesh, m, c, light, OverlayTexture.NO_OVERLAY, -1));
    }

    /** O cristal, misturando como vidro; afinado, na cor da afinação (a conta do original, sobre duzentos). */
    private static void crystal(PoseStack pose, SubmitNodeCollector collector, int colour, int glow) {
        int tint = -1;
        if (colour >= 0 && colour < VisRelayBlockEntity.COLOURS.length) {
            int c = VisRelayBlockEntity.COLOURS[colour];
            int r = Math.min(255, (c >> 16 & 255) * 255 / 200), g = Math.min(255, (c >> 8 & 255) * 255 / 200), b = Math.min(255, (c & 255) * 255 / 200);
            tint = 0xFF000000 | r << 16 | g << 8 | b;
        }
        float[] mesh = ObjModel.part("vis_relay", "Crystal");
        int shade = tint;
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(TEXTURE), (m, c) -> ObjMesh.draw(mesh, m, c, glow, OverlayTexture.NO_OVERLAY, shade));
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    /** O relé e o carregador na mão e no inventário. */
    public static class Item implements SpecialModelRenderer<net.minecraft.util.Unit> {
        private final boolean charger;

        Item(boolean charger) {
            this.charger = charger;
        }

        @Override
        public void submit(@Nullable net.minecraft.util.Unit ignored, PoseStack pose, SubmitNodeCollector collector, int light, int overlay,
                           boolean foil, int tint) {
            if (this.charger) charger(pose, collector, -1, light, light);
            else relay(pose, collector, Direction.UP, -1, light, light);
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

    public record Unbaked(boolean charger) implements SpecialModelRenderer.Unbaked<net.minecraft.util.Unit> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                com.mojang.serialization.Codec.BOOL.optionalFieldOf("charger", false).forGetter(Unbaked::charger)).apply(instance, Unbaked::new));

        @Override
        public SpecialModelRenderer<net.minecraft.util.Unit> bake(SpecialModelRenderer.BakingContext context) {
            return new Item(this.charger);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
