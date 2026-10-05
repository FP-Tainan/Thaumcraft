package net.thaumcraft.occulta.divine;

import net.minecraft.server.level.ServerLevel;

/**
 * A profecia que <b>o mod cumpre sempre</b>: a {@code PredictionAlwaysForced} do Witchery.
 *
 * <p>As outras esperam que o mundo as cumpra e só forçam a mão depois do prazo. Estas <b>nunca</b> esperam:
 * há coisas que o mundo não faz sozinho — ninguém se apaixona por acaso, ninguém tropeça num baú enterrado —
 * e por isso elas já nascem com uma chance de se fazerem a cada batida, mesmo em dia.
 *
 * <p>A chance é pequena enquanto a profecia está em dia (um por cento) e sobe para a de forçar (cinco por
 * cento) depois do prazo. E o prazo delas é <b>próprio</b>, e não o de oito minutos das outras: mil duzentas
 * e tantas batidas, que é cerca de um minuto.
 */
public abstract class AlwaysForcedPrediction extends Prediction {
    /** O prazo próprio desta. */
    protected final int prazo;

    /** A chance por batida enquanto ela está em dia. */
    protected final double emDia;

    protected AlwaysForcedPrediction(int id, int peso, double forçaPorBatida, String recado, int prazo,
                                     double emDia) {
        super(id, peso, forçaPorBatida, recado);
        this.prazo = prazo;
        this.emDia = emDia;
    }

    @Override
    public long prazo() {
        return this.prazo;
    }

    /** Se é a vez dela: um por cento em dia, cinco por cento atrasada. */
    protected boolean évez(ServerLevel level, boolean atrasada) {
        return level.getRandom().nextDouble() < (atrasada ? this.forçaPorBatida : this.emDia);
    }
}
