package net.thaumcraft.api.aspects;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * O que sabe passar essência adiante: tubo, jarro, alambique, forno.
 *
 * <p>É o {@code IEssentiaTransport} do Thaumcraft 4.2.3.5, com os mesmos nomes e a mesma ideia. A essência
 * não é empurrada: ela é <em>puxada</em>. Cada peça anuncia uma <strong>sucção</strong> — um aspecto que
 * quer e uma força com que quer — e a essência corre de onde a sucção é fraca para onde ela é forte, uma
 * unidade de cada vez. É por isso que um jarro no fim da linha basta para toda a tubulação andar: é a
 * fome dele que puxa tudo.
 */
public interface EssentiaTransport {
    /** Este lado aceita encanamento? */
    boolean isConnectable(Direction face);

    /** Por este lado entra essência? */
    boolean canInputFrom(Direction face);

    /** Por este lado sai essência? */
    boolean canOutputTo(Direction face);

    /** Anuncia o que esta peça está puxando e com que força. */
    void setSuction(@Nullable Aspect aspect, int amount);

    /** O aspecto que esta peça puxa por este lado, ou nulo se ela puxa qualquer um. */
    @Nullable
    Aspect getSuctionType(@Nullable Direction face);

    /** A força com que esta peça puxa por este lado. */
    int getSuctionAmount(@Nullable Direction face);

    /** Tira essência daqui. Devolve quanto saiu de fato. */
    int takeEssentia(Aspect aspect, int amount, Direction face);

    /** Põe essência aqui. Devolve quanto entrou de fato. */
    int addEssentia(Aspect aspect, int amount, Direction face);

    /** O aspecto que está guardado aqui, se houver. */
    @Nullable
    Aspect getEssentiaType(@Nullable Direction face);

    /** Quanto está guardado aqui. */
    int getEssentiaAmount(@Nullable Direction face);

    /** Abaixo desta sucção ninguém consegue tirar nada daqui. */
    int getMinimumSuction();

    /** Se o encanamento desenha um bico até esta peça, em vez de parar na beirada. */
    boolean renderExtendedTube();
}
