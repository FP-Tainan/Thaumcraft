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
import net.thaumcraft.block.TubeValveBlock;
import net.thaumcraft.block.entity.TubeValveBlockEntity;

/**
 * A roda de registro da válvula. É o {@code TileTubeValveRenderer} da 4.2.3.5, descompilado.
 *
 * <p>Ela tem duas peças. A haste é a única caixa do {@code ModelTubeValve}: dois por dois por dois,
 * saindo do meio do cano. E a roda é a própria textura {@code pipe_valve} — as pegas de latão em volta
 * de um cubo escuro — desenhada como o jogo desenha um item na mão: a figura chata com um ponto de
 * espessura, deitada por cima da haste, com meio bloco de largura.
 *
 * <p>Fechando, a roda dá uma volta e meia e afunda doze centésimos de bloco, como registro de verdade
 * rosqueando. São as duas coisas juntas que dizem de longe se aquela válvula está aberta.
 */
public class TubeValveRenderer implements BlockEntityRenderer<TubeValveBlockEntity, TubeValveRenderer.State> {
    private static final Identifier ROD_TEXTURE = Thaumcraft.id("textures/models/valve.png");
    private static final Identifier WHEEL_TEXTURE = Thaumcraft.id("textures/block/pipe_valve.png");

    /** A haste, nas medidas do modelo original. */
    private static final float[] ROD = BoxMesh.box(-1.0f, 2.0f, -1.0f, 2.0f, 2.0f, 2.0f, 0.0f, 10.0f, 64.0f, 32.0f);
    /** Do tamanho do modelo para o do bloco. */
    private static final float UNIT = 1.0f / 16.0f;
    /** Quantos pontos a figura da roda tem de lado. */
    private static final int WHEEL_PIXELS = 16;

    /** O que o desenhista precisa saber da válvula neste quadro. */
    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.UP;
        public float rotation;
    }

    public TubeValveRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TubeValveBlockEntity valve, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(valve, state, crumbling);
        var block = valve.getBlockState();
        state.facing = block.hasProperty(TubeValveBlock.FACING)
                ? block.getValue(TubeValveBlock.FACING) : Direction.UP;
        state.rotation = valve.rotation();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;

        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        // a roda é desenhada apontando para cima; daqui ela vai para o lado do manípulo
        pose.mulPose(upTowards(state.facing));
        // fechando, uma volta e meia e doze centésimos para dentro
        pose.mulPose(Axis.YP.rotationDegrees(-state.rotation * 1.5f));
        pose.translate(0.0f, -(state.rotation / TubeValveBlockEntity.CLOSED) * 0.12f, 0.0f);

        // a haste
        pose.pushPose();
        pose.scale(UNIT, UNIT, UNIT);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(ROD_TEXTURE), (matrix, consumer) ->
                MeshDrawer.draw(ROD, matrix, consumer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();

        // e a roda: a figura deitada, meio bloco de largura, por cima da haste
        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        pose.translate(-0.25f, -0.25f, -0.25f);
        pose.scale(0.5f, 0.5f, 0.5f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(WHEEL_TEXTURE), (matrix, consumer) ->
                ExtrudedSprite.draw(matrix, consumer, WHEEL_PIXELS, 0.1f, light, OverlayTexture.NO_OVERLAY,
                        0xFFFFFFFF));
        pose.popPose();

        pose.popPose();
    }

    /** O giro que leva o eixo de cima até a face pedida. */
    private static org.joml.Quaternionf upTowards(Direction face) {
        return switch (face) {
            case UP -> Axis.XP.rotationDegrees(0.0f);
            case DOWN -> Axis.XP.rotationDegrees(180.0f);
            case NORTH -> Axis.XP.rotationDegrees(-90.0f);
            case SOUTH -> Axis.XP.rotationDegrees(90.0f);
            case WEST -> Axis.ZP.rotationDegrees(90.0f);
            case EAST -> Axis.ZP.rotationDegrees(-90.0f);
        };
    }
}
