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
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.vampire.CoffinBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O Caixão no mundo: o {@code RenderCoffin} do Witchery.
 *
 * <p>As duas metades desenham o <b>mesmo modelo</b>, e o que as distingue são quatro números que o original
 * muda à mão antes de desenhar: na metade dos <b>pés</b> o corpo e as paredes andam um para a frente e a
 * parede de topo <b>vira-se ao contrário</b>, de modo que o caixão feche pelas duas pontas e não tenha
 * parede no meio. É um truque de 2014 e continua a ser o certo: um modelo, duas metades.
 *
 * <p>A <b>tampa</b> gira em volta da borda esquerda, até um quarto de volta, com a curva cúbica dos baús do
 * jogo — e é essa curva que a faz parecer pesada.
 */
public class CoffinRenderer implements BlockEntityRenderer<CoffinBlockEntity, CoffinRenderer.State> {
    private static final Identifier FOLHA = Thaumcraft.id("textures/models/coffin.png");

    /** O quanto a tampa gira, de todo aberta. */
    public static final float TAMPA_ABERTA = 90.0f;

    /** E o ajuste do original, que desce o modelo um pouco dentro do bloco. */
    public static final float DESCE = -0.1f;

    private final ModelPart raiz;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        boolean cabeça;
        float tampa;
    }

    public CoffinRenderer(BlockEntityRendererProvider.Context contexto) {
        this.raiz = contexto.bakeLayer(CoffinModel.CAIXÃO);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CoffinBlockEntity caixão, State estado, float parcial, Vec3 câmara,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay quebrando) {
        BlockEntityRenderer.super.extractRenderState(caixão, estado, parcial, câmara, quebrando);
        var feitio = caixão.getBlockState();
        estado.facing = feitio.hasProperty(BedBlock.FACING)
                ? feitio.getValue(BedBlock.FACING) : Direction.NORTH;
        estado.cabeça = feitio.hasProperty(BedBlock.PART)
                && feitio.getValue(BedBlock.PART) == BedPart.HEAD;
        estado.tampa = caixão.tampa(parcial);
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor, CameraRenderState câmara) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, DESCE, 0.0f);
        pose.mulPose(Axis.YP.rotationDegrees(SpinningWheelRenderer.angle(estado.facing)));

        RenderType tipo = RenderTypes.entityCutout(FOLHA);
        int luz = estado.lightCoords;

        // o que as duas metades partilham
        desenha(coletor, pose, tipo, luz, "base_lower");
        desenha(coletor, pose, tipo, luz, "side_right");
        desenha(coletor, pose, tipo, luz, "side_left");
        desenha(coletor, pose, tipo, luz, "base");

        /*
         * A parede de topo, que é a única peça que as duas metades não partilham: na metade dos pés ela
         * vira-se meia volta, e com isso o caixão fica fechado nas duas pontas e aberto no meio.
         */
        pose.pushPose();
        if (!estado.cabeça) pose.mulPose(Axis.YP.rotationDegrees(180.0f));
        desenha(coletor, pose, tipo, luz, "side_end");
        pose.popPose();

        // e a tampa, que gira em volta da borda
        pose.pushPose();
        pose.translate(-7.0f / 16.0f, -5.0f / 16.0f, 0.0f);
        pose.mulPose(Axis.ZP.rotationDegrees(-estado.tampa * TAMPA_ABERTA));
        pose.translate(7.0f / 16.0f, 5.0f / 16.0f, 0.0f);
        desenha(coletor, pose, tipo, luz, "lid");
        pose.popPose();

        pose.popPose();
    }

    private void desenha(SubmitNodeCollector coletor, PoseStack pose, RenderType tipo, int luz, String qual) {
        coletor.submitModelPart(this.raiz.getChild(qual), pose, tipo, luz, OverlayTexture.NO_OVERLAY, null);
    }
}
