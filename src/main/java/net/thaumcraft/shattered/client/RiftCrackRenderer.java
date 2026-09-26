package net.thaumcraft.shattered.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

/**
 * O rasgão da fenda: o {@code RiftCrackRenderer} das Portas Dimensionais.
 *
 * <p>É a gavinha do {@link RiftTendril} pendurada no ar — preta como céu sem lua, com estrelas presas na pele —, e
 * ela abana. Três coisas mexem nela ao mesmo tempo, e são as três do original: o <b>tremor</b>, que abana o rasgão
 * inteiro de um lado para o outro e cresce com o cubo do tamanho dele; o <b>esvoaçar</b>, que mexe cada canto por
 * conta própria, com dez ondas diferentes a correr e tanto mais quanto mais alto o canto estiver — o pé fica
 * quieto e a ponta ondula, que é como uma gavinha se mexe; e o <b>giro</b>, que é o lado a que a fenda ficou
 * virada quando nasceu.
 *
 * <p>O tempo de cada fenda é o dela: a conta leva um número tirado do lugar onde ela está, de modo que duas
 * fendas lado a lado não tremem juntas.
 *
 * <p><b>Duas diferenças declaradas.</b> A primeira: lá o rasgão é pintado com uma mistura que escurece o que está
 * atrás ({@code GL_ONE_MINUS_DST_COLOR}), e essa mistura já não existe no jogo de hoje — aqui ele vai de preto
 * quase opaco, que é o que se via lá. A segunda é de feitio, e foi pedida duas vezes: lá o rasgão é um rabisco de
 * dragão, chato e quadrado; aqui é um corpo redondo de três dimensões, que se vê de qualquer lado.
 */
public final class RiftCrackRenderer {
    /** O desenho: cor só, sem folha, sem corte de faces e sem escrever fundura. */
    private static final RenderType FENDA = RenderTypes.debugQuads();

    /** A cor do corpo dela: preto de céu sem lua, com um fio de azul. */
    private static final int ALPHA = 0xF0;
    private static final int RED = 0x32;
    private static final int GREEN = 0x2D;
    private static final int BLUE = 0x5E;

    /**
     * O quanto o rasgão abana por inteiro, e o quanto cada canto esvoaça por conta própria.
     *
     * <p><b>Desvio declarado:</b> o original esvoaça seis décimos por igual em todo o rabisco. Numa gavinha isso
     * não serve — o pé dela está preso ao mundo e não se mexe. Aqui o esvoaçar cresce com a altura do canto, do
     * nada no pé até seis décimos na ponta.
     */
    private static final double JITTER = 1.0;
    private static final float FLUTTER = 0.6f;
    private static final int FLUTTER_WAVES = 10;
    private static final float SPEED = 0.014f;

    /** Por quanto se divide o tamanho da fenda para dar a medida do rasgão em blocos. */
    private static final double SIZE_SCALE = 150.0;

    /** O piscar das estrelas: quão depressa vão e quanto do brilho delas o piscar come. */
    private static final float TWINKLE_SPEED = 0.0022f;
    private static final float TWINKLE_DEPTH = 0.75f;

    private RiftCrackRenderer() {
    }

    /**
     * Desenha o rasgão de uma fenda.
     *
     * @param giro    para que lado a fenda está virada, em graus
     * @param tamanho o {@code size} dela
     * @param onde    a casa em que ela mora, que dá o tempo próprio dela
     */
    public static void submit(PoseStack pose, SubmitNodeCollector collector, int curva,
                              float giro, float tamanho, BlockPos onde) {
        RiftTendril.Tendril gavinha = RiftTendril.get(curva);
        if (gavinha.triangles() == 0) return;

        double medida = tamanho / SIZE_SCALE;
        if (medida <= 0.0001) return;
        double escala = medida / gavinha.length();

        // o tempo próprio desta fenda, para duas vizinhas não tremerem juntas
        long semente = (long) (4.045620584E9 * (onde.getX() + onde.getY() * 4194304.0 + onde.getZ() * 8.796093E12));
        float tempo = (float) ((System.currentTimeMillis() + semente) % 2000000L);

        double abano = JITTER * medida * medida * medida / 2000.0;
        double abanoX = abano * Math.sin(1.1f * tempo * medida * SPEED) * Math.sin(0.8f * tempo * SPEED);
        double abanoY = abano * Math.sin(1.2f * tempo * medida * SPEED) * Math.sin(0.9f * tempo * SPEED);
        double abanoZ = abano * Math.sin(1.3f * tempo * medida * SPEED) * Math.sin(0.7f * tempo * SPEED);

        double[] ondas = new double[FLUTTER_WAVES];
        for (int i = 0; i < FLUTTER_WAVES; i++) {
            ondas[i] = Math.sin((1.0f + i / 10.0f) * tempo * SPEED)
                    * Math.cos(1.0f - i / 10.0f * tempo * SPEED) * FLUTTER;
        }

        double cos = Math.cos(Math.toRadians(giro));
        double sin = Math.sin(Math.toRadians(giro));
        float[] pontos = gavinha.points();
        float[] luz = gavinha.shade();
        float comprido = gavinha.length();

        collector.submitCustomGeometry(pose, FENDA, (m, v) -> {
            for (int canto = 0; canto + 9 <= pontos.length; canto += 9) {
                for (int i = 0; i < 4; i++) {
                    // o quarto canto é o terceiro outra vez: é o triângulo posto num quadrado
                    int passo = Math.min(i, 2);
                    int qual = canto + passo * 3;
                    int cor = tinge(luz[canto / 3 + passo]);
                    vertex(m, v, pontos[qual], pontos[qual + 1], pontos[qual + 2], ondas, cos, sin, escala,
                            comprido, abanoX, abanoY, abanoZ, cor);
                }
            }
        });

        stars(pose, collector, gavinha, tempo, ondas, cos, sin, escala, abanoX, abanoY, abanoZ);
    }

