package net.thaumcraft.occulta.brew;

import net.minecraft.util.Mth;

/**
 * O que os temperos fazem ao efeito seguinte: o {@code ModifiersEffect} do Witchery.
 *
 * <p>Um cozimento se lê de cima para baixo. O pó de pedra luminosa que se joga antes do olho de aranha não faz
 * nada sozinho — ele <b>espera</b> pelo efeito seguinte e lhe dá mais força. Depois de cada efeito aplicado, os
 * temperos que esperavam se apagam ({@link #reset()}), e os que vierem depois valem para o efeito seguinte.
 *
 * <p>Há dois tetos no original: a força e a duração param de subir aos <b>sete</b> — a não ser que a Estrela do
 * Nether esteja no caldeirão, que é o que levanta o teto.
 */
public class BrewModifiers {
    /** Onde a força e a duração param de subir, se nada levantar o teto. */
    public static final int CEILING = 7;
    /** E o teto de força de um efeito, quando o peixe-palhaço não o desliga. */
    public static final int STRENGTH_CEILING = 10;

    public int strength;
    public int strengthPenalty;
    public int duration;
    public boolean noParticles;
    public boolean inverted;
    public boolean disableBlockTarget;
    public boolean disableEntityTarget;
    public boolean strengthCeilingDisabled;
    public boolean powerCeilingDisabled;

    /** Quem traz Máscara de Gás não apanha o que é ruim numa névoa: o {@code protectedFromNegativePotions}. */
    public boolean protectedFromBadEffects;
    public int totalStrength;
    public int totalDuration;

    /**
     * Quem fez o cozimento, quando se sabe: o {@code modifiers.caster} do original.
     *
     * <p>Quase nenhum efeito precisa dele — o que importa a um frasco de vinhas é onde ele bateu, e não quem o
     * atirou. Mas dois precisam, e por boa razão: o <b>Cozimento da Ressurreição</b> precisa dele para saber de
     * quem são os mortos que levanta, e o Grotesco para saber de quem fugir.
     *
     * <p>Fica <b>nulo</b> quando não se sabe — numa nuvem que já estava no chão, por exemplo, ou num frasco
     * que um dispensador atirou.
     */
    @org.jetbrains.annotations.Nullable
    public net.minecraft.world.entity.player.Player quemFez;

    /** O espaço do caldeirão, que os efeitos vão gastando enquanto se aplicam. */
    private final BrewCapacity espaço = new BrewCapacity();

    public BrewCapacity capacity() {
        return this.espaço;
    }

    /** O quanto o jeito de servir estica a força e a duração: beber é um por um; o frasco atirado, menos. */
    public final double powerScale;
    public final double durationScale;

    public BrewModifiers(double powerScale, double durationScale) {
        this.powerScale = powerScale;
        this.durationScale = durationScale;
    }

    public BrewModifiers() {
        this(1.0, 1.0);
    }

    public int getStrength() {
        return Math.max(this.strength - this.strengthPenalty, 0);
    }

    public int modifiedDuration(int ticks) {
        return Mth.ceil(this.durationScale * ticks * (this.duration + 1));
    }

    /** Depois de cada efeito, os temperos que esperavam se apagam. */
    public void reset() {
        this.inverted = false;
        this.strength = 0;
        this.duration = 0;
        this.noParticles = false;
    }

    public void increaseStrength(int quanto) {
        if (this.totalStrength < CEILING || this.powerCeilingDisabled) {
            this.strength += quanto;
            this.totalStrength += quanto;
        }
    }

    public void increaseDuration(int quanto) {
        if (this.totalDuration < CEILING || this.powerCeilingDisabled) {
            this.duration += quanto;
            this.totalDuration += quanto;
        }
    }
}
