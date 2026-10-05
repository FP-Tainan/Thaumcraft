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
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.ChaliceBlock;
import net.thaumcraft.occulta.ChaliceBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O Cálice no mundo: o {@code RenderChalice} do Witchery.
 *
 * <p>Duas peças e uma pergunta: a taça se desenha sempre, o <b>líquido</b> só quando ela está cheia. No
 * original a pergunta é feita à alma do bloco; aqui ao {@linkplain ChaliceBlock#CHEIO feitio dele}, que é
 * onde o cheio passou a morar.
 */
public class ChaliceRenderer implements BlockEntityRenderer<ChaliceBlockEntity, ChaliceRenderer.State> {
    public static final Identifier FOLHA = Thaumcraft.id("textures/block/chalice.png");

    private final ModelPart raiz;

    public static class State extends BlockEntityRenderState {
        boolean cheio;
    }

    public ChaliceRenderer(BlockEntityRendererProvider.Context contexto) {
        this.raiz = contexto.bakeLayer(ChaliceModel.CÁLICE);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ChaliceBlockEntity cálice, State estado, float parcial, Vec3 câmara,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay quebrando) {
        BlockEntityRenderer.super.extractRenderState(cálice, estado, parcial, câmara, quebrando);
        var feitio = cálice.getBlockState();
        estado.cheio = feitio.hasProperty(ChaliceBlock.CHEIO) && feitio.getValue(ChaliceBlock.CHEIO);
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor,
                       CameraRenderState câmara) {
        desenha(pose, coletor, this.raiz, estado.lightCoords, estado.cheio);
    }

    /** Desenha o cálice, no mundo ou na mão. */
    public static void desenha(PoseStack pose, SubmitNodeCollector coletor, ModelPart raiz, int luz,
                               boolean cheio) {
        RenderType tipo = RenderTypes.entityCutout(FOLHA);
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);
        coletor.submitModelPart(raiz.getChild("chalice"), pose, tipo, luz, OverlayTexture.NO_OVERLAY, null);
        if (cheio) {
            coletor.submitModelPart(raiz.getChild("liquid"), pose, tipo, luz,
                    OverlayTexture.NO_OVERLAY, null);
        }
        pose.popPose();
    }
}
