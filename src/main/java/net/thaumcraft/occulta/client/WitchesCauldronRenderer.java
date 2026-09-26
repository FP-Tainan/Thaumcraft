package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.WitchesCauldronBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O que ferve dentro do Caldeirão da Bruxa.
 *
 * <p>A panela é modelo de bloco — o crisol do mod com as ervas amarradas —, e o que este desenhista põe é só o
 * <b>líquido</b>: um retalho chato de água na altura do que está dentro, tingido da cor da mistura. É o mesmo
 * jeito do {@code CrucibleRenderer}, que é o do {@code TileCrucibleRenderer} da 4.2.3.5.
 */
public class WitchesCauldronRenderer
        implements BlockEntityRenderer<WitchesCauldronBlockEntity, WitchesCauldronRenderer.State> {
    /** A altura do líquido: do terço até quatro quintos do bloco, como no crisol. */
    public static final float FLOOR = 0.3f;
    public static final float DEPTH = 0.5f;

    public static class State extends BlockEntityRenderState {
        boolean filled;
        float height;
        int color = WitchesCauldronBlockEntity.PLAIN_COLOR;
    }

    public WitchesCauldronRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WitchesCauldronBlockEntity caldeirão, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(caldeirão, state, crumbling);
        state.filled = caldeirão.water() > 0;
        state.height = FLOOR + DEPTH * caldeirão.filled();
        state.color = caldeirão.color();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.filled) return;
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager()
                .get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.withDefaultNamespace("block/water_still")));
        int tint = 0xFF000000 | state.color;
        float h = state.height;
        int light = state.lightCoords;
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS),
                (matrix, consumer) -> {
                    // o retalho é do tamanho do bloco, como no crisol: as paredes é que o escondem nas bordas
                    vertex(matrix, consumer, 0.0f, h, 0.0f, sprite.getU0(), sprite.getV0(), tint, light);
                    vertex(matrix, consumer, 0.0f, h, 1.0f, sprite.getU0(), sprite.getV1(), tint, light);
                    vertex(matrix, consumer, 1.0f, h, 1.0f, sprite.getU1(), sprite.getV1(), tint, light);
                    vertex(matrix, consumer, 1.0f, h, 0.0f, sprite.getU1(), sprite.getV0(), tint, light);
                });
    }

    private static void vertex(PoseStack.Pose matrix, VertexConsumer consumer, float x, float y, float z,
                               float u, float v, int color, int light) {
        consumer.addVertex(matrix, x, y, z).setColor(color).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(matrix, 0.0f, 1.0f, 0.0f);
    }
}
