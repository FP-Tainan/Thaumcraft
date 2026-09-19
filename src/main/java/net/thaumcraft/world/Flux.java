package net.thaumcraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * O fluxo que a magia derrama no mundo: a gosma ({@code blockFluxGoo}) que escorre para baixo e o gás
 * ({@code blockFluxGas}) que sobe.
 *
 * <p>Os blocos de fluxo chegam na fatia do fluxo e da fauna da mácula; até lá, quem derrama fluxo chama por aqui, e o
 * derrame fica registrado no lugar certo de cada peça — quando os blocos existirem, é só este método que muda.
 */
public final class Flux {
    private Flux() {
    }

    /**
     * O derrame do reservatório quebrado (e de quem mais estourar): até {@code amount} blocos de fluxo em volta, a gosma
     * abaixo e o gás acima.
     */
    public static void spill(ServerLevel level, BlockPos pos, int amount) {
        // PENDENTE (fatia do fluxo): gosma e gás de fluxo, 50 tentativas a até 4 blocos, cheios (8)
    }
}
