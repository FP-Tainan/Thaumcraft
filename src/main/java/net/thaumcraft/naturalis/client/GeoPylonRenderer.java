package net.thaumcraft.naturalis.client;

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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.ObjMesh;
import net.thaumcraft.client.render.ObjModel;
import net.thaumcraft.naturalis.GeoPylonBlockEntity;

/**
 * O Geo-Pilone no mundo: o {@code TileGeoMorpherRenderer} do Magia Naturalis 0.5.0 — os dois anéis do relé de vis,
 * girando um contra o outro, e no meio o cristal, na cor da terra que ele vai escrever (cinza-azulado enquanto
 * está parado).
 */
public class GeoPylonRenderer implements BlockEntityRenderer<GeoPylonBlockEntity, GeoPylonRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/vis_relay.png");
    /** A cor do cristal parado: o {@code new Color(0.25F, 0.25F, 0.3F)} do original. */
    private static final int RESTING = 0x40404C;

    public static class State extends BlockEntityRenderState {
        boolean idle = true;
        int colour = RESTING;
        float ticks;
        int jitter;
        float wobble = 1.0f;
    }

    public GeoPylonRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GeoPylonBlockEntity pylon, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(pylon, state, crumbling);
        state.idle = pylon.idle();
        state.colour = RESTING;
        if (!state.idle) {
            var key = pylon.target();
            var level = pylon.getLevel();
            if (key != null && level != null) {
                var biome = level.registryAccess().lookupOrThrow(Registries.BIOME).get(key).orElse(null);
                if (biome != null) state.colour = biome.value().getFoliageColor();
            }
        }
        // o original semeia o acaso com a posição, para cada pilone tremer no seu tempo
        var pos = pylon.getBlockPos();
        RandomSource random = RandomSource.create((long) pos.getX() * pos.getY() + pos.getZ());
        state.jitter = random.nextInt(10);
        state.wobble = 10.0f + random.nextFloat();
        var viewer = Minecraft.getInstance().getCameraEntity();
        state.ticks = viewer == null ? partial : viewer.tickCount + partial;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        // parado, o pilone congela no tempo em que parou: aqui o relógio só corre quando ele trabalha
        float ticks = state.idle ? 0.0f : state.ticks;
        float shade = Mth.sin((ticks + state.jitter) / state.wobble) * 0.05f + 0.95f;
        float bob = Mth.sin(ticks / 14.0f) * 0.025f;

        pose.pushPose();
        pose.translate(0.5f, 1.0f + bob, 0.5f);
        pose.mulPose(Axis.XP.rotationDegrees(270.0f));
        pose.scale(2.0f, 2.0f, 2.0f);

        // o anel de fora, girando para um lado
        pose.pushPose();
        pose.scale(2.25f, 2.25f, 1.25f);
        pose.translate(0.0f, 0.0f, 0.05f + bob);
        pose.mulPose(Axis.ZP.rotationDegrees(-ticks * 0.5f));
        part(pose, collector, "RingFloat", state.lightCoords);
        pose.popPose();

        // e o de dentro, para o outro
        pose.mulPose(Axis.ZP.rotationDegrees(ticks * 0.5f));
        pose.pushPose();
        pose.translate(0.0f, 0.0f, 0.2f);
        part(pose, collector, "RingBase", state.lightCoords);
        pose.popPose();

        // o cristal, na cor da terra, brilhando com o tremor
        int glow = (int) (210.0f * shade);
        float[] crystal = ObjModel.part("vis_relay", "Crystal");
        int tint = 0xFF000000 | state.colour;
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(TEXTURE), (m, c) ->
                ObjMesh.draw(crystal, m, c, glow, OverlayTexture.NO_OVERLAY, tint));
        pose.popPose();
    }

    private static void part(PoseStack pose, SubmitNodeCollector collector, String name, int light) {
        float[] mesh = ObjModel.part("vis_relay", name);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE), (m, c) ->
                ObjMesh.draw(mesh, m, c, light, OverlayTexture.NO_OVERLAY, -1));
    }
}
