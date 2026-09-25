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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.ArcaneBoreBaseBlockEntity;
import net.thaumcraft.block.entity.ArcaneBoreBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * A broca arcana e a base dela: o {@code TileArcaneBoreRenderer} e o {@code TileArcaneBoreBaseRenderer} da 4.2.3.5,
 * com o {@code ModelBore}, o {@code ModelBoreEmit}, o {@code ModelBoreBase} e o miolo do {@code ModelJar}, tudo na
 * {@code Bore.png} (folha de 128 por 64).
 *
 * <p>A broca gira o corpo (base, laterais e travessa) em torno do eixo do chão, inclina o bico e o emissor para onde
 * cava, e o emissor gira quando trabalha. No meio, três redemoinhos girando e um vidro de jarro por cima.
 */
public final class ArcaneBoreRenderers {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/bore.png");
    private static final Identifier VORTEX = Thaumcraft.id("textures/misc/vortex.png");
    private static final Identifier JAR = Thaumcraft.id("textures/models/jar.png");

    // o ModelBore: base, laterais e travessa; o bico (meio e frente), com o ponto de giro em y 8
    private static final float[] BORE_BASE = BoxMesh.join(
            BoxMesh.box(-6, 0, -6, 12, 2, 12, 0, 32, 128, 64),
            BoxMesh.box(-2, 2, -5.5f, 4, 8, 1, 0, 0, 128, 64),
            BoxMesh.box(-2, 2, 4.5f, 4, 8, 1, 0, 0, 128, 64),
            BoxMesh.box(-1, 7, -6, 2, 2, 12, 0, 48, 128, 64));
    private static final float[] BORE_NOZZLE = BoxMesh.join(
            BoxMesh.box(4, 5.5f, -2.5f, 4, 5, 5, 30, 14, 128, 64),
            BoxMesh.box(-2, 4, -4, 6, 8, 8, 0, 14, 128, 64));
    // o ModelBoreEmit: a bola (só com foco), as três cruzetas e a haste
    private static final float[] EMIT_KNOB = BoxMesh.box(-2, 12, -2, 4, 4, 4, 66, 0, 128, 64);
    private static final float[] EMIT = BoxMesh.join(
            BoxMesh.box(-2, 8, -2, 4, 1, 4, 56, 16, 128, 64),
            BoxMesh.box(-2, 0, -2, 4, 1, 4, 56, 16, 128, 64),
            BoxMesh.box(-3, 4, -3, 6, 1, 6, 56, 24, 128, 64),
            BoxMesh.box(-1, 1, -1, 2, 11, 2, 56, 0, 128, 64));
    // o miolo do ModelJar
    private static final float[] JAR_CORE = BoxMesh.box(-5, -12, -5, 10, 12, 10, 0, 0, 64, 32);
    // o ModelBoreBase: as duas chapas e as cinco colunas; o bico de lado
    private static final float[] BASE = BoxMesh.join(
            BoxMesh.box(-8, 0, -8, 16, 2, 16, 64, 24, 128, 64),
            BoxMesh.box(-8, 14, -8, 16, 2, 16, 64, 24, 128, 64),
            BoxMesh.box(-2.5f, 2, -2.5f, 5, 12, 5, 84, 42, 128, 64),
            BoxMesh.box(-7, 2, -7, 4, 12, 4, 64, 42, 128, 64),
            BoxMesh.box(-7, 2, 3, 4, 12, 4, 64, 42, 128, 64),
            BoxMesh.box(3, 2, 3, 4, 12, 4, 64, 42, 128, 64),
            BoxMesh.box(3, 2, -7, 4, 12, 4, 64, 42, 128, 64));
    private static final float[] BASE_NOZZLE = BoxMesh.join(
            BoxMesh.box(2.5f, 6, -2, 5, 4, 4, 106, 42, 128, 64),
            BoxMesh.box(7, 5.5f, -2.5f, 1, 5, 5, 106, 51, 128, 64));

    private ArcaneBoreRenderers() {
    }

