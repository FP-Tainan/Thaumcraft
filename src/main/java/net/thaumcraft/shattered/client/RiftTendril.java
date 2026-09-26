package net.thaumcraft.shattered.client;

import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * O feitio do rasgão de uma fenda: uma gavinha comprida que sobe do nada e se enrola, grossa embaixo e afinando
 * até a ponta se perder, toda de um preto de céu sem lua com estrelas presas dentro — e, conforme a fenda vai
 * crescendo, <b>brotando galhos</b>, como raiz de planta.
 *
 * <p><b>Isto é do porte, e não do original.</b> Lá o rasgão é um rabisco de dragão — um sistema-L andado num papel
 * quadriculado, que dá uma mancha chata e quadrada. Quem manda mostrou o que queria numa foto e é isto: um corpo
 * de três dimensões, redondo, que se vê de qualquer lado. E depois pediu mais: <i>que elas fossem se ramificando
 * tipo uma raiz de planta</i> em vez de só engordarem.
 *
 * <p>Então a gavinha não é uma só: é uma lista de <b>braços</b>. O primeiro nasce com a fenda; cada um dos outros
 * brota de um ponto de um braço mais velho, e só aparece depois que a fenda passa de um tamanho. Quem desenha
 * escolhe quantos mostrar pelo tamanho dela — é isso que faz a fenda parecer que está se abrindo, e não inchando.
 *
 * <p>Cada fenda tem a sua: o número que ela sorteou quando nasceu é a semente, e dele saem o virar de cada braço,
 * a grossura, de onde brota cada galho e onde cada estrela fica presa. Duas fendas do mesmo número são iguais.
 */
public final class RiftTendril {
    /** De ponta a ponta, e a grossura no ponto mais gordo. */
    private static final float LENGTH = 64.0f;
    private static final float GIRTH = 3.6f;

    /** Em quantos anéis o braço maior se divide, e de quantos lados é cada anel. */
    private static final int RINGS = 48;
    private static final int SIDES = 8;

    /** O quanto ele vira de um anel para o outro, e o quanto esse virar vai se acumulando. */
    private static final float TURN = 0.095f;
    private static final float TURN_DRIFT = 0.028f;

    /** Quantas estrelas ficam presas no braço maior; os galhos levam menos, na conta do tamanho deles. */
    private static final int STARS = 120;

    /** Onde ele dobra o virar para o outro lado, e fica com feitio de S em vez de gancho. */
    private static final float BEND_BACK = 0.55f;

    /** De que lado vem a luz que dá o redondo, e o quanto o lado escuro escurece. */
    private static final Vector3f LIGHT = new Vector3f(0.35f, 0.82f, -0.45f).normalize();
    private static final float DARK = 0.35f;

    /** Quantos galhos uma fenda chega a ter, e a partir de que tamanho brota o primeiro. */
    private static final int BRANCHES = 7;
    public static final float FIRST_BRANCH = 55.0f;
    public static final float BRANCH_STEP = 62.0f;

    /** O quanto um galho é menor que o braço de onde brotou, e onde ao longo dele pode brotar. */
    private static final float BRANCH_LENGTH = 0.38f;
    private static final float BRANCH_GIRTH = 0.55f;
    private static final float BRANCH_FROM = 0.15f;
    private static final float BRANCH_TO = 0.80f;
    /** E o quanto ele se abre para o lado de onde brotou. */
    private static final float BRANCH_ANGLE = 1.45f;

    /**
     * Um braço da fenda.
     *
     * <p>O {@code shade} traz um número por canto, de {@link #DARK} a um: é a luz que aquele lado do corpo pega.
     * Sem ele o braço se lia como uma fita chata, porque o desenho dele não tem folha nem normais.
     *
     * <p>O {@code bornAt} é o tamanho de fenda a partir do qual este braço aparece. O primeiro é zero.
     */
    public record Limb(float[] points, float[] shade, float[] stars, float bornAt) {
        public int triangles() {
            return this.points.length / 9;
        }
    }

    /** A gavinha inteira: os braços dela, do mais velho para o mais novo. */
    public record Tendril(List<Limb> limbs, float length, float girth) {
        /** Quantos braços aparecem numa fenda deste tamanho — sempre pelo menos o primeiro. */
        public int visible(float tamanho) {
            int quantos = 1;
            for (int i = 1; i < this.limbs.size(); i++) {
                if (this.limbs.get(i).bornAt() <= tamanho) quantos++;
            }
            return quantos;
        }
    }

    /**
     * Uma espinha montada: por onde ela passou, para onde ia e quão grossa era em cada ponto — e de que tamanho
     * ela é, que é do que os galhos dela saem.
     */
    private record Spine(Vector3f[] middle, Vector3f[] forward, float[] girth, float length, float thickness) {
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

        List<Spine> espinhas = new ArrayList<>();
        List<Limb> braços = new ArrayList<>();

        Spine maior = spine(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f),
                LENGTH, GIRTH, RINGS, sorte);
        espinhas.add(maior);
        braços.add(limb(maior, STARS, 0.0f, sorte));

