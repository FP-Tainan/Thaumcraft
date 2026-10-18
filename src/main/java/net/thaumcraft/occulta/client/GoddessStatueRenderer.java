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
import net.thaumcraft.occulta.GoddessStatueBlock;
import net.thaumcraft.occulta.GoddessStatueBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A Estátua da Deusa no mundo: o {@code RenderGoddess} do Witchery.
 *
 * <p>Como as outras estátuas deste ramo, ela é construída de <b>cabeça para baixo</b> e posta de pé aqui,
 * com um meio-giro em Z e um bloco para baixo. Depois vira-se para o lado que ela olha.
 */
public class GoddessStatueRenderer
        implements BlockEntityRenderer<GoddessStatueBlockEntity, GoddessStatueRenderer.State> {
    private static final Identifier FOLHA = Thaumcraft.id("textures/models/goddess_statue.png");

    private final ModelPart raiz;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
    }

    public GoddessStatueRenderer(BlockEntityRendererProvider.Context contexto) {
        this.raiz = contexto.bakeLayer(GoddessStatueModel.STATUE);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GoddessStatueBlockEntity estátua, State estado, float parcial,
                                   Vec3 câmara, ModelFeatureRenderer.@Nullable CrumblingOverlay quebrando) {
        BlockEntityRenderer.super.extractRenderState(estátua, estado, parcial, câmara, quebrando);
        var feitio = estátua.getBlockState();
        estado.facing = feitio.hasProperty(GoddessStatueBlock.FACING)
                ? feitio.getValue(GoddessStatueBlock.FACING) : Direction.NORTH;
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor,
                       CameraRenderState câmara) {
        desenha(pose, coletor, this.raiz, estado.lightCoords,
                SpinningWheelRenderer.angle(estado.facing), false);
    }

    /** O desenho dela, que serve ao bloco no mundo e à estátua na mão. */
    static void desenha(PoseStack pose, SubmitNodeCollector coletor, ModelPart raiz, int luz, float giro,
                        boolean naMão) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);
        if (naMão) {
            pose.scale(GoddessStatueItemRenderer.ENCOLHE, GoddessStatueItemRenderer.ENCOLHE,
                    GoddessStatueItemRenderer.ENCOLHE);
            pose.mulPose(Axis.YP.rotationDegrees(GoddessStatueItemRenderer.DE_FRENTE));
            pose.translate(0.0f, GoddessStatueItemRenderer.LEVANTA, 0.0f);
        } else {
            pose.mulPose(Axis.YP.rotationDegrees(giro));
        }

        RenderType tipo = RenderTypes.entityCutout(FOLHA);
        for (String nome : GoddessStatueModel.PEÇAS) {
            coletor.submitModelPart(raiz.getChild(nome), pose, tipo, luz, OverlayTexture.NO_OVERLAY, null);
        }
        pose.popPose();
    }
}
