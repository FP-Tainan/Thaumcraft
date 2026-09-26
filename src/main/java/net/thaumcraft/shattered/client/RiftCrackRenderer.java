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
 * <p>É um talho preto pendurado no ar, que treme, com fagulhas de estrela a piscar em volta. Três coisas mexem
 * nele ao mesmo tempo, e são as três do original: o <b>tremor</b>, que abana o rasgão inteiro de um lado para o
 * outro e cresce com o cubo do tamanho dele; o <b>esvoaçar</b>, que mexe cada canto por conta própria, com dez
 * ondas diferentes a correr; e o <b>giro</b>, que é o lado a que a fenda ficou virada quando nasceu.
 *
 * <p>O tempo de cada fenda é o dela: a conta leva um número tirado do lugar onde ela está, de modo que duas
 * fendas lado a lado não tremem juntas.
 *
 * <p><b>Duas diferenças declaradas.</b> A primeira: lá o rasgão é pintado com uma mistura que escurece o que está
 * atrás ({@code GL_ONE_MINUS_DST_COLOR}), e essa mistura já não existe no jogo de hoje — aqui ele vai de preto
 * quase opaco, que é o que se via lá. A segunda é de feitio, e foi pedida: lá o rasgão é um rabisco de dragão,
 * largo e quadrado; aqui é o talho alto do {@link RiftTear}, com as fagulhas dele.
 */
public final class RiftCrackRenderer {
    /** O desenho: cor só, sem folha, sem corte de faces e sem escrever fundura. */
    private static final RenderType FENDA = RenderTypes.debugQuads();

    /** A cor do rasgão, a do original. */
    private static final int COLOUR = 0xD8141418;

    /**
     * O quanto o rasgão abana por inteiro, e o quanto cada canto esvoaça por conta própria.
     *
     * <p><b>Desvio pequeno:</b> o original esvoaça seis décimos. Lá o rabisco é feito de poucos triângulos
     * grandes e o esvoaçar só lhe ondula as beiras; aqui ele é feito de muitas tiras pequenas, e seis décimos
     * esfarelavam-no. Dois décimos e meio dão o mesmo tremor sem o desfazer.
     */
    private static final double JITTER = 1.0;
    private static final float FLUTTER = 0.25f;
    private static final int FLUTTER_WAVES = 10;
    private static final float SPEED = 0.014f;

    /** Por quanto se divide o tamanho da fenda para dar a medida do rasgão em blocos. */
    private static final double SIZE_SCALE = 150.0;

    /** O piscar das fagulhas: quão depressa vão e quanto do brilho delas o piscar come. */
    private static final float TWINKLE_SPEED = 0.0022f;
    private static final float TWINKLE_DEPTH = 0.7f;

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
        RiftTear.Tear talho = RiftTear.get(curva);
        RiftTear.Shape forma = talho.shape();
        if (forma == null || forma.triangles() == 0) return;

        double medida = tamanho / SIZE_SCALE;
        if (medida <= 0.0001) return;
        // o talho é alto e estreito: quem manda na medida é o lado maior dele, e não a largura
        double maior = Math.max(forma.width(), forma.height());
        double escala = medida / Math.max(1.0, maior);
        double meioX = (forma.maxX() + forma.minX()) / 2.0;
        double meioY = (forma.maxY() + forma.minY()) / 2.0;

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
        float[] pontos = forma.points();

        collector.submitCustomGeometry(pose, FENDA, (m, v) -> {
            for (int canto = 0; canto + 6 <= pontos.length; canto += 6) {
                for (int i = 0; i < 4; i++) {
                    // o quarto canto é o terceiro outra vez: é o triângulo posto num quadrado
                    int qual = canto + Math.min(i, 2) * 2;
                    vertex(m, v, pontos[qual], pontos[qual + 1], ondas, cos, sin, escala,
                            meioX, meioY, abanoX, abanoY, abanoZ, COLOUR);
                }
            }
        });

        stars(pose, collector, talho.stars(), tempo, ondas, cos, sin, escala,
                meioX, meioY, abanoX, abanoY, abanoZ);
    }

    /**
     * As fagulhas de estrela: quadradinhos brancos a piscar, cada um no seu compasso, no mesmo plano do talho e
     * levados pelo mesmo tremor — senão descolavam-se dele quando a fenda abana.
     */
    private static void stars(PoseStack pose, SubmitNodeCollector collector, float[] fagulhas, float tempo,
                              double[] ondas, double cos, double sin, double escala,
                              double meioX, double meioY, double abanoX, double abanoY, double abanoZ) {
        if (fagulhas.length == 0) return;
        collector.submitCustomGeometry(pose, FENDA, (m, v) -> {
            for (int i = 0; i + 5 <= fagulhas.length; i += 5) {
                float px = fagulhas[i], py = fagulhas[i + 1];
                float compasso = fagulhas[i + 2], medida = fagulhas[i + 3], roxo = fagulhas[i + 4];
                float brilho = 1.0f - TWINKLE_DEPTH * (0.5f - 0.5f * Mth.cos(tempo * TWINKLE_SPEED + compasso));
                // do branco para o roxo do vazio, que é a cor que o resto do ramo usa
                int vermelho = Math.round(255.0f - roxo * (255.0f - 0x9B));
                int verde = Math.round(255.0f - roxo * (255.0f - 0x4D));
                int azul = 255;
                int cor = (Math.round(brilho * 255.0f) << 24) | (vermelho << 16) | (verde << 8) | azul;
                for (int quem = 0; quem < 4; quem++) {
                    float dx = (quem == 1 || quem == 2) ? medida : -medida;
                    float dy = quem >= 2 ? medida : -medida;
                    vertex(m, v, px + dx, py + dy, ondas, cos, sin, escala,
                            meioX, meioY, abanoX, abanoY, abanoZ, cor);
                }
            }
        });
    }

    /** Um canto do rabisco, com o esvoaçar dele, o giro da fenda e o abano de todos. */
    private static void vertex(PoseStack.Pose m, VertexConsumer v, float px, float py, double[] ondas,
                               double cos, double sin, double escala, double meioX, double meioY,
                               double abanoX, double abanoY, double abanoZ, int cor) {
        // o original escolhe a onda de cada canto por uma conta com o lugar dele, e é a mesma sempre
        int qual = Math.abs((int) ((px + py) * (px + py + 1) / 2 + py));
        double a = ondas[(qual + 1) % FLUTTER_WAVES];
        double b = ondas[(qual + 2) % FLUTTER_WAVES];
        double c = ondas[qual % FLUTTER_WAVES];

        double x = (px + a - meioX) * cos - b * sin;
        double y = py + c - meioY;
        double z = (px + b) * sin + b * cos;

        v.addVertex(m, (float) (x * escala + abanoX), (float) (y * escala + abanoY), (float) (z * escala + abanoZ))
                .setColor(cor);
    }

    /** O quanto o rasgão mede de ponta a ponta, em blocos — serve aos testes. */
    public static double span(float tamanho) {
        return Mth.clamp(tamanho / SIZE_SCALE, 0.0, 64.0);
    }
}
