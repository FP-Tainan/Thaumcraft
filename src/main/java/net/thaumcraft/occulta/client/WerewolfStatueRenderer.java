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
import net.thaumcraft.occulta.wolf.WerewolfStatueBlock;
import net.thaumcraft.occulta.wolf.WerewolfStatueBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A Estátua do Lobisomem no mundo: o {@code RenderStatueWerewolf} do Witchery.
 *
 * <p>Ela é construída de <b>cabeça para baixo</b> e posta de pé aqui, com um meio-giro em Z e um bloco para
 * baixo — é o que o original faz, e é o jeito de 2014 de desenhar uma estátua alta numa casa de um bloco.
 *
 * <p>Depois vira-se para o lado que ela olha, que é por onde ela larga o que dá.
 */
public class WerewolfStatueRenderer
        implements BlockEntityRenderer<WerewolfStatueBlockEntity, WerewolfStatueRenderer.State> {
    private static final Identifier FOLHA = Thaumcraft.id("textures/models/werewolf_statue.png");

    private final ModelPart raiz;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
    }

    public WerewolfStatueRenderer(BlockEntityRendererProvider.Context contexto) {
        this.raiz = contexto.bakeLayer(WerewolfStatueModel.STATUE);
        WerewolfStatueModel.encolhe(this.raiz);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WerewolfStatueBlockEntity estátua, State estado, float parcial,
                                   Vec3 câmara, ModelFeatureRenderer.@Nullable CrumblingOverlay quebrando) {
        BlockEntityRenderer.super.extractRenderState(estátua, estado, parcial, câmara, quebrando);
        var feitio = estátua.getBlockState();
        estado.facing = feitio.hasProperty(WerewolfStatueBlock.FACING)
                ? feitio.getValue(WerewolfStatueBlock.FACING) : Direction.NORTH;
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor, CameraRenderState câmara) {
        desenha(pose, coletor, this.raiz, estado.lightCoords,
                SpinningWheelRenderer.angle(estado.facing), false);
    }

    /**
     * O desenho dela, que serve ao bloco no mundo e à estátua na mão.
     *
     * <p>A ordem é a do original, e ela importa: o meio-giro em Z <b>antes</b> de tudo o mais, porque é ele
     * que põe de pé um modelo construído de cabeça para baixo. O que a estátua na mão faz a mais — encolher,
     * virar de frente e levantar — acontece <b>depois</b> do giro, no mesmo lugar em que o original o faz.
     */
    static void desenha(PoseStack pose, SubmitNodeCollector coletor, ModelPart raiz, int luz, float giro,
                        boolean naMão) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);
        if (naMão) {
            pose.scale(WerewolfStatueItemRenderer.ENCOLHE, WerewolfStatueItemRenderer.ENCOLHE,
                    WerewolfStatueItemRenderer.ENCOLHE);
            pose.mulPose(Axis.YP.rotationDegrees(WerewolfStatueItemRenderer.DE_FRENTE));
            pose.translate(0.0f, WerewolfStatueItemRenderer.LEVANTA, 0.0f);
        } else {
            pose.mulPose(Axis.YP.rotationDegrees(giro));
        }

        RenderType tipo = RenderTypes.entityCutout(FOLHA);
        for (String nome : WerewolfStatueModel.RAÍZES) {
            coletor.submitModelPart(raiz.getChild(nome), pose, tipo, luz, OverlayTexture.NO_OVERLAY, null);
        }
        pose.popPose();
    }
}
