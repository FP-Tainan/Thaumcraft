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
 * <p>No original ele <strong>não</strong> é uma chama de tocha — é um orbe difuso, com um miolo branco de
 * tão quente e um halo alaranjado em volta que se espalha macio pelo chão, e uma faísca menor boiando
 * logo acima. É por isso que ele não pode ser um desenho chapado: são três discos de brilho, um dentro do
 * outro, sempre virados para quem olha, e o do meio pulsa.
 *
 * <p>A textura do brilho é a do próprio mod ({@code misc/p_large.png}), a mesma que ele usa nas faíscas.
 */
public class NitorRenderer implements BlockEntityRenderer<NitorBlockEntity, NitorRenderer.State> {
    private static final Identifier GLOW = Thaumcraft.id("textures/misc/glow.png");

    /** Os três discos: o halo largo e ralo, o corpo, e o miolo branco. */
    private static final float[] SIZES = {0.95f, 0.52f, 0.22f};
    private static final int[] COLOURS = {0x33FF7A18, 0xAAFFA53C, 0xFFFFF4D6};

    /** O tamanho e a cor da faísca que boia acima. */
    private static final float EMBER_SIZE = 0.16f;
    private static final int EMBER_COLOUR = 0xAAFF8A2B;

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
        float pulse = 1.0f + (float) Math.sin(state.ticks * 0.08f) * 0.07f;

        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(camera.orientation);
        for (int disc = 0; disc < SIZES.length; disc++) {
            // o halo de fora respira mais do que o miolo, que é o que dá a impressão de calor
            float grow = disc == 0 ? pulse * pulse : pulse;
            glow(pose, collector, SIZES[disc] * grow, COLOURS[disc], 0.0f);
        }
        pose.popPose();

        // e a faísca que boia logo acima, subindo e descendo devagar
        pose.pushPose();
        pose.translate(0.5f, 0.85f + (float) Math.sin(state.ticks * 0.05f) * 0.09f, 0.5f);
        pose.mulPose(camera.orientation);
        glow(pose, collector, EMBER_SIZE, EMBER_COLOUR, 0.0f);
        pose.popPose();
    }

    /** Um disco de brilho virado para quem olha. */
    private static void glow(PoseStack pose, SubmitNodeCollector collector, float size, int colour, float z) {
        float half = size / 2.0f;
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucentEmissive(GLOW), (matrix, consumer) -> {
            corner(matrix, consumer, -half, -half, z, 0.0f, 1.0f, colour);
            corner(matrix, consumer, half, -half, z, 1.0f, 1.0f, colour);
            corner(matrix, consumer, half, half, z, 1.0f, 0.0f, colour);
            corner(matrix, consumer, -half, half, z, 0.0f, 0.0f, colour);
        });
    }

    private static void corner(PoseStack.Pose matrix, VertexConsumer consumer, float x, float y, float z,
                               float u, float v, int colour) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(colour)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(matrix, 0.0f, 0.0f, -1.0f);
    }
}
