package net.thaumcraft.occulta.divine;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

/**
 * <b>Vais te molhar</b>: a {@code PredictionWet} do Witchery.
 *
 * <p>A irmã pequena da queda, e a mais engraçada das dezessete. Em dia, qualquer chuva a cumpre. Passado o
 * prazo, ela abre no chão o mesmo buraco de nove blocos — mas de <b>três</b> de fundo em vez de seis, e
 * <b>o enche de água</b>.
 *
 * <p>Quer dizer: a profecia que promete o menor dos incómodos é a que faz o maior estrago na casa de quem a
 * leva. E ela <b>não vale no Nether</b>, onde a água não ficaria.
 */
public class WetPrediction extends Prediction {
    /** Quanto fundo é a poça. */
    public static final int FUNDO = 3;

    /** E a altura abaixo da qual ela não se atreve. */
    public static final int CHÃO = 5;

    public WetPrediction(int id, int peso, double forçaPorBatida, String recado) {
        super(id, peso, forçaPorBatida, recado);
    }

    @Override
    public boolean força(ServerLevel level, ServerPlayer quem) {
        if (level.dimension() == net.minecraft.world.level.Level.NETHER) return false;
        return FallPrediction.abre(level, quem.blockPosition().below(), FUNDO, CHÃO, Blocks.WATER);
    }

    @Override
    public boolean cumprida(ServerLevel level, ServerPlayer quem, boolean atrasada, boolean velha) {
        return quem.isInWaterOrRain();
    }
}
