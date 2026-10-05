package net.thaumcraft.occulta.divine;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

/**
 * <b>Vais ao Nether</b>: a {@code PredictionNetherTrip} do Witchery.
 *
 * <p>É a única que só se diz a quem <b>já lá esteve</b> — o original guarda no jogador uma marca de que ele
 * visitou o Nether, e sem ela esta profecia nem entra no sorteio. Faz sentido: dizer a um principiante que
 * ele vai ao Nether não é ler a sorte, é dar-lhe uma missão.
 *
 * <p>Cumpre-se no instante em que ele lá chega. E, passado o prazo, o mod <b>manda o Nether a ele</b>: um
 * recado, e um <b>blaze</b> ao lado.
 */
public class NetherTripPrediction extends Prediction {
    public NetherTripPrediction(int id, int peso, double forçaPorBatida, String recado) {
        super(id, peso, forçaPorBatida, recado);
    }

    @Override
    public boolean possível(ServerLevel level, ServerPlayer quem) {
        return level.dimension() != Level.NETHER && Predictions.jáFoiAoNether(quem);
    }

    @Override
    public boolean força(ServerLevel level, ServerPlayer quem) {
        if (level.dimension() == Level.NETHER) return false;
        quem.sendSystemMessage(Component.translatable("tc.occulta.prediction.tothenether.summoned")
                .withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));

        Mob blaze = EntityTypes.BLAZE.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (blaze == null) return true;
        BlockPos onde = Predictions.lugarPerto(level, quem, Predictions.LONGE, Predictions.SALTO,
                blaze.getBbHeight());
        if (onde == null) return true;
        blaze.snapTo(onde.getX(), onde.getY(), onde.getZ(), 0.0f, 0.0f);
        level.addFreshEntity(blaze);
        return true;
    }

    @Override
    public boolean cumprida(ServerLevel level, ServerPlayer quem, boolean atrasada, boolean velha) {
        return level.dimension() == Level.NETHER;
    }
}
