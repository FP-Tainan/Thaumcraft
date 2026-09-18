package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.block.entity.NitorBlockEntity;

/**
 * O Nitor: a chama que o {@code TileNitor} do original acende soltando fachos.
 *
 * <p>O bloco do original é vazio — a textura dele é {@code thaumcraft:blank}. Tudo o que se vê são os
 * fachos {@code FXWisp} que ele cospe, e aqui eles são reproduzidos <em>pelo mesmo processo</em>, tique a
 * tique, com os números tirados do código descompilado:
 *
 * <ul>
 * <li>a cada tique, um dado: com a configuração padrão de partículas nasce um facho vermelho uma vez em
 * cada cinco tiques, e um amarelo uma vez em cada sete;</li>
 * <li>cada facho vive entre trinta e seis e cinquenta e um tiques;</li>
 * <li>nasce com escala sorteada de um a dois — é o sorteio do próprio {@code EntityFX} do jogo — vezes
 * meio para o vermelho e um quarto para o amarelo; e a largura é essa escala inteira, ou seja, o facho
 * vermelho tem de meio a um bloco;</li>
 * <li>sai do meio do bloco rumo a um ponto sorteado a dois décimos (um décimo, o amarelo), sobe pela
 * gravidade negativa e perde dois por cento da velocidade a cada tique;</li>
 * <li>e encolhe em linha reta até sumir.</li>
 * </ul>
 *
 * <p>Como todo facho nasce no mesmo ponto e vive muito, o meio fica com uma dúzia deles empilhados — e
 * como a luz <strong>soma</strong> (veja {@link AdditiveGlow}), o vermelho estoura para o branco-amarelo
 * ali e fica vermelho só na borda. Esse miolo branco não é uma peça desenhada: é o empilhamento.
 *
 * <p>O sorteio de cada tique sai do relógio do mundo e da posição do bloco, sempre com as mesmas contas.
 * Assim a mesma chama se desenha igual em todo quadro sem nada para guardar entre um e outro, e dois
 * Nitors lado a lado não fervem no mesmo compasso.
 */
public class NitorRenderer implements BlockEntityRenderer<NitorBlockEntity, NitorRenderer.State> {
    /** O facho do original: a célula de baixo à esquerda da folha {@code misc/particles.png}. */
    private static final Identifier WISP = Thaumcraft.id("textures/misc/wisp.png");

    /** O facho que vive mais tempo: {@code 36 / 0,7}. É até aí que se olha para trás. */
    private static final int LONGEST_LIFE = 52;

    /** Um dos dois jatos do {@code TileNitor}. */
    private record Jet(int oneIn, float scale, int colour, float spread, float gravity, int salt) {
    }

    /**
     * Os dois jatos, com os números do original.
     *
     * <p>O {@code 9 - particleCount(2)} e o {@code 15 - particleCount(4)} dão cinco e sete com as
     * partículas no máximo, que é o padrão do jogo. O quatro e o um são os índices de cor do
     * {@code FXWisp}: vermelho e amarelo.
     */
    private static final Jet[] JETS = {
            new Jet(5, 0.5f, 4, 0.2f, -0.025f, 0),
            new Jet(7, 0.25f, 1, 0.1f, -0.02f, 1),
    };

    /** O que o desenhista precisa saber do Nitor neste quadro. */
    public static class State extends BlockEntityRenderState {
        public long tick;
        public float partial;
        public int seed;
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
        state.tick = nitor.getLevel() == null ? 0L : nitor.getLevel().getGameTime();
        state.partial = partial;
        state.seed = nitor.getBlockPos().hashCode();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        for (Jet jet : JETS) {
            // cada tique dos últimos cinquenta e poucos pode ter parido um facho que ainda está no ar
            for (int back = 0; back <= LONGEST_LIFE; back++) {
                long born = state.tick - back;
                int seed = mix(state.seed, born, jet.salt());
                if (noise(seed) * jet.oneIn() >= 1.0f) continue;
                this.wisp(state, pose, collector, camera, jet, seed, back + state.partial);
            }
        }
        pose.popPose();
    }

    /** Um facho, com a idade que ele tem agora. */
    private void wisp(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera,
                      Jet jet, int seed, float age) {
        int life = (int) (36.0 / (noise(seed + 1) * 0.3 + 0.7));
        if (age >= life) return;

        // a escala: o sorteio do EntityFX, de um a dois, vezes a que o TileNitor pede
        float scale = (noise(seed + 2) * 0.5f + 0.5f) * 2.0f * jet.scale();
        // e o shrink do original: encolhe em linha reta até sumir
        float half = 0.5f * scale * (life - age) / life;
        if (half <= 0.001f) return;

        // o caminho: parte rumo ao alvo sorteado, sobe pela gravidade e perde força a cada tique
        double vx = (noise(seed + 3) - 0.5) * 2.0 * jet.spread() / life;
        double vz = (noise(seed + 4) - 0.5) * 2.0 * jet.spread() / life;
        double vy = 0.0;
        double x = 0.0, y = 0.0, z = 0.0;
        int whole = (int) age;
        for (int step = 0; step < whole; step++) {
            vy -= 0.04 * jet.gravity();
            x += vx;
            y += vy;
            z += vz;
            vx *= 0.98;
            vy *= 0.98;
            vz *= 0.98;
        }
        // e o pedaço de tique que falta, para o movimento não andar aos soquinhos
        float rest = age - whole;
        x += vx * rest;
        y += (vy - 0.04 * jet.gravity()) * rest;
        z += vz * rest;

        int colour = colour(jet.colour(), seed + 5);

        pose.pushPose();
        pose.translate((float) x, (float) y, (float) z);
        pose.mulPose(camera.orientation);
        collector.submitCustomGeometry(pose, AdditiveGlow.of(WISP), (matrix, consumer) -> {
            corner(matrix, consumer, -half, -half, 0.0f, 1.0f, colour);
            corner(matrix, consumer, half, -half, 1.0f, 1.0f, colour);
            corner(matrix, consumer, half, half, 1.0f, 0.0f, colour);
            corner(matrix, consumer, -half, half, 0.0f, 0.0f, colour);
        });
        pose.popPose();
    }

    /**
     * As cores do {@code FXWisp}, pelo índice.
     *
     * <p>Com meia opacidade, que é o alfa com que o original monta o facho.
     */
    private static int colour(int index, int seed) {
        float r, g, b;
        if (index == 4) {
            r = 0.7f + noise(seed) * 0.3f;
            g = 0.2f;
            b = 0.2f;
        } else {
            r = 0.5f + noise(seed) * 0.3f;
            g = 0.5f + noise(seed + 1) * 0.3f;
            b = 0.2f;
        }
        return 0x80000000 | ((int) (r * 255.0f) << 16) | ((int) (g * 255.0f) << 8) | (int) (b * 255.0f);
    }

    /** A semente de um tique: a posição do bloco, o tique e qual dos dois jatos. */
    private static int mix(int where, long tick, int salt) {
        long h = where * 0x9E3779B97F4A7C15L + tick * 0xC2B2AE3D27D4EB4FL + salt * 0x165667B19E3779F9L;
        h ^= h >>> 29;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 32;
        return (int) h;
    }

    /** Um número entre zero e um, sempre o mesmo para a mesma semente. */
    private static float noise(int seed) {
        int x = seed * 0x9E3779B1;
        x ^= x >>> 15;
        x *= 0x85EBCA6B;
        x ^= x >>> 13;
        x *= 0xC2B2AE35;
        x ^= x >>> 16;
        return (x >>> 8) / (float) (1 << 24);
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
