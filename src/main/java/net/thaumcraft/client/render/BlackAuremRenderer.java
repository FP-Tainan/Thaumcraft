package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
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
import net.thaumcraft.arcana.machine.BlackAuremBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O <b>Aurem Negro</b>: o {@code renderBlackAurem} e o {@code renderArsMagicaEffect} da 1.4.0.009.
 *
 * <p>Ele não tem modelo. O que se vê é uma <b>figura chapada virada para quem olha</b> — um redemoinho
 * escuro —, do dobro do tamanho, que roda sobre si mesma a <b>um grau por batida</b> e que se desenha
 * sem sombra e sem escrever na profundidade.
 *
 * <p><b>O giro sai de quem olha, e não do bloco.</b> O original lê o contador de batidas do jogador
 * local, e por isso dois Aurens ao lado um do outro giram em perfeita sincronia — é uma coisa só,
 * desenhada duas vezes. Fica tal e qual.
 */
public class BlackAuremRenderer implements BlockEntityRenderer<BlackAuremBlockEntity, BlackAuremRenderer.State> {
    private static final Identifier PELE = Thaumcraft.id("textures/block/black_aurem.png");

    /** O {@code glScalef(2, 2, 2)} do original. */
    private static final float ESCALA = 2.0f;
    /** O quanto a figura se desloca antes e depois do giro, para rodar em torno do meio dela. */
    private static final float MEIO = 0.25f;

    public static class State extends BlockEntityRenderState {
        float spin;
    }

    public BlackAuremRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BlackAuremBlockEntity aurem, State estado, float parcial, Vec3 câmara,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.@Nullable CrumblingOverlay migalhas) {
        BlockEntityRenderer.super.extractRenderState(aurem, estado, parcial, câmara, migalhas);
        var quem = Minecraft.getInstance().player;
        estado.spin = quem == null ? 0.0f : quem.tickCount;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public void submit(State estado, PoseStack pose, SubmitNodeCollector coletor, CameraRenderState câmara) {
        pose.pushPose();
        pose.translate(0.5f, 1.0f, 0.5f);
        // o original vira a figura para quem olha com o rumo e a inclinação da câmara
        pose.mulPose(Axis.YP.rotationDegrees(180.0f - câmara.yRot));
        pose.mulPose(Axis.XP.rotationDegrees(-câmara.xRot));
        pose.translate(0.0f, MEIO, 0.0f);
        pose.mulPose(Axis.ZP.rotationDegrees(estado.spin));
        pose.scale(ESCALA, ESCALA, ESCALA);
        pose.translate(0.0f, -MEIO, 0.0f);
        coletor.submitCustomGeometry(pose, RenderTypes.entityTranslucentEmissive(PELE), BlackAuremRenderer::sprite);
        pose.popPose();
    }

    /** O {@code renderSprite}: um quadrado de um por um, com o meio um quarto acima do pé. */
    private static void sprite(PoseStack.Pose m, VertexConsumer v) {
        canto(m, v, -0.5f, -0.25f, 0.0f, 1.0f);
        canto(m, v, 0.5f, -0.25f, 1.0f, 1.0f);
        canto(m, v, 0.5f, 0.75f, 1.0f, 0.0f);
        canto(m, v, -0.5f, 0.75f, 0.0f, 0.0f);
    }

    private static void canto(PoseStack.Pose m, VertexConsumer v, float x, float y, float u, float w) {
        v.addVertex(m, x, y, 0.0f).setColor(0xFFFFFFFF).setUv(u, w)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(m, 0.0f, 0.0f, 1.0f);
    }
}
