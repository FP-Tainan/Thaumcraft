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
import net.thaumcraft.occulta.LeechChestBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O Baú de Sanguessugas no mundo: o {@code RenderLeechChest} do Witchery.
 *
 * <p>A tampa é de <b>quatro quartos</b> e eles não dobram: afastam-se uns dos outros em <b>três eixos ao
 * mesmo tempo</b>, cada um para o seu canto. Um baú comum range; este floresce.
 *
 * <p>A curva é a dos baús do jogo — um menos o cubo do que falta —, de modo que ele abre depressa no
 * princípio e vai parando no fim, como uma coisa pesada que cede.
 *
 * <p>E os <b>sacos de sangue</b> aparecem um por nome guardado. Eles são a parte honesta da armadilha: ficam
 * à vista, e quem souber lê neles quantas pessoas já abriram aquele baú.
 */
public class LeechChestRenderer implements BlockEntityRenderer<LeechChestBlockEntity,
        LeechChestRenderer.State> {
    private static final Identifier FOLHA = Thaumcraft.id("textures/block/leech_chest.png");

    /** O quanto cada quarto se abre, nos três eixos. */
    public static final float QUARTO = (float) (Math.PI / 2.0);
    public static final float DE_LADO = 1.5f;
    public static final float DE_FRENTE = 3.0f;

    private final ModelPart raiz;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        float tampa;
        int sacos;
    }

    public LeechChestRenderer(BlockEntityRendererProvider.Context contexto) {
        this.raiz = contexto.bakeLayer(LeechChestModel.BAÚ);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(LeechChestBlockEntity baú, State estado, float parcial, Vec3 câmara,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay quebrando) {
        BlockEntityRenderer.super.extractRenderState(baú, estado, parcial, câmara, quebrando);
        var feitio = baú.getBlockState();
        estado.facing = feitio.hasProperty(HorizontalDirectionalBlock.FACING)
                ? feitio.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
        estado.tampa = baú.tampa(parcial);
        estado.sacos = baú.quantosNomes();
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor, CameraRenderState câmara) {
        RenderType tipo = RenderTypes.entityCutout(FOLHA);
        int luz = estado.lightCoords;

        /*
         * A ordem é a do original, e ela importa: <b>primeiro o virar de cabeça para baixo</b>, depois o
         * giro. Girando antes de virar, o giro sai espelhado e a frente do baú — onde moram os sacos — fica
         * do lado de lá.
         */
        pose.pushPose();
        pose.translate(0.0f, 1.0f, 1.0f);
        pose.scale(1.0f, -1.0f, -1.0f);
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(giro(estado.facing)));
        pose.translate(-0.5f, -0.5f, -0.5f);

        /*
         * A curva dos baús do jogo: um menos o cubo do que falta. Ela abre depressa no princípio e vai
         * parando no fim, como uma coisa pesada que cede.
         */
        float quanto = 1.0f - estado.tampa;
        quanto = 1.0f - quanto * quanto * quanto;

        quarto(coletor, pose, tipo, luz, "lidbl",
                -quanto * QUARTO / DE_LADO, quanto * QUARTO / DE_FRENTE, quanto * QUARTO / DE_LADO);
        quarto(coletor, pose, tipo, luz, "lidbr",
                -quanto * QUARTO / DE_LADO, -quanto * QUARTO / DE_FRENTE, -quanto * QUARTO / DE_LADO);
        quarto(coletor, pose, tipo, luz, "lidfl",
                quanto * QUARTO / DE_LADO, -quanto * QUARTO / DE_FRENTE, quanto * QUARTO / DE_LADO);
        quarto(coletor, pose, tipo, luz, "lidfr",
                quanto * QUARTO / DE_LADO, quanto * QUARTO / DE_FRENTE, -quanto * QUARTO / DE_LADO);

        desenha(coletor, pose, tipo, luz, "below");
        if (estado.sacos >= 1) desenha(coletor, pose, tipo, luz, "sac1");
        if (estado.sacos >= 2) desenha(coletor, pose, tipo, luz, "sac2");
        if (estado.sacos >= 3) desenha(coletor, pose, tipo, luz, "sac3");
        pose.popPose();
    }

    /** Um quarto de tampa, com os três ângulos dele. */
    private void quarto(SubmitNodeCollector coletor, PoseStack pose, RenderType tipo, int luz, String qual,
                        float xRot, float yRot, float zRot) {
        ModelPart peça = this.raiz.getChild(qual);
        peça.xRot = xRot;
        peça.yRot = yRot;
        peça.zRot = zRot;
        coletor.submitModelPart(peça, pose, tipo, luz, OverlayTexture.NO_OVERLAY, null);
    }

    private void desenha(SubmitNodeCollector coletor, PoseStack pose, RenderType tipo, int luz, String qual) {
        coletor.submitModelPart(this.raiz.getChild(qual), pose, tipo, luz, OverlayTexture.NO_OVERLAY, null);
    }

    /** Para onde ele olha, pelos quatro ângulos do original. */
    public static float giro(Direction para) {
        return switch (para) {
            case SOUTH -> 180.0f;
            case WEST -> 270.0f;
            case EAST -> 90.0f;
            default -> 0.0f;
        };
    }
}
