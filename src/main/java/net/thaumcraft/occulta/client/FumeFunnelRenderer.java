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
import net.thaumcraft.occulta.FumeFunnelBlock;
import net.thaumcraft.occulta.FumeFunnelBlockEntity;
import net.thaumcraft.occulta.WitchesOvenBlock;
import org.jetbrains.annotations.Nullable;

/**
 * O Funil de Fumos no mundo: o {@code RenderFumeFunnel} e o {@code ModelFumeFunnel} do Witchery.
 *
 * <p>O feitio dele muda com o que tem em volta, e é isso que o modelo do original faz:
 *
 * <ul>
 *   <li>com um forno <b>embaixo</b>, o funil vira um cano com chapéu, e o corpo largo some;</li>
 *   <li>sem forno embaixo, é o corpo largo — a base, o bojo e as duas tampas —, mais o filtro se for o com
 *       filtro;</li>
 *   <li>e com um forno <b>de cada lado</b>, sai de lá o tubo e a canalização daquele lado: a de cima para o da
 *       mão esquerda, a de baixo para o da direita.</li>
 * </ul>
 *
 * <p>As duas peças do cano vêm de uma folha de 64 por 128, e o resto de uma de 64 por 64 — é assim no original,
 * que chama {@code setTextureSize} diferente nessas duas.
 */
