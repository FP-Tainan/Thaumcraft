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
import net.thaumcraft.occulta.demon.DemonHeartBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O Coração no chão: o {@code RenderDemonHeart} do Witchery.
 *
 * <p>O bloco <b>não se desenha</b> — o {@code BlockContainer} do jogo de então devolvia o tipo de desenho
 * −1, que quer dizer "não desenhes nada", e quem põe o coração no mundo é este desenhista. Por isso o modelo
 * de bloco deste porte não tem uma única caixa: só diz de que cor são as lascas quando alguém o parte.
 *
 * <p>E o que ele faz é o que o original faz, pela mesma ordem: põe-se no meio da casa, <b>vira-se de
 * cabeça para baixo</b>, encolhe a sete décimos, gira para o lado para onde foi posto, e então se desloca um
 * pouco para fora do meio — porque um coração centrado parece um enfeite, e um coração <b>encostado</b>
 * parece uma coisa que alguém deixou ali.
 *
 * <p>Os canos ficam parados e o músculo {@linkplain DemonHeartModel#inchaço bate}.
 */
public class DemonHeartRenderer implements BlockEntityRenderer<DemonHeartBlockEntity,
        DemonHeartRenderer.State> {
    private static final Identifier FOLHA = Thaumcraft.id("textures/block/demon_heart.png");

    /** O quanto ele encolhe, e para onde se desloca depois de girar. */
    public static final float TAMANHO = 0.7f;
    public static final float DESLOCA_X = 0.1f;
    public static final float DESLOCA_Y = -0.9f;
    public static final float DESLOCA_Z = -0.15f;

    /** As quatro peças do músculo, que são as que incham. */
    private static final String[] MÚSCULO = {"bigtube1", "shape1", "shape2", "shape3"};

    /** E as seis que ficam paradas. */
    private static final String[] PARADAS = {"tube1", "tube2", "tube3", "tube4", "tube5", "shape4"};

    private final ModelPart raiz;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        long batidas;
    }

    public DemonHeartRenderer(BlockEntityRendererProvider.Context contexto) {
        this.raiz = contexto.bakeLayer(DemonHeartModel.CORAÇÃO);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DemonHeartBlockEntity coração, State estado, float parcial, Vec3 câmara,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay quebrando) {
        BlockEntityRenderer.super.extractRenderState(coração, estado, parcial, câmara, quebrando);
        var feitio = coração.getBlockState();
        estado.facing = feitio.hasProperty(HorizontalDirectionalBlock.FACING)
                ? feitio.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
        estado.batidas = coração.batidas();
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor, CameraRenderState câmara) {
        RenderType tipo = RenderTypes.entityCutout(FOLHA);
        int luz = estado.lightCoords;

        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.scale(TAMANHO, TAMANHO, TAMANHO);
        pose.mulPose(Axis.YP.rotationDegrees(giro(estado.facing)));
        pose.translate(DESLOCA_X, DESLOCA_Y, DESLOCA_Z);

        for (String qual : PARADAS) desenha(coletor, pose, tipo, luz, qual);

        /*
         * E o músculo, que incha em volta de um ponto um bloco acima do que o resto usa. O original faz esta
         * conta no meio do desenho, entre os canos e o músculo, e é de propósito: assim os canos ficam presos
         * onde estavam enquanto a carne se move por baixo deles.
         */
        pose.pushPose();
        float quanto = DemonHeartModel.inchaço(estado.batidas);
        pose.translate(0.0f, 1.0f, 0.0f);
        pose.scale(quanto, quanto, quanto);
        pose.translate(0.0f, -1.0f, 0.0f);
        for (String qual : MÚSCULO) desenha(coletor, pose, tipo, luz, qual);
        pose.popPose();

        pose.popPose();
    }

    /** Para onde o coração olha, pelos quatro ângulos do original. */
    public static float giro(Direction para) {
        return switch (para) {
            case SOUTH -> 180.0f;
            case WEST -> 270.0f;
            case EAST -> 90.0f;
            default -> 0.0f;
        };
    }

    private void desenha(SubmitNodeCollector coletor, PoseStack pose, RenderType tipo, int luz, String qual) {
        coletor.submitModelPart(this.raiz.getChild(qual), pose, tipo, luz, OverlayTexture.NO_OVERLAY, null);
    }
}
