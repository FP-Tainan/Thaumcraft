package net.thaumcraft.mortuorum.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.render.BoxMesh;
import net.thaumcraft.client.render.MeshDrawer;
import net.thaumcraft.mortuorum.SewingMachineBlockEntity;

/**
 * A Máquina de Costura no mundo: o {@code TileEntitySewingRenderer} do Necromancy.
 *
 * <p>São cinco caixas do {@code ModelSewing} — a mesa, a agulha e os três pedaços do braço que a segura —, numa
 * folha de 64 por 32. As outras seis peças do modelo original são a manivela, e o {@code render()} dele
 * <b>tem-nas comentadas</b>: nunca aparecem em jogo. Aqui também não.
 *
 * <p>O original não guarda para que lado a máquina está: o bloco não põe nada na marca ao ser assentado, logo a
 * marca é sempre zero e o desenhista gira sempre o mesmo tanto. Fica igual aqui — a máquina olha sempre para o
 * mesmo lado, seja de onde for que se a ponha.
 */
public class SewingMachineRenderer implements BlockEntityRenderer<SewingMachineBlockEntity, BlockEntityRenderState> {
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/sewing_machine.png");

    // as cinco caixas que o render() do ModelSewing desenha, todas no canto 0,0 da folha de 64 por 32
    private static final float[] BASE = BoxMesh.box(0, 0, 0, 12, 1, 6, 0, 0, 64, 32);
    private static final float[] NEEDLE = BoxMesh.box(0, 0, 0, 1, 4, 1, 0, 0, 64, 32);
    private static final float[] ARM1 = BoxMesh.box(0, 0, 0, 2, 6, 2, 0, 0, 64, 32);
    private static final float[] ARM3 = BoxMesh.box(0, 0, 0, 2, 2, 2, 0, 0, 64, 32);
    private static final float[] ARM2 = BoxMesh.box(0, 0, 0, 7, 1, 1, 0, 0, 64, 32);

    public SewingMachineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack pose, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        pose.pushPose();
        // o translate(x, y+2, z+1) com o scale(1,-1,-1) e o translate(0.5,0.5,0.5) do original, já resolvidos,
        // e o giro de 270 graus da marca zero — que no quadro virado dá noventa
        pose.translate(0.5f, 1.5f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(90.0f));
        pose.scale(1.0f, -1.0f, -1.0f);
        boxes(pose, collector, state.lightCoords);
        pose.popPose();
    }

    /** As cinco caixas do {@code ModelSewing}, cada uma no ponto de giro dela. */
    static void boxes(PoseStack pose, SubmitNodeCollector collector, int light) {
        part(pose, collector, BASE, -7.0f, 23.0f, -3.0f, light);
        part(pose, collector, NEEDLE, -6.5f, 18.3f, -0.5f, light);
        part(pose, collector, ARM1, 2.0f, 17.0f, -1.0f, light);
        part(pose, collector, ARM3, -7.0f, 17.1f, -1.0f, light);
        part(pose, collector, ARM2, -5.0f, 17.3f, -0.5f, light);
    }

    /** Uma caixa do modelo, no ponto de giro dela; nenhuma destas cinco tem giro. */
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
