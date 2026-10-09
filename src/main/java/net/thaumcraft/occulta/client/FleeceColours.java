package net.thaumcraft.occulta.client;

import net.minecraft.util.ARGB;

/**
 * As dezesseis cores do {@code fleeceColorTable} de 2014.
 *
 * <p>Elas <b>não</b> são as do jogo de hoje. A lã da 1.7.10 tinha a sua tabela, e todo o Witchery pinta
 * com ela: a vassoura, o espantalho e o ídolo. Copiá-la é o que faz uma coisa pintada de azul no original
 * ficar do mesmo azul aqui — e as duas tabelas são bem diferentes no ciano, no castanho e no cinza.
 *
 * <p>A ordem é a do dado da lã, que é também a do {@code DyeColor} de hoje: branco no zero, preto no
 * quinze. Por isso o número de um corante serve de índice sem conta nenhuma pelo meio.
 */
public final class FleeceColours {
    /** As dezesseis, uma a uma. */
    public static final int[] ALL = {
            cor(1.0f, 1.0f, 1.0f), cor(0.85f, 0.5f, 0.2f), cor(0.7f, 0.3f, 0.85f), cor(0.4f, 0.6f, 0.85f),
            cor(0.9f, 0.9f, 0.2f), cor(0.5f, 0.8f, 0.1f), cor(0.95f, 0.5f, 0.65f), cor(0.3f, 0.3f, 0.3f),
            cor(0.6f, 0.6f, 0.6f), cor(0.3f, 0.5f, 0.6f), cor(0.5f, 0.25f, 0.7f), cor(0.2f, 0.3f, 0.7f),
            cor(0.4f, 0.3f, 0.2f), cor(0.4f, 0.5f, 0.2f), cor(0.6f, 0.2f, 0.2f), cor(0.1f, 0.1f, 0.1f),
    };

    private FleeceColours() {
    }

    private static int cor(float r, float g, float b) {
        return ARGB.colorFromFloat(1.0f, r, g, b);
    }

    /** A cor de um índice, presa entre zero e quinze. */
    public static int of(int qual) {
        return ALL[Math.clamp(qual, 0, ALL.length - 1)];
    }
}
