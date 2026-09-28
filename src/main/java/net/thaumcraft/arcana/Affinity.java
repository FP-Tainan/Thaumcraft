package net.thaumcraft.arcana;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A Afinidade: o {@code am2.api.spell.enums.Affinity} do Ars Magica 2.
 *
 * <p>É a ideia mais bonita do ramo, e a que mais o separa de tudo o que já está portado: <b>lançar feitiços
 * muda quem os lança</b>. Quem só atira fogo não vira um mago melhor — vira um mago <i>de fogo</i>, e vai
 * perdendo o gelo pelo caminho. A Afinidade não se escolhe em lugar nenhum: ela é o registro do que a pessoa
 * fez.
 *
 * <p>São <b>dez</b>, mais a Afinidade nenhuma. Cada uma tem quatro relações com as outras, e é a soma delas
 * que faz a roda girar:
 *
 * <ul>
 *   <li>a <b>oposta direta</b>, que perde tanto quanto esta ganha;
 *   <li>as <b>opostas maiores</b>, quatro delas, que perdem três quartos;
 *   <li>as <b>opostas menores</b>, duas, que perdem metade;
 *   <li>e as <b>vizinhas</b>, duas, que perdem um quarto — porque mesmo o que é parecido se afasta.
 * </ul>
 *
 * <p><b>A conta não fecha em zero, e é de propósito.</b> Quem soma <b>um</b> numa Afinidade tira
 * 1 + 4×0,75 + 2×0,5 + 2×0,25 = <b>5,5</b> das outras — saldo de <b>menos 4,5</b> por ponto ganho. Não é para
 * render; é para doer escolher.
 *
 * <p>E há o <b>tranco</b>: quem chega aos 100 numa delas fica preso ali para sempre. É o {@code isLocked} do
 * original, e é a única coisa deste ramo que não tem volta.
 *
 * <p><b>As relações não são simétricas, e isso é do original.</b> O Arcano tem a Vida como oposta direta, mas
 * a Vida tem o Ender; o Relâmpago tem o Gelo, e a Natureza também tem o Relâmpago, que já está tomado. Fica
 * assim: são as tabelas do jar, lidas número a número.
 */
public enum Affinity {
    /** Afinidade nenhuma: o que um feitiço tem quando as Essências dele não puxam para lado nenhum. */
    NONE(0xFFFFFF, -1, new int[0], new int[0], new int[0]),
    /** <b>Arcano</b>: a magia por si mesma. */
    ARCANE(0xB935CD, 9, new int[]{9, 4, 2, 7}, new int[]{5, 10}, new int[]{6, 3}),
    /** <b>Água</b>. */
    WATER(0x0B5CEF, 3, new int[]{7, 4, 1, 10}, new int[]{5, 7}, new int[]{9, 8}),
    /** <b>Fogo</b>. */
    FIRE(0xEF260B, 2, new int[]{5, 7, 8, 9}, new int[]{4, 6}, new int[]{10, 1}),
    /** <b>Terra</b>. */
    EARTH(0x61330B, 5, new int[]{2, 1, 9, 6}, new int[]{8, 3}, new int[]{7, 10}),
    /** <b>Ar</b>. */
    AIR(0x777777, 4, new int[]{8, 3, 7, 10}, new int[]{2, 1}, new int[]{9, 6}),
    /** <b>Relâmpago</b>. */
    LIGHTNING(0xDECE19, 7, new int[]{2, 10, 8, 4}, new int[]{9, 3}, new int[]{5, 1}),
    /** <b>Gelo</b>. */
    ICE(0xD3E8FC, 6, new int[]{9, 3, 5, 1}, new int[]{2, 10}, new int[]{8, 4}),
    /** <b>Natureza</b>. */
    NATURE(0x228718, 6, new int[]{5, 10, 6, 3}, new int[]{9, 4}, new int[]{2, 7}),
    /** <b>Vida</b>. */
    LIFE(0x34E122, 10, new int[]{1, 7, 3, 4}, new int[]{8, 6}, new int[]{2, 5}),
    /** <b>Ender</b>. */
    ENDER(0x3F043D, 9, new int[]{8, 6, 2, 5}, new int[]{1, 7}, new int[]{4, 3});

    /** O fundo do poço: os 100 do original, que é onde a Afinidade tranca. */
    public static final float MAX_DEPTH = 100.0f;

    /** O que as vizinhas perdem quando uma cresce: um quarto. */
    public static final float ADJACENT_FACTOR = 0.25f;
    /** O que as opostas menores perdem: metade. */
    public static final float MINOR_FACTOR = 0.5f;
    /** E o que as maiores perdem: três quartos. */
    public static final float MAJOR_FACTOR = 0.75f;

    /** A cor dela, para figuras e partículas: a do original, tal e qual. */
    public final int color;

    private final int opposite;
    private final int[] major;
    private final int[] minor;
    private final int[] adjacent;

    Affinity(int color, int opposite, int[] major, int[] minor, int[] adjacent) {
        this.color = color;
        this.opposite = opposite;
        this.major = major;
        this.minor = minor;
        this.adjacent = adjacent;
    }

    /** A oposta direta, que perde tanto quanto esta ganha. */
    public Affinity opposite() {
        return this.opposite < 0 ? NONE : values()[this.opposite];
    }

    /** As quatro opostas maiores, que perdem três quartos. */
    public List<Affinity> major() {
        return lista(this.major);
    }

    /** As duas opostas menores, que perdem metade. */
    public List<Affinity> minor() {
        return lista(this.minor);
    }

    /** E as duas vizinhas, que perdem um quarto. */
    public List<Affinity> adjacent() {
        return lista(this.adjacent);
    }

    private static List<Affinity> lista(int[] quais) {
        var saco = new ArrayList<Affinity>(quais.length);
        for (int i : quais) saco.add(values()[i]);
        return List.copyOf(saco);
    }

    /** A chave de texto dela, para o livro e para as dicas. */
    public String key() {
        return "tc.spell.affinity." + this.name().toLowerCase(Locale.ROOT);
    }
}