        for (int g = 0; g < BRANCHES; g++) {
            // cada galho brota de um braço que já existe. Metade das vezes é do braço maior: sem esse puxão eles
            // iam todos brotar uns dos outros e se amontoavam na ponta, em vez de se espalharem pelo corpo
            Spine pai = sorte.nextBoolean() ? maior : espinhas.get(sorte.nextInt(espinhas.size()));
            int onde = (int) ((BRANCH_FROM + sorte.nextFloat() * (BRANCH_TO - BRANCH_FROM))
                    * (pai.middle().length - 1));

            // o tamanho sai do braço de onde ele brotou, e não do último que se montou: senão os galhos iam
            // encolhendo uns em cima dos outros e do quinto em diante nem se viam
            float comprido = pai.length() * (BRANCH_LENGTH + sorte.nextFloat() * 0.25f);
            float grosso = pai.thickness() * (BRANCH_GIRTH + sorte.nextFloat() * 0.2f);
            comprido = Math.max(LENGTH * 0.18f, comprido);
            grosso = Math.max(GIRTH * 0.14f, grosso);

            // ele sai de lado, e não na direção em que o pai ia: é o que faz parecer galho e não emenda. O lado
            // tem de ser de través ao pai — se sobrar nele um tanto da direção do pai, o galho corre grudado no
            // corpo dele e a fenda vira um borrão em vez de uma raiz
            Vector3f rumo = new Vector3f(pai.forward()[onde]);
            Vector3f lado = new Vector3f(sorte.nextFloat() - 0.5f, sorte.nextFloat() - 0.5f,
                    sorte.nextFloat() - 0.5f);
            lado.sub(new Vector3f(rumo).mul(lado.dot(rumo)));
            if (lado.lengthSquared() < 1.0e-6f) {
                lado.set(Math.abs(rumo.y) > 0.9f ? 1.0f : 0.0f, Math.abs(rumo.y) > 0.9f ? 0.0f : 1.0f, 0.0f);
            }
            rumo.add(lado.normalize().mul(BRANCH_ANGLE)).normalize();

            int anéis = Math.max(8, Math.round(RINGS * comprido / LENGTH));
            Spine galho = spine(new Vector3f(pai.middle()[onde]), rumo, comprido, grosso, anéis, sorte);
            espinhas.add(galho);
            braços.add(limb(galho, Math.max(8, Math.round(STARS * comprido / LENGTH)),
                    FIRST_BRANCH + g * BRANCH_STEP, sorte));
        }

        return new Tendril(List.copyOf(braços), LENGTH, GIRTH);
    }

    /** Monta uma espinha: sobe daquele ponto naquele rumo, virando devagar, afinando até sumir. */
    private static Spine spine(Vector3f de, Vector3f rumo, float comprido, float grosso, int anéis,
                               RandomSource sorte) {
        Vector3f[] meio = new Vector3f[anéis + 1];
        Vector3f[] frenteDe = new Vector3f[anéis + 1];
        float[] grossura = new float[anéis + 1];

        Vector3f onde = new Vector3f(de);
        Vector3f frente = new Vector3f(rumo).normalize();
        float viraX = (sorte.nextFloat() - 0.5f) * 2.0f * TURN;
        float viraZ = (sorte.nextFloat() - 0.5f) * 2.0f * TURN;
        float passo = comprido / anéis;

        for (int i = 0; i <= anéis; i++) {
            float t = i / (float) anéis;
            meio[i] = new Vector3f(onde);
            frenteDe[i] = new Vector3f(frente);
            // gorda embaixo, afinando até sumir: a raiz dá a barriga e a potência dá a ponta fina
            grossura[i] = grosso
                    * (float) (Math.sqrt(Math.max(0.0, 1.0 - t)) * (0.35 + 0.65 * Math.pow(1.0 - t, 0.6)));

            if (i == anéis) break;
            onde = new Vector3f(onde).add(new Vector3f(frente).mul(passo));
            // no meio do caminho ela dobra o virar para o outro lado: é o que dá o feitio de S e não de gancho
            float lado = t < BEND_BACK ? 1.0f : -0.9f;
            frente.add(viraX * lado * (0.4f + t), 0.0f, viraZ * lado * (0.4f + t)).normalize();
            viraX += (sorte.nextFloat() - 0.5f) * TURN_DRIFT;
            viraZ += (sorte.nextFloat() - 0.5f) * TURN_DRIFT;
        }
        return new Spine(meio, frenteDe, grossura, comprido, grosso);
    }

    /** E veste a espinha de anéis, com a luz de cada canto e as estrelas presas na pele. */
    private static Limb limb(Spine espinha, int estrelas, float nasceEm, RandomSource sorte) {
        int anéis = espinha.middle().length - 1;
        float[] pontos = new float[anéis * SIDES * 2 * 9];
        float[] luz = new float[anéis * SIDES * 2 * 3];
        int escreve = 0;
        int pinta = 0;

        Vector3f[] anelAntes = ring(espinha.middle()[0], espinha.forward()[0], espinha.girth()[0]);
        float[] luzAntes = shade(anelAntes, espinha.middle()[0]);
        for (int i = 1; i <= anéis; i++) {
            Vector3f[] anel = ring(espinha.middle()[i], espinha.forward()[i], espinha.girth()[i]);
            float[] luzDele = shade(anel, espinha.middle()[i]);
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

        float[] fagulhas = new float[estrelas * 5];
        for (int i = 0; i < estrelas; i++) {
            int anel = sorte.nextInt(anéis + 1);
            float volta = sorte.nextFloat() * (float) (Math.PI * 2.0);
            Vector3f[] roda = ring(espinha.middle()[anel], espinha.forward()[anel],
                    espinha.girth()[anel] * 1.04f + 0.12f);
            Vector3f ponto = pick(roda, volta);
            fagulhas[i * 5] = ponto.x;
            fagulhas[i * 5 + 1] = ponto.y;
            fagulhas[i * 5 + 2] = ponto.z;
            fagulhas[i * 5 + 3] = sorte.nextFloat() * (float) (Math.PI * 2.0);
            fagulhas[i * 5 + 4] = 0.14f + sorte.nextFloat() * 0.22f;
        }

        return new Limb(pontos, luz, fagulhas, nasceEm);
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

    /** A luz que cada canto de um anel pega: quem olha para a luz fica claro, quem dá as costas fica escuro. */
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
