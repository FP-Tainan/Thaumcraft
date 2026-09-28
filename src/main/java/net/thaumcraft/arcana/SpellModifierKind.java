package net.thaumcraft.arcana;

/**
 * O que um modificador mexe: o {@code SpellModifiers} do Ars Magica 2.
 *
 * <p>Cada um traz o <b>valor que vale quando ninguém mexe nele</b>, que é o que o original guarda no construtor
 * do enum. Um feitiço sem modificador nenhum não é um feitiço sem números: é um feitiço com estes.
 */
public enum SpellModifierKind {
    /** A velocidade do que voa. */
    SPEED(1.0),
    /** O quanto ele cai. */
    GRAVITY(0.0),
    /** E o quanto ele pula do chão. */
    BOUNCE(0.0),
    /** O dano, que é quatro quando ninguém mexe. */
    DAMAGE(4.0),
    /** O que se cura. */
    HEALING(1.0),
    /** A velocidade que se soma à de partida. */
    VELOCITY_ADDED(0.0),
    /** O raio do que se abre em roda. */
    RADIUS(1.0),
    /** Quanto tempo dura. */
    DURATION(1.0),
    /** Quantas vezes acontece. */
    PROCS(1.0),
    /** E até onde chega: oito casas, que é o alcance da mão estendida. */
    RANGE(8.0),
    /** Se ele pega em água e coisa que não é sólida. */
    TARGET_NONSOLID_BLOCKS(0.0),
    /** O quanto ele atravessa. */
    PIERCING(2.0),
    /** A cor, que por omissão é branca. */
    COLOR(0xFFFFFF),
    /** A força com que ele cava. */
    MINING_POWER(1.0),
    /** A sorte e o toque suave de quem cava com ele. */
    FORTUNE_LEVEL(1.0),
    SILKTOUCH_LEVEL(1.0),
    /** O quanto ele desmembra. */
    DISMEMBERING_LEVEL(1.0),
    /** E o grau do bem que ele faz. */
    BUFF_POWER(1.0),
    /** Se ele persegue quem mira. */
    HOMING(0.0);

    /** O valor de fábrica deste feitio. */
    public final double base;

    SpellModifierKind(double base) {
        this.base = base;
    }
}
