package net.thaumcraft.occulta.divine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.thaumcraft.occulta.OccultaSounds;

/**
 * <b>Vais te apaixonar</b>: a {@code PredictionFallInLove} do Witchery.
 *
 * <p>A mais estranha das dezessete, e a única que não tem como se cumprir sozinha pelo mundo — ninguém se
 * apaixona por acaso num jogo de blocos. Por isso ela já nasce com a chance de se fazer a cada batida.
 *
 * <p>O que ela faz: nasce um <b>aldeão</b> a quatro ou seis blocos de quem foi avisado, com uma tarefa
 * própria — a de <b>cortejar aquele jogador</b> — enfiada no topo da lista do que ele faz. E toca-lhe o som
 * do original.
 *
 * <p>A tarefa é a do {@link net.thaumcraft.occulta.love.MateWithPlayerGoal}, que é o
 * {@code EntityAIMateWithPlayer} do Witchery. O aldeão não desiste dela.
 */
public class FallInLovePrediction extends AlwaysForcedPrediction {
    /** Quão longe dele o aldeão nasce. */
    public static final int LONGE = 6;

    /** E o salto que faz o anel ter um buraco no meio. */
    public static final int SALTO = 8;

    public FallInLovePrediction(int id, int peso, double forçaPorBatida, String recado, int prazo,
                                double emDia) {
        super(id, peso, forçaPorBatida, recado, prazo, emDia);
    }

    @Override
    public boolean cumprida(ServerLevel level, ServerPlayer quem, boolean atrasada, boolean velha) {
        if (!this.évez(level, atrasada)) return false;

        Villager aldeão = EntityTypes.VILLAGER.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (aldeão == null) return false;
        BlockPos onde = Predictions.lugarPerto(level, quem, LONGE, SALTO, aldeão.getBbHeight());
        if (onde == null) return false;
        if (!level.getBlockState(onde.below()).isSolidRender()) return false;

        aldeão.snapTo(onde.getX() + 0.5, onde.getY() + 0.05, onde.getZ() + 0.5, 0.0f, 0.0f);
        level.addFreshEntity(aldeão);
        net.thaumcraft.occulta.love.MateWithPlayerGoal.põe(aldeão, quem);
        Predictions.fumo(level, aldeão);
        level.playSound(null, quem.blockPosition(), OccultaSounds.LOVED.value(),
                SoundSource.PLAYERS, 1.0f, 1.0f);
        return true;
    }
}
