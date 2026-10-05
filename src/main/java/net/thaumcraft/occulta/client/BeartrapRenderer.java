package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
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
import net.thaumcraft.occulta.trap.BeartrapBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A armadilha no chão: o {@code RenderBeartrap} do Witchery.
 *
 * <p><b>Armada</b>, ela fica deitada: os arcos no chão, os dentes para cima, a placa do gatilho levantada.
 * <b>Disparada</b>, os dois arcos se levantam 1,2 radiano e os dentes se encontram no meio — e a placa
 * afundou, porque foi nela que alguém pisou.
 *
 * <p>E ela <b>desaparece</b>: uma armadilha armada por outra pessoa fica a <b>três décimos de opaca</b>, o
 * que num chão de pedra é quase nada. Quem a pôs a vê inteira; mais ninguém. É o {@code mantrapAlpha} do
 * original, que o tinha no arquivo de ajustes e não o deixava descer abaixo de um décimo.
 *
 * <p>A ordem das contas é a do original e ela importa: o <b>virar de cabeça para baixo</b> — que é a meia
 * volta em Z — vem <b>antes</b> do giro para o lado em que ela foi posta. É a mesma lição do Baú de
 * Sanguessugas: girando primeiro, o giro sai espelhado.
 */
public class BeartrapRenderer implements BlockEntityRenderer<BeartrapBlockEntity,
        BeartrapRenderer.State> {
    public static final Identifier FOLHA = Thaumcraft.id("textures/block/beartrap.png");

    /** Quanto cada arco se levanta ao disparar, em radianos. */
    public static final float ABRE = 1.2f;

    /** Onde fica a placa do gatilho, armada e disparada. */
    public static final float PLACA_ARMADA = 23.2f;
    public static final float PLACA_DISPARADA = 23.8f;

    /** O quanto dela se vê quando é de outra pessoa e está armada: os três décimos do original. */
    public static final int ESCONDIDA = 0x4DFFFFFF;

    private final ModelPart raiz;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        boolean disparada = true;
        boolean àVista = true;
    }

    public BeartrapRenderer(BlockEntityRendererProvider.Context contexto) {
        this.raiz = contexto.bakeLayer(BeartrapModel.ARMADILHA);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BeartrapBlockEntity armadilha, State estado, float parcial, Vec3 câmara,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay quebrando) {
        BlockEntityRenderer.super.extractRenderState(armadilha, estado, parcial, câmara, quebrando);
        var feitio = armadilha.getBlockState();
        estado.facing = feitio.hasProperty(HorizontalDirectionalBlock.FACING)
                ? feitio.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
        estado.disparada = armadilha.disparada();
        estado.àVista = armadilha.àVistaDe(Minecraft.getInstance().player);
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor, CameraRenderState câmara) {
        desenha(pose, coletor, this.raiz, estado.lightCoords,
                estado.àVista ? -1 : ESCONDIDA, estado.facing, estado.disparada);
    }

    /**
     * Desenha a armadilha, no mundo ou na mão.
     *
     * <p>Na mão ela vem <b>armada e sem giro</b>: o original passa alma nenhuma ao desenhista nesse caso, e o
     * modelo cai no lado de "ainda não disparou". É de propósito que o item mostre a armadilha deitada — é
     * assim que ela é quando serve para alguma coisa.
     */
    public static void desenha(PoseStack pose, SubmitNodeCollector coletor, ModelPart raiz, int luz, int cor,
                               @Nullable Direction para, boolean disparada) {
        RenderType tipo = RenderTypes.entityTranslucent(FOLHA);

        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);
        if (para != null) pose.mulPose(Axis.YP.rotationDegrees(giro(para)));

        peça(coletor, pose, tipo, luz, cor, raiz.getChild("base"));
        peça(coletor, pose, tipo, luz, cor, raiz.getChild("diskLeft"));
        peça(coletor, pose, tipo, luz, cor, raiz.getChild("diskRight"));

        // a placa afunda ao disparar, e o afundar vai na pilha
        ModelPart placa = raiz.getChild("plate");
        pose.pushPose();
        pose.translate(0.0f, ((disparada ? PLACA_DISPARADA : PLACA_ARMADA) - placa.y) / 16.0f, 0.0f);
        peça(coletor, pose, tipo, luz, cor, placa);
        pose.popPose();

        arco(coletor, pose, tipo, luz, cor, raiz.getChild("armFront"), disparada ? -ABRE : 0.0f);
        arco(coletor, pose, tipo, luz, cor, raiz.getChild("armBack"), disparada ? ABRE : 0.0f);
        pose.popPose();
    }

    /**
     * Um arco e tudo o que está pregado nele: as duas hastes e os cinco dentes.
     *
     * <p><b>O giro vai na pilha, e não na peça</b> — e é preciso dizer por quê, porque o jeito óbvio está
     * errado. O jogo de hoje <b>não desenha na hora</b>: ele junta tudo o que lhe mandam e desenha depois, de
     * uma vez. A peça do modelo é <b>uma só</b>, compartilhada por todas as armadilhas do mundo; mexer no
     * ângulo dela antes de mandá-la faz com que, na hora de desenhar, <b>todas</b> saiam com o ângulo da
     * última — e um campo de armadilhas disparadas aparece todo armado.
     *
     * <p>Girando a pilha à volta do eixo da peça, cada submissão leva o seu próprio giro e a peça vai tal
     * como foi assada. A conta é a mesma que o {@code translateAndRotate} faria.
     */
    private static void arco(SubmitNodeCollector coletor, PoseStack pose, RenderType tipo, int luz, int cor,
                             ModelPart qual, float xRot) {
        if (xRot == 0.0f) {
            peça(coletor, pose, tipo, luz, cor, qual);
            return;
        }
        pose.pushPose();
        pose.translate(qual.x / 16.0f, qual.y / 16.0f, qual.z / 16.0f);
        pose.mulPose(Axis.XP.rotation(xRot));
        pose.translate(-qual.x / 16.0f, -qual.y / 16.0f, -qual.z / 16.0f);
        peça(coletor, pose, tipo, luz, cor, qual);
        pose.popPose();
    }

    private static void peça(SubmitNodeCollector coletor, PoseStack pose, RenderType tipo, int luz, int cor,
                             ModelPart qual) {
        coletor.submitModelPart(qual, pose, tipo, luz, OverlayTexture.NO_OVERLAY, null, cor, null);
    }

    /** Para onde ela olha, pelos quatro ângulos do original. */
    public static float giro(Direction para) {
        return switch (para) {
            case SOUTH -> 180.0f;
            case WEST -> 270.0f;
            case EAST -> 90.0f;
            default -> 0.0f;
        };
    }
}
