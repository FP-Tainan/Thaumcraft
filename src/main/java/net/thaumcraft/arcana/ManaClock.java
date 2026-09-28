package net.thaumcraft.arcana;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;

/**
 * O relógio que enche a mana: o {@code updateTick} do {@code ExtendedProperties} do Ars Magica 2.
 *
 * <p>De vinte em vinte batidas a mana sobe um pouco e o desgaste desce outro. Encher por inteiro leva
 * <b>1800 batidas</b> — um minuto e meio —, e é isso que faz a mana valer alguma coisa: quem a
 * gastou espera.
 *
 * <p><b>O desgaste desce com o nível</b>, e é a única coisa aqui que o faz: um mago de nível um leva quase
 * eternamente para se livrar dele, e um de 99 se livra rápido. Um arcanista sem nível não é alguém
 * com pouca mana — é alguém que fica gasto e não se recupera.
 *
 * <p>Quem joga em criativo enche na hora, como no original.
 */
public final class ManaClock {
    /** De quantas em quantas batidas o relógio bate: as 20 do original. */
    public static final int EVERY = 20;

    /** Quantas batidas leva a encher por inteiro, antes de se contar o nível. */
    public static final int BASE_FULL_REGEN = 2400;

    /**
     * Quantas batidas leva a encher, para aquele nível.
     *
     * <p><b>Desvio declarado — e é um <i>erro do original</i> que este porte corrige.</b> A conta lá é
     * {@code 2400 × (0,75 − 0,25 × (nível/99))}, com {@code nível} e {@code 99} inteiros: essa divisão dá
     * <b>zero</b> para todo nível abaixo de 99, e o nível não conta para nada. Aqui a divisão é
     * feita em vírgula flutuante, que é o que a fórmula claramente queria — encher fica mais rápido conforme o
     * mago cresce, de 1800 batidas para 1200.
     */
    public static int ticksForFullRegen(int level) {
        int nível = Math.clamp(level, 0, Mana.MAX_LEVEL);
        return (int) Math.round(BASE_FULL_REGEN * (0.75 - 0.25 * ((double) nível / Mana.MAX_LEVEL)));
    }

    /** A parte do desgaste que se perde por batida e por nível: o {@code 0,01} do original. */
    public static final float BURNOUT_DROP = 0.01f;

    private ManaClock() {
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % EVERY != 0) return;
            for (ServerPlayer quem : server.getPlayerList().getPlayers()) {
                tick(quem);
            }
        });
    }

    /** Uma batida do relógio daquela pessoa. */
    public static void tick(ServerPlayer quem) {
        Mana era = Mana.of(quem);
        if (quem.hasInfiniteMaterials()) {
            if (era.mana() < era.maxMana() || era.burnout() > 0.0f) {
                Mana.set(quem, era.withMana(era.maxMana()).withBurnout(0.0f));
            }
            return;
        }

        Mana agora = era;
        if (era.mana() < era.maxMana()) {
            float sobe = era.maxMana() / ticksForFullRegen(era.level()) * EVERY;
            agora = agora.withMana(agora.mana() + sobe);
        }
        if (era.burnout() > 0.0f) {
            float desce = BURNOUT_DROP * era.level() * EVERY;
            agora = agora.withBurnout(agora.burnout() - desce);
        }
        if (!agora.equals(era)) Mana.set(quem, agora);

        recoverFalloff(quem);
    }

    /**
     * E o retorno decrescente da Afinidade sobe: o {@code tickDiminishingReturns}.
     *
     * <p><b>Desvio declarado.</b> No original isto corre <b>a cada batida</b>, somando 0,005 — 0,1 por
     * segundo, e o 1,2 volta cheio em 12 segundos a quem o esvaziou. Aqui corre junto com o relógio da mana,
     * de 20 em 20, somando os mesmos 0,005 <b>vinte vezes</b>. Dá no mesmo, e poupa um laço por batida em
     * cima de todo mundo que está no servidor.
     */
    public static void recoverFalloff(ServerPlayer quem) {
        AffinityData era = AffinityData.of(quem);
        if (era.falloff() >= AffinityData.MAX_FALLOFF) return;
        AffinityData agora = era;
        for (int i = 0; i < EVERY; i++) agora = agora.recovered();
        AffinityData.set(quem, agora);
    }
}
