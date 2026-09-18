package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.thaumcraft.block.entity.TubeBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A essência correndo dentro do tubo.
 *
 * <p>É o que faz a tubulação do Thaumcraft ser bonita de olhar: enquanto a essência anda, vê-se um
 * bolinho de luz na cor do aspecto passando de cano em cano. Sem isso o encanamento é um monte de cano
 * parado, e não dá para saber, de longe, se a linha está trabalhando.
 *
 * <p>O bolinho pulsa no compasso do tique para dar a impressão de que está correndo; e, com os Óculos da
 * Revelação no rosto, sai também o símbolo do aspecto por cima do cano, como em qualquer outra peça que
 * guarde essência.
 */
public class TubeRenderer implements BlockEntityRenderer<TubeBlockEntity, TubeRenderer.State> {
    private static final Identifier MIST = Thaumcraft.id("textures/misc/essentia.png");
    /** O tamanho do bolinho de essência dentro do cano. */
    private static final float BLOB = 0.17f;

    /** O que o desenhista precisa saber do tubo neste quadro. */
    public static class State extends BlockEntityRenderState {
        @Nullable
        public Aspect aspect;
        public int amount;
        public boolean venting;
        public float ticks;
        /** Os lados em que o cano entra no aparelho vizinho, na ordem de {@code Direction.get3DDataValue}. */
        public final boolean[] plugged = new boolean[6];
    }


    public TubeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TubeBlockEntity tube, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(tube, state, crumbling);
        state.aspect = tube.getEssentiaType(null);
        state.amount = tube.getEssentiaAmount(null);
        state.venting = tube.venting() > 0;
        state.ticks = tube.getLevel() == null ? 0.0f : tube.getLevel().getGameTime() + partial;
        plugs(tube, state.plugged);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        submitPlugs(state.plugged, pose, collector, state.lightCoords);
        if (state.aspect == null || state.amount <= 0) return;

        // o bolinho de essência, pulsando como se estivesse correndo
        float pulse = 1.0f + (float) Math.sin(state.ticks * 0.6f) * 0.25f;
        float half = BLOB * pulse / 2.0f;
        int color = 0xE0000000 | state.aspect.color();

        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        pose.mulPose(camera.orientation);
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucentEmissive(MIST), (matrix, consumer) -> {
            consumer.addVertex(matrix, -half, -half, 0.0f).setColor(color).setUv(0.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, -1.0f);
            consumer.addVertex(matrix, half, -half, 0.0f).setColor(color).setUv(1.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, -1.0f);
            consumer.addVertex(matrix, half, half, 0.0f).setColor(color).setUv(1.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, -1.0f);
            consumer.addVertex(matrix, -half, half, 0.0f).setColor(color).setUv(0.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(matrix, 0.0f, 0.0f, -1.0f);
        });
        pose.popPose();
    }

    // ---- o cano entrando no aparelho ----

    private static final Identifier PIPE = Thaumcraft.id("textures/block/pipe_1.png");
    private static final float UNIT = 1.0f / 16.0f;
    /** O quanto o cano entra no aparelho: seis dezesseis avos, o W6 do BlockTubeRenderer. */
    private static final float PLUG = 6.0f;

    /**
     * Em que lados o cano entra no vizinho.
     *
     * <p>No original, quando o vizinho diz {@code renderExtendedTube} — o jarro e o alambique dizem —, o
     * braço do cano não para na beirada do bloco: ele avança seis dezesseis avos para dentro do vizinho. É
     * o que faz o cano afundar na rolha do jarro em vez de só encostar nela.
     */
    public static void plugs(net.thaumcraft.block.entity.TubeBlockEntity tube, boolean[] into) {
        java.util.Arrays.fill(into, false);
        var level = tube.getLevel();
        if (level == null) return;
        var block = tube.getBlockState();
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            var side = net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION.get(dir);
            if (!block.hasProperty(side) || !block.getValue(side)) continue;
            if (level.getBlockEntity(tube.getBlockPos().relative(dir))
                    instanceof net.thaumcraft.api.aspects.EssentiaTransport next && next.renderExtendedTube()) {
                into[dir.get3DDataValue()] = true;
            }
        }
    }

    /** Os tocos de cano que entram nos vizinhos. */
    public static void submitPlugs(boolean[] plugged, PoseStack pose, SubmitNodeCollector collector, int light) {
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            if (!plugged[dir.get3DDataValue()]) continue;
            pose.pushPose();
            pose.translate(0.5f, 0.5f, 0.5f);
            pose.mulPose(downTowards(dir));
            pose.translate(-0.5f, -0.5f, -0.5f);
            collector.submitCustomGeometry(pose, RenderTypes.entityCutout(PIPE), (matrix, consumer) -> {
                // um braço da cruz da pipe_1, de 7 a 9, descendo da beirada do bloco para dentro do vizinho
                float a = 7.0f * UNIT, b = 9.0f * UNIT, bottom = -PLUG * UNIT;
                float u0 = 7.0f / 16.0f, u1 = 9.0f / 16.0f, v0 = 0.0f, v1 = PLUG / 16.0f;
                face(matrix, consumer, light, a, 0, a, b, 0, a, b, bottom, a, a, bottom, a, u0, u1, v0, v1, 0, 0, -1);
                face(matrix, consumer, light, b, 0, b, a, 0, b, a, bottom, b, b, bottom, b, u0, u1, v0, v1, 0, 0, 1);
                face(matrix, consumer, light, a, 0, b, a, 0, a, a, bottom, a, a, bottom, b, u0, u1, v0, v1, -1, 0, 0);
                face(matrix, consumer, light, b, 0, a, b, 0, b, b, bottom, b, b, bottom, a, u0, u1, v0, v1, 1, 0, 0);
            });
            pose.popPose();
        }
    }

    /** O giro que leva o toco, desenhado descendo, até o lado pedido. */
    private static org.joml.Quaternionf downTowards(net.minecraft.core.Direction dir) {
        return switch (dir) {
            case DOWN -> com.mojang.math.Axis.XP.rotationDegrees(0.0f);
            case UP -> com.mojang.math.Axis.XP.rotationDegrees(180.0f);
            case NORTH -> com.mojang.math.Axis.XP.rotationDegrees(90.0f);
            case SOUTH -> com.mojang.math.Axis.XP.rotationDegrees(-90.0f);
            case EAST -> com.mojang.math.Axis.ZP.rotationDegrees(90.0f);
            case WEST -> com.mojang.math.Axis.ZP.rotationDegrees(-90.0f);
        };
    }

    private static void face(PoseStack.Pose matrix, com.mojang.blaze3d.vertex.VertexConsumer consumer, int light,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3,
                             float u0, float u1, float v0, float v1, float nx, float ny, float nz) {
        consumer.addVertex(matrix, x0, y0, z0).setColor(-1).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(matrix, nx, ny, nz);
        consumer.addVertex(matrix, x1, y1, z1).setColor(-1).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(matrix, nx, ny, nz);
        consumer.addVertex(matrix, x2, y2, z2).setColor(-1).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(matrix, nx, ny, nz);
        consumer.addVertex(matrix, x3, y3, z3).setColor(-1).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(matrix, nx, ny, nz);
    }
}
