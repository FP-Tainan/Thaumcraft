package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.eldritch.EldritchPortalBlockEntity;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

/**
 * O portal eldritch: o {@code TileEldritchPortalRenderer} da 4.2.3.5. Um quadro de dois blocos virado para quem vê, com
 * os dezesseis quadros da {@code eldritch_portal.png} passando a vinte por segundo; ao abrir, cresce para os lados em
 * cinco tiques e para cima em trinta.
 */
public class EldritchPortalRenderer implements BlockEntityRenderer<EldritchPortalBlockEntity, EldritchPortalRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/misc/eldritch_portal.png");

    public static class State extends BlockEntityRenderState {
        float open;
    }

    public EldritchPortalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public void extractRenderState(EldritchPortalBlockEntity te, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(te, state, partial, camera, crumbling);
        state.open = te.opencount + partial;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float scale = (int) Math.min(5.0f, state.open) / 5.0f;
        float scaley = (int) Math.min(30.0f, state.open) / 30.0f;
        if (scale <= 0.0f || scaley <= 0.0f) return;
        long time = System.nanoTime() / 50000000L;
        int frame = (int) (time % 16);
        float u0 = frame / 16.0f, u1 = u0 + 0.0625f;
        Vector3f right = camera.orientation.transform(new Vector3f(1.0f, 0.0f, 0.0f));
        Vector3f up = camera.orientation.transform(new Vector3f(0.0f, 1.0f, 0.0f));
        float[][] c = {
                {-right.x - up.x, -right.y - up.y, -right.z - up.z},
                {-right.x + up.x, -right.y + up.y, -right.z + up.z},
                {right.x + up.x, right.y + up.y, right.z + up.z},
                {right.x - up.x, right.y - up.y, right.z - up.z}};
        // os cantos com as coordenadas do original (a folha vai deitada: u sobe com o "para cima" da câmera)
        float[][] uv = {{u0, 1.0f}, {u1, 1.0f}, {u1, 0.0f}, {u0, 0.0f}};
        collector.submitCustomGeometry(pose, AdditiveGlow.blended(TEXTURE), (matrix, consumer) -> {
            for (int k = 0; k < 8; k++) {
                int i = k < 4 ? k : 7 - k;
                consumer.addVertex(matrix, 0.5f + c[i][0] * scale, 0.5f + c[i][1] * scaley, 0.5f + c[i][2] * scale).setColor(0xFFFFFFFF)
                        .setUv(uv[i][0], uv[i][1]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(220).setNormal(matrix, 0.0f, 0.0f, -1.0f);
            }
        });
    }
}
