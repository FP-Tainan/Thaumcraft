package net.thaumcraft.shattered.client;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * O feitio do rasgão de uma fenda: um talho de pé no ar, estreito, afilado nas duas pontas, de beiras tortas, com
 * uma gavinha ou outra a sair-lhe do lado.
 *
 * <p><b>Isto é do porte, e não do original.</b> Lá o rasgão é um rabisco de dragão — um sistema-L andado num papel
 * quadriculado, que dá uma mancha larga e quadrada. Quem manda pediu outra coisa: uma fenda como as que se veem
 * por aí, um talho alto e preto com fagulhas de estrela à volta. É esse o feitio que se monta aqui.
 *
 * <p>O que lia os rabiscos de dragão — o {@code RiftCurves}, o {@code rift_curves.mesh} e o
 * {@code scratchpad/dd-curvas.js} que o fazia — saiu daqui e ficou no histórico, que é onde há de estar se
 * alguém quiser o feitio do original de volta.
 *
 * <p>Cada fenda tem o seu: o número que ela sorteou quando nasceu é a semente, e dele saem o torcer da espinha, o
 * inchar da barriga e as gavinhas. Duas fendas do mesmo número são iguais; de números diferentes, não.
 */
public final class RiftTear {
    /** De ponta a ponta, e a meia-largura da barriga. */
    private static final float HEIGHT = 64.0f;
    private static final float BELLY = 9.0f;

    /** Em quantos degraus a espinha se divide, e o quanto ela se torce de um para o outro. */
    private static final int STEPS = 40;
    private static final float SWAY = 1.1f;

    /** Quantas gavinhas saem do lado, e que tamanho têm em relação ao talho. */
    private static final int MIN_TENDRILS = 1;
    private static final int MAX_TENDRILS = 3;
    private static final float TENDRIL_LENGTH = 0.26f;

    /** Quantas fagulhas de estrela andam à volta de um talho. */
    private static final int STARS = 44;

    /** Um feitio: a caixa dele e os triângulos, três cantos de dois números cada. */
    public record Shape(float minX, float maxX, float minY, float maxY, float[] points) {
        /** Quantos triângulos ele tem. */
        public int triangles() {
            return this.points.length / 6;
        }

        public float width() {
            return this.maxX - this.minX;
        }

        public float height() {
            return this.maxY - this.minY;
        }
    }

    /** O talho e as fagulhas dele. */
    public record Tear(Shape shape, float[] stars) {
    }

    private static final Map<Integer, Tear> FEITOS = new ConcurrentHashMap<>();

    private RiftTear() {
    }

    /** O talho de número n. */
    public static Tear get(int qual) {
        return FEITOS.computeIfAbsent(qual, RiftTear::build);
    }

    /** Esquece os que montou — serve aos testes. */
    public static void forget() {
        FEITOS.clear();
    }

