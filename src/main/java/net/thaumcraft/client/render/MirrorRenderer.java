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
import net.thaumcraft.block.MirrorBlock;
import net.thaumcraft.block.entity.LinkedMirrorBlockEntity;
import net.thaumcraft.block.entity.MirrorBlockEntity;
import org.joml.Vector3f;

/**
 * O vidro dos espelhos: o {@code TileMirrorRenderer} da 4.2.3.5. A moldura é o modelo do bloco; aqui vai o que muda
 * com a ligação. Sem par, o vidro prateado ({@code mirrorpane}). Com par, o mesmo céu de estrelas do buraco portátil
 * no fundo da moldura, recuado três dezesseis avos de cada lado, e por cima o vidro quase transparente
 * ({@code mirrorpanetrans}). O vidro fica a dois centésimos da parede; no espelho mágico instável ele treme para
 * fora, tanto mais quanto maior a instabilidade.
 */
public class MirrorRenderer<T extends LinkedMirrorBlockEntity> implements BlockEntityRenderer<T, MirrorRenderer.State> {
    private static final Identifier PANE = Thaumcraft.id("textures/block/mirror_pane.png");
    private static final Identifier PANE_TRANS = Thaumcraft.id("textures/block/mirror_pane_trans.png");
    /** O {@code p} do original: a borda que o céu deixa de cada lado. */
    private static final float INSET = 0.1875f;

    public static class State extends BlockEntityRenderState {
        Direction facing = Direction.UP;
        boolean linked;
        float jitter;
    }

    public MirrorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T mirror, State state, float partial, Vec3 camera,
                                   net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(mirror, state, crumbling);
        state.facing = mirror.getBlockState().hasProperty(MirrorBlock.FACING) ? mirror.getBlockState().getValue(MirrorBlock.FACING) : Direction.UP;
        state.linked = mirror.linked;
        state.jitter = 0.0f;
        if (mirror instanceof MirrorBlockEntity magic && magic.instability > 0 && mirror.getLevel() != null) {
            state.jitter = mirror.getLevel().getRandom().nextFloat() * (magic.instability / 10000.0f);
        }
    }

    /** Para cima de quem olha o vidro: o y nos de parede; nos deitados, o que as rotações do modelo dão. */
    private static Vector3f up(Direction facing) {
        return switch (facing) {
            case UP -> new Vector3f(0, 0, 1);
            case DOWN -> new Vector3f(0, 0, -1);
            default -> new Vector3f(0, 1, 0);
        };
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        Direction facing = state.facing;
        Vector3f n = new Vector3f(facing.getStepX(), facing.getStepY(), facing.getStepZ());
        Vector3f up = up(facing);
        Vector3f right = new Vector3f(up).cross(n);
        int light = state.lightCoords;
        if (state.linked) {
            // o céu, no fundo da moldura (a um centésimo da parede)
            float[][] sky = quad(n, up, right, 0.01f, INSET);
            collector.submitCustomGeometry(pose, HoleRenderer.HOLE, (matrix, consumer) -> {
                for (int i = 0; i < 4; i++) consumer.addVertex(matrix, sky[i][0], sky[i][1], sky[i][2]);
                for (int i = 3; i >= 0; i--) consumer.addVertex(matrix, sky[i][0], sky[i][1], sky[i][2]);
            });
        }
        float[][] pane = quad(n, up, right, 0.02f + state.jitter, 0.0f);
        collector.submitCustomGeometry(pose, RenderTypes.entityTranslucent(state.linked ? PANE_TRANS : PANE), (matrix, consumer) -> {
            float[][] uv = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
            // uma face só, no sentido anti-horário para quem olha o vidro (duas no mesmo plano brigam)
            for (int i = 3; i >= 0; i--) vertex(matrix, consumer, pane[i], uv[i], n, light);
        });
    }

    /**
     * Os quatro cantos (em cima à esquerda, em cima à direita, embaixo à direita, embaixo à esquerda, para quem olha
     * o vidro) de um quadrado a {@code off} da parede, recuado {@code inset} de cada borda.
     */
    private static float[][] quad(Vector3f n, Vector3f up, Vector3f right, float off, float inset) {
        float half = 0.5f - inset;
        Vector3f centre = new Vector3f(0.5f, 0.5f, 0.5f).sub(new Vector3f(n).mul(0.5f - off));
        float[][] out = new float[4][];
        float[][] signs = {{-1, 1}, {1, 1}, {1, -1}, {-1, -1}};
        for (int i = 0; i < 4; i++) {
            Vector3f p = new Vector3f(centre).add(new Vector3f(right).mul(signs[i][0] * half)).add(new Vector3f(up).mul(signs[i][1] * half));
            out[i] = new float[]{p.x, p.y, p.z};
        }
        return out;
    }

    private static void vertex(PoseStack.Pose matrix, VertexConsumer consumer, float[] p, float[] uv, Vector3f n, int light) {
        consumer.addVertex(matrix, p[0], p[1], p[2])
                .setColor(0xFFFFFFFF)
                .setUv(uv[0], uv[1])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(matrix, n.x, n.y, n.z);
    }
}
