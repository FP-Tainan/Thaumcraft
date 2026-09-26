package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.BoxMesh;
import net.thaumcraft.client.render.MeshDrawer;
import net.thaumcraft.occulta.WitchesCauldronBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O Caldeirão da Bruxa no mundo: o {@code RenderCauldron} e o {@code ModelCauldron} do Witchery.
 *
 * <p>São as vinte e uma peças do modelo — o fundo, as quatro paredes, o pescoço, o lábio e os quatro pés —, numa
 * folha de 64 por 64, e todas penduradas no mesmo ponto de giro. Por cima delas vai a água, um retalho chato na
 * altura do que estiver dentro e na cor da mistura, como no original.
 */
public class WitchesCauldronRenderer
        implements BlockEntityRenderer<WitchesCauldronBlockEntity, WitchesCauldronRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/witches_cauldron.png");
    private static final Identifier WATER = Identifier.withDefaultNamespace("textures/block/water_still.png");

    // as vinte e uma caixas do ModelCauldron, todas no ponto de giro (0, 16, 0)
    private static final float[] POT = BoxMesh.join(
            BoxMesh.box(-5, 5, -5, 10, 1, 10, 0, 53, 64, 64),
            BoxMesh.box(-5, 4, -6, 10, 1, 1, 0, 50, 64, 64),
            BoxMesh.box(-5, 4, 5, 10, 1, 1, 0, 50, 64, 64),
            BoxMesh.box(5, 4, -6, 1, 1, 12, 0, 36, 64, 64),
            BoxMesh.box(-6, 4, -6, 1, 1, 12, 0, 36, 64, 64),
            BoxMesh.box(-6, -2, -7, 12, 6, 1, 27, 45, 64, 64),
            BoxMesh.box(-6, -2, 6, 12, 6, 1, 27, 45, 64, 64),
            BoxMesh.box(6, -2, -7, 1, 6, 14, 27, 24, 64, 64),
            BoxMesh.box(-7, -2, -7, 1, 6, 14, 27, 24, 64, 64),
            BoxMesh.box(-5, -4, -6, 10, 2, 1, 0, 32, 64, 64),
            BoxMesh.box(-5, -4, 5, 10, 2, 1, 0, 32, 64, 64),
            BoxMesh.box(5, -4, -6, 1, 2, 12, 0, 17, 64, 64),
            BoxMesh.box(-6, -4, -6, 1, 2, 12, 0, 17, 64, 64),
            BoxMesh.box(-6, -5, -7, 12, 1, 1, 27, 21, 64, 64),
            BoxMesh.box(-6, -5, 6, 12, 1, 1, 27, 21, 64, 64),
            BoxMesh.box(6, -5, -7, 1, 1, 14, 27, 5, 64, 64),
            BoxMesh.box(-7, -5, -7, 1, 1, 14, 27, 5, 64, 64),
            BoxMesh.box(1.5f, 7.5f, -1.5f, 1, 3, 1, 0, 0, 64, 64),
            BoxMesh.box(-2.5f, 7.5f, -1.5f, 1, 3, 1, 0, 0, 64, 64),
            BoxMesh.box(1.5f, 7.5f, 0.5f, 1, 3, 1, 0, 0, 64, 64),
            BoxMesh.box(-2.5f, 7.5f, 0.5f, 1, 3, 1, 0, 0, 64, 64));

    /**
     * A água: um retalho chato de doze por doze, virado para cima.
     *
     * <p>A folha da água do jogo é uma tira de quadros, um por cima do outro; aqui só se usa o primeiro, que é o
     * pedaço de cima da tira.
     */
    private static final float FRAME = 16.0f / 512.0f;
    private static final float[] SURFACE = {
            -6.0f, 0.0f, -6.0f, 0.0f, 0.0f,
            6.0f, 0.0f, -6.0f, 1.0f, 0.0f,
            6.0f, 0.0f, 6.0f, 1.0f, FRAME,
            -6.0f, 0.0f, 6.0f, 0.0f, FRAME,
    };

    public static class State extends BlockEntityRenderState {
        boolean filled;
        float level;
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
        BlockEntityRenderer.super.extractRenderState(caldeirão, state, partial, camera, crumbling);
        state.filled = caldeirão.water() > 0;
        state.level = caldeirão.filled();
        state.color = caldeirão.color();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        cauldron(pose, collector, state.lightCoords);
        if (state.filled) water(pose, collector, state.level, state.color, state.lightCoords);
        pose.popPose();
    }

    /** O caldeirão, no mesmo caminho do original: de cabeça para baixo e um bloco abaixo. */
    static void cauldron(PoseStack pose, SubmitNodeCollector collector, int light) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);
        pose.translate(0.0f, 16.0f / 16.0f, 0.0f);
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                (m, v) -> MeshDrawer.draw(POT, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }

    /** E a água, na altura do que está dentro e na cor da mistura. */
    private static void water(PoseStack pose, SubmitNodeCollector collector, float filled, int color, int light) {
        pose.pushPose();
        // o quanto o original levanta a água: de um terço a quatro quintos da altura do caldeirão
        pose.translate(0.5f, 0.2f + filled * 0.5f, 0.5f);
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        int tint = 0xFF000000 | color;
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(WATER),
                (m, v) -> MeshDrawer.draw(SURFACE, m, v, light, OverlayTexture.NO_OVERLAY, tint));
        pose.popPose();
    }
}
