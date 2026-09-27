package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.BoxMesh;
import net.thaumcraft.client.render.MeshDrawer;
import net.thaumcraft.occulta.DistilleryBlock;
import net.thaumcraft.occulta.DistilleryBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A Destilaria no mundo: o {@code RenderDistillery} e o {@code ModelDistillery} do Witchery.
 *
 * <p>São nove peças — o alambique de três andares com o cano torto, e a armação de quatro — mais até
 * <b>quatro garrafas</b>: uma por pote de barro que estiver na casa deles, que é o que o original desenha.
 *
 * <p>As garrafas são as únicas do modelo que ligam o espelho <b>antes</b> de a caixa entrar, que é quando ele
 * vale (ver o {@code BoxMesh.mirror}); nas outras o espelho vem depois e não faz nada.
 */
public class DistilleryRenderer implements BlockEntityRenderer<DistilleryBlockEntity, DistilleryRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/distillery.png");

    // o alambique
    private static final float[] STILL_BASE = BoxMesh.box(0, 0, 0, 10, 6, 10, 0, 16, 64, 64);
    private static final float[] STILL_MIDDLE = BoxMesh.box(0, 0, 0, 6, 4, 6, 0, 6, 64, 64);
    private static final float[] STILL_TOP = BoxMesh.box(0, 0, 0, 4, 3, 4, 25, 9, 64, 64);
    private static final float[] STILL_BEND = BoxMesh.box(0, 0, 0, 2, 2, 4, 0, 0, 64, 64);
    private static final float[] STILL_TUBE = BoxMesh.box(-0.5f, -0.5f, 0, 1, 1, 8, 46, 10, 64, 64);

    // a armação
    private static final float[] FRAME_TOP = BoxMesh.box(0, 0, 0, 16, 1, 1, 30, 6, 64, 64);
    private static final float[] FRAME_SIDE = BoxMesh.box(0, 0, 0, 1, 7, 1, 47, 24, 64, 64);
    private static final float[] FRAME_BASE = BoxMesh.box(0, 0, 0, 16, 1, 5, 22, 0, 64, 64);

    // e a garrafa, de três caixas espelhadas
    private static final float[] BOTTLE = BoxMesh.join(
            BoxMesh.mirror(BoxMesh.box(0, 2, 0, 3, 3, 3, 52, 26, 64, 64)),
            BoxMesh.mirror(BoxMesh.box(1, 1, 1, 1, 1, 1, 60, 24, 64, 64)),
            BoxMesh.mirror(BoxMesh.box(0.5f, 0, 0.5f, 2, 1, 2, 56, 21, 64, 64)));

    /** A inclinação do cano: os {@code -2.341978} radianos do original, em graus. */
    private static final float TUBE_TILT = (float) Math.toDegrees(-2.341978);

    /** E a da segunda garrafa, que o original deixa um grau fora de prumo. */
    private static final float BOTTLE2_TILT = (float) Math.toDegrees(0.0174533);

    /** Onde cada garrafa fica: os quatro pontos do original. */
    private static final float[][] BOTTLES = {{-7.0f, 16.0f, -7.0f}, {-3.3f, 16.0f, -7.0f},
            {0.4f, 16.0f, -7.0f}, {4.0f, 16.0f, -7.0f}};

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        int jars;
    }

    public DistilleryRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(DistilleryBlockEntity destilaria, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(destilaria, state, partial, camera, crumbling);
        state.facing = destilaria.getBlockState().hasProperty(DistilleryBlock.FACING)
                ? destilaria.getBlockState().getValue(DistilleryBlock.FACING) : Direction.NORTH;
        state.jars = destilaria.getItem(DistilleryBlockEntity.JARS).getCount();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        distillery(pose, collector, state.facing, state.jars, state.lightCoords);
        pose.popPose();
    }

    /** O modelo inteiro, virado para onde a destilaria olha. */
    static void distillery(PoseStack pose, SubmitNodeCollector collector, Direction facing, int potes, int light) {
        pose.pushPose();
        // o mesmo caminho do original: meio do bloco, de cabeça para baixo, um bloco abaixo, e o giro
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);
        pose.mulPose(Axis.YP.rotationDegrees(angle(facing)));

        part(pose, collector, STILL_BASE, -5.0f, 18.0f, -2.0f, 0.0f, light);
        part(pose, collector, STILL_MIDDLE, -3.0f, 14.0f, 0.0f, 0.0f, light);
        part(pose, collector, STILL_TOP, -2.0f, 11.0f, 1.0f, 0.0f, light);
        part(pose, collector, STILL_BEND, -1.0f, 9.0f, -1.0f, 0.0f, light);
        part(pose, collector, STILL_TUBE, 0.0f, 10.0f, 0.0f, TUBE_TILT, light);
        part(pose, collector, FRAME_TOP, -8.0f, 15.0f, -6.0f, 0.0f, light);
        part(pose, collector, FRAME_SIDE, -8.0f, 16.0f, -6.0f, 0.0f, light);
        part(pose, collector, FRAME_SIDE, 7.0f, 16.0f, -6.0f, 0.0f, light);
        part(pose, collector, FRAME_BASE, -8.0f, 23.0f, -8.0f, 0.0f, light);

        // uma garrafa por pote de barro guardado, até quatro
        for (int i = 0; i < Math.min(potes, BOTTLES.length); i++) {
            part(pose, collector, BOTTLE, BOTTLES[i][0], BOTTLES[i][1], BOTTLES[i][2],
                    i == 1 ? BOTTLE2_TILT : 0.0f, light);
        }
        pose.popPose();
    }

    /** O giro de cada marca: norte não gira, sul dá meia-volta, oeste e leste um quarto. */
    private static float angle(Direction facing) {
        return switch (facing) {
            case SOUTH -> 180.0f;
            case WEST -> 270.0f;
            case EAST -> 90.0f;
            default -> 0.0f;
        };
    }

    /** Uma peça no ponto de giro dela, com a inclinação que ela tiver em torno do X. */
    private static void part(PoseStack pose, SubmitNodeCollector collector, float[] mesh,
                             float px, float py, float pz, float tilt, int light) {
        pose.pushPose();
        pose.translate(px / 16.0f, py / 16.0f, pz / 16.0f);
        if (tilt != 0.0f) pose.mulPose(Axis.XP.rotationDegrees(tilt));
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                (m, v) -> MeshDrawer.draw(mesh, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }
}
