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
import org.joml.Quaternionf;

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
    public static final Identifier FOLHA = Thaumcraft.id("textures/block/leech_chest.png");

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
        desenha(pose, coletor, this.raiz, estado.lightCoords, estado.facing, estado.tampa, estado.sacos);
    }

    /**
     * Desenha o baú, no mundo ou na mão.
     *
     * <p>Na mão ele vem <b>fechado, sem saco nenhum e sem giro</b>: o original liga o mesmo desenhista ao
     * bloco e ao item, e sem alma o baú cai no lado de "ninguém o abriu ainda".
     */
    public static void desenha(PoseStack pose, SubmitNodeCollector coletor, ModelPart raiz, int luz,
                               @Nullable Direction para, float tampa, int sacos) {
        RenderType tipo = RenderTypes.entityCutout(FOLHA);

        /*
         * A ordem é a do original, e ela importa: <b>primeiro o virar de cabeça para baixo</b>, depois o
         * giro. Girando antes de virar, o giro sai espelhado e a frente do baú — onde moram os sacos — fica
         * do lado de lá.
         */
        pose.pushPose();
        pose.translate(0.0f, 1.0f, 1.0f);
        pose.scale(1.0f, -1.0f, -1.0f);
        pose.translate(0.5f, 0.5f, 0.5f);
        if (para != null) pose.mulPose(Axis.YP.rotationDegrees(giro(para)));
        pose.translate(-0.5f, -0.5f, -0.5f);

        /*
         * A curva dos baús do jogo: um menos o cubo do que falta. Ela abre depressa no princípio e vai
         * parando no fim, como uma coisa pesada que cede.
         */
        float quanto = 1.0f - tampa;
        quanto = 1.0f - quanto * quanto * quanto;

        quarto(coletor, pose, raiz, tipo, luz, "lidbl",
                -quanto * QUARTO / DE_LADO, quanto * QUARTO / DE_FRENTE, quanto * QUARTO / DE_LADO);
        quarto(coletor, pose, raiz, tipo, luz, "lidbr",
                -quanto * QUARTO / DE_LADO, -quanto * QUARTO / DE_FRENTE, -quanto * QUARTO / DE_LADO);
        quarto(coletor, pose, raiz, tipo, luz, "lidfl",
                quanto * QUARTO / DE_LADO, -quanto * QUARTO / DE_FRENTE, quanto * QUARTO / DE_LADO);
        quarto(coletor, pose, raiz, tipo, luz, "lidfr",
                quanto * QUARTO / DE_LADO, quanto * QUARTO / DE_FRENTE, -quanto * QUARTO / DE_LADO);

        peça(coletor, pose, raiz, tipo, luz, "below");
        if (sacos >= 1) peça(coletor, pose, raiz, tipo, luz, "sac1");
        if (sacos >= 2) peça(coletor, pose, raiz, tipo, luz, "sac2");
        if (sacos >= 3) peça(coletor, pose, raiz, tipo, luz, "sac3");
        pose.popPose();
    }

    /**
     * Um quarto de tampa, com os três ângulos dele — <b>postos na pilha e não na peça</b>.
     *
     * <p>É preciso dizer por quê, porque o jeito óbvio está errado. O jogo de hoje <b>não desenha na hora</b>:
     * ele junta tudo o que lhe mandam e desenha depois, de uma vez. A peça do modelo é <b>uma só</b>,
     * compartilhada por todos os baús do mundo; mexer no ângulo dela antes de a mandar faz com que, na hora
     * de desenhar, <b>todos</b> saiam com o ângulo do último — e uma sala de baús com um deles aberto
     * apareceria com todos abertos.
     *
     * <p>Girando a pilha à volta do eixo da peça, cada submissão leva o seu próprio giro. A ordem dos três
     * ângulos é a do {@code translateAndRotate}: Z, depois Y, depois X.
     */
    private static void quarto(SubmitNodeCollector coletor, PoseStack pose, ModelPart raiz, RenderType tipo,
                               int luz, String qual, float xRot, float yRot, float zRot) {
        ModelPart peça = raiz.getChild(qual);
        if (xRot == 0.0f && yRot == 0.0f && zRot == 0.0f) {
            coletor.submitModelPart(peça, pose, tipo, luz, OverlayTexture.NO_OVERLAY, null);
            return;
        }
        pose.pushPose();
        pose.translate(peça.x / 16.0f, peça.y / 16.0f, peça.z / 16.0f);
        pose.mulPose(new Quaternionf().rotationZYX(zRot, yRot, xRot));
        pose.translate(-peça.x / 16.0f, -peça.y / 16.0f, -peça.z / 16.0f);
        coletor.submitModelPart(peça, pose, tipo, luz, OverlayTexture.NO_OVERLAY, null);
        pose.popPose();
    }

    private static void peça(SubmitNodeCollector coletor, PoseStack pose, ModelPart raiz, RenderType tipo,
                             int luz, String qual) {
        coletor.submitModelPart(raiz.getChild(qual), pose, tipo, luz, OverlayTexture.NO_OVERLAY, null);
    }

    /**
     * Para onde ele olha, pelos quatro ângulos do original.
     *
     * <p>E eles <b>não são os da Armadilha de Urso</b>, embora as duas façam a mesma coisa: o baú está
     * <b>meia volta adiantado</b>. A razão está no jeito de o virar de cabeça para baixo. A armadilha usa um
     * <b>giro de meia volta em Z</b>, que troca o sinal de X e Y; o baú usa uma <b>escala negativa em Y e
     * Z</b>, que troca o sinal de Y e Z. Os dois viram o modelo, mas deixam-no olhando para lados opostos — e
     * a tabela de giros de cada um corrige o seu.
     *
     * <p>Copiar a tabela da armadilha para o baú — que foi o que esta fatia encontrou feito — põe os
     * <b>sacos de sangue no fundo</b>, do lado em que ninguém os vê. Era por isso que eles custavam tanto a
     * aparecer numa tela de prova.
     */
    public static float giro(Direction para) {
        return switch (para) {
            case NORTH -> 180.0f;
            case WEST -> 90.0f;
            case EAST -> 270.0f;
            default -> 0.0f;
        };
    }
}