    private static Tear build(int semente) {
        RandomSource sorte = RandomSource.create(semente * 341873128712L + 132897987541L);
        List<Float> pontos = new ArrayList<>();

        // a espinha: de baixo para cima, torcendo-se devagar
        float[] espinha = new float[STEPS + 1];
        float torto = 0.0f;
        float passo = 0.0f;
        for (int i = 0; i <= STEPS; i++) {
            passo += (sorte.nextFloat() - 0.5f) * SWAY;
            passo *= 0.72f;
            torto += passo;
            espinha[i] = torto;
        }

        // e o corpo: uma barriga que incha no meio e afina nas pontas, com as beiras roídas
        float[] largura = new float[STEPS + 1];
        for (int i = 0; i <= STEPS; i++) {
            float t = i / (float) STEPS * 2.0f - 1.0f;
            float cheio = (float) Math.pow(Math.max(0.0, 1.0 - t * t), 0.75);
            largura[i] = BELLY * cheio * (0.70f + sorte.nextFloat() * 0.55f);
        }
        // as pontas fecham-se de todo, senão o talho acaba num corte reto
        largura[0] = 0.0f;
        largura[STEPS] = 0.0f;

        tira(pontos, espinha, largura, 0.0f, HEIGHT / STEPS, 0.0f);

        // as gavinhas: talhos pequenos a sair do lado do grande, cada um com a sua espinha
        int quantas = MIN_TENDRILS + sorte.nextInt(MAX_TENDRILS - MIN_TENDRILS + 1);
        for (int g = 0; g < quantas; g++) {
            int onde = 6 + sorte.nextInt(STEPS - 12);
            int degraus = Math.max(4, (int) (STEPS * TENDRIL_LENGTH * (0.6f + sorte.nextFloat() * 0.8f)));
            float lado = sorte.nextBoolean() ? 1.0f : -1.0f;
            float[] espinhaG = new float[degraus + 1];
            float[] larguraG = new float[degraus + 1];
            float andar = espinha[onde];
            float inclina = lado * (0.7f + sorte.nextFloat() * 0.9f);
            for (int i = 0; i <= degraus; i++) {
                andar += inclina + (sorte.nextFloat() - 0.5f) * 0.6f;
                espinhaG[i] = andar;
                float t = i / (float) degraus;
                larguraG[i] = largura[onde] * 0.42f * (1.0f - t) * (0.6f + sorte.nextFloat() * 0.7f);
            }
            larguraG[degraus] = 0.0f;
            // a gavinha sobe mais devagar do que o talho, e por isso parece sair-lhe de lado
            tira(pontos, espinhaG, larguraG, onde * (HEIGHT / STEPS), HEIGHT / STEPS * 0.45f, 0.0f);
        }

        float[] saída = new float[pontos.size()];
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (int i = 0; i < saída.length; i++) {
            saída[i] = pontos.get(i);
            if ((i & 1) == 0) {
                minX = Math.min(minX, saída[i]);
                maxX = Math.max(maxX, saída[i]);
            } else {
                minY = Math.min(minY, saída[i]);
                maxY = Math.max(maxY, saída[i]);
            }
        }
        var forma = new Shape(minX, maxX, minY, maxY, saída);

        // as fagulhas: umas dentro do talho, outras a boiar em volta dele, cada uma com o seu compasso
        float[] fagulhas = new float[STARS * 5];
        for (int i = 0; i < STARS; i++) {
            int degrau = sorte.nextInt(STEPS + 1);
            float longe = 1.6f + sorte.nextFloat() * sorte.nextFloat() * 6.0f;
            fagulhas[i * 5] = espinha[degrau] + (sorte.nextFloat() - 0.5f) * 2.0f * BELLY * longe;
            fagulhas[i * 5 + 1] = degrau * (HEIGHT / STEPS) + (sorte.nextFloat() - 0.5f) * 2.0f;
            fagulhas[i * 5 + 2] = sorte.nextFloat() * (float) (Math.PI * 2.0);
            fagulhas[i * 5 + 3] = 0.22f + sorte.nextFloat() * 0.40f;
            // a maior parte é branca; uma em cada três puxa para o roxo do vazio
            fagulhas[i * 5 + 4] = sorte.nextInt(3) == 0 ? 0.4f + sorte.nextFloat() * 0.6f : 0.0f;
        }
        return new Tear(forma, fagulhas);
    }

    /** Uma tira de triângulos entre a beira esquerda e a direita de uma espinha. */
    private static void tira(List<Float> saída, float[] espinha, float[] largura,
                             float base, float altura, float desvio) {
        for (int i = 0; i < espinha.length - 1; i++) {
            float y0 = base + i * altura, y1 = base + (i + 1) * altura;
            float e0 = espinha[i] + desvio - largura[i], d0 = espinha[i] + desvio + largura[i];
            float e1 = espinha[i + 1] + desvio - largura[i + 1], d1 = espinha[i + 1] + desvio + largura[i + 1];
            triângulo(saída, e0, y0, d0, y0, d1, y1);
            triângulo(saída, e0, y0, d1, y1, e1, y1);
        }
    }

    private static void triângulo(List<Float> saída, float ax, float ay, float bx, float by, float cx, float cy) {
        saída.add(ax);
        saída.add(ay);
        saída.add(bx);
        saída.add(by);
        saída.add(cx);
        saída.add(cy);
    }
}
