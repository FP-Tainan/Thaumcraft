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
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.arcana.machine.ObeliskBlock;
import net.thaumcraft.arcana.machine.ObeliskBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Obelisco</b>: o {@code EssenceGeneratorRenderer} da 1.4.0.009, a parte dele.
 *
 * <p>Uma agulha de pedra, com <b>três peles</b>: a apagada, a acesa e a de alta potência — que é a que
 * aparece enquanto ainda há mais de dez segundos de combustível a arder.
 *
 * <p>E, estando aceso, <b>as runas por cima</b>: a mesma malha outra vez com outra figura, que escorrega
 * devagar — a horizontal dá uma volta em 32 segundos e a vertical em 28, de modo que o desenho nunca se
 * repete — e que aparece e desaparece ao ritmo de um seno de um segundo.
 */
public class ObeliskRenderer implements BlockEntityRenderer<ObeliskBlockEntity, ObeliskRenderer.State> {
    private static final Identifier APAGADO = Thaumcraft.id("textures/block/obelisk.png");
    private static final Identifier ACESO = Thaumcraft.id("textures/block/obelisk_active.png");
    private static final Identifier FORTE = Thaumcraft.id("textures/block/obelisk_active_highpower.png");
    private static final Identifier RUNAS = Thaumcraft.id("textures/block/obelisk_runes.png");

    /** Quanto tempo leva a volta de cada eixo das runas, em milissegundos. */
    private static final long VOLTA_U = 32_000L;
    private static final long VOLTA_V = 28_000L;
    /** E o passo do piscar delas. */
    private static final double PISCA = 1000.0;

    public static class State extends BlockEntityRenderState {
        int angle;
        boolean active;
        boolean highPower;
        float u;
        float v;
        float alpha;
    }

    public ObeliskRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ObeliskBlockEntity obelisco, State estado, float parcial, Vec3 câmara,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.@Nullable CrumblingOverlay migalhas) {
        BlockEntityRenderer.super.extractRenderState(obelisco, estado, parcial, câmara, migalhas);
        estado.angle = ObeliskBlock.angle(obelisco.getBlockState());
        estado.active = obelisco.isActive();
        estado.highPower = obelisco.isHighPowerActive();
        long agora = System.currentTimeMillis();
        estado.u = (float) (agora % VOLTA_U) / VOLTA_U;
        estado.v = (float) (agora % VOLTA_V) / VOLTA_V;
        estado.alpha = (float) Math.abs(Math.sin(agora / PISCA));
    }

    /** Ele tem dois blocos de altura: não pode sumir quando o pé sai da tela. */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor, CameraRenderState câmara) {
        float[] malha = ObjModel.part("obelisk_am", null);
        if (malha.length == 0) return;

        Identifier pele = estado.highPower ? FORTE : estado.active ? ACESO : APAGADO;
        pose.pushPose();
        pose.translate(0.5f, 0.0f, 0.5f);
        pose.mulPose(Axis.YP.rotationDegrees(estado.angle));
        coletor.submitCustomGeometry(pose, RenderTypes.entityTranslucent(pele),
                (m, v) -> ObjMesh.draw(malha, m, v, estado.lightCoords, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF));

        if (estado.active) {
            int cor = ((int) (estado.alpha * 255.0f) << 24) | 0xFFFFFF;
            coletor.submitCustomGeometry(pose, RenderTypes.entityTranslucent(RUNAS),
                    (m, v) -> ObjMesh.draw(malha, m, v, estado.lightCoords, OverlayTexture.NO_OVERLAY, cor,
                            estado.u, estado.v));
        }
        pose.popPose();
    }
}
