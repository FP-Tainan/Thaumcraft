package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.occulta.PlacedItemBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O Item Posto no mundo: o {@code RenderPlacedItem} do Witchery.
 *
 * <p>A coisa é desenhada <b>deitada</b> — uma volta de quarto em X a põe com a cara para cima — e virada
 * para o lado de quem a pôs. É o mesmo desenho de um item largado no chão, menos as duas coisas que o
 * tornariam vivo: ele <b>não boia</b> e <b>não gira</b>.
 *
 * <p>Essa quietude é o ponto. Uma faca largada no chão de um altar é lixo; uma faca <b>deitada</b> nele é um
 * instrumento. O original gastou um bloco inteiro para fazer essa diferença, e ela só se vê porque a coisa
 * está parada.
 */
public class PlacedItemRenderer implements BlockEntityRenderer<PlacedItemBlockEntity,
        PlacedItemRenderer.State> {
    /** A altura a que ela se deita: a lasca do original. */
    public static final float RENTE = 0.05f;

    private final ItemModelResolver modelos;

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState oquê = new ItemStackRenderState();
        boolean temAlgo;
        Direction para = Direction.NORTH;
    }

    public PlacedItemRenderer(BlockEntityRendererProvider.Context contexto) {
        this.modelos = contexto.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PlacedItemBlockEntity posto, State estado, float parcial, Vec3 câmara,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay quebrando) {
        BlockEntityRenderer.super.extractRenderState(posto, estado, parcial, câmara, quebrando);
        var feitio = posto.getBlockState();
        estado.para = feitio.hasProperty(HorizontalDirectionalBlock.FACING)
                ? feitio.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
        ItemStack oquê = posto.oquê();
        estado.temAlgo = !oquê.isEmpty() && posto.getLevel() != null;
        if (estado.temAlgo) {
            this.modelos.updateForTopItem(estado.oquê, oquê.copyWithCount(1), ItemDisplayContext.FIXED,
                    posto.getLevel(), null, 0);
        }
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor,
                       CameraRenderState câmara) {
        if (!estado.temAlgo) return;
        pose.pushPose();
        pose.translate(0.5f, RENTE, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(giro(estado.para)));
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        estado.oquê.submit(pose, coletor, estado.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }

    /** Os quatro ângulos do original, que ele escreve pelos números dos lados de 2014. */
    public static float giro(Direction para) {
        return switch (para) {
            case SOUTH -> 180.0f;
            case WEST -> 90.0f;
            case EAST -> 270.0f;
            default -> 0.0f;
        };
    }
}
