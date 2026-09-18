package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.entity.TubeFilterBlockEntity;

/**
 * O miolo do tubo filtro: a segunda passada do {@code BlockTubeRenderer} da 4.2.3.5 no metadado três — a caixa
 * do filtro de novo, com a {@code pipe_filter_core} pintada da cor do aspecto do rótulo (branca sem rótulo).
 *
 * <p>A caixa de fora é do modelo do bloco; o miolo vem daqui porque a cor muda com o rótulo, e o modelo do bloco
 * não se redesenha com o que o bloco guarda.
 */
public class TubeFilterRenderer implements BlockEntityRenderer<TubeFilterBlockEntity, TubeFilterRenderer.State> {
    private static final Identifier CORE = Thaumcraft.id("textures/block/pipe_filter_core.png");
    /** O {@code W6 - 1/32} e o {@code W10 + 1/32} do original, com um fio a mais para não brigar com a caixa. */
    private static final float LO = 0.34375f - 0.002f, HI = 0.65625f + 0.002f;

    public static class State extends BlockEntityRenderState {
        int colour;
        final boolean[] plugged = new boolean[6];
    }

    public TubeFilterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TubeFilterBlockEntity tube, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(tube, state, crumbling);
        Aspect aspect = tube.aspectFilter();
        state.colour = 0xFF000000 | (aspect == null ? 0xFFFFFF : aspect.color());
        TubeRenderer.plugs(tube, state.plugged);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        TubeRenderer.submitPlugs(state.plugged, pose, collector, state.lightCoords);
        int colour = state.colour, light = state.lightCoords;
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(CORE), (m, c) -> {
            face(m, c, new float[][]{{LO, LO, HI}, {HI, LO, HI}, {HI, HI, HI}, {LO, HI, HI}}, 0, 0, 1, colour, light);
            face(m, c, new float[][]{{HI, LO, LO}, {LO, LO, LO}, {LO, HI, LO}, {HI, HI, LO}}, 0, 0, -1, colour, light);
            face(m, c, new float[][]{{HI, LO, HI}, {HI, LO, LO}, {HI, HI, LO}, {HI, HI, HI}}, 1, 0, 0, colour, light);
            face(m, c, new float[][]{{LO, LO, LO}, {LO, LO, HI}, {LO, HI, HI}, {LO, HI, LO}}, -1, 0, 0, colour, light);
            face(m, c, new float[][]{{LO, HI, HI}, {HI, HI, HI}, {HI, HI, LO}, {LO, HI, LO}}, 0, 1, 0, colour, light);
            face(m, c, new float[][]{{LO, LO, LO}, {HI, LO, LO}, {HI, LO, HI}, {LO, LO, HI}}, 0, -1, 0, colour, light);
        });
    }

    /** Uma face do miolo; a textura toma o recorte de 5,5 a 10,5 da folha, como o bloco do original. */
    private static void face(PoseStack.Pose m, VertexConsumer c, float[][] k, float nx, float ny, float nz, int colour, int light) {
        float u0 = 5.5f / 16.0f, u1 = 10.5f / 16.0f;
        float[][] uv = {{u0, u1}, {u1, u1}, {u1, u0}, {u0, u0}};
        for (int i = 0; i < 4; i++) {
            c.addVertex(m, k[i][0], k[i][1], k[i][2]).setColor(colour).setUv(uv[i][0], uv[i][1])
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(m, nx, ny, nz);
        }
    }
}
