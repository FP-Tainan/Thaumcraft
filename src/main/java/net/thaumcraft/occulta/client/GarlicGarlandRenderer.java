package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.GarlicGarlandBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A Guirlanda de Alho na parede: o {@code RenderGarlicGarland} do Witchery.
 *
 * <p>Ela é construída <b>de cabeça para baixo</b>, como tudo o que vem desse tempo: as cabeças de alho
 * crescem para o <b>chão</b> a partir do cordel, e quem as põe a pender é o meio-giro em Z. Depois sobe-se
 * ao alto do bloco — nove décimos —, vira-se para o lado da parede, e afasta-se dois centésimos dela para
 * que o cordel não entre na pedra.
 */
public class GarlicGarlandRenderer
        implements BlockEntityRenderer<GarlicGarlandBlockEntity, GarlicGarlandRenderer.State> {
    private static final Identifier FOLHA = Thaumcraft.id("textures/models/garlic_garland.png");

    /** Onde ela se pendura dentro do bloco, e o quanto se afasta da parede. */
    public static final float ALTO = 0.9f;
    public static final float DESCE = -0.1f;
    public static final float DA_PAREDE = 0.02f;

    private final ModelPart raiz;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
    }

    public GarlicGarlandRenderer(BlockEntityRendererProvider.Context contexto) {
        this.raiz = contexto.bakeLayer(GarlicGarlandModel.GUIRLANDA);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GarlicGarlandBlockEntity guirlanda, State estado, float parcial,
                                   Vec3 câmara, ModelFeatureRenderer.@Nullable CrumblingOverlay quebrando) {
        BlockEntityRenderer.super.extractRenderState(guirlanda, estado, parcial, câmara, quebrando);
        var feitio = guirlanda.getBlockState();
        estado.facing = feitio.hasProperty(HorizontalDirectionalBlock.FACING)
                ? feitio.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor, CameraRenderState câmara) {
        pose.pushPose();
        pose.translate(0.5f, ALTO, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.mulPose(Axis.YP.rotationDegrees(SpinningWheelRenderer.angle(estado.facing)));
        pose.translate(0.0f, DESCE, DA_PAREDE);

        RenderType tipo = RenderTypes.entityCutout(FOLHA);
        for (String cabeça : GarlicGarlandModel.CABEÇAS) {
            coletor.submitModelPart(this.raiz.getChild(cabeça), pose, tipo,
                    estado.lightCoords, OverlayTexture.NO_OVERLAY, null);
        }
        for (String cordel : GarlicGarlandModel.CORDÉIS) {
            coletor.submitModelPart(this.raiz.getChild(cordel), pose, tipo,
                    estado.lightCoords, OverlayTexture.NO_OVERLAY, null);
        }
        pose.popPose();
    }
}
