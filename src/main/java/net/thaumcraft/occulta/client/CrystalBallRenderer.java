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
import net.thaumcraft.occulta.CrystalBallBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A Bola de Cristal no mundo: o {@code RenderCrystalBall} do Witchery.
 *
 * <p>O pé se desenha como qualquer coisa. A <b>esfera</b> é que tem o truque: três cascas encaixadas, todas
 * translúcidas, e a de dentro <b>pulsa com a hora do mundo</b>.
 *
 * <p>A conta da pulsação mora na {@linkplain CrystalBallBlockEntity#pulso alma do bloco}, e não aqui: é uma
 * conta de números e o servidor também a tem de poder ver, nem que seja só para a provar.
 *
 * <p>As duas cascas de fora não pulsam: ficam num <b>azul muito claro</b> fixo, que é o que dá ao vidro a
 * cor de vidro.
 */
public class CrystalBallRenderer implements BlockEntityRenderer<CrystalBallBlockEntity,
        CrystalBallRenderer.State> {
    public static final Identifier FOLHA = Thaumcraft.id("textures/block/crystal_ball.png");

    /** E a cor fixa das duas cascas de fora. */
    public static final int VIDRO = 0xFFCCCCFF;

    private final ModelPart raiz;

    public static class State extends BlockEntityRenderState {
        int miolo = 0xFFFFFFFF;
    }

    public CrystalBallRenderer(BlockEntityRendererProvider.Context contexto) {
        this.raiz = contexto.bakeLayer(CrystalBallModel.BOLA);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CrystalBallBlockEntity bola, State estado, float parcial, Vec3 câmara,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay quebrando) {
        BlockEntityRenderer.super.extractRenderState(bola, estado, parcial, câmara, quebrando);
        estado.miolo = CrystalBallBlockEntity.pulso(
                bola.getLevel() == null ? 0L : bola.getLevel().getGameTime());
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor,
                       CameraRenderState câmara) {
        desenha(pose, coletor, this.raiz, estado.lightCoords, estado.miolo);
    }

    /** Desenha a bola, no mundo ou na mão. */
    public static void desenha(PoseStack pose, SubmitNodeCollector coletor, ModelPart raiz, int luz,
                               int miolo) {
        RenderType pé = RenderTypes.entityCutout(FOLHA);
        RenderType vidro = RenderTypes.entityTranslucent(FOLHA);

        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);

        for (String qual : new String[] {"baseBottom", "baseMiddle", "baseTop"}) {
            coletor.submitModelPart(raiz.getChild(qual), pose, pé, luz, OverlayTexture.NO_OVERLAY, null);
        }
        coletor.submitModelPart(raiz.getChild("globeInner"), pose, vidro, luz, OverlayTexture.NO_OVERLAY,
                null, miolo, null);
        coletor.submitModelPart(raiz.getChild("globeMiddle"), pose, vidro, luz, OverlayTexture.NO_OVERLAY,
                null, VIDRO, null);
        coletor.submitModelPart(raiz.getChild("globeOuter"), pose, vidro, luz, OverlayTexture.NO_OVERLAY,
                null, VIDRO, null);
        pose.popPose();
    }
}
