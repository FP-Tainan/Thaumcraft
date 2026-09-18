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
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.block.entity.JarBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O que se vê dentro do jarro, e o rótulo colado na frente dele.
 *
 * <p>É o que o {@code TileJarRenderer} do original desenha, nas mesmas medidas: a essência como uma
 * névoa na cor do aspecto, enchendo o vidro de baixo para cima, e — <strong>só quando há rótulo</strong> —
 * um papelzinho na face da frente com o símbolo do aspecto por cima dele, pequeno.
 *
 * <p>O rótulo do original fica a {@code 0.315} do meio do bloco e é desenhado a meia escala; são esses os
 * números usados aqui. Jarro sem rótulo não mostra símbolo nenhum — para saber o que há dentro põem-se os
 * Óculos da Revelação, que é como se lê isso no mod.
 */
public class JarRenderer implements BlockEntityRenderer<JarBlockEntity, JarRenderer.State> {
    /**
     * O vão de dentro do jarro, nas medidas do {@code ModelJar} original.
     *
     * <p>Lá o vidro é uma caixa de dez por doze e a salmoura é uma de oito por dez, <strong>encolhida uma
     * unidade para dentro em cada lado</strong>. Essa unidade é o que faz o jarro cheio continuar
     * parecendo um pote de vidro em vez de um tijolo colorido: sobra vidro à vista em volta do líquido,
     * com o brilho e a borda dele. Eu tinha dado ao líquido o tamanho exato do vidro, e um jarro cheio
     * ficava sólido de canto a canto.
     */
    private static final float FLOOR = 1.0f / 16.0f;
    private static final float CEILING = 11.0f / 16.0f;
    private static final float NEAR = 4.0f / 16.0f;
    private static final float FAR = 12.0f / 16.0f;

    /** A que distância do meio do bloco o rótulo fica, como no original. */
    private static final float LABEL_OUT = 0.315f;
    /** A altura do meio do rótulo. */
    private static final float LABEL_MID = 0.42f;
    /** O tamanho do rótulo: meia escala, como no original. */
    private static final float LABEL_SIZE = 0.26f;
    /** O símbolo do aspecto, menor que o papel, e um fio à frente dele. */
    private static final float SYMBOL_SIZE = 0.17f;
    private static final float SYMBOL_OUT = LABEL_OUT + 0.002f;

    private static final Identifier MIST = Thaumcraft.id("textures/misc/essentia.png");
    private static final Identifier LABEL = Thaumcraft.id("textures/misc/label.png");

    /** O que o desenhista precisa saber do jarro neste quadro. */
    public static class State extends BlockEntityRenderState {
        public int color;
        public float fullness;
        @Nullable
        public Identifier symbol;
        public int symbolColor;
        public Direction facing = Direction.NORTH;
        public float ticks;
        @Nullable
        public Aspect held;
        public int amount;
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
        // símbolo só onde há rótulo, como no original
        Aspect label = jar.label();
        state.symbol = label == null ? null : Thaumcraft.id("textures/aspects/" + label.tag() + ".png");
        state.symbolColor = label == null ? -1 : 0xFF000000 | label.color();
        state.facing = jar.facing();
        state.ticks = jar.getLevel() == null ? 0.0f : jar.getLevel().getGameTime() + partial;
        state.held = aspect;
        state.amount = jar.amount();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.fullness > 0.0f) this.submitMist(state, pose, collector);
        if (state.symbol != null) this.submitLabel(state, pose, collector);
        // o que os Óculos da Revelação mostram quem desenha é o GogglesOverlay, para todas as peças
    }

    /** A névoa lá dentro: sobe com o que o jarro guarda e respira devagar. */
    private void submitMist(State state, PoseStack pose, SubmitNodeCollector collector) {
        float breath = 1.0f + (float) Math.sin(state.ticks / 12.0f) * 0.02f;
        float top = FLOOR + (CEILING - FLOOR) * Math.min(1.0f, state.fullness) * breath;
        int color = 0xF2000000 | state.color;
        int light = 0xF000F0;

        pose.pushPose();
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(MIST), (matrix, consumer) -> {
            wall(matrix, consumer, NEAR, FAR, NEAR, NEAR, FLOOR, top, color, light);
            wall(matrix, consumer, FAR, NEAR, FAR, FAR, FLOOR, top, color, light);
            wall(matrix, consumer, NEAR, NEAR, FAR, NEAR, FLOOR, top, color, light);
            wall(matrix, consumer, FAR, FAR, NEAR, FAR, FLOOR, top, color, light);
            lid(matrix, consumer, top, color, light);
        });
        pose.popPose();
    }

    /**
     * O rótulo: um papelzinho na frente do jarro, com o símbolo do aspecto por cima.
     *
     * <p>Fica só na face para onde o jarro está virado, como no original — e não nas quatro.
     */
    private void submitLabel(State state, PoseStack pose, SubmitNodeCollector collector) {
        int light = state.lightCoords;

        pose.pushPose();
        pose.translate(0.5f, 0.0f, 0.5f);
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-state.facing.toYRot()));
        pose.translate(-0.5f, 0.0f, -0.5f);

        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(LABEL), (matrix, consumer) ->
                flat(matrix, consumer, LABEL_SIZE, LABEL_OUT, -1, light));
        collector.submitCustomGeometry(pose, RenderTypes.entityCutout(state.symbol), (matrix, consumer) ->
                flat(matrix, consumer, SYMBOL_SIZE, SYMBOL_OUT, state.symbolColor, light));
        pose.popPose();
    }

    /** Um quadrado chapado na face da frente do bloco, virado para fora. */
    private static void flat(PoseStack.Pose matrix, VertexConsumer consumer, float size, float out,
                             int color, int light) {
        float half = size / 2.0f;
        float z = 0.5f - out;
        vertex(matrix, consumer, 0.5f - half, LABEL_MID - half, z, 0.0f, 1.0f, color, light);
        vertex(matrix, consumer, 0.5f + half, LABEL_MID - half, z, 1.0f, 1.0f, color, light);
        vertex(matrix, consumer, 0.5f + half, LABEL_MID + half, z, 1.0f, 0.0f, color, light);
        vertex(matrix, consumer, 0.5f - half, LABEL_MID + half, z, 0.0f, 0.0f, color, light);
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
