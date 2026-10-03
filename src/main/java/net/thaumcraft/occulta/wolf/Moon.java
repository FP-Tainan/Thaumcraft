package net.thaumcraft.occulta.wolf;

import net.minecraft.world.level.Level;

/**
 * A lua, que é quem manda no lobisomem: o {@code CreatureUtil.isFullMoon} do Witchery.
 *
 * <p>Ela é <b>cheia na fase zero</b>, que é a conta do próprio jogo, e isso dura <b>uma noite inteira</b> —
 * do pôr ao nascer do sol. Fora dela, o lobisomem volta a ser aldeão na primeira vez que olhar o céu.
 *
 * <p>É o relógio mais lento do ofício: oito dias de jogo entre uma lua cheia e a seguinte, e nada que se faça
 * o apressa. Quem quer um lobisomem espera, ou usa um vínculo.
 */
public final class Moon {
    /** A fase em que ela está cheia: o zero do jogo. */
    public static final int CHEIA = 0;

    private Moon() {
    }

    /** Se está lua cheia neste mundo. */
    public static boolean cheia(Level level) {
        return fase(level) == CHEIA;
    }

    /** Quantas batidas tem um dia, e quantas fases tem a lua. */
    public static final long UM_DIA = 24000L;
    public static final long FASES = 8L;

    /**
     * Em que fase ela está, de zero a sete.
     *
     * <p>No jogo de 2014 isto era {@code dimensionType.moonPhase(dayTime)}. O jogo de hoje mudou o tempo de
     * lugar — há relógios, linhas do tempo e marcas —, e a lua passou a ser uma linha do tempo de
     * <b>192000 batidas</b>, que são os mesmos oito dias. A conta é a mesma, e a fase zero continua a ser a
     * cheia.
     */
    public static int fase(Level level) {
        return (int) ((level.getOverworldClockTime() / UM_DIA % FASES + FASES) % FASES);
    }

    /** E se é de noite, que é quando a lua se vê. */
    public static boolean deNoite(Level level) {
        return !level.isBrightOutside();
    }
}
