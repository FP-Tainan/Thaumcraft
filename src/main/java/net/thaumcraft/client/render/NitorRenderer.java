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
 * O Nitor: uma nuvem de fachos de luz fervendo no ar.
 *
 * <p>Eu vinha desenhando o Nitor como quatro discos parados, e o dono do mod tinha razão em achar ruim.
 * O original não desenha nada — o bloco é vazio. Toda a aparência dele são <strong>partículas</strong>:
 * o {@code TileNitor} cospe fachos {@code FXWisp} sem parar, e o que a gente vê é o amontoado deles.
 *
 * <p>E daí vem o desenho que o Nitor tem: como todo facho <em>nasce no meio do bloco</em> e só depois se
 * afasta, no centro eles se sobrepõem. Somando luz sobre luz, a sobreposição estoura para o branco-
 * amarelo enquanto as bordas ficam no vermelho. O miolo quente do Nitor não é uma peça desenhada: é o
 * empilhamento.
 *
 * <p>Os números são os do original: facho vermelho de escala meia, amarelinho de um quarto, vida entre
 * vinte e cinco e trinta e seis tiques, meio bloco de largura no nascimento encolhendo até sumir, alvo
 * sorteado a dois décimos do centro, e a gravidade negativa que os faz subir. O desenho vai pela porta
 * do {@code eyes}, que soma a cor ao que está atrás em vez de misturar — a diferença entre um brilho e
 * uma mancha marrom na areia.
 *
 * <p>Aqui os fachos não são partículas de verdade: eles são contados a partir do relógio do mundo, um
 * por vez, sempre nas mesmas contas. Sai igual, e sem depender do limite de partículas do jogo nem de o
 * bloco ficar cuspindo bicho no mundo.
 */
public class NitorRenderer implements BlockEntityRenderer<NitorBlockEntity, NitorRenderer.State> {
    private static final Identifier GLOW = Thaumcraft.id("textures/misc/glow.png");

    /** Quanto tempo um facho dura, em tiques. O original sorteia entre vinte e cinco e trinta e seis. */
    private static final float SHORTEST_LIFE = 25.0f;
    private static final float LONGEST_LIFE = 36.0f;
    /**
     * De quanto em quanto tempo cada lugar da nuvem torna a acender.
     *
     * <p>É maior do que a vida de um facho de propósito: a sobra é o silêncio entre um e outro. O facho
     * nasce num instante sorteado dentro da volta, vive a vida dele e some, e o lugar fica vazio até a
     * volta seguinte.
     */
    private static final float PERIOD = 46.0f;
    /**
     * Quantos lugares a nuvem tem. Nem todos estão acesos ao mesmo tempo.
     *
     * <p>No original a emissão é <strong>sorteada</strong>, um dado por tique: às vezes nascem três
     * fachos quase juntos e o miolo dá um estalo, às vezes abre um buraco e a chama afina. Esse
     * desencontro é o que faz o Nitor parecer vivo. Eu tinha feito os fachos nascerem em intervalos
     * exatos, e intervalo exato dá bola parada, por mais que cada um se mexa.
     */
    private static final int RED = 9;
    private static final int YELLOW = 7;
    /** A escala de cada facho; dela sai a largura, que é metade dela para cada lado. */
    private static final float RED_SCALE = 0.5f;
    private static final float YELLOW_SCALE = 0.25f;
    /**
     * O quanto o alvo do facho se afasta do centro — e é aqui que nasce o miolo quente.
     *
     * <p>No original o facho vermelho mira num ponto a dois décimos do centro e o amarelo a um décimo
     * só: os amarelos ficam apertados no meio enquanto os vermelhos se espalham. Somando luz, o meio
     * recebe vermelho <em>e</em> amarelo e estoura no laranja-claro, e a borda fica só no vermelho. Eu
     * tinha dado a mesma dispersão aos dois e o Nitor saiu uma bola vermelha chapada, sem miolo.
     */
    private static final float RED_SPREAD = 0.2f;
    private static final float YELLOW_SPREAD = 0.1f;
    /** O quanto cada cor pesa na soma; o amarelo pesa mais, senão o miolo não vence o vermelho em volta. */
    private static final int RED_ALPHA = 0x8C;
    private static final int YELLOW_ALPHA = 0xC0;
    /** A subida: o original põe gravidade negativa, que no jogo vira aceleração para cima. */
    private static final float RED_RISE = 0.025f * 0.04f;
    private static final float YELLOW_RISE = 0.02f * 0.04f;
    /**
     * A conta da subida é a de queda livre: meio da aceleração vezes o tempo ao quadrado.
     *
     * <p>Eu tinha esquecido o meio, e os fachos saíam voando um bloco inteiro para cima, esticando o
     * Nitor num rastro em vez de uma chama.
     */
    private static final float HALF = 0.5f;

