package net.thaumcraft.crimson;

/**
 * O chão do Altar Antigo: as duzentas e tantas casas que o {@code CrimsonWorldGenerator} escreve em volta
 * dele — um disco de pedra arcana com a orla de tijolo arcano.
 *
 * <p><b>Arquivo gerado</b> por {@code scratchpad/cw-estrutura.js} a partir do jar original — não se escreve à
 * mão. Cada linha do desenho é uma fila em Z, e cada letra uma casa em X: o ponto é pedra e o cardinal é
 * tijolo; o espaço fica como estava.
 */
public final class AncientAltarFloor {
    /** A casa de menor X e de menor Z do desenho, em relação ao altar. */
    public static final int MIN_X = -6;
    public static final int MIN_Z = -6;

    /** O desenho, fila a fila. */
    public static final String[] PLAN = {
            "     ###     ",
            "   ##...##   ",
            "  #.......#  ",
            " #.........# ",
            " #.........# ",
            "#...........#",
            "#.....#.....#",
            "#...........#",
            " #.........# ",
            " #.........# ",
            "  #.......#  ",
            "   ##...##   ",
            "     ###     ",
    };

    private AncientAltarFloor() {
    }

    /** Quantas casas o desenho enche. */
    public static int size() {
        int conta = 0;
        for (String fila : PLAN) {
            for (int i = 0; i < fila.length(); i++) if (fila.charAt(i) != ' ') conta++;
        }
        return conta;
    }
}
