package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.block.entity.CrucibleBlockEntity;

/**
 * A água do crisol: o {@code TileCrucibleRenderer} da 4.2.3.5 — a água parada do jogo num quadrado do tamanho do bloco,
 * na altura do tanque e da essência, e puxando para o roxo quanto mais essência houver (vermelho perde um terço, verde
 * tudo e azul metade da mistura). A água de hoje é cinza na textura; a cor da água comum do jogo entra por baixo.
 */
public class CrucibleRenderer implements BlockEntityRenderer<CrucibleBlockEntity, CrucibleRenderer.State> {
    /** O azul da água comum, que no jogo de então já vinha na textura. */
    private static final int WATER = 0x3F76E4;

    public static class State extends BlockEntityRenderState {
        public boolean water;
        public float height;
        public float recolor;
    }

    public CrucibleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CrucibleBlockEntity crucible, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(crucible, state, crumbling);
        state.water = crucible.hasWater();
        state.height = crucible.fluidHeight();
        float recolor = crucible.tagAmount() / 100.0f;
        if (recolor > 0.0f) recolor = 0.5f + recolor / 2.0f;
        state.recolor = recolor;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.water) return;
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager()
                .get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.withDefaultNamespace("block/water_still")));
        float r = (1.0f - state.recolor / 3.0f) * (WATER >> 16 & 255) / 255.0f;
        float g = (1.0f - state.recolor) * (WATER >> 8 & 255) / 255.0f;
        float b = (1.0f - state.recolor / 2.0f) * (WATER & 255) / 255.0f;
        int color = 0xFF000000 | (int) (Math.max(0, r) * 255) << 16 | (int) (Math.max(0, g) * 255) << 8 | (int) (Math.max(0, b) * 255);
        float h = state.height;
        int light = state.lightCoords;
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (matrix, consumer) -> {
            vertex(matrix, consumer, 0, h, 0, sprite.getU0(), sprite.getV0(), color, light);
            vertex(matrix, consumer, 0, h, 1, sprite.getU0(), sprite.getV1(), color, light);
            vertex(matrix, consumer, 1, h, 1, sprite.getU1(), sprite.getV1(), color, light);
            vertex(matrix, consumer, 1, h, 0, sprite.getU1(), sprite.getV0(), color, light);
        });
    }

    private static void vertex(PoseStack.Pose matrix, VertexConsumer consumer, float x, float y, float z,
                               float u, float v, int color, int light) {
        consumer.addVertex(matrix, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(matrix, 0.0f, 1.0f, 0.0f);
    }
}
