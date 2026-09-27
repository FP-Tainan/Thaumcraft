package net.thaumcraft.occulta.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Um passo de um rito: o {@code RitualStep} do Witchery.
 *
 * <p>Um rito é uma fila de passos. A cada batida o primeiro da fila corre e diz o que aconteceu: se ainda está
 * <b>começando</b>, fica; se <b>acabou</b>, sai da fila e o seguinte entra; se <b>desistiu</b>, o rito inteiro
 * morre — e, se desistiu pedindo devolução, o que se ofereceu volta para o chão.
 *
 * <p>O passo que devolve <b>sustento</b> é outra coisa: o rito sai da fila dos que estão a correr e passa para a
 * dos que se sustentam, que correm um passo só, para sempre, até desistirem.
 */
public interface RiteStep {
    enum Result {
        /** Ainda não é a hora: fica-se no mesmo passo. */
        STARTING,
        /** O passo acabou: o seguinte entra. */
        COMPLETED,
        /** O rito passa a sustentar-se sozinho. */
        UPKEEP,
        /** O rito morre. */
        ABORTED,
        /** O rito morre e devolve o que se ofereceu. */
        ABORTED_REFUND
    }

    Result run(ServerLevel level, BlockPos onde, long ticks, ActiveRite rito);
}
