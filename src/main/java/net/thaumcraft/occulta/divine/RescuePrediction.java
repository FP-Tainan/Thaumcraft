package net.thaumcraft.occulta.divine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.thaumcraft.occulta.NoDrops;

/**
 * <b>Vais ser salvo</b>: a {@code PredictionRescue} do Witchery.
 *
 * <p>É a única que se cumpre <b>no pior momento</b>, e é por isso que ela é a melhor do ramo: o gancho dela
 * não é o de a profecia vencer, é o de <b>alguém bater em quem foi avisado</b>. No instante em que ele leva
 * um golpe, nasce ao lado dele uma coruja — ou um lobo — que vai direto a quem o atacou.
 *
 * <p>A coruja que vem salvar é <b>passageira</b>: vive trezentas batidas, ou menos se aquilo de que ela o
 * salvou morrer primeiro, e depois estoura num pó e vai embora. E <b>não larga nada</b> — no original
 * porque ela sabe que é temporária, aqui porque leva a marca do {@link NoDrops}, que dá no mesmo e já
 * existia.
 */
public class RescuePrediction extends AlwaysForcedPrediction {
    private final EntityType<? extends Mob> espécie;

    /** Quanto tempo vive quem vem salvar. */
    public static final int VIVE = 300;

    public RescuePrediction(int id, int peso, double forçaPorBatida, String recado, int prazo, double emDia,
                            EntityType<? extends Mob> espécie) {
        super(id, peso, forçaPorBatida, recado, prazo, emDia);
        this.espécie = espécie;
    }

    @Override
    public boolean cumprida(ServerLevel level, ServerPlayer quem, DamageSource fonte, boolean atrasada,
                            boolean velha) {
        if (!(fonte.getEntity() instanceof LivingEntity agressor)) return false;

        Mob salvador = this.espécie.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (salvador == null) return false;
        BlockPos onde = Predictions.lugarPerto(level, quem, Predictions.LONGE, Predictions.SALTO,
                salvador.getBbHeight());
        if (onde == null) return false;

        salvador.snapTo(onde.getX() + 0.5, onde.getY() + 0.05, onde.getZ() + 0.5, 0.0f, 0.0f);
        level.addFreshEntity(salvador);
        salvador.finalizeSpawn(level, level.getCurrentDifficultyAt(onde), EntitySpawnReason.MOB_SUMMONED,
                null);
        NoDrops.marca(salvador);
        if (salvador instanceof net.thaumcraft.occulta.familiar.OwlEntity coruja) {
            coruja.viveSó(VIVE);
        }
        salvador.setTarget(agressor);
        if (salvador instanceof net.minecraft.world.entity.PathfinderMob caça) {
            caça.setLastHurtByMob(agressor);
        }
        Predictions.fumo(level, salvador);
        return true;
    }
}
