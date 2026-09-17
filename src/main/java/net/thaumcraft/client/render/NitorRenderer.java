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
 *
 * <p>E ele <strong>não fica parado</strong>. Chama parada parece adesivo colado no ar; esta balança de
 * um lado para o outro, sobe e desce e treme de brilho, cada camada no seu compasso — é o desencontro
 * entre os compassos que faz a coisa parecer viva em vez de pulsar toda junta como um coração.
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
        float t = state.ticks;
        // o bailado da chama: sobe e desce e balança, em compassos que não fecham entre si
        float sobe = (float) Math.sin(t * 0.07f) * 0.055f + (float) Math.sin(t * 0.031f) * 0.03f;
        float balanca = (float) Math.sin(t * 0.045f) * 0.045f + (float) Math.cos(t * 0.019f) * 0.025f;

        pose.pushPose();
        pose.translate(0.5f + balanca, 0.5f + sobe, 0.5f);
        pose.mulPose(camera.orientation);
        for (int disc = 0; disc < DISCS.length; disc++) {
            float[] shape = DISCS[disc];
            // cada camada treme no seu compasso; a de fora abre e fecha mais que o miolo
            float ritmo = 0.09f + disc * 0.037f;
            float tremor = (float) Math.sin(t * ritmo + disc * 1.7f);
            float grow = 1.0f + tremor * (disc == 0 ? 0.11f : 0.06f);
            // e a chama é mais alta do que larga quando estica, como fogo de verdade
            float estica = 1.0f + tremor * 0.05f;
            glow(pose, collector, shape[0] * grow, shape[1] * grow * estica, shape[2], COLOURS[disc]);
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
