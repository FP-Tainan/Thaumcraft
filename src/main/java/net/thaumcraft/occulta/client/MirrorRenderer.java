package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.thaumcraft.client.render.BoxMesh;
import net.thaumcraft.client.render.MeshDrawer;
import net.thaumcraft.occulta.mirror.MirrorBlock;
import net.thaumcraft.occulta.mirror.MirrorBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O Espelho no mundo: o {@code RenderMirror} e o {@code ModelMirror} do Witchery.
 *
 * <p>São dezenove peças, e a moldura é <b>oval</b>: um arco de peças miúdas em cima e duas colunas dos lados. A
 * mesma figura serve às duas metades do bloco — a de baixo desenha-se <b>de cabeça para baixo</b>, e é assim que o
 * arco de cima vira o arco de baixo e o oval se fecha.
 *
 * <p>O vidro do meio troca de figura quando há <b>alguém à frente</b>: é a segunda folha do original, desenhada por
 * cima da primeira. A conta de quantos estão à frente corre no cliente também, e por isso não espera pela rede.
 */
public class MirrorRenderer implements BlockEntityRenderer<MirrorBlockEntity, MirrorRenderer.State> {
    private static final Identifier FRAME = Thaumcraft.id("textures/models/witch_mirror.png");
    private static final Identifier SEEN = Thaumcraft.id("textures/models/witch_mirror_seen.png");

    /** O fundo do espelho, que é o vidro. */
    private static final float[] BACK_MIDDLE = BoxMesh.box(-5.0f, -7.0f, -1.0f, 10, 15, 1, 7, 16, 32, 32);

    /** As duas ombreiras de trás. */
    private static final float[] BACK_SIDES = BoxMesh.join(
            BoxMesh.box(-7.0f, -6.0f, -1.0f, 2, 14, 1, 0, 17, 32, 32),
            BoxMesh.mirror(BoxMesh.box(5.0f, -6.0f, -1.0f, 2, 14, 1, 0, 17, 32, 32)));

    /** A moldura de fora: as duas colunas, o arco e o topo. */
    private static final float[] OUTER = BoxMesh.join(
            BoxMesh.box(-7.5f, -3.0f, -1.0f, 1, 11, 1, 0, 0, 32, 32),
            BoxMesh.mirror(BoxMesh.box(6.5f, -3.0f, -1.0f, 1, 11, 1, 0, 0, 32, 32)),
            BoxMesh.box(-8.0f, -5.0f, -1.0f, 1, 2, 1, 5, 0, 32, 32),
            BoxMesh.mirror(BoxMesh.box(7.0f, -5.0f, -1.0f, 1, 2, 1, 5, 0, 32, 32)),
            BoxMesh.box(-7.5f, -6.0f, -1.0f, 2, 1, 1, 10, 0, 32, 32),
            BoxMesh.mirror(BoxMesh.box(5.5f, -6.0f, -1.0f, 2, 1, 1, 10, 0, 32, 32)),
            BoxMesh.box(-6.0f, -7.0f, -1.0f, 1, 1, 1, 17, 0, 32, 32),
            BoxMesh.mirror(BoxMesh.box(5.0f, -7.0f, -1.0f, 1, 1, 1, 17, 0, 32, 32)),
            BoxMesh.box(-5.0f, -7.5f, -1.0f, 10, 1, 1, 4, 3, 32, 32));

    /** E a de dentro. */
    private static final float[] INNER = BoxMesh.join(
            BoxMesh.box(-6.0f, -2.0f, -1.0f, 1, 10, 1, 5, 5, 32, 32),
            BoxMesh.mirror(BoxMesh.box(5.0f, -2.0f, -1.0f, 1, 10, 1, 5, 5, 32, 32)),
            BoxMesh.box(-5.0f, -3.0f, -1.0f, 1, 2, 1, 10, 6, 32, 32),
            BoxMesh.mirror(BoxMesh.box(4.0f, -3.0f, -1.0f, 1, 2, 1, 10, 6, 32, 32)),
            BoxMesh.box(-4.0f, -4.0f, -1.0f, 1, 2, 1, 15, 6, 32, 32),
            BoxMesh.mirror(BoxMesh.box(3.0f, -4.0f, -1.0f, 1, 2, 1, 15, 6, 32, 32)),
            BoxMesh.box(-3.0f, -4.0f, -1.0f, 6, 1, 1, 10, 10, 32, 32));

    /** A moldura toda, menos o vidro: é ela que se desenha sempre. */
    private static final float[] SHELL = BoxMesh.join(OUTER, INNER, BACK_SIDES);

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        boolean upper;
        boolean seen;
    }

    public MirrorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MirrorBlockEntity espelho, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(espelho, state, partial, camera, crumbling);
        var feitio = espelho.getBlockState();
        state.facing = feitio.hasProperty(MirrorBlock.FACING)
                ? feitio.getValue(MirrorBlock.FACING) : Direction.NORTH;
        state.upper = !feitio.hasProperty(MirrorBlock.HALF)
                || feitio.getValue(MirrorBlock.HALF)
                == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER;
        state.seen = espelho.men() > 0;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        // o caminho do original: meio do bloco, de cabeça para baixo, o giro da marca, e a meia-volta da metade
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.mulPose(Axis.YP.rotationDegrees(angle(state.facing)));
        if (!state.upper) pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, 0.1f, 0.42f);
        pose.translate(0.0f, -0.1f, 0.02f);
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);

        draw(pose, collector, SHELL, FRAME, state.lightCoords);
        draw(pose, collector, BACK_MIDDLE, state.seen ? SEEN : FRAME, state.lightCoords);
        pose.popPose();
    }

    /** O giro de cada marca: os quatro do original, pela ordem dele. */
    private static float angle(Direction facing) {
        return switch (facing) {
            case SOUTH -> 180.0f;
            case WEST -> 270.0f;
            case EAST -> 90.0f;
            default -> 0.0f;
        };
    }

    private static void draw(PoseStack pose, SubmitNodeCollector collector, float[] mesh, Identifier folha,
                             int light) {
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(folha),
                (m, v) -> MeshDrawer.draw(mesh, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
    }
}
