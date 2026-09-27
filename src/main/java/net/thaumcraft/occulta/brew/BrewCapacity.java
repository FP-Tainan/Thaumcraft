package net.thaumcraft.occulta.brew;

/**
 * Quanto cabe num cozimento: o {@code EffectLevelCounter} do Witchery.
 *
 * <p>Um caldeirão de água não recebe efeito nenhum. Quem abre espaço são os <b>ingredientes de porte</b> — a Raiz
 * de Mandrágora, a Verruga do Nether, o diamante, a Estrela do Nether —, e cada um deles só abre espaço se o que
 * já se abriu ainda for menor que o teto dele. É por isso que dois diamantes não valem o dobro: o segundo olha
 * para o teto, vê que já se passou dele, e não faz nada.
 *
 * <p>Cada efeito <b>gasta</b> do espaço aberto, conforme o peso dele — os simples pesam dois, os graves quatro ou
 * seis. O que não couber, não entra: é o caldeirão a dizer que aquela receita não se faz.
 */
public class BrewCapacity {
    private int teto;
    private int gasto;
    private int efeitos;

    /** O {@code increaseAvailableLevelIf}: abre espaço, mas só enquanto o que há for menor que o teto do ingrediente. */
    public void openIf(int quanto, int tetoDoIngrediente) {
        if (this.teto < tetoDoIngrediente) this.teto += quanto;
    }

    public int remaining() {
        return this.teto - this.gasto;
    }

    public int used() {
        return this.gasto;
    }

    public int max() {
        return this.teto;
    }

    public int effects() {
        return this.efeitos;
    }

    /** O {@code tryConsumeLevel}: gasta o espaço do efeito, se couber. */
    public boolean consume(int peso) {
        if (!this.fits(peso)) return false;
        this.gasto += peso;
        this.efeitos++;
        return true;
    }

    public boolean fits(int peso) {
        return peso + this.gasto <= this.teto;
    }

    public boolean hasEffects() {
        return this.gasto > 0;
    }
}
