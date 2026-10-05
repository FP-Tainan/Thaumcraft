package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.object.skull.SkullModel;
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
import net.thaumcraft.occulta.AlluringSkullBlock;
import net.thaumcraft.occulta.AlluringSkullBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A Caveira do Chamado no mundo: o {@code RenderAlluringSkull} do Witchery.
 *
 * <p>É a <b>cabeça de esqueleto do jogo</b> com uma folha própria — duas, na verdade: uma para a caveira
 * dormindo e outra para a acordada. O original não lhe faz modelo nenhum; pega o do jogo e lhe troca a pele.
 *
 * <p>As contas de onde ela fica são as do original, à letra: no chão ela assenta no meio do bloco, e numa
 * parede ela fica a <b>um quarto de altura</b> e encostada a <b>vinte e seis centésimos</b> da face — que é
 * o que faz a cabeça sair da parede sem a atravessar.
 *
 * <p>O giro vai na <b>pilha</b> e não na peça, porque o jogo de hoje desenha depois: a peça do modelo é uma
 * só, compartilhada por todas as caveiras do mundo, e mexer no ângulo dela antes de mandá-la faria com que
 * <b>todas</b> saíssem com o ângulo da última.
 */
public class AlluringSkullRenderer implements BlockEntityRenderer<AlluringSkullBlockEntity,
        AlluringSkullRenderer.State> {
    /** A nossa cópia da cabeça de esqueleto do jogo. */
    public static final ModelLayerLocation CAVEIRA =
            new ModelLayerLocation(Thaumcraft.id("alluring_skull"), "main");

    /** Dormindo. */
    public static final Identifier FOLHA = Thaumcraft.id("textures/block/alluring_skull.png");

    /** E acordada. */
    public static final Identifier FOLHA_ACESA = Thaumcraft.id("textures/block/alluring_skull_awake.png");

    private final ModelPart raiz;

    public static class State extends BlockEntityRenderState {
        Direction para = Direction.UP;
        int volta;
        boolean acordada;
    }

    public AlluringSkullRenderer(BlockEntityRendererProvider.Context contexto) {
        this.raiz = contexto.bakeLayer(CAVEIRA);
    }

    /** A cabeça do jogo, tal e qual: o original não lhe faz modelo próprio. */
    public static LayerDefinition caveira() {
        return SkullModel.createMobHeadLayer();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AlluringSkullBlockEntity caveira, State estado, float parcial,
                                   Vec3 câmara,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay quebrando) {
        BlockEntityRenderer.super.extractRenderState(caveira, estado, parcial, câmara, quebrando);
        var feitio = caveira.getBlockState();
        estado.para = feitio.getValue(AlluringSkullBlock.FACING);
        estado.volta = feitio.getValue(AlluringSkullBlock.ROTATION);
        estado.acordada = feitio.getValue(AlluringSkullBlock.ACORDADA);
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor,
                       CameraRenderState câmara) {
        desenha(pose, coletor, this.raiz, estado.lightCoords, estado.para, estado.volta, estado.acordada);
    }

    /** Desenha a caveira, no mundo ou na mão. */
    public static void desenha(PoseStack pose, SubmitNodeCollector coletor, ModelPart raiz, int luz,
                               Direction para, int volta, boolean acordada) {
        RenderType tipo = RenderTypes.entityCutoutCull(acordada ? FOLHA_ACESA : FOLHA);

        pose.pushPose();
        float rumo;
        switch (para) {
            case NORTH -> {
                pose.translate(0.5f, 0.25f, 0.74f);
                rumo = 0.0f;
            }
            case SOUTH -> {
                pose.translate(0.5f, 0.25f, 0.26f);
                rumo = 180.0f;
            }
            case WEST -> {
                pose.translate(0.74f, 0.25f, 0.5f);
                rumo = 270.0f;
            }
            case EAST -> {
                pose.translate(0.26f, 0.25f, 0.5f);
                rumo = 90.0f;
            }
            default -> {
                pose.translate(0.5f, 0.0f, 0.5f);
                rumo = volta * 360.0f / 16.0f;
            }
        }
        pose.scale(-1.0f, -1.0f, 1.0f);
        pose.mulPose(Axis.YP.rotationDegrees(rumo));
        coletor.submitModelPart(raiz.getChild("head"), pose, tipo, luz, OverlayTexture.NO_OVERLAY, null);
        pose.popPose();
    }
}
