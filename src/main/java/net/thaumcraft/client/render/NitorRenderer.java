package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.thaumcraft.block.entity.NitorBlockEntity;

/**
 * O Nitor: um bolo de luz parado no ar.
 *
 * <p>No original ele é um orbe <strong>vermelho</strong> em forma de gota — mais alto que largo, com um
 * bico no topo —, com um miolo amarelo-branco de tão quente e faíscas soltando em volta. Não é chama de
 * tocha nem bola alaranjada: a cor puxa para o vermelho-rosado, e é isso que o faz parecer magia e não
 * fogo.
 *
 * <p>Os discos vão pela porta do {@code eyes}, que é a que o jogo usa para olho de aranha e coisa que
 * <em>emite</em> luz em vez de refletir: ela soma a cor ao que está atrás em vez de misturar. É o que
 * separa um brilho de uma mancha — a primeira tentativa usou a porta comum e o Nitor saiu marrom na
 * areia.
 */
public class NitorRenderer implements BlockEntityRenderer<NitorBlockEntity, NitorRenderer.State> {
    private static final Identifier GLOW = Thaumcraft.id("textures/misc/glow.png");

    /**
     * Os discos, de fora para dentro: {@code largura, altura, subida, cor}.
     *
     * <p>A gota sai de os discos serem mais altos que largos e de o miolo ficar um pouco abaixo do meio,
     * com um pingo por cima fazendo o bico.
     */
    private static final float[][] DISCS = {
            {1.05f, 1.25f, 0.00f},
            {0.60f, 0.76f, -0.02f},
            {0.36f, 0.46f, -0.05f},
            {0.15f, 0.17f, 0.30f},
    };
    private static final int[] COLOURS = {0x55FF2A44, 0x99FF3355, 0xFFFFE07A, 0xAAFF4466};

    /** O que o desenhista precisa saber do Nitor neste quadro. */
    public static class State extends BlockEntityRenderState {
        public float ticks;
    }

    public NitorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(NitorBlockEntity nitor, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(nitor, state, crumbling);
        // o compasso é próprio de cada Nitor, para dois lado a lado não pulsarem juntos
        state.ticks = (nitor.getLevel() == null ? 0.0f : nitor.getLevel().getGameTime() + partial)
                + Math.abs(nitor.getBlockPos().hashCode() % 128);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float pulse = 1.0f + (float) Math.sin(state.ticks * 0.09f) * 0.06f;

        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(camera.orientation);
        for (int disc = 0; disc < DISCS.length; disc++) {
            float[] shape = DISCS[disc];
            // o halo de fora respira mais do que o miolo, que é o que dá a impressão de calor
            float grow = disc == 0 ? pulse * pulse : pulse;
            glow(pose, collector, shape[0] * grow, shape[1] * grow, shape[2], COLOURS[disc]);
        }
        pose.popPose();
    }

    /** Um disco de brilho virado para quem olha, mais alto que largo. */
    private static void glow(PoseStack pose, SubmitNodeCollector collector,
                             float width, float height, float rise, int colour) {
        float halfW = width / 2.0f;
        float halfH = height / 2.0f;
        collector.submitCustomGeometry(pose, RenderTypes.eyes(GLOW), (matrix, consumer) -> {
            corner(matrix, consumer, -halfW, rise - halfH, 0.0f, 1.0f, colour);
            corner(matrix, consumer, halfW, rise - halfH, 1.0f, 1.0f, colour);
            corner(matrix, consumer, halfW, rise + halfH, 1.0f, 0.0f, colour);
            corner(matrix, consumer, -halfW, rise + halfH, 0.0f, 0.0f, colour);
        });
    }

    private static void corner(PoseStack.Pose matrix, VertexConsumer consumer, float x, float y,
                               float u, float v, int colour) {
        consumer.addVertex(matrix, x, y, 0.0f)
                .setColor(colour)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(matrix, 0.0f, 0.0f, -1.0f);
    }
}
