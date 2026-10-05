package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.CandelabraBlockEntity;

/**
 * O Candelabro no mundo: o {@code RenderCandelabra} do Witchery.
 *
 * <p>Ele não tem nada que se mexa, nem feitio aceso e apagado: as chamas dele são <b>pós</b>, postos pelo
 * bloco, e não peças do modelo. O que este desenhista faz é só pôr as catorze caixas de pé no meio do
 * bloco, que é mais do que um modelo de arquivo saberia fazer com uma folha de trinta e dois por trinta e
 * dois desenhada para um modelo.
 *
 * <p>O virar de cabeça para baixo é o do original — <b>meia volta em Z</b>, e depois um bloco para baixo —,
 * que é como todo modelo de bicho entra num bloco: eles são desenhados com o chão em vinte e quatro e o
 * céu em zero.
 */
public class CandelabraRenderer implements BlockEntityRenderer<CandelabraBlockEntity,
        CandelabraRenderer.State> {
    public static final Identifier FOLHA = Thaumcraft.id("textures/block/candelabra.png");

    private final ModelPart raiz;

    public static class State extends BlockEntityRenderState {
    }

    public CandelabraRenderer(BlockEntityRendererProvider.Context contexto) {
        this.raiz = contexto.bakeLayer(CandelabraModel.CANDELABRO);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor,
                       CameraRenderState câmara) {
        desenha(pose, coletor, this.raiz, estado.lightCoords);
    }

    /** Desenha o candelabro, no mundo ou na mão. */
    public static void desenha(PoseStack pose, SubmitNodeCollector coletor, ModelPart raiz, int luz) {
        RenderType tipo = RenderTypes.entityCutout(FOLHA);
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);
        for (String qual : PEÇAS) {
            coletor.submitModelPart(raiz.getChild(qual), pose, tipo, luz, OverlayTexture.NO_OVERLAY, null);
        }
        pose.popPose();
    }

    /** As catorze, na ordem em que o original as desenha. */
    private static final String[] PEÇAS = {
        "candleLeft", "candleRight", "candleFront", "candleBack", "candleMiddle",
        "supportLR", "supportFB",
        "sconceLeft", "sconceRight", "sconceFront", "sconceBack", "sconceMiddle",
        "baseTop", "baseBottom",
    };
}
