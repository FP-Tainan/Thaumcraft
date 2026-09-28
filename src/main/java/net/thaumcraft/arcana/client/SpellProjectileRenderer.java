package net.thaumcraft.arcana.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.arcana.SpellProjectileEntity;
import net.thaumcraft.client.render.AdditiveGlow;

/**
 * O feitiço voando visto: o {@code RenderSpellProjectile} do Ars Magica 2.
 *
 * <p>Ele é um <b>quadrado virado para a câmera</b>, de meio bloco de lado, com a figura de partícula do original
 * pintada da cor do feitiço, em mistura comum e sem escrever profundidade — o que faz dele uma luz que passa por
 * cima do que estiver atrás sem recortar nada.
 *
 * <p>A figura é a {@code lens_flare} do original, que é o que ele dá a um feitiço sem Afinidade: uma tira de
 * <b>13 quadros</b> de 64 por 64. <b>Desvio declarado:</b> o original deixa o
 * jogo animar a figura sozinho, porque ela vive no atlas dos itens e traz um {@code .mcmeta} de animação. Aqui
 * ela é a textura da entidade, que não passa pelo atlas, e por isso o desenho <b>anda a tira ele mesmo</b>, um
 * quadro por batida — que é o que o {@code .mcmeta} sem tempo declarado pede, e dá a mesma coisa na tela.
 */
public class SpellProjectileRenderer
        extends EntityRenderer<SpellProjectileEntity, SpellProjectileRenderer.State> {
    /** A {@code lens_flare} do original, a figura da Afinidade nenhuma. */
    public static final Identifier TEXTURE = Thaumcraft.id("textures/entity/spell_projectile.png");

    /** Quantos quadros tem a tira. */
    public static final int FRAMES = 13;

    /** Metade do bloco do original: o {@code glScalef(0.5F, 0.5F, 0.5F)} sobre um quadrado de um bloco. */
    private static final float HALF = 0.25f;

    public static class State extends EntityRenderState {
        int frame;
        int color;
    }

    public SpellProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SpellProjectileEntity entity, State state, float partial) {
        super.extractRenderState(entity, state, partial);
        state.frame = entity.tickCount % FRAMES;
        // sem Afinidade portada, todo feitiço sai da cor que o original dá à Afinidade nenhuma: branco
        state.color = 0xFFFFFFFF;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float v0 = (float) state.frame / FRAMES;
        float v1 = (float) (state.frame + 1) / FRAMES;
        int cor = state.color;

        pose.pushPose();
        pose.mulPose(camera.orientation);
        collector.submitCustomGeometry(pose, AdditiveGlow.blended(TEXTURE), (matrix, buffer) -> {
            corner(matrix, buffer, -HALF, -HALF, 0.0f, v1, cor);
            corner(matrix, buffer, HALF, -HALF, 1.0f, v1, cor);
            corner(matrix, buffer, HALF, HALF, 1.0f, v0, cor);
            corner(matrix, buffer, -HALF, HALF, 0.0f, v0, cor);
        });
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }

    private static void corner(PoseStack.Pose matrix, VertexConsumer buffer, float x, float y,
                               float u, float v, int cor) {
        buffer.addVertex(matrix, x, y, 0.0f).setColor(cor).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
                .setNormal(matrix, 0.0f, 0.0f, 1.0f);
    }
}
