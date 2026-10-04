package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.GrassperBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O Apanha-Erva no mundo: o {@code RenderGrassper} do Witchery.
 *
 * <p>O bloco não se desenha; quem o põe no mundo é este desenhista. Ele vira o modelo de cabeça para baixo,
 * gira-o para o lado em que foi posto, e desenha por cima a coisa que a planta segura — <b>girando
 * devagar</b>, a três quartos do tamanho, logo acima da boca.
 *
 * <p>O giro é o que faz a coisa na boca dela ser <b>vista</b>. Um item parado num canto do mundo some na
 * paisagem; um item que roda chama o olho, e é por ele que se lê uma receita de quatro Apanha-Ervas sem
 * precisar de chegar perto.
 */
public class GrassperRenderer implements BlockEntityRenderer<GrassperBlockEntity,
        GrassperRenderer.State> {
    private static final Identifier FOLHA = Thaumcraft.id("textures/block/grassper.png");

    /** O quanto a coisa na boca encolhe, e onde ela fica. */
    public static final float TAMANHO = 0.75f;
    public static final float ACIMA = 0.6f;
    public static final float DESLOCA = 0.65f;
    public static final float DESCE = -0.15f;

    /** E o quanto ela roda: uma volta a cada doze segundos, como no original. */
    public static final float VOLTA = 10.0f;

    private final ModelPart raiz;
    private final ItemModelResolver items;

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState naBoca = new ItemStackRenderState();
        boolean temAlgo;
        Direction facing = Direction.NORTH;
    }

    public GrassperRenderer(BlockEntityRendererProvider.Context contexto) {
        this.raiz = contexto.bakeLayer(GrassperModel.APANHA_ERVA);
        this.items = contexto.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(GrassperBlockEntity planta, State estado, float parcial, Vec3 câmara,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay quebrando) {
        BlockEntityRenderer.super.extractRenderState(planta, estado, parcial, câmara, quebrando);
        var feitio = planta.getBlockState();
        estado.facing = feitio.hasProperty(HorizontalDirectionalBlock.FACING)
                ? feitio.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;

        ItemStack tem = planta.naBoca();
        estado.temAlgo = !tem.isEmpty() && planta.getLevel() != null;
        if (estado.temAlgo) {
            this.items.updateForTopItem(estado.naBoca, tem.copyWithCount(1), ItemDisplayContext.FIXED,
                    planta.getLevel(), null, 0);
        }
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor, CameraRenderState câmara) {
        RenderType tipo = RenderTypes.entityCutout(FOLHA);
        int luz = estado.lightCoords;

        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);
        pose.mulPose(Axis.YP.rotationDegrees(giro(estado.facing)));
        for (String qual : new String[]{"stalktop", "leafright", "leaffront", "leafback", "leafleft",
                "petalbackright", "stalkbottom", "petalfrontright", "petalbackleft", "petalfrontleft"}) {
            coletor.submitModelPart(this.raiz.getChild(qual), pose, tipo, luz,
                    OverlayTexture.NO_OVERLAY, null);
        }
        pose.popPose();

        if (!estado.temAlgo) return;
        pose.pushPose();
        pose.translate(0.0f, ACIMA, 0.0f);
        pose.scale(TAMANHO, TAMANHO, TAMANHO);
        pose.translate(DESLOCA, DESCE, DESLOCA);
        pose.mulPose(Axis.YP.rotationDegrees((float) (System.currentTimeMillis() % 36000L) / VOLTA));
        estado.naBoca.submit(pose, coletor, luz, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
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
