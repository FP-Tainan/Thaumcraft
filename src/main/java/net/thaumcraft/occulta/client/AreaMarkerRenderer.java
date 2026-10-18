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
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.rite.AreaMarkerBlock;
import net.thaumcraft.occulta.rite.AreaMarkerBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Os dois marcadores de área no mundo: o {@code RenderStatueWolf} e o {@code RenderStatueMandrake}.
 *
 * <p>São o mesmo desenho com duas malhas e duas folhas. Como as outras estátuas deste ramo, eles são
 * construídos de <b>cabeça para baixo</b> e postos de pé com um meio-giro em Z — mas estes descem só
 * <b>um quarto de bloco</b> e <b>encolhem a metade</b>, que é o que faz deles estatuetas.
 */
public final class AreaMarkerRenderer {
    public static final Identifier FOLHA_LOBO = Thaumcraft.id("textures/models/wolf_statue.png");
    public static final Identifier FOLHA_MANDRÁGORA =
            Thaumcraft.id("textures/models/mandrake_statue.png");

    /** Quanto eles descem e quanto encolhem: os números do original, que diferem num milésimo. */
    public static final float DESCE_LOBO = -0.26f;
    public static final float DESCE_MANDRÁGORA = -0.255f;
    public static final float ENCOLHE = 0.5f;

    private AreaMarkerRenderer() {
    }

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        AreaMarkerBlock.Feitio feitio = AreaMarkerBlock.Feitio.WOLF;
    }

    /** O desenhista, que serve aos dois blocos. */
    public static class Marcador implements BlockEntityRenderer<AreaMarkerBlockEntity, State> {
        private final ModelPart lobo;
        private final ModelPart mandrágora;

        public Marcador(BlockEntityRendererProvider.Context contexto) {
            this.lobo = contexto.bakeLayer(AreaMarkerModels.WOLF);
            this.mandrágora = contexto.bakeLayer(AreaMarkerModels.MANDRAKE);
        }

        @Override
        public State createRenderState() {
            return new State();
        }

        @Override
        public void extractRenderState(AreaMarkerBlockEntity marcador, State estado, float parcial,
                                       Vec3 câmara,
                                       ModelFeatureRenderer.@Nullable CrumblingOverlay quebrando) {
            BlockEntityRenderer.super.extractRenderState(marcador, estado, parcial, câmara, quebrando);
            var feitio = marcador.getBlockState();
            estado.facing = feitio.hasProperty(AreaMarkerBlock.FACING)
                    ? feitio.getValue(AreaMarkerBlock.FACING) : Direction.NORTH;
            if (feitio.getBlock() instanceof AreaMarkerBlock bloco) estado.feitio = bloco.feitio();
        }

        @Override
        public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor,
                           CameraRenderState câmara) {
            boolean éLobo = estado.feitio == AreaMarkerBlock.Feitio.WOLF;
            desenha(pose, coletor, éLobo ? this.lobo : this.mandrágora, estado.lightCoords,
                    SpinningWheelRenderer.angle(estado.facing), éLobo, false);
        }
    }

    /** O desenho de um deles, que serve ao bloco no mundo e à estatueta na mão. */
    static void desenha(PoseStack pose, SubmitNodeCollector coletor, ModelPart raiz, int luz, float giro,
                        boolean éLobo, boolean naMão) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, éLobo ? DESCE_LOBO : DESCE_MANDRÁGORA, 0.0f);
        pose.scale(ENCOLHE, ENCOLHE, ENCOLHE);
        if (naMão) {
            pose.scale(AreaMarkerItemRenderer.ENCOLHE, AreaMarkerItemRenderer.ENCOLHE,
                    AreaMarkerItemRenderer.ENCOLHE);
            pose.mulPose(Axis.YP.rotationDegrees(AreaMarkerItemRenderer.DE_FRENTE));
            pose.translate(0.0f, AreaMarkerItemRenderer.LEVANTA, 0.0f);
        } else {
            pose.mulPose(Axis.YP.rotationDegrees(giro));
        }

        RenderType tipo = RenderTypes.entityCutout(éLobo ? FOLHA_LOBO : FOLHA_MANDRÁGORA);
        List<String> peças = éLobo ? AreaMarkerModels.LOBO : AreaMarkerModels.MANDRÁGORA;
        for (String nome : peças) {
            coletor.submitModelPart(raiz.getChild(nome), pose, tipo, luz, OverlayTexture.NO_OVERLAY, null);
        }
        pose.popPose();
    }
}
