package net.thaumcraft.api.aspects;

import org.jetbrains.annotations.Nullable;

/**
 * O que guarda aspecto dentro: o jarro, o alambique, o forno alquímico, o crisol.
 *
 * <p>É o {@code IAspectContainer} do Thaumcraft 4.2.3.5. Fica separado do {@link EssentiaTransport} de
 * propósito, como no original: guardar é uma coisa, passar adiante é outra — há quem só guarde, e há
 * quem só passe.
 */
public interface AspectContainer {
    /** Tudo o que está guardado aqui. */
    AspectList getAspects();

    /** Este recipiente aceita esse aspecto? Um jarro com rótulo só aceita o do rótulo. */
    boolean doesContainerAccept(Aspect aspect);

    /**
     * Põe aspecto dentro.
     *
     * @return o que <em>não</em> coube, como no original — zero quer dizer que entrou tudo
     */
    int addToContainer(Aspect aspect, int amount);

    /** Tira aspecto de dentro. Devolve se deu para tirar tudo o que se pediu. */
    boolean takeFromContainer(Aspect aspect, int amount);

    /** Tem pelo menos essa quantidade desse aspecto aqui dentro? */
    boolean doesContainerContainAmount(Aspect aspect, int amount);

    /** Quanto deste aspecto está guardado aqui. */
    int containerContains(@Nullable Aspect aspect);
}