    /**
     * As estrelas presas na pele: quadradinhos brancos a piscar, cada um no seu compasso, levados pelo mesmo
     * esvoaçar do corpo — senão descolavam-se dele quando a gavinha ondula.
     */
    private static void stars(PoseStack pose, SubmitNodeCollector collector, RiftTendril.Tendril gavinha,
                              float tempo, double[] ondas, double cos, double sin, double escala,
                              double abanoX, double abanoY, double abanoZ) {
        float[] estrelas = gavinha.stars();
        if (estrelas.length == 0) return;
        float comprido = gavinha.length();
        collector.submitCustomGeometry(pose, FENDA, (m, v) -> {
            for (int i = 0; i + 5 <= estrelas.length; i += 5) {
                float px = estrelas[i], py = estrelas[i + 1], pz = estrelas[i + 2];
                float compasso = estrelas[i + 3], medida = estrelas[i + 4];
                float brilho = 1.0f - TWINKLE_DEPTH * (0.5f - 0.5f * Mth.cos(tempo * TWINKLE_SPEED + compasso));
                int cor = (Math.round(brilho * 255.0f) << 24) | 0xFFFFFF;
                for (int quem = 0; quem < 4; quem++) {
                    float dx = (quem == 1 || quem == 2) ? medida : -medida;
                    float dy = quem >= 2 ? medida : -medida;
                    vertex(m, v, px + dx, py + dy, pz, ondas, cos, sin, escala, comprido,
                            abanoX, abanoY, abanoZ, cor);
                }
            }
        });
    }

    /** Um canto da gavinha, com o esvoaçar dele, o giro da fenda e o abano de todos. */
    private static void vertex(PoseStack.Pose m, VertexConsumer v, float px, float py, float pz, double[] ondas,
                               double cos, double sin, double escala, float comprido,
                               double abanoX, double abanoY, double abanoZ, int cor) {
        // o original escolhe a onda de cada canto por uma conta com o lugar dele, e é a mesma sempre
        int qual = Math.abs((int) ((px + py) * (px + py + 1) / 2 + pz));
        // e o pé não se mexe: o esvoaçar cresce com a altura
        double quanto = Mth.clamp(py / comprido, 0.0f, 1.0f);
        quanto *= quanto;
        double a = ondas[(qual + 1) % FLUTTER_WAVES] * quanto;
        double b = ondas[(qual + 2) % FLUTTER_WAVES] * quanto;
        double c = ondas[qual % FLUTTER_WAVES] * quanto;

        double x = px + a;
        double y = py + c;
        double z = pz + b;

        // o giro da fenda, à volta do pé dela
        double gx = x * cos - z * sin;
        double gz = x * sin + z * cos;

        v.addVertex(m, (float) (gx * escala + abanoX), (float) (y * escala + abanoY), (float) (gz * escala + abanoZ))
                .setColor(cor);
    }

    /** A cor do corpo com a luz daquele canto por cima. */
    private static int tinge(float luz) {
        float quanto = Mth.clamp(luz, 0.0f, 1.0f);
        return (ALPHA << 24)
                | (Math.round(RED * quanto) << 16)
                | (Math.round(GREEN * quanto) << 8)
                | Math.round(BLUE * quanto);
    }

    /** O quanto o rasgão mede de ponta a ponta, em blocos — serve aos testes. */
    public static double span(float tamanho) {
        return Mth.clamp(tamanho / SIZE_SCALE, 0.0, 64.0);
    }
}
