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
import net.thaumcraft.occulta.WitchesOvenBlock;
import net.thaumcraft.occulta.WitchesOvenBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O Forno das Bruxas no mundo: o {@code RenderWitchesOven} e o {@code ModelWitchesOven} do Witchery.
 *
 * <p>São as nove peças do modelo — o corpo, as duas tampas, o cano com o chapéu e os quatro pés —, numa folha de
 * 64 por 64. Os pés são duas caixinhas cada, e são os únicos do modelo que ligam o espelho <b>antes</b> de a
 * caixa entrar, que é quando ele vale (ver o {@code BoxMesh.mirror}).
 *
 * <p>O jeito de pôr o modelo no bloco é o do original, passo por passo: o modelo é desenhado de cabeça para
 * baixo — meia-volta em torno do Z e um bloco para baixo —, e só então gira para o lado a que o forno olha.
 */
public class WitchesOvenRenderer implements BlockEntityRenderer<WitchesOvenBlockEntity, WitchesOvenRenderer.State> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/witches_oven.png");

    // as cinco peças inteiras do ModelWitchesOven
    private static final float[] BODY = BoxMesh.box(0, 1, 0, 12, 8, 12, 0, 0, 64, 64);
    private static final float[] LID_BOTTOM = BoxMesh.box(0, 0, 0, 14, 1, 14, 0, 20, 64, 64);
    private static final float[] LID_TOP = BoxMesh.box(0, 0, 0, 10, 1, 10, 8, 35, 64, 64);
    private static final float[] CHIMNEY = BoxMesh.box(0, 0, 0, 4, 13, 4, 48, 0, 64, 64);
    private static final float[] CHIMNEY_TOP = BoxMesh.box(0, 0, 0, 4, 4, 1, 38, 0, 64, 64);

    // e os pés, dois de cada lado, com as caixas espelhadas
    private static final float[] LEG_RIGHT = BoxMesh.join(
            BoxMesh.mirror(BoxMesh.box(-2, 0, 0, 2, 1, 1, 0, 0, 64, 64)),
            BoxMesh.mirror(BoxMesh.box(-3, 0, 0, 1, 3, 1, 0, 2, 64, 64)));
    private static final float[] LEG_LEFT = BoxMesh.join(
            BoxMesh.mirror(BoxMesh.box(0, 0, 0, 2, 1, 1, 0, 0, 64, 64)),
            BoxMesh.mirror(BoxMesh.box(2, 0, 0, 1, 3, 1, 0, 2, 64, 64)));

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
    }

    public WitchesOvenRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WitchesOvenBlockEntity forno, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(forno, state, partial, camera, crumbling);
        state.facing = forno.getBlockState().hasProperty(WitchesOvenBlock.FACING)
                ? forno.getBlockState().getValue(WitchesOvenBlock.FACING) : Direction.NORTH;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        oven(pose, collector, state.facing, state.lightCoords);
        pose.popPose();
    }

    /** O modelo inteiro, virado para onde o forno olha. */
    static void oven(PoseStack pose, SubmitNodeCollector collector, Direction facing, int light) {
        pose.pushPose();
        // o mesmo caminho do RenderWitchesOven: meio do bloco, de cabeça para baixo, um bloco abaixo, e o giro
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
        pose.translate(0.0f, -1.0f, 0.0f);
        pose.mulPose(Axis.YP.rotationDegrees(angle(facing)));

        part(pose, collector, BODY, -6.0f, 14.0f, -6.0f, light);
        part(pose, collector, LID_BOTTOM, -7.0f, 14.0f, -7.0f, light);
        part(pose, collector, LID_TOP, -5.0f, 13.0f, -5.0f, light);
        part(pose, collector, CHIMNEY, -2.0f, 8.0f, 3.0f, light);
        part(pose, collector, CHIMNEY_TOP, -2.0f, 8.0f, 7.0f, light);
        part(pose, collector, LEG_RIGHT, -5.0f, 21.0f, -7.0f, light);
        part(pose, collector, LEG_RIGHT, -5.0f, 21.0f, 6.0f, light);
        part(pose, collector, LEG_LEFT, 5.0f, 21.0f, -7.0f, light);
        part(pose, collector, LEG_LEFT, 5.0f, 21.0f, 6.0f, light);
        pose.popPose();
    }

    /** O giro de cada marca do original: norte não gira, sul dá meia-volta, oeste e leste um quarto. */
    private static float angle(Direction facing) {
        return switch (facing) {
            case SOUTH -> 180.0f;
            case WEST -> 270.0f;
            case EAST -> 90.0f;
            default -> 0.0f;
        };
    }

    /** Uma peça no ponto de giro dela; nenhuma das nove tem inclinação. */
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
