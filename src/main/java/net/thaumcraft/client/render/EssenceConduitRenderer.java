package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.arcana.machine.EssenceConduitBlock;
import net.thaumcraft.arcana.machine.EssenceConduitBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Conduto de Essência</b>: o {@code EssenceConduitRenderer} e o {@code ModelEssenceConduit} da
 * 1.4.0.009.
 *
 * <p>Um pedestal de quatro caixas e, por cima dele, um <b>cristal a girar</b>. Cada conduto gira no seu
 * ritmo, sorteado quando nasce, e <b>muito devagar</b>: o passo é de cinco milésimos de grau por quadro,
 * que dá menos de um terço de grau por segundo. Uma fileira deles não parece uma fileira de cópias, e
 * nenhum deles parece um pião.
 *
 * <p>O conduto <b>pendura-se na face em que foi posto</b>, e o modelo acompanha — de cabeça para baixo no
 * teto, deitado na parede. As seis posições são as do original.
 *
 * <p><b>O cristal gira só num eixo.</b> O modelo do original guarda três giros e aplica-os a uma caixa
 * {@code Crystal1} que ele <b>nunca desenha</b>; ao cristal de verdade, que é o {@code crystal.obj}, ele
 * aplica <b>um</b> deles, e em volta do eixo vertical. Fica como está.
 */
public class EssenceConduitRenderer implements BlockEntityRenderer<EssenceConduitBlockEntity, EssenceConduitRenderer.State> {
    private static final Identifier PELE = Thaumcraft.id("textures/block/essence_conduit.png");

    /** O quanto o cristal cresce: o {@code glScalef(5, -5, 5)} do original, com o espelho de fora já contado. */
    private static final float ESCALA = 5.0f;

    /**
     * A cor do cristal: o {@code glColor3f} do original com o défice a zero dá <b>ciano puro</b>.
     *
     * <p>A conta lá é {@code (0,906 × défice; 1 − 0,894 × défice; 1 − défice)}, e o desenhista passa-lhe
     * sempre zero — o défice é coisa do Cristal de Défice, que o conduto não usa. Com zero, sai (0; 1; 1).
     */
    private static final int CIANO = 0xFF00FFFF;

    /** E ele desenha-se <b>sem luz</b>: o original desliga a iluminação antes de o desenhar. */
    private static final int SEM_SOMBRA = 0xF000F0;

    /**
     * As quatro caixas do {@code ModelEssenceConduit}, em coordenadas de Techne.
     *
     * <p>O desenhista do original põe o modelo em (0,5; 1,5; 0,5) e espelha o Y e o Z, que é o giro de
     * meia-volta de sempre; por isso as caixas ficam como foram escritas, e é a pose que as vira.
     */
    private static final float[] PEDESTAL = BoxMesh.join(
            BoxMesh.box(-6, 23, -6, 12, 2, 12, 0, 0, 64, 32),
            BoxMesh.box(-4, 18, -4, 8, 5, 8, 0, 18, 64, 32),
            BoxMesh.box(-4, 11, 3, 1, 7, 1, 56, 7, 64, 32),
            BoxMesh.box(3, 11, -4, 1, 7, 1, 49, 1, 64, 32));

    /** A mesma malha, para o desenhista do item na mão. */
    public static float[] pedestal() {
        return PEDESTAL;
    }

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.UP;
        float spin;
    }

    public EssenceConduitRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EssenceConduitBlockEntity conduto, State estado, float parcial, Vec3 câmara,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.@Nullable CrumblingOverlay migalhas) {
        BlockEntityRenderer.super.extractRenderState(conduto, estado, parcial, câmara, migalhas);
        estado.facing = conduto.getBlockState().getValue(EssenceConduitBlock.FACING);
        // o original anda o giro aqui, uma vez por quadro, e não por batida
        conduto.clientTick();
        estado.spin = conduto.rotationZ();
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor, CameraRenderState câmara) {
        pose.pushPose();
        pousa(pose, estado.facing);
        pose.pushPose();
        pose.scale(0.0625f, -0.0625f, -0.0625f);
        coletor.submitCustomGeometry(pose, RenderTypes.entityCutout(PELE),
                (m, v) -> MeshDrawer.draw(PEDESTAL, m, v, estado.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();

        float[] cristal = ObjModel.part("arcana_crystal", null);
        if (cristal.length > 0) {
            pose.pushPose();
            // o translate(0,1,0) e o scale(5,-5,5) do original, já com o espelho de fora dentro da conta
            pose.translate(0.0f, -1.0f, 0.0f);
            pose.scale(ESCALA, ESCALA, -ESCALA);
            pose.mulPose(Axis.YP.rotationDegrees(estado.spin));
            coletor.submitCustomGeometry(pose, RenderTypes.entityTranslucent(PELE),
                    (m, v) -> ObjMesh.draw(cristal, m, v, SEM_SOMBRA, OverlayTexture.NO_OVERLAY, CIANO));
            pose.popPose();
        }
        pose.popPose();
    }

    /** As seis poses do original, uma por face em que o conduto se pode pendurar. */
    private static void pousa(PoseStack pose, Direction onde) {
        switch (onde) {
            case DOWN -> {
                pose.translate(0.5f, -0.5f, 0.5f);
                pose.mulPose(Axis.XP.rotationDegrees(180.0f));
            }
            case NORTH -> {
                pose.translate(0.5f, 0.5f, -0.5f);
                pose.mulPose(Axis.XP.rotationDegrees(270.0f));
            }
            case SOUTH -> {
                pose.translate(0.5f, 0.5f, 1.5f);
                pose.mulPose(Axis.XP.rotationDegrees(90.0f));
            }
            case WEST -> {
                pose.translate(-0.5f, 0.5f, 0.5f);
                pose.mulPose(Axis.ZP.rotationDegrees(90.0f));
            }
            case EAST -> {
                pose.translate(1.5f, 0.5f, 0.5f);
                pose.mulPose(Axis.ZP.rotationDegrees(270.0f));
            }
            default -> pose.translate(0.5f, 1.5f, 0.5f);
        }
    }
}
