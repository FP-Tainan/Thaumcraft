package net.thaumcraft.shattered.client;

import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * O feitio do rasgão de uma fenda: uma gavinha comprida que sobe do nada e se enrola, grossa em baixo e a afinar
 * até a ponta se perder, toda de um preto de céu sem lua com estrelas presas dentro.
 *
 * <p><b>Isto é do porte, e não do original.</b> Lá o rasgão é um rabisco de dragão — um sistema-L andado num papel
 * quadriculado, que dá uma mancha chata e quadrada. Quem manda mostrou o que queria numa foto e é isto: um corpo
 * de três dimensões, redondo, que se vê de qualquer lado, e não um recorte de papel virado para um lado só.
 *
 * <p>O que lia os rabiscos de dragão — o {@code RiftCurves}, o {@code rift_curves.mesh} e o
 * {@code scratchpad/dd-curvas.js} que o fazia — ficou no histórico, e com ele o {@code RiftTear} chato que veio a
 * seguir.
 *
 * <p>Cada fenda tem a sua: o número que ela sorteou quando nasceu é a semente, e dele saem o virar da espinha, a
 * grossura, e onde é que cada estrela fica presa. Duas fendas do mesmo número são iguais.
 */
public final class RiftTendril {
    /** De ponta a ponta, e a grossura no ponto mais gordo. */
    private static final float LENGTH = 64.0f;
    private static final float GIRTH = 3.6f;

    /** Em quantos anéis a gavinha se divide, e de quantos lados é cada anel. */
    private static final int RINGS = 48;
    private static final int SIDES = 8;

    /** O quanto ela se vira de um anel para o outro, e o quanto esse virar se vai acumulando. */
    private static final float TURN = 0.095f;
    private static final float TURN_DRIFT = 0.028f;

    /** Quantas estrelas ficam presas no corpo dela. */
    private static final int STARS = 120;

    /** Onde ela dobra o virar para o outro lado, e fica com feitio de S em vez de gancho. */
    private static final float BEND_BACK = 0.55f;

    /** De que lado vem a luz que dá o redondo a ela, e o quanto o lado escuro escurece. */
    private static final Vector3f LIGHT = new Vector3f(0.35f, 0.82f, -0.45f).normalize();
    private static final float DARK = 0.35f;

    /**
     * A gavinha e as estrelas dela.
     *
     * <p>O {@code shade} traz um número por canto, de {@link #DARK} a um: é a luz que apanha aquele lado do
     * corpo. Sem ele a gavinha lê-se como uma fita chata, porque o desenho dela não tem folha nem normais.
     */
    public record Tendril(float[] points, float[] shade, float[] stars, float length, float girth) {
        public int triangles() {
            return this.points.length / 9;
        }
    }

    private static final Map<Integer, Tendril> FEITAS = new ConcurrentHashMap<>();

    private RiftTendril() {
    }

    /** A gavinha de número n. */
    public static Tendril get(int qual) {
        return FEITAS.computeIfAbsent(qual, RiftTendril::build);
    }

    /** Esquece as que montou — serve aos testes. */
    public static void forget() {
        FEITAS.clear();
    }

