package net.thaumcraft.shattered.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.shattered.DimensionalDoorBlock;
import net.thaumcraft.shattered.RiftBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;


import java.util.Random;

/**
 * O vão da Porta Dimensional: o {@code DimensionalPortalRenderer} das Portas Dimensionais.
 *
 * <p>São dezesseis panos da mesma folha, um por cima do outro, cada um com a sua cor, o seu tamanho, o seu giro e
 * o seu correr no tempo — o primeiro por baixo, com a tinta normal, e os outros quinze a somar luz. O desenho de
 * cada pano não sai do pano: sai de onde a casa está <i>diante da câmara</i>, e por isso o fundo do vão mexe-se
 * quando quem joga anda, e parece um túnel, e não um papel colado.
 *
 * <p>O original faz isso com o {@code glTexGen} de olho, que dá as coordenadas por pixel. Hoje não há {@code
 * TexGen}: as contas dele estão feitas aqui, canto a canto, com a posição de cada canto no quadro da câmara. Num
 * pano de uma casa por duas a diferença entre contar por pixel e contar por canto é pequena, e o que se vê é o
 * mesmo vem-e-vai.
 */
public class DimensionalPortalRenderer
        implements BlockEntityRenderer<RiftBlockEntity, DimensionalPortalRenderer.State> {
    private static final Identifier WARP = Thaumcraft.id("textures/misc/warp.png");

    /** Quantos panos, como no original. */
    private static final int LAYERS = 16;
    /**
     * As dezesseis cores do {@code getEntranceRenderColor}, com a mesma semente do original.
     *
     * <p>Lá há duas paletas: a do Nether é vermelha e a de todo o resto é desta água-marinha. O porte não tem
     * porta no Nether, então fica só esta.
     */
    private static final float[][] COLOURS = colours();

    private static float[][] colours() {
        Random sorte = new Random(31100L);
        float[][] saida = new float[LAYERS][3];
        for (int i = 0; i < LAYERS; i++) {
            saida[i][0] = sorte.nextFloat() * 0.5f + 0.1f;
            saida[i][1] = sorte.nextFloat() * 0.4f + 0.4f;
            saida[i][2] = sorte.nextFloat() * 0.6f + 0.5f;
        }
        return saida;
    }
    /**
     * Os dois desenhos do vão: o pano de baixo é sólido e escuro, e os outros quinze somam luz por cima dele.
     *
     * <p>É o que o original faz — lá o primeiro pano vai com a tinta normal e os outros com a que soma. Aqui há
     * uma manha a mais: quem manda na ordem do desenho é o tipo dele, e não quem o mandou desenhar, e um pano
     * translúcido acabava por cima de tudo e tapava o resto. Sólido, ele vai na primeira leva, que é o que se
     * quer; e por ser sólido, tapa o que está atrás, que é o que faz o vão ler-se à luz do dia.
     */
    private static final RenderType FUNDO = RenderTypes.entitySolid(WARP);
    private static final RenderType SOMA = RenderTypes.energySwirl(WARP, 0.0f, 0.0f);

    public static class State extends BlockEntityRenderState {
        boolean door;
        /** Em que eixo a folha da porta é fina, que é onde o vão se cola. */
        boolean thinOnZ = true;
        /** A caixa da folha, já em medida de bloco: onde ela começa e acaba nos três eixos. */
        float minA, maxA, minFundo, maxFundo;
    }

    public DimensionalPortalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    /** O vão sobe um bloco acima da metade de baixo, onde mora a fenda. */
    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public void extractRenderState(RiftBlockEntity fenda, State state, float partial, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(fenda, state, partial, camera, crumbling);
        var bloco = fenda.getBlockState();
        state.door = bloco.getBlock() instanceof DimensionalDoorBlock
                && bloco.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
        if (!state.door || fenda.getLevel() == null) return;

        // o vão fica no buraco da porta, e não na folha: é a forma da porta FECHADA que o diz. Assim ele cola-se
        // à folha enquanto ela está fechada e fica no lugar quando ela abre, que é o que o original faz — lá o
        // vão é um bloco à parte, e não a porta.
        var fechada = bloco.hasProperty(DoorBlock.OPEN) ? bloco.setValue(DoorBlock.OPEN, false) : bloco;
        var forma = fechada.getShape(fenda.getLevel(), fenda.getBlockPos());
        if (forma.isEmpty()) return;
        var caixa = forma.bounds();
        state.thinOnZ = (caixa.maxZ - caixa.minZ) <= (caixa.maxX - caixa.minX);
        if (state.thinOnZ) {
            state.minA = (float) caixa.minX;
            state.maxA = (float) caixa.maxX;
            state.minFundo = (float) caixa.minZ;
            state.maxFundo = (float) caixa.maxZ;
        } else {
            state.minA = (float) caixa.minZ;
            state.maxA = (float) caixa.maxZ;
            state.minFundo = (float) caixa.minX;
            state.maxFundo = (float) caixa.maxX;
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        // a fenda solta do mundo tem desenho próprio; aqui só se trata do vão das portas
        if (!state.door) return;

        float tempo = (float) (System.currentTimeMillis() % 200000L) / 200000.0f;
        for (int pano = 0; pano < LAYERS; pano++) {
            // o original: o pano de baixo é escuro e distante, o segundo é o maior, e os outros vão apertando
            float distancia = LAYERS - pano;
            float medida = 0.2625f;
            float tinta = 1.0f / (distancia + 0.8f);
            if (pano == 0) {
                tinta = 0.1f;
                distancia = 25.0f;
                medida = 0.125f;
            } else if (pano == 1) {
                medida = 0.5f;
            }
            float giro = (float) Math.toRadians((pano * pano * 4321 + pano * 9) * 2.0);
            float cos = Mth.cos(giro), sin = Mth.sin(giro);
            float corre = tempo * distancia;
            float[] cor = COLOURS[pano];
            int argb = argb(1.0f, cor[0] * tinta, cor[1] * tinta, cor[2] * tinta);


            // o pano de baixo fica um fio mais para dentro, para os que somam ficarem à frente dele
            final float medidaF = medida, cosF = cos, sinF = sin, correF = corre;
            collector.submitCustomGeometry(pose, pano == 0 ? FUNDO : SOMA,
                    (m, v) -> quad(m, v, state, medidaF, cosF, sinF, correF, argb));
        }
    }

    /**
     * O pano: a cara da folha da porta, dos dois lados dela, de baixo até ao alto das duas metades.
     *
     * <p>Não se corta face nenhuma, e por isso o mesmo pano vai nas duas voltas — visto de um lado e do outro.
     */
    private static void quad(PoseStack.Pose m, VertexConsumer v, State state,
                             float medida, float cosGiro, float sinGiro, float corre, int argb) {
        // de que lado a folha olha, para a conta do desenho sair como no original
        Direction olhar = state.thinOnZ ? Direction.NORTH : Direction.WEST;
        // um pano só, no meio da folha da porta. Eram dois, um em cada cara dela, e de lado viam-se os dois —
        // o de trás por detrás do da frente, a três dedos um do outro. Como o pano vai desenhado nas duas
        // voltas, um chega para os dois lados.
        float fundura = (state.minFundo + state.maxFundo) / 2.0f;
        for (boolean avesso : new boolean[]{false, true}) {
            for (int i = 0; i < 4; i++) {
                int qual = avesso ? 3 - i : i;
                float largo = (qual == 1 || qual == 2) ? state.maxA : state.minA;
                float alto = qual >= 2 ? 2.0f : 0.0f;
                if (state.thinOnZ) canto(m, v, largo, alto, fundura, olhar, medida, cosGiro, sinGiro, corre, argb);
                else canto(m, v, fundura, alto, largo, olhar, medida, cosGiro, sinGiro, corre, argb);
            }
        }
    }

    /**
     * Um canto do pano, com o desenho tirado de onde ele está diante da câmara.
     *
     * <p>É a conta do {@code glTexGen} do original: de um lado tira-se o alto e o través, do outro a fundura, e o
     * quarto valor — o que o original chama Q — divide os dois primeiros. É essa divisão que faz o fundo parecer
     * ir longe. Depois vem a matriz de textura do original, na ordem dele: meio, giro, meio, medida e o correr.
     */
    private static void canto(PoseStack.Pose m, VertexConsumer v, float x, float y, float z, Direction facing,
                              float medida, float cosGiro, float sinGiro, float corre, int argb) {
        // onde este canto fica diante da câmara, sem pedir memória emprestada a cada canto
        Matrix4f matriz = m.pose();
        float ex = matriz.m00() * x + matriz.m10() * y + matriz.m20() * z + matriz.m30();
        float ey = matriz.m01() * x + matriz.m11() * y + matriz.m21() * z + matriz.m31();
        float ez = matriz.m02() * x + matriz.m12() * y + matriz.m22() * z + matriz.m32();

        float s = ey;
        float t;
        float q;
        switch (facing) {
            case NORTH -> {
                t = ex;
                q = ez + 0.15f;
            }
            case SOUTH -> {
                t = ex;
                q = ez - 0.15f;
            }
            case WEST -> {
                t = ez;
                q = ex + 0.15f;
            }
            default -> {
                t = ez;
                q = ex - 0.15f;
            }
        }
        if (Math.abs(q) < 1.0e-4f) q = q < 0 ? -1.0e-4f : 1.0e-4f;

        // translate(0.5, 0.5, 0.5) — num quadro de textura o deslocamento anda com o Q
        s += 0.5f * q;
        t += 0.5f * q;
        // rotate em Z
        float rs = s * cosGiro - t * sinGiro;
        float rt = s * sinGiro + t * cosGiro;
        s = rs;
        t = rt;
        // translate outra vez, depois a medida e o correr
        s += 0.5f * q;
        t += 0.5f * q;
        s *= medida;
        t *= medida;
        t += corre * q;

        v.addVertex(m, x, y, z)
                .setColor(argb)
                .setUv(s / q, t / q)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(m, 0.0f, 1.0f, 0.0f);
    }

    private static int argb(float a, float r, float g, float b) {
        return ((int) (Mth.clamp(a, 0.0f, 1.0f) * 255.0f) << 24)
                | ((int) (Mth.clamp(r, 0.0f, 1.0f) * 255.0f) << 16)
                | ((int) (Mth.clamp(g, 0.0f, 1.0f) * 255.0f) << 8)
                | (int) (Mth.clamp(b, 0.0f, 1.0f) * 255.0f);
    }
}
