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
 * O manípulo da válvula.
 *
 * <p>É a única peça dela que não cabe num arquivo de modelo: ele <strong>gira</strong> ao abrir e fechar,
 * e ao mesmo tempo rosqueia para dentro, como manípulo de registro de verdade. São as duas coisas juntas
 * que dizem, de longe, se aquela válvula está aberta — no original é assim que se lê uma tubulação sem
 * ter de bater em cada peça.
 *
 * <p>A caixa é a do {@code ModelTubeValve}: dois por dois por dois, saindo do canto {@code (0, 10)} de
 * uma folha de sessenta e quatro por trinta e dois, que é o {@code models/valve.png} do próprio mod.
 */
public class TubeValveRenderer implements BlockEntityRenderer<TubeValveBlockEntity, TubeValveRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/valve.png");

    /** O manípulo, nas medidas do modelo original. */
    private static final float[] KNOB = BoxMesh.box(-1.0f, 2.0f, -1.0f, 2.0f, 2.0f, 2.0f, 0.0f, 10.0f, 64.0f, 32.0f);
    /** Do tamanho do modelo para o do bloco. */
    private static final float UNIT = 1.0f / 16.0f;

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
        float closed = state.rotation / TubeValveBlockEntity.TURN;

        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        // o manípulo é desenhado apontando para cima; daqui ele vai para o lado do bloco
        pose.mulPose(upTowards(state.facing));
        // fechando, ele rosqueia para dentro enquanto gira
        pose.translate(0.0f, -closed * TubeValveBlockEntity.SCREW, 0.0f);
        pose.mulPose(Axis.YP.rotationDegrees(state.rotation));
        pose.scale(UNIT, UNIT, UNIT);

        int light = state.lightCoords;
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE), (matrix, consumer) ->
                MeshDrawer.draw(KNOB, matrix, consumer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
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