    /** O que o desenhista precisa saber do Nitor neste quadro. */
    public static class State extends BlockEntityRenderState {
        public float ticks;
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
        state.ticks = nitor.getLevel() == null ? 0.0f : nitor.getLevel().getGameTime() + partial;
        // cada Nitor sorteia os seus próprios fachos, para dois lado a lado não ferverem no mesmo compasso
        state.seed = nitor.getBlockPos().hashCode();
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.5f, 0.5f, 0.5f);
        cloud(state, pose, collector, camera, RED, RED_SCALE, RED_RISE, RED_SPREAD, RED_ALPHA, 0);
        cloud(state, pose, collector, camera, YELLOW, YELLOW_SCALE, YELLOW_RISE, YELLOW_SPREAD, YELLOW_ALPHA, 1);
        pose.popPose();
    }

    /**
     * Uma leva de fachos da mesma cor.
     *
     * <p>Em vez de guardar uma lista viva, cada facho é uma conta. O lugar de índice {@code i} tem a sua
     * volta, e a cada volta ele sorteia quando acende e por quanto tempo — tudo a partir do número da
     * volta, que sai do relógio do mundo. Assim o desencontro é sempre o mesmo para o mesmo Nitor no
     * mesmo instante, e não há nada para guardar entre um quadro e outro.
     */
    private static void cloud(State state, PoseStack pose, SubmitNodeCollector collector,
                              CameraRenderState camera, int count, float scale, float rise,
                              float spread, int alpha, int hue) {
        for (int mote = 0; mote < count; mote++) {
            float turn = state.ticks / PERIOD + mote / (float) count;
            int round = (int) Math.floor(turn);
            float clock = (turn - round) * PERIOD;

            int seed = state.seed * 31 + round * 7919 + mote * 104729 + hue * 65537;
            // cada volta este lugar acende noutro instante e por outro tanto de tempo
            float life = SHORTEST_LIFE + noise(seed + 4) * (LONGEST_LIFE - SHORTEST_LIFE);
            float age = clock - noise(seed + 5) * (PERIOD - life);
            if (age < 0.0f || age >= life) continue;

            // para onde este facho vai: um ponto sorteado perto do centro, como no original
            float goX = (noise(seed) - 0.5f) * 2.0f * spread;
            float goZ = (noise(seed + 1) - 0.5f) * 2.0f * spread;
            float walk = age / life;

            // o facho começa do tamanho cheio e encolhe até sumir, que é o que o original faz com shrink
            // o encolhimento e mais rapido que o do original de proposito: la a emissao e sorteada, aqui
            // ha sempre um facho velho no ar, e facho velho grande vira bola pálida boiando em vez de ponta
            // de chama
            float shrink = (1.0f - walk) * (1.0f - walk * 0.7f);
            float half = 0.5f * scale * shrink;
            if (half <= 0.001f) continue;

            float[] rgb = colour(seed, hue);
            // e vai apagando junto. O original só encolhe, mas lá a emissão é sorteada e às vezes não há
            // facho velho nenhum; aqui há sempre um, e um facho velho que só encolheu fica pairando meio
            // bloco acima como uma bolinha solta, que é justamente o que não parece chama
            float fade = 1.0f - walk * 0.85f;
            // meia opacidade, como o original: é a soma de vários fachos, e não um só, que faz o miolo
            int colour = ((int) (alpha * fade) << 24)
                    | ((int) (rgb[0] * 255.0f) << 16)
                    | ((int) (rgb[1] * 255.0f) << 8)
                    | (int) (rgb[2] * 255.0f);

            final float size = half;
            pose.pushPose();
            pose.translate(goX * walk, HALF * rise * age * age, goZ * walk);
            pose.mulPose(camera.orientation);
            collector.submitCustomGeometry(pose, RenderTypes.eyes(GLOW), (matrix, consumer) -> {
                corner(matrix, consumer, -size, -size, 0.0f, 1.0f, colour);
                corner(matrix, consumer, size, -size, 1.0f, 1.0f, colour);
                corner(matrix, consumer, size, size, 1.0f, 0.0f, colour);
                corner(matrix, consumer, -size, size, 0.0f, 0.0f, colour);
            });
            pose.popPose();
        }
    }

    /**
     * As duas cores do Nitor, tiradas da tabela do {@code FXWisp}.
     *
     * <p>A quatro é o vermelho e a um o amarelo — são esses dois índices que o {@code TileNitor} pede.
     */
    private static float[] colour(int seed, int hue) {
        if (hue == 0) return new float[]{0.7f + noise(seed + 2) * 0.3f, 0.2f, 0.2f};
        return new float[]{0.5f + noise(seed + 2) * 0.3f, 0.5f + noise(seed + 3) * 0.3f, 0.2f};
    }

    /** Um número entre zero e um, sempre o mesmo para a mesma semente. */
    private static float noise(int seed) {
        int x = seed * 0x9E3779B1;
        x ^= x >>> 15;
        x *= 0x85EBCA6B;
        x ^= x >>> 13;
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
