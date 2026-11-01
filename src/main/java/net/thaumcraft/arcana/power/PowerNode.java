package net.thaumcraft.arcana.power;

/**
 * Quem está ligado à rede de Etherium: o {@code IPowerNode} da 1.4.0.009.
 *
 * <p>Um nó pode ser três coisas, e as três são perguntas separadas: <b>dar</b> energia ({@link
 * #canProvidePower}), <b>passar</b> energia adiante ({@link #canRelayPower}) e <b>pedir</b> energia
 * ({@link #canRequestPower}). Uma fonte dá e não pede; um conduto passa e pede; uma máquina pede e mais
 * nada.
 *
 * <p>E cada uma dessas perguntas se faz <b>por tipo</b>: o Obelisco dá Neutro e não dá mais nada, e um
 * conduto passa os três.
 */
public interface PowerNode {
    /** Quanto deste tipo o nó aguenta guardar. */
    float capacity();

    /** Se este nó pode ser a ponta de onde a energia sai. */
    boolean canProvidePower(PowerType type);

    /** Se um caminho pode passar por este nó a caminho de outro. */
    boolean canRelayPower(PowerType type);

    /** Se ele mesmo pede energia de vinte em vinte batidas. */
    boolean canRequestPower();

    /** Se ele é uma fonte, para quem olha de fora. */
    boolean isSource();

    /** Quanto ele pede de cada vez. */
    int chargeRate();

    /** De que tipos ele se serve. */
    PowerType[] validPowerTypes();

    /**
     * Onde no bloco nascem as faíscas, por eixo (0 = X, 1 = Y, 2 = Z).
     *
     * <p>O padrão é o meio; quem tem forma torta — o conduto na parede — move o ponto.
     */
    default float particleOffset(int axis) {
        return 0.5f;
    }
}