public class FumeFunnelRenderer implements BlockEntityRenderer<FumeFunnelBlockEntity, FumeFunnelRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/fume_funnel.png");

    // o corpo largo
    private static final float[] BASE = BoxMesh.box(0, 0, 0, 12, 1, 12, 0, 51, 64, 64);
    private static final float[] BODY = BoxMesh.box(0, 0, 0, 10, 11, 10, 4, 27, 64, 64);
    private static final float[] TOP1 = BoxMesh.box(0, 0, 0, 12, 1, 12, 0, 51, 64, 64);
    private static final float[] TOP2 = BoxMesh.box(0, 0, 0, 6, 1, 6, 37, 55, 64, 64);

    // o filtro, que só o funil com filtro mostra
    private static final float[] FILTER_LEFT = BoxMesh.box(0, 0, 0, 1, 1, 2, 0, 0, 64, 64);
    private static final float[] FILTER_RIGHT = BoxMesh.box(0, 0, 0, 1, 1, 2, 0, 0, 64, 64);
    private static final float[] FILTER_MID = BoxMesh.box(0, 0, 0, 6, 1, 1, 24, 0, 64, 64);
    private static final float[] FILTER_CASE = BoxMesh.box(0, 0, 0, 4, 3, 2, 25, 3, 64, 64);

    /**
     * O cano, para quando há forno embaixo.
     *
     * <p><b>Cuidado ao ler o original:</b> estas duas são as únicas do modelo que chamam
     * {@code setTextureSize(64, 128)} — e chamam <i>depois</i> do {@code addBox}, onde aquilo já não vale, como o
     * espelho do Techne. A folha é de 64 por 64 como as outras, e é com ela que as contas saem certas; com 128 o
     * cano ia buscar um pedaço vazio da folha e sumia.
     */
    private static final float[] CHIMNEY = BoxMesh.box(0, 0, 0, 4, 10, 4, 27, 13, 64, 64);
    private static final float[] CHIMNEY_TOP = BoxMesh.box(0, 0, 0, 6, 3, 6, 40, 7, 64, 64);

    // a canalização de cima, do forno da mão esquerda
    private static final float[] TUBE_LEFT = BoxMesh.box(0, 0, 0, 5, 2, 2, 1, 18, 64, 64);
    private static final float[] PIPE_TOP1 = BoxMesh.box(0, 0, 0, 1, 3, 1, 0, 0, 64, 64);
    private static final float[] PIPE_TOP2 = BoxMesh.box(0, 0, 0, 1, 1, 6, 0, 0, 64, 64);
    private static final float[] PIPE_TOP3 = BoxMesh.box(0, 0, 0, 11, 1, 1, 0, 0, 64, 64);
    private static final float[] PIPE_TOP4 = BoxMesh.box(0, 0, 0, 1, 11, 1, 0, 0, 64, 64);
    private static final float[] PIPE_TOP5 = BoxMesh.box(0, 0, 0, 2, 3, 3, 0, 0, 64, 64);

    // e a de baixo, do forno da direita
    private static final float[] TUBE_RIGHT = BoxMesh.box(0, 1, 0, 5, 2, 2, 1, 18, 64, 64);
    private static final float[] PIPE_BOTTOM1 = BoxMesh.box(0, 0, 0, 2, 1, 1, 0, 0, 64, 64);
    private static final float[] PIPE_BOTTOM2 = BoxMesh.box(0, 0, 0, 1, 1, 4, 0, 0, 64, 64);
    private static final float[] PIPE_BOTTOM3 = BoxMesh.box(0, 0, 0, 5, 1, 1, 0, 0, 64, 64);
    private static final float[] PIPE_BOTTOM4 = BoxMesh.box(0, 0, 0, 1, 3, 1, 0, 0, 64, 64);
    private static final float[] PIPE_BOTTOM5 = BoxMesh.box(0, 0, 0, 1, 7, 1, 0, 0, 64, 64);

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        boolean filtered;
        boolean ovenLeft;
        boolean ovenRight;
        boolean ovenBelow;
    }

    public FumeFunnelRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FumeFunnelBlockEntity funil, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(funil, state, partial, camera, crumbling);
        var qual = funil.getBlockState();
        state.facing = qual.hasProperty(FumeFunnelBlock.FACING)
                ? qual.getValue(FumeFunnelBlock.FACING) : Direction.NORTH;
        state.filtered = qual.getBlock() instanceof FumeFunnelBlock bloco && bloco.filtered();
        var level = funil.getLevel();
        var pos = funil.getBlockPos();
        if (level == null) return;
        Direction mão = state.facing.getClockWise();
        state.ovenLeft = isOven(level, pos.relative(mão));
        state.ovenRight = isOven(level, pos.relative(mão.getOpposite()));
        state.ovenBelow = isOven(level, pos.below());
    }

    private static boolean isOven(net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos onde) {
        return level.getBlockState(onde).getBlock() instanceof WitchesOvenBlock;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        funnel(pose, collector, state.facing, state.filtered, state.ovenLeft, state.ovenRight, state.ovenBelow,
                state.lightCoords);
        pose.popPose();
    }

    /** O modelo inteiro, com as peças que o que está em volta manda desenhar. */
    static void funnel(PoseStack pose, SubmitNodeCollector collector, Direction facing, boolean filtered,
                       boolean ovenLeft, boolean ovenRight, boolean ovenBelow, int light) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);
        pose.mulPose(Axis.YP.rotationDegrees(angle(facing)));

        if (ovenLeft) {
            part(pose, collector, TUBE_LEFT, -10.0f, 17.0f, -1.0f, light);
            part(pose, collector, PIPE_TOP1, -4.0f, 8.0f, 3.0f, light);
            part(pose, collector, PIPE_TOP2, -4.0f, 8.0f, -3.0f, light);
            part(pose, collector, PIPE_TOP3, -3.0f, 8.0f, -3.0f, light);
            part(pose, collector, PIPE_TOP4, 7.0f, 9.0f, -3.0f, light);
            part(pose, collector, PIPE_TOP5, 5.0f, 18.0f, -4.0f, light);
        }
        if (ovenRight) {
            part(pose, collector, TUBE_RIGHT, 5.0f, 18.0f, 1.0f, light);
            part(pose, collector, PIPE_BOTTOM1, -7.0f, 13.0f, -3.0f, light);
            part(pose, collector, PIPE_BOTTOM2, -7.0f, 20.0f, -7.0f, light);
            part(pose, collector, PIPE_BOTTOM3, -6.0f, 20.0f, -7.0f, light);
            part(pose, collector, PIPE_BOTTOM4, -2.0f, 21.0f, -7.0f, light);
            part(pose, collector, PIPE_BOTTOM5, -7.0f, 14.0f, -3.0f, light);
        }

        if (ovenBelow) {
            part(pose, collector, CHIMNEY, -2.0f, 14.0f, 3.0f, light);
            part(pose, collector, CHIMNEY_TOP, -3.0f, 11.0f, 2.0f, light);
        } else {
            part(pose, collector, BASE, -6.0f, 23.0f, -6.0f, light);
            part(pose, collector, BODY, -5.0f, 12.0f, -5.0f, light);
            part(pose, collector, TOP1, -6.0f, 11.0f, -6.0f, light);
            part(pose, collector, TOP2, -3.0f, 10.0f, -3.0f, light);
            if (filtered) {
                part(pose, collector, FILTER_LEFT, -4.0f, 14.0f, -7.0f, light);
                part(pose, collector, FILTER_RIGHT, 3.0f, 14.0f, -7.0f, light);
                part(pose, collector, FILTER_MID, -3.0f, 14.0f, -7.0f, light);
                part(pose, collector, FILTER_CASE, -2.0f, 13.0f, -8.0f, light);
            }
        }
        pose.popPose();
    }

    private static float angle(Direction facing) {
        return switch (facing) {
            case SOUTH -> 180.0f;
            case WEST -> 270.0f;
            case EAST -> 90.0f;
            default -> 0.0f;
        };
    }

    private static void part(PoseStack pose, SubmitNodeCollector collector, float[] mesh,
                             float px, float py, float pz, int light) {
        pose.pushPose();
        pose.translate(px / 16.0f, py / 16.0f, pz / 16.0f);
        pose.scale(1.0f / 16.0f, 1.0f / 16.0f, 1.0f / 16.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(TEXTURE),
                (m, v) -> MeshDrawer.draw(mesh, m, v, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));
        pose.popPose();
    }
}
