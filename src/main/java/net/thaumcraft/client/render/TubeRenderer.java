package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.entity.TubeBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A essência correndo dentro do tubo.
 *
 * <p>É o que faz a tubulação do Thaumcraft ser bonita de olhar: enquanto a essência anda, vê-se um
 * bolinho de luz na cor do aspecto passando de cano em cano. Sem isso o encanamento é um monte de cano
 * parado, e não dá para saber, de longe, se a linha está trabalhando.
 *
 * <p>O bolinho pulsa no compasso do tique para dar a impressão de que está correndo; e, com os Óculos da
 * Revelação no rosto, sai também o símbolo do aspecto por cima do cano, como em qualquer outra peça que
 * guarde essência.
 */
public class TubeRenderer implements BlockEntityRenderer<TubeBlockEntity, TubeRenderer.State> {
    private static final Identifier MIST = Thaumcraft.id("textures/misc/essentia.png");
    /** O tamanho do bolinho de essência dentro do cano. */
    private static final float BLOB = 0.17f;

    /** O que o desenhista precisa saber do tubo neste quadro. */
    public static class State extends BlockEntityRenderState {
        @Nullable
        public Aspect aspect;
        public int amount;
        public boolean venting;
        public boolean labelled;
        public float ticks;
    }

    private final Font font;

    public TubeRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TubeBlockEntity tube, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(tube, state, crumbling);
        state.aspect = tube.getEssentiaType(null);
        state.amount = tube.getEssentiaAmount(null);
        state.venting = tube.venting() > 0;
        state.labelled = EssentiaLabel.visible(tube);
        state.ticks = tube.getLevel() == null ? 0.0f : tube.getLevel().getGameTime() + partial;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.aspect == null || state.amount <= 0) return;

        // o bolinho de essência, pulsando como se estivesse correndo
        float pulse = 1.0f + (float) Math.sin(state.ticks * 0.6f) * 0.25f;
        float half = BLOB * pulse / 2.0f;
        int color = 0xE0000000 | state.aspect.color();

        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(camera.orientation);
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucentEmissive(MIST), (matrix, consumer) -> {
            consumer.addVertex(matrix, -half, -half, 0.0f).setColor(color).setUv(0.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, -1.0f);
            consumer.addVertex(matrix, half, -half, 0.0f).setColor(color).setUv(1.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, -1.0f);
            consumer.addVertex(matrix, half, half, 0.0f).setColor(color).setUv(1.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, -1.0f);
            consumer.addVertex(matrix, -half, half, 0.0f).setColor(color).setUv(0.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, -1.0f);
        });
        pose.popPose();

        if (state.labelled) {
            EssentiaLabel.submit(pose, collector, camera, this.font,
                    state.aspect, state.amount, 1.1f, state.lightCoords);
        }
    }
}