    private static void mesh(PoseStack pose, SubmitNodeCollector collector, float[] mesh, int light) {
        pose.pushPose();
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                (m, v) -> MeshDrawer.draw(mesh, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }

    /** O {@code renderQuadCenteredFromTexture}: um quadrado centrado, do lado dado, misturando como vidro. */
    private static void vortex(PoseStack pose, SubmitNodeCollector collector, float scale, float alpha) {
        int colour = (int) (alpha * 255.0f) << 24 | 0xFFFFFF;
        float h = scale / 2.0f;
        collector.submitCustomGeometry(pose, AdditiveGlow.blended(VORTEX), (m, v) -> {
            v.addVertex(m, -h, h, 0).setColor(colour).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xC000C0).setNormal(m, 0, 0, 1);
            v.addVertex(m, h, h, 0).setColor(colour).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xC000C0).setNormal(m, 0, 0, 1);
            v.addVertex(m, h, -h, 0).setColor(colour).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xC000C0).setNormal(m, 0, 0, 1);
            v.addVertex(m, -h, -h, 0).setColor(colour).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xC000C0).setNormal(m, 0, 0, 1);
        });
    }

    // ----------------------------------------------------------------- a broca

    public static class BoreState extends BlockEntityRenderState {
        float yaw, tilt, top, ticks;
        boolean baseBelow = true, focus;
    }

    public static class Bore implements BlockEntityRenderer<ArcaneBoreBlockEntity, BoreState> {
        public Bore(BlockEntityRendererProvider.Context context) {
        }

        @Override
        public BoreState createRenderState() {
            return new BoreState();
        }

        @Override
        public void extractRenderState(ArcaneBoreBlockEntity bore, BoreState state, float partial, Vec3 camera,
                                       ModelFeatureRenderer.CrumblingOverlay crumbling) {
            BlockEntityRenderState.extractBase(bore, state, crumbling);
            state.yaw = bore.rotX - bore.vRadX + partial * bore.speedX;
            state.tilt = bore.rotZ - bore.vRadZ + partial * bore.speedZ;
            state.top = bore.topRotation;
            state.baseBelow = bore.baseOrientation() != Direction.DOWN;
            state.focus = bore.hasFocus;
            var viewer = Minecraft.getInstance().getCameraEntity();
            state.ticks = (viewer == null ? 0 : viewer.tickCount % 45) + partial;
        }

        @Override
        public void submit(BoreState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            draw(pose, collector, state, state.lightCoords);
        }

        static void draw(PoseStack pose, SubmitNodeCollector collector, BoreState state, int light) {
            pose.pushPose();
            pose.translate(0.5f, 0.5f, 0.5f);
            pose.mulPose(Axis.YP.rotationDegrees(state.yaw));
            pose.pushPose();
            if (!state.baseBelow) pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
            pose.translate(0.0f, -0.5f, 0.0f);
            mesh(pose, collector, BORE_BASE, light);
            pose.popPose();
            pose.mulPose(Axis.ZP.rotationDegrees(state.tilt));
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
            pose.translate(0.0f, -0.5f, 0.0f);
            mesh(pose, collector, BORE_NOZZLE, light);
            pose.popPose();
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(state.top));
            pose.translate(0.0f, 0.5f, 0.0f);
            mesh(pose, collector, state.focus ? BoxMesh.join(EMIT, EMIT_KNOB) : EMIT, light);
            pose.popPose();
            float[][] vortices = {{-0.17f, -1, -10, 0.4f, 1.0f}, {-0.21f, 1, 10, 0.3f, 0.8f}, {-0.25f, -1, -10, 0.2f, 0.8f}};
            float[] tilts = {10.0f, 10.0f, -10.0f};
            for (int i = 0; i < 3; i++) {
                float[] vo = vortices[i];
                pose.pushPose();
                pose.translate(0.0f, vo[0], 0.0f);
                pose.mulPose(Axis.XP.rotationDegrees(-90.0f));
                pose.mulPose(Axis.ZP.rotationDegrees(vo[1] * state.ticks * 8.0f));
                pose.mulPose(Axis.YP.rotationDegrees(tilts[i]));
                vortex(pose, collector, vo[3], vo[4]);
                pose.popPose();
            }
            pose.pushPose();
            pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
            pose.translate(0.0f, 0.3f, 0.0f);
            pose.scale(0.6f / 16.0f, 0.6f / 16.0f, 0.6f / 16.0f);
            collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(JAR),
                    (m, v) -> MeshDrawer.draw(JAR_CORE, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
            pose.popPose();
            pose.popPose();
        }
    }

    // ----------------------------------------------------------------- a base

    public static class BaseState extends BlockEntityRenderState {
        Direction facing = Direction.EAST;
    }

    public static class Base implements BlockEntityRenderer<ArcaneBoreBaseBlockEntity, BaseState> {
        public Base(BlockEntityRendererProvider.Context context) {
        }

        @Override
        public BaseState createRenderState() {
            return new BaseState();
        }

        @Override
        public void extractRenderState(ArcaneBoreBaseBlockEntity base, BaseState state, float partial, Vec3 camera,
                                       ModelFeatureRenderer.CrumblingOverlay crumbling) {
            BlockEntityRenderState.extractBase(base, state, crumbling);
            state.facing = base.orientation();
        }

        @Override
        public void submit(BaseState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            draw(pose, collector, state.facing, state.lightCoords);
        }

        static void draw(PoseStack pose, SubmitNodeCollector collector, Direction facing, int light) {
            pose.pushPose();
            pose.translate(0.5f, 0.0f, 0.5f);
            mesh(pose, collector, BASE, light);
            switch (facing) {
                case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(90.0f));
                case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(270.0f));
                case WEST -> pose.mulPose(Axis.YP.rotationDegrees(180.0f));
                default -> {
                }
            }
            mesh(pose, collector, BASE_NOZZLE, light);
            pose.popPose();
        }
    }

    // ----------------------------------------------------------------- na mão

    /** A base e a broca no inventário: o {@code BlockWoodenDeviceRenderer} desenha as peças paradas. */
    public record ItemRenderer(boolean bore) implements SpecialModelRenderer<net.minecraft.util.Unit> {
        @Override
        public void submit(@Nullable net.minecraft.util.Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                           int light, int overlay, boolean foil, int tint) {
            if (this.bore) {
                BoreState state = new BoreState();
                state.tilt = 0.0f;
                Bore.draw(pose, collector, state, light);
            } else {
                Base.draw(pose, collector, Direction.EAST, light);
            }
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

    public record Unbaked(boolean bore) implements SpecialModelRenderer.Unbaked<net.minecraft.util.Unit> {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                com.mojang.serialization.Codec.BOOL.fieldOf("bore").forGetter(Unbaked::bore)).apply(instance, Unbaked::new));

        @Override
        public SpecialModelRenderer<net.minecraft.util.Unit> bake(SpecialModelRenderer.BakingContext context) {
            return new ItemRenderer(this.bore);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
