package net.thaumcraft.occulta;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Os três anéis de giz em volta do glifo do meio: o desenho que o {@code activateBlock} do Witchery percorre.
 *
 * <p>São dezessete por dezessete blocos, e nele cabem três anéis: o <b>de dentro</b>, de dezesseis glifos; o
 * <b>do meio</b>, de vinte e oito; e o <b>de fora</b>, de quarenta. Cada um deles pode ser riscado com qualquer
 * dos três gizes, e o que o ritual pede é <b>quantos de cada giz</b> há em cada anel.
 *
 * <p>O desenho é o do original, letra por letra — não é um círculo por conta de distância, é este risco.
 */
public final class RitualCircles {
    /** Quantos glifos cada anel tem, quando está inteiro. */
    public static final int INNER = 16, MIDDLE = 28, OUTER = 40;

    /** O desenho do original: {@code a} é o anel de dentro, {@code b} o do meio, {@code c} o de fora. */
    private static final String[] PATTERN = {
            ".................",
            ".....ccccccc.....",
            "....c.......c....",
            "...c..bbbbb..c...",
            "..c..b.....b..c..",
            ".c..b..aaa..b..c.",
            ".c.b..a...a..b.c.",
            ".c.b.a.....a.b.c.",
            ".c.b.a.....a.b.c.",
            ".c.b.a.....a.b.c.",
            ".c.b..a...a..b.c.",
            ".c..b..aaa..b..c.",
            "..c..b.....b..c..",
            "...c..bbbbb..c...",
            "....c.......c....",
            ".....ccccccc.....",
            ".................",
    };

    /**
     * O que se achou num anel: quantos glifos de cada giz.
     *
     * @param ritual     os do giz de Ritual
     * @param otherwhere os do giz do Alhures
     * @param infernal   os do giz Infernal
     * @param total      quantos glifos o anel precisa ter para estar inteiro
     */
    public record Ring(int ritual, int otherwhere, int infernal, int total) {
        /** Se o anel está inteiro — todos os glifos dele riscados, de qualquer giz. */
        public boolean complete() {
            return this.ritual + this.otherwhere + this.infernal >= this.total;
        }

        /** Se o anel é todo de um giz só. */
        public boolean allOf(int quantos, int qual) {
            return switch (qual) {
                case 0 -> this.ritual >= quantos;
                case 1 -> this.otherwhere >= quantos;
                default -> this.infernal >= quantos;
            };
        }
    }

    /** Os três anéis em volta daquele ponto, contados. */
    public record Circles(Ring inner, Ring middle, Ring outer) {
    }

    /** O lado do desenho, em blocos. */
    public static int side() {
        return PATTERN.length;
    }

    /**
     * Uma linha do desenho, contada <b>do sul para o norte</b> — que é a ordem em que o original o lê.
     *
     * <p>Serve a quem precisa de andar o desenho sem ser para o contar: o <b>Talismã de Círculo</b>, que
     * o risca em vez de o ler.
     */
    public static String linha(int z) {
        return PATTERN[PATTERN.length - 1 - z];
    }

    private RitualCircles() {
    }

    /** Conta os três anéis em volta do glifo do meio. */
    public static Circles read(BlockGetter level, BlockPos meio) {
        int[] dentro = new int[3];
        int[] noMeio = new int[3];
        int[] fora = new int[3];
        int raio = (PATTERN.length - 1) / 2;

        for (int z = 0; z < PATTERN.length; z++) {
            String linha = PATTERN[PATTERN.length - 1 - z];
            for (int x = 0; x < linha.length(); x++) {
                char qual = linha.charAt(x);
                if (qual == '.') continue;
                BlockPos onde = meio.offset(x - raio, 0, z - raio);
                int giz = chalkOf(level.getBlockState(onde));
                if (giz < 0) continue;
                switch (qual) {
                    case 'a' -> dentro[giz]++;
                    case 'b' -> noMeio[giz]++;
                    default -> fora[giz]++;
                }
            }
        }
        return new Circles(new Ring(dentro[0], dentro[1], dentro[2], INNER),
                new Ring(noMeio[0], noMeio[1], noMeio[2], MIDDLE),
                new Ring(fora[0], fora[1], fora[2], OUTER));
    }

    /** Qual giz riscou aquilo: zero ritual, um alhures, dois infernal — ou menos um, se não é giz. */
    public static int chalkOf(BlockState state) {
        if (state.is(OccultaBlocks.RITUAL_GLYPH)) return 0;
        if (state.is(OccultaBlocks.OTHERWHERE_GLYPH)) return 1;
        if (state.is(OccultaBlocks.INFERNAL_GLYPH)) return 2;
        return -1;
    }
}
