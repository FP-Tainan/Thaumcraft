package net.thaumcraft.research;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Uma casa do tabuleiro de hexágonos das notas de pesquisa: o {@code HexUtils.Hex} da 4.2.3.5, com as contas
 * do {@code HexUtils} ao lado.
 *
 * <p>As casas usam coordenadas axiais ({@code q}, {@code r}). O tabuleiro é um hexágono de raio um a quatro,
 * conforme a complexidade da pesquisa, e os aspectos que a pesquisa pede ficam espalhados no anel de fora.
 *
 * <p>A volta de pixel para casa ({@link #fromPixel}) é copiada tal e qual do original, inclusive a troca de
 * eixos do {@code CubicHex.toHex}, que devolve {@code (x, z)}: é assim que o mod acha a casa sob o cursor.
 */
public record Hex(int q, int r) {
    private static final int[][] NEIGHBOURS = {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}};

    public Hex neighbour(int direction) {
        int[] d = NEIGHBOURS[direction];
        return new Hex(this.q + d[0], this.r + d[1]);
    }

    /** O centro da casa em pixels, com o tamanho de casa dado. */
    public double pixelX(int size) {
        return size * 1.5 * this.q;
    }

    public double pixelY(int size) {
        return size * Math.sqrt(3.0) * (this.r + this.q / 2.0);
    }

    /** A chave com que o original guarda a casa: {@code "q:r"}. */
    public String key() {
        return this.q + ":" + this.r;
    }

    /** O {@code Pixel.toHex} do original. */
    public static Hex fromPixel(double x, double y, int size) {
        double qq = 2.0 / 3.0 * x / size;
        double rr = (1.0 / 3.0 * Math.sqrt(3.0) * -y - 1.0 / 3.0 * x) / size;
        // getRoundedCubicHex(qq, rr, -qq - rr).toHex()
        double xx = qq, yy = rr, zz = -qq - rr;
        int rx = (int) Math.round(xx);
        int ry = (int) Math.round(yy);
        int rz = (int) Math.round(zz);
        double dx = Math.abs(rx - xx), dy = Math.abs(ry - yy), dz = Math.abs(rz - zz);
        if (dx > dy && dx > dz) {
            rx = -ry - rz;
        } else if (dy > dz) {
            ry = -rx - rz;
        } else {
            rz = -rx - ry;
        }
        return new Hex(rx, rz);
    }

    /** O anel de casas a esta distância do meio, começando pela de baixo à esquerda. */
    public static List<Hex> ring(int radius) {
        Hex h = new Hex(0, 0);
        for (int k = 0; k < radius; k++) h = h.neighbour(4);
        List<Hex> ring = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < radius; j++) {
                ring.add(h);
                h = h.neighbour(i);
            }
        }
        return ring;
    }

    /** O {@code distributeRingRandomly}: tantas casas do anel, igualmente espaçadas, a partir de uma ao acaso. */
    public static List<Hex> distributeRingRandomly(int radius, int entries, Random random) {
        List<Hex> ring = ring(radius);
        List<Hex> results = new ArrayList<>();
        float spacing = (float) ring.size() / entries;
        // o original sorteia o começo e não usa: o sorteio fica, para a sequência do acaso ser a mesma
        random.nextInt(ring.size());
        float pos = 0.0f;
        for (int i = 0; i < entries; i++) {
            results.add(ring.get(Math.round(pos)));
            pos += spacing;
        }
        return results;
    }

    /** Todas as casas até esta distância do meio. */
    public static Map<String, Hex> generate(int radius) {
        Map<String, Hex> results = new LinkedHashMap<>();
        Hex h = new Hex(0, 0);
        results.put(h.key(), h);
        for (int k = 0; k < radius; k++) {
            h = h.neighbour(4);
            Hex hd = new Hex(h.q, h.r);
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j <= k; j++) {
                    results.put(hd.key(), hd);
                    hd = hd.neighbour(i);
                }
            }
        }
        return results;
    }
}
