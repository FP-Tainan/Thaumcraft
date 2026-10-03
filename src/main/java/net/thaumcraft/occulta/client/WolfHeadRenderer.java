package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.wolf.WolfHeadBlock;
import net.thaumcraft.occulta.wolf.WolfHeadBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A Cabeça de Lobo desenhada: o {@code RenderWolfHead} e o {@code ModelWolfHead} do Witchery.
 *
 * <p>Quatro caixas — a cabeça, as duas orelhas e o focinho — e a <b>pele do lobo do jogo</b>, sem folha nova.
 * É o crânio do jogo com outro modelo, e por isso ela também é desenhada <b>de cabeça para baixo</b> e posta
 * de pé pelo desenhista.
 *
 * <p>No chão ela assenta na casa e gira; na parede ela sai dela a meia altura, e é o lado que lhe dá o giro.
 */
public class WolfHeadRenderer
        implements BlockEntityRenderer<WolfHeadBlockEntity, WolfHeadRenderer.State> {
    public static final ModelLayerLocation WOLF_HEAD =
            new ModelLayerLocation(Thaumcraft.id("mounted_wolf_head"), "main");

    /** A pele do lobo do jogo, que é a que o original usa. */
    private static final Identifier PELE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/entity/wolf/wolf.png");

    /** A que altura a cabeça da parede sai, e a que fundo. */
    public static final float NA_PAREDE = 0.25f;
    public static final float FUNDO = 0.74f;

    private final ModelPart cabeça;

    public static class State extends BlockEntityRenderState {
        float giro;
        @Nullable
        Direction parede;
    }

    public WolfHeadRenderer(BlockEntityRendererProvider.Context contexto) {
        this.cabeça = contexto.bakeLayer(WOLF_HEAD).getChild("head");
    }

    /** Sessenta e quatro por trinta e dois, que é a pele do lobo do jogo. */
    public static LayerDefinition wolfHead() {
        MeshDefinition malha = new MeshDefinition();
        malha.getRoot().addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0f, -6.0f, 0.0f, 6.0f, 6.0f, 4.0f)
                        .texOffs(16, 14).addBox(-3.0f, -8.0f, 3.0f, 2.0f, 2.0f, 1.0f)
                        .texOffs(16, 14).addBox(1.0f, -8.0f, 3.0f, 2.0f, 2.0f, 1.0f)
                        .texOffs(0, 10).addBox(-1.5f, -3.1f, -3.0f, 3.0f, 3.0f, 4.0f),
                PartPose.ZERO);
        return LayerDefinition.create(malha, 64, 32);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WolfHeadBlockEntity cabeça, State estado, float parcial, Vec3 câmara,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay quebrando) {
        BlockEntityRenderer.super.extractRenderState(cabeça, estado, parcial, câmara, quebrando);
        var feitio = cabeça.getBlockState();
        estado.giro = WolfHeadBlock.giro(feitio);
        estado.parede = WolfHeadBlock.parede(feitio);
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor, CameraRenderState câmara) {
        desenha(pose, coletor, this.cabeça, estado.lightCoords, estado.giro, estado.parede);
    }

    /** O desenho dela, que serve ao bloco no mundo e à cabeça na mão. */
    static void desenha(PoseStack pose, SubmitNodeCollector coletor, ModelPart cabeça, int luz, float giro,
                        @Nullable Direction parede) {
        pose.pushPose();
        if (parede == null) {
            pose.translate(0.5f, 0.0f, 0.5f);
        } else {
            pose.translate(0.5f - parede.getStepX() * (FUNDO - 0.5f), NA_PAREDE,
                    0.5f - parede.getStepZ() * (FUNDO - 0.5f));
        }
        // o de cabeça para baixo do original, que é o glScalef(-1, -1, 1) dele
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.mulPose(Axis.YP.rotationDegrees(giro));

        coletor.submitModelPart(cabeça, pose, RenderTypes.entityCutout(PELE), luz,
                OverlayTexture.NO_OVERLAY, null);
        pose.popPose();
    }
}
