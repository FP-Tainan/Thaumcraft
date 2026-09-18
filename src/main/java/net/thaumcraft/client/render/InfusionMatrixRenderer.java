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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.InfusionMatrixBlockEntity;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Consumer;

/**
 * A matriz rúnica: o {@code TileRunicMatrixRenderer} da 4.2.3.5.
 *
 * <p>Oito cubos de pedra entalhada, dois por dois por dois. Parada, é um bloco quase inteiro com as frestas das
 * runas; ligada, ela se inclina, gira e os cubos tremem conforme a instabilidade, com um brilho roxo por cima de
 * cada um. Durante a infusão, um halo de raios brancos e roxos cresce à volta dela.
 */
public class InfusionMatrixRenderer implements BlockEntityRenderer<InfusionMatrixBlockEntity, InfusionMatrixRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/infuser.png");
    /** O {@code ModelCube(0)} e o {@code ModelCube(32)}: a caixa de 16 na folha de 64 por 64, espelhada. */
    private static final float[] CUBE = BoxMesh.mirror(BoxMesh.box(-8, -8, -8, 16, 16, 16, 0, 0, 64, 64));
    private static final float[] CUBE_OVER = BoxMesh.mirror(BoxMesh.box(-8, -8, -8, 16, 16, 16, 0, 32, 64, 64));

    public static class State extends BlockEntityRenderState {
        float ticks;
        float startUp;
        boolean active;
        boolean crafting;
        int instability;
        int craftCount;
    }

    public InfusionMatrixRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(InfusionMatrixBlockEntity matrix, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(matrix, state, crumbling);
        var viewer = net.minecraft.client.Minecraft.getInstance().getCameraEntity();
        state.ticks = (viewer == null ? 0 : viewer.tickCount) + partial;
        state.startUp = matrix.startUp;
        state.active = matrix.isActive();
        state.crafting = matrix.isCrafting();
        state.instability = matrix.instability();
        state.craftCount = matrix.craftCount;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        draw(pose, collector, state.ticks, state.startUp, state.active, state.instability, state.craftCount,
                state.lightCoords);
        pose.popPose();
        if (state.crafting) halo(pose, collector, state.craftCount);
    }

    /** Os oito cubos, já no meio do bloco. */
    static void draw(PoseStack pose, SubmitNodeCollector collector, float ticks, float startUp, boolean active,
                     int instabilityLevel, int craftCount, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(ticks % 360.0f * startUp));
        pose.mulPose(Axis.XP.rotationDegrees(35.0f * startUp));
        pose.mulPose(Axis.ZP.rotationDegrees(45.0f * startUp));
        float instability = Math.min(6.0f, 1.0f + instabilityLevel * 0.66f * (Math.min(craftCount, 50) / 50.0f));

        for (int pass = 0; pass < (active ? 2 : 1); pass++) {
            for (int a = 0; a < 2; a++) {
                for (int b = 0; b < 2; b++) {
                    for (int c = 0; c < 2; c++) {
                        float b1 = 0.0f, b2 = 0.0f, b3 = 0.0f;
                        if (active) {
                            b1 = Mth.sin((ticks + a * 10) / (15.0f - instability / 2.0f)) * 0.01f * startUp * instability;
                            b2 = Mth.sin((ticks + b * 10) / (14.0f - instability / 2.0f)) * 0.01f * startUp * instability;
                            b3 = Mth.sin((ticks + c * 10) / (13.0f - instability / 2.0f)) * 0.01f * startUp * instability;
                        }
                        pose.pushPose();
                        pose.translate(b1 + (a == 0 ? -0.25f : 0.25f), b2 + (b == 0 ? -0.25f : 0.25f),
                                c == 0 ? b3 - 0.25f : b3 + 0.25f);
                        if (a > 0) pose.mulPose(Axis.XP.rotationDegrees(90.0f));
                        if (b > 0) pose.mulPose(Axis.YP.rotationDegrees(90.0f));
                        if (c > 0) pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
                        pose.scale(0.45f / 16.0f, 0.45f / 16.0f, 0.45f / 16.0f);
                        if (pass == 0) {
                            collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                                    (m, v) -> MeshDrawer.draw(CUBE, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
                        } else {
                            // o brilho roxo por cima, somando luz, cada cubo no seu compasso
                            float alpha = (Mth.sin((ticks + a * 2 + b * 3 + c * 4) / 4.0f) * 0.1f + 0.2f) * startUp;
                            int colour = (int) (Mth.clamp(alpha, 0.0f, 1.0f) * 255.0f) << 24 | 0xCC19FF;
                            collector.submitCustomGeometry(pose, AdditiveGlow.of(TEXTURE),
                                    (m, v) -> MeshDrawer.draw(CUBE_OVER, m, v, 0xF000F0, OverlayTexture.NO_OVERLAY, colour));
                        }
                        pose.popPose();
                    }
                }
            }
        }
        pose.popPose();
    }

    /** O {@code drawHalo}: vinte leques de raios, brancos no meio e roxos sumindo nas pontas. */
    private static void halo(PoseStack pose, SubmitNodeCollector collector, int count) {
        if (count <= 0) return;
        float grow = Math.min(count, 50) / 50.0f;
        float spin = count / 500.0f;
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        Random random = new Random(245L);
        for (int i = 0; i < 20; i++) {
            pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
            pose.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f + spin * 360.0f));
            float l = (random.nextFloat() * 20.0f + 5.0f) / (20.0f / grow);
            float w = (random.nextFloat() * 2.0f + 1.0f) / (20.0f / grow);
            collector.submitCustomGeometry(pose, RenderTypes.dragonRays(), (m, v) -> {
                float[] left = {-0.866f * w, l, -0.5f * w};
                float[] right = {0.866f * w, l, -0.5f * w};
                float[] bottom = {0.0f, l, w};
                ray(m, v, left, right);
                ray(m, v, right, bottom);
                ray(m, v, bottom, left);
            });
        }
        pose.popPose();
    }

    private static void ray(PoseStack.Pose m, com.mojang.blaze3d.vertex.VertexConsumer v, float[] a, float[] b) {
        v.addVertex(m, 0.0f, 0.0f, 0.0f).setColor(0xFFFFFFFF);
        v.addVertex(m, a[0], a[1], a[2]).setColor(0x00CC00FF);
        v.addVertex(m, b[0], b[1], b[2]).setColor(0x00CC00FF);
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    /** A matriz na mão e no inventário: parada, os oito cubos juntos. */
    public static class Item implements SpecialModelRenderer<net.minecraft.util.Unit> {
        @Override
        public void submit(@Nullable net.minecraft.util.Unit ignored, PoseStack pose, SubmitNodeCollector collector,
                           int light, int overlay, boolean foil, int tint) {
            pose.pushPose();
            pose.translate(0.5f, 0.5f, 0.5f);
            draw(pose, collector, 0.0f, 0.0f, false, 0, 0, light);
            pose.popPose();
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
