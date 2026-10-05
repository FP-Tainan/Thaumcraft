package net.thaumcraft.occulta.divine;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

/**
 * <b>Vais brigar com um {@code X}</b>: a {@code PredictionFight} do Witchery.
 *
 * <p>Cumpre-se quando o bicho daquela espécie bate em quem foi avisado. E, passado o prazo, <b>faz nascer
 * um</b> a dois ou quatro blocos dele, de costas para nada, já olhando para ele.
 *
 * <p>A mesma classe serve para a profecia <b>boa</b>: com {@code amansa} ligado, o bicho que nasce é
 * <b>dele</b> em vez de ser contra ele — é assim que o original diz «vais fazer um amigo» com o mesmo
 * código com que diz «vais brigar com um lobo».
 */
public class FightPrediction extends Prediction {
    protected final EntityType<? extends Mob> espécie;

    /** Se o bicho que nasce é amigo em vez de inimigo. */
    protected final boolean amansa;

    public FightPrediction(int id, int peso, double forçaPorBatida, String recado,
                           EntityType<? extends Mob> espécie, boolean amansa) {
        super(id, peso, forçaPorBatida, recado);
        this.espécie = espécie;
        this.amansa = amansa;
    }

    @Override
    public boolean força(ServerLevel level, ServerPlayer quem) {
        Mob bicho = this.espécie.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (bicho == null) return true;
        BlockPos onde = Predictions.lugarPerto(level, quem, Predictions.LONGE, Predictions.SALTO,
                bicho.getBbHeight());
        if (onde == null) return false;

        boolean amigo = this.amansa && amansa(bicho, quem);
        bicho.snapTo(onde.getX() + 0.5, onde.getY() + 0.05, onde.getZ() + 0.5, 0.0f, 0.0f);
        level.addFreshEntity(bicho);
        bicho.finalizeSpawn(level, level.getCurrentDifficultyAt(onde), EntitySpawnReason.MOB_SUMMONED, null);
        if (!amigo) bicho.setTarget(quem);
        Predictions.fumo(level, bicho);
        return true;
    }

    /** Tenta fazer o bicho ser dele. Devolve se conseguiu. */
    static boolean amansa(Mob bicho, ServerPlayer quem) {
        if (bicho instanceof TamableAnimal manso) {
            manso.setTame(true, true);
            manso.setOwner(quem);
            return true;
        }
        if (bicho instanceof net.thaumcraft.occulta.baba.BabaYagaEntity baba) {
            baba.dono(quem);
            return true;
        }
        return false;
    }

    @Override
    public boolean cumprida(ServerLevel level, ServerPlayer quem, DamageSource fonte, boolean atrasada,
                            boolean velha) {
        Entity quemBateu = fonte.getEntity();
        return quemBateu != null && quemBateu.getType() == this.espécie;
    }

    /** Quem bateu, para quem precisa dele. */
    protected static LivingEntity agressor(DamageSource fonte) {
        return fonte.getEntity() instanceof LivingEntity vivo ? vivo : null;
    }
}
