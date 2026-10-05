package net.thaumcraft.occulta.divine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A profecia que um jogador <b>carrega</b>: o que o original guarda na lista {@code WITCPreList} dele.
 *
 * <p>Duas coisas: <b>qual</b> é e <b>quando</b> foi dita. O resto — o prazo, o peso, o que ela faz — está na
 * {@link Prediction} que tem aquele número, e não no jogador.
 *
 * <p>No original isto é uma <b>lista</b>, mas quem a enche nunca põe mais do que uma: o gerente recusa dizer
 * a sorte a quem já tem uma por dizer. Aqui é <b>uma só</b>, porque é o que o original permite na prática, e
 * uma lista de no máximo um elemento é uma lista que mente sobre o que é.
 */
public record Prophecy(int id, long quando) {
    public static final Codec<Prophecy> CODEC = RecordCodecBuilder.create(campo -> campo.group(
            Codec.INT.fieldOf("id").forGetter(Prophecy::id),
            Codec.LONG.fieldOf("quando").forGetter(Prophecy::quando)
    ).apply(campo, Prophecy::new));
}
