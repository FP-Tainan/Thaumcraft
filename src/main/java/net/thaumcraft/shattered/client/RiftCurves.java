package net.thaumcraft.shattered.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * As catorze formas do rasgão da fenda: o {@code LSystem} das Portas Dimensionais.
 *
 * <p>Lá o mod monta os sistemas-L ao arrancar — quatro rabiscos de dragão em várias gerações —, anda cada curva
 * num papel quadriculado, tira o contorno dela e recorta-o com uma biblioteca de Delaunay. Aqui as curvas vêm
 * prontas num arquivo de dados, feito por {@code scratchpad/dd-curvas.js} com o mesmo sistema-L e os mesmos
 * catorze pares de rabisco e geração, na mesma ordem — a ordem importa, porque cada fenda escolhe a sua pelo
 * número.
 *
 * <p><b>Uma diferença de dentro, que não se vê:</b> o original recorta o contorno; aqui pintam-se as casas que o
 * rabisco ocupa, juntas em tiras deitadas. A silhueta é a mesma — o rabisco é uma união de quadradinhos e vai
 * todo da mesma cor —, e fica sem os buracos que o recorte deixa onde o contorno toca em si mesmo.
 */
public final class RiftCurves {
    private static final Identifier ONDE = Thaumcraft.id("models/rift_curves.mesh");

    /** Uma forma: a caixa dela e os triângulos, três cantos de dois números cada. */
    public record Curve(float minX, float maxX, float minY, float maxY, float[] points) {
        /** Quantos triângulos ela tem. */
        public int triangles() {
            return this.points.length / 6;
        }

        public float width() {
            return this.maxX - this.minX;
        }
    }

    private static List<Curve> curvas;

    private RiftCurves() {
    }

    public static List<Curve> all() {
        if (curvas == null) curvas = read();
        return curvas;
    }

    /** A forma de número n, dando a volta se o número passar da conta. */
    public static Curve get(int qual) {
        List<Curve> todas = all();
        if (todas.isEmpty()) return null;
        return todas.get(Math.floorMod(qual, todas.size()));
    }

    /** Esquece o que leu, para a próxima leitura pegar o que o pacote de recursos tiver posto. */
    public static void forget() {
        curvas = null;
    }

    private static List<Curve> read() {
        var recursos = Minecraft.getInstance().getResourceManager();
        try (InputStream entrada = recursos.open(ONDE);
             DataInputStream dados = new DataInputStream(entrada)) {
            int quantas = dados.readInt();
            List<Curve> saída = new java.util.ArrayList<>(quantas);
            for (int i = 0; i < quantas; i++) {
                float minX = dados.readFloat(), maxX = dados.readFloat();
                float minY = dados.readFloat(), maxY = dados.readFloat();
                int triângulos = dados.readInt();
                float[] pontos = new float[triângulos * 6];
                for (int k = 0; k < pontos.length; k++) pontos[k] = dados.readFloat();
                saída.add(new Curve(minX, maxX, minY, maxY, pontos));
            }
            return List.copyOf(saída);
        } catch (IOException erro) {
            Thaumcraft.LOGGER.error("não consegui ler as formas do rasgão em {}", ONDE, erro);
            return List.of();
        }
    }
}
