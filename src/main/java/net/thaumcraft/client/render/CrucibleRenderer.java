package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.block.entity.CrucibleBlockEntity;

/**
 * A água dentro do crisol.
 *
 * <p>Ela fica na cor do que está dissolvido — o crisol mistura as cores dos aspectos —, sobe um dedo
 * quando ferve e treme de leve, que é o que o original faz para mostrar que ele está no ponto.
 */
public class CrucibleRenderer implements BlockEntityRenderer<CrucibleBlockEntity, CrucibleRenderer.State> {
    private static final Identifier WATER = Identifier.withDefaultNamespace("textures/block/water_still.png");
    /** A altura da água parada dentro do caldeirão. */
    private static final float LEVEL = 0.86f;
    /** A textura da água do jogo é uma tira de trinta e dois quadros; só o primeiro serve. */
    private static final float FRAME = 1.0f / 32.0f;

    /** O que o desenhista precisa saber do crisol neste quadro. */
    public static class State extends BlockEntityRenderState {
        public boolean water;
        public boolean boiling;
        public int color;
        public float ticks;
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
        state.boiling = crucible.boiling();
        state.color = crucible.brew();
        state.ticks = crucible.getLevel() == null ? 0.0f : crucible.getLevel().getGameTime() + partial;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.water) return;
        // fervendo a água sobe um dedo e treme de leve
        float height = LEVEL + (state.boiling
                ? 0.02f + (float) Math.sin(state.ticks / 4.0f) * 0.01f
                : 0.0f);
        int light = state.boiling ? 0xF000F0 : state.lightCoords;
        int color = 0xFF000000 | state.color;

        pose.pushPose();
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(WATER), (matrix, consumer) -> {
            // um quadrado no tamanho do vão de dentro do caldeirão, desenhado dos dois lados para que
            // ele apareça tanto de cima quanto de quem está mais baixo que a borda
            vertex(matrix, consumer, 0.125f, height, 0.125f, 0.0f, 0.0f, color, light);
            vertex(matrix, consumer, 0.125f, height, 0.875f, 0.0f, FRAME, color, light);
            vertex(matrix, consumer, 0.875f, height, 0.875f, 1.0f, FRAME, color, light);
            vertex(matrix, consumer, 0.875f, height, 0.125f, 1.0f, 0.0f, color, light);

            vertex(matrix, consumer, 0.875f, height, 0.125f, 1.0f, 0.0f, color, light);
            vertex(matrix, consumer, 0.875f, height, 0.875f, 1.0f, FRAME, color, light);
            vertex(matrix, consumer, 0.125f, height, 0.875f, 0.0f, FRAME, color, light);
            vertex(matrix, consumer, 0.125f, height, 0.125f, 0.0f, 0.0f, color, light);
        });
        pose.popPose();
    }

    private static void vertex(PoseStack.Pose matrix, VertexConsumer consumer, float x, float y, float z,
                               float u, float v, int color, int light) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(matrix, 0.0f, 1.0f, 0.0f);
    }
}