    private static Tendril build(int semente) {
        RandomSource sorte = RandomSource.create(semente * 341873128712L + 132897987541L);

        // a espinha: sobe, virando devagar e sempre para o mesmo lado, que é o que lhe dá o enrolar
        Vector3f[] meio = new Vector3f[RINGS + 1];
        float[] grossura = new float[RINGS + 1];
        Vector3f[] frenteDe = new Vector3f[RINGS + 1];

        Vector3f onde = new Vector3f(0.0f, 0.0f, 0.0f);
        Vector3f frente = new Vector3f(0.0f, 1.0f, 0.0f);
        // para que lado ela se vira, e o quanto — de cada fenda é o seu
        float viraX = (sorte.nextFloat() - 0.5f) * 2.0f * TURN;
        float viraZ = (sorte.nextFloat() - 0.5f) * 2.0f * TURN;
        float passo = LENGTH / RINGS;

        for (int i = 0; i <= RINGS; i++) {
            float t = i / (float) RINGS;
            meio[i] = new Vector3f(onde);
            frenteDe[i] = new Vector3f(frente);
            // gorda em baixo, a afinar até sumir: a raiz dá-lhe a barriga e a potência a ponta fina
            grossura[i] = GIRTH * (float) (Math.sqrt(Math.max(0.0, 1.0 - t)) * (0.35 + 0.65 * Math.pow(1.0 - t, 0.6)));

            if (i == RINGS) break;
            onde = new Vector3f(onde).add(new Vector3f(frente).mul(passo));
            // a meio do caminho ela dobra o virar para o outro lado: é o que lhe dá o feitio de S e não de gancho
            float lado = t < BEND_BACK ? 1.0f : -0.9f;
            frente.add(viraX * lado * (0.4f + t), 0.0f, viraZ * lado * (0.4f + t)).normalize();
            viraX += (sorte.nextFloat() - 0.5f) * TURN_DRIFT;
            viraZ += (sorte.nextFloat() - 0.5f) * TURN_DRIFT;
        }

        float[] pontos = new float[RINGS * SIDES * 2 * 9];
        float[] luz = new float[RINGS * SIDES * 2 * 3];
        int escreve = 0;
        int pinta = 0;
        Vector3f[] anelAntes = ring(meio[0], frenteDe[0], grossura[0]);
        float[] luzAntes = shade(anelAntes, meio[0]);
        for (int i = 1; i <= RINGS; i++) {
            Vector3f[] anel = ring(meio[i], frenteDe[i], grossura[i]);
            float[] luzDele = shade(anel, meio[i]);
            for (int lado = 0; lado < SIDES; lado++) {
                int outro = (lado + 1) % SIDES;
                escreve = tri(pontos, escreve, anelAntes[lado], anelAntes[outro], anel[outro]);
                luz[pinta++] = luzAntes[lado];
                luz[pinta++] = luzAntes[outro];
                luz[pinta++] = luzDele[outro];
                escreve = tri(pontos, escreve, anelAntes[lado], anel[outro], anel[lado]);
                luz[pinta++] = luzAntes[lado];
                luz[pinta++] = luzDele[outro];
                luz[pinta++] = luzDele[lado];
            }
            anelAntes = anel;
            luzAntes = luzDele;
        }

        // as estrelas: presas na pele dela, um pouco para fora, para se verem contra o preto
        float[] estrelas = new float[STARS * 5];
        for (int i = 0; i < STARS; i++) {
            int anel = sorte.nextInt(RINGS + 1);
            float volta = sorte.nextFloat() * (float) (Math.PI * 2.0);
            Vector3f[] roda = ring(meio[anel], frenteDe[anel], grossura[anel] * 1.04f + 0.12f);
            Vector3f ponto = pick(roda, volta);
            estrelas[i * 5] = ponto.x;
            estrelas[i * 5 + 1] = ponto.y;
            estrelas[i * 5 + 2] = ponto.z;
            estrelas[i * 5 + 3] = sorte.nextFloat() * (float) (Math.PI * 2.0);
            estrelas[i * 5 + 4] = 0.14f + sorte.nextFloat() * 0.22f;
        }

        return new Tendril(pontos, luz, estrelas, LENGTH, GIRTH);
    }

    /** Um anel de {@link #SIDES} cantos à volta daquele ponto da espinha, virado para onde ela vai. */
    private static Vector3f[] ring(Vector3f meio, Vector3f frente, float raio) {
        // dois lados quaisquer perpendiculares ao caminho — qualquer um serve, desde que seja sempre o mesmo
        Vector3f cima = Math.abs(frente.y) > 0.9f ? new Vector3f(1.0f, 0.0f, 0.0f) : new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f direita = new Vector3f(frente).cross(cima).normalize();
        Vector3f outro = new Vector3f(direita).cross(frente).normalize();

        Vector3f[] anel = new Vector3f[SIDES];
        for (int i = 0; i < SIDES; i++) {
            double volta = i * Math.PI * 2.0 / SIDES;
            anel[i] = new Vector3f(meio)
                    .add(new Vector3f(direita).mul((float) Math.cos(volta) * raio))
                    .add(new Vector3f(outro).mul((float) Math.sin(volta) * raio));
        }
        return anel;
    }

    /** A luz que cada canto de um anel apanha: quem olha para a luz fica claro, quem lhe dá as costas fica escuro. */
    private static float[] shade(Vector3f[] anel, Vector3f meio) {
        float[] saída = new float[anel.length];
        for (int i = 0; i < anel.length; i++) {
            Vector3f fora = new Vector3f(anel[i]).sub(meio);
            if (fora.lengthSquared() < 1.0e-8f) {
                saída[i] = 1.0f;
                continue;
            }
            float quanto = fora.normalize().dot(LIGHT);
            saída[i] = DARK + (1.0f - DARK) * Math.max(0.0f, quanto);
        }
        return saída;
    }

    /** Um ponto qualquer daquele anel, pela volta pedida. */
    private static Vector3f pick(Vector3f[] anel, float volta) {
        float onde = volta / (float) (Math.PI * 2.0) * anel.length;
        int qual = (int) onde;
        return anel[Math.floorMod(qual, anel.length)];
    }

    private static int tri(float[] saída, int escreve, Vector3f a, Vector3f b, Vector3f c) {
        for (Vector3f canto : new Vector3f[]{a, b, c}) {
            saída[escreve++] = canto.x;
            saída[escreve++] = canto.y;
            saída[escreve++] = canto.z;
        }
        return escreve;
    }
}
