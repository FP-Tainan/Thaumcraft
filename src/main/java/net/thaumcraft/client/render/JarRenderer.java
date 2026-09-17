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
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.entity.JarBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O que se vê dentro do jarro: a essência, e o rótulo colado na frente.
 *
 * <p>A essência do original é uma névoa que gira devagar dentro do vidro, na cor do aspecto, subindo
 * conforme o jarro enche. Aqui ela é um bloco de névoa translúcida que respira no mesmo compasso e sobe
 * junto; e o símbolo do aspecto fica desenhado nos quatro lados do vidro, para se ler o jarro de qualquer
 * ângulo, como no original se lê pelo rótulo.
 */
public class JarRenderer implements BlockEntityRenderer<JarBlockEntity, JarRenderer.State> {
    /** O fundo do vão de dentro do jarro. */
    private static final float FLOOR = 0.07f;
    /** O teto do vão de dentro do jarro. */
    private static final float CEILING = 0.66f;
    /** Onde as paredes de dentro ficam, um pouco para dentro do vidro. */
    private static final float NEAR = 0.23f;
    private static final float FAR = 0.77f;

    /** O que o desenhista precisa saber do jarro neste quadro. */
    public static class State extends BlockEntityRenderState {
        public int color;
        public float fullness;
        @Nullable
        public Identifier symbol;
        public float ticks;
    }

    public JarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(JarBlockEntity jar, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(jar, state, crumbling);
        Aspect aspect = jar.aspect();
        state.color = aspect == null ? 0 : aspect.color();
        state.fullness = jar.fullness();
        Aspect shown = jar.label() != null ? jar.label() : aspect;
        state.symbol = shown == null ? null : Thaumcraft.id("textures/aspects/" + shown.tag() + ".png");
        state.ticks = jar.getLevel() == null ? 0.0f : jar.getLevel().getGameTime() + partial;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.fullness > 0.0f) this.submitMist(state, pose, collector);
        if (state.symbol != null) this.submitLabel(state, pose, collector);
    }

    /** A névoa lá dentro: sobe com o que o jarro guarda e respira devagar. */
    private void submitMist(State state, PoseStack pose, SubmitNodeCollector collector) {
        float breath = 1.0f + (float) Math.sin(state.ticks / 12.0f) * 0.02f;
        float top = FLOOR + (CEILING - FLOOR) * Math.min(1.0f, state.fullness) * breath;
        int color = 0xD8000000 | state.color;
        int light = 0xF000F0;

        pose.pushPose();
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(MIST), (matrix, consumer) -> {
            // as quatro paredes da névoa, cada uma dos dois lados para se ver de fora e de dentro
            wall(matrix, consumer, NEAR, FAR, NEAR, NEAR, FLOOR, top, color, light);
            wall(matrix, consumer, FAR, NEAR, FAR, FAR, FLOOR, top, color, light);
            wall(matrix, consumer, NEAR, NEAR, FAR, NEAR, FLOOR, top, color, light);
            wall(matrix, consumer, FAR, FAR, NEAR, FAR, FLOOR, top, color, light);
            // e a tampa da névoa, que é o que se vê de cima
            lid(matrix, consumer, top, color, light);
        });
        pose.popPose();
    }

    /** O símbolo do aspecto nos quatro lados do vidro. */
    private void submitLabel(State state, PoseStack pose, SubmitNodeCollector collector) {
        int light = state.lightCoords;
        pose.pushPose();
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(state.symbol), (matrix, consumer) -> {
            for (int turn = 0; turn < 4; turn++) {
                float[] corner = FACES[turn];
                face(matrix, consumer, corner, light);
            }
        });
        pose.popPose();
    }

    /**
     * Os quatro lados em que o símbolo aparece, cada um como {@code x1, z1, x2, z2}.
     *
     * <p>A ordem dos cantos é a que deixa a face virada para fora — trocada, o símbolo some, porque o jogo
     * não desenha o avesso.
     */
    private static final float[][] FACES = {
            {0.28f, 0.2f, 0.72f, 0.2f},   // norte
            {0.72f, 0.8f, 0.28f, 0.8f},   // sul
            {0.2f, 0.72f, 0.2f, 0.28f},   // oeste
            {0.8f, 0.28f, 0.8f, 0.72f},   // leste
    };

    private static final Identifier MIST = Thaumcraft.id("textures/misc/essentia.png");
    /** Onde o símbolo começa e acaba na altura do jarro. */
    private static final float LABEL_BOTTOM = 0.16f;
    private static final float LABEL_TOP = 0.6f;

    private static void face(PoseStack.Pose matrix, VertexConsumer consumer, float[] corner, int light) {
        vertex(matrix, consumer, corner[0], LABEL_BOTTOM, corner[1], 0.0f, 1.0f, -1, light);
        vertex(matrix, consumer, corner[2], LABEL_BOTTOM, corner[3], 1.0f, 1.0f, -1, light);
        vertex(matrix, consumer, corner[2], LABEL_TOP, corner[3], 1.0f, 0.0f, -1, light);
        vertex(matrix, consumer, corner[0], LABEL_TOP, corner[1], 0.0f, 0.0f, -1, light);
    }

    private static void wall(PoseStack.Pose matrix, VertexConsumer consumer, float x1, float z1,
                             float x2, float z2, float bottom, float top, int color, int light) {
        vertex(matrix, consumer, x1, bottom, z1, 0.0f, 1.0f, color, light);
        vertex(matrix, consumer, x2, bottom, z2, 1.0f, 1.0f, color, light);
        vertex(matrix, consumer, x2, top, z2, 1.0f, 0.0f, color, light);
        vertex(matrix, consumer, x1, top, z1, 0.0f, 0.0f, color, light);
    }

    private static void lid(PoseStack.Pose matrix, VertexConsumer consumer, float top, int color, int light) {
        vertex(matrix, consumer, NEAR, top, NEAR, 0.0f, 0.0f, color, light);
        vertex(matrix, consumer, NEAR, top, FAR, 0.0f, 1.0f, color, light);
        vertex(matrix, consumer, FAR, top, FAR, 1.0f, 1.0f, color, light);
        vertex(matrix, consumer, FAR, top, NEAR, 1.0f, 0.0f, color, light);
    }

    private static void vertex(PoseStack.Pose matrix, VertexConsumer consumer, float x, float y, float z,
                               float u, float v, int color, int light) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(matrix, 0.0f, 1.0f, 0.0f);
    }
}
