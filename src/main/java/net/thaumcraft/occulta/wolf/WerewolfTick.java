package net.thaumcraft.occulta.wolf;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/**
 * <b>O relógio da lua</b>: o pedaço do {@code GenericEvents} que olha, de duas em duas segundos, se a lua já
 * pegou alguém.
 *
 * <p>A regra é curta e vale a pena lê-la de uma vez:
 *
 * <ul>
 *   <li>de <b>gente</b>, em <b>lua cheia</b>, ele <b>vira lobo</b> — queira ou não;</li>
 *   <li>de <b>bicho</b>, <b>fora</b> da lua cheia, ele <b>volta a ser gente</b> — queira ou não;</li>
 *   <li>e o <b>Amuleto da Lua</b> ou o <b>acônito</b> cancelam as duas coisas.</li>
 * </ul>
 *
 * <p>Repare que o amuleto não transforma nem destransforma: ele <b>segura</b>. Quem virar lobo de propósito
 * numa noite qualquer e trouxer o amuleto fica lobo até o largar — e quem for apanhado pela lua cheia com o
 * amuleto na mochila passa a noite inteira de gente.
 *
 * <p>É de <b>quarenta em quarenta batidas</b>, como no original: dois segundos de atraso numa lua que dura
 * uma noite não se vê, e varrer todo jogador a cada batida seria caro para nada.
 */
public final class WerewolfTick {
    /** De quanto em quanto se olha a lua: as quarenta batidas do original. */
    public static final int DE_QUANTO_EM_QUANTO = 40;

    private WerewolfTick() {
    }

    /**
     * Põe o relógio a correr.
     *
     * <p>Duas coisas correm aqui, e em compassos diferentes: <b>a lua</b>, de quarenta em quarenta batidas, e
     * <b>o salto</b>, a cada batida — porque o arranco de um bicho a correr tem de entrar na batida em que ele
     * corre. Quem não é nada sai da conta na primeira pergunta, e por isso varrer todo jogador a cada batida
     * não custa nada.
     */
    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            boolean aLua = server.getTickCount() % DE_QUANTO_EM_QUANTO == 1;
            for (ServerLevel level : server.getAllLevels()) {
                for (Player quem : level.players()) {
                    if (Werewolf.grauDe(quem) <= 0) continue;
                    WerewolfPowers.salta(quem);
                    if (aLua) olha(level, quem);
                }
            }
        });
    }

    /** O que a lua faz a esta pessoa agora. */
    public static void olha(ServerLevel level, Player quem) {
        Werewolf oQueÉ = Werewolf.de(quem);
        if (oQueÉ.grau() <= 0) return;

        boolean cheia = Moon.cheia(level) && !level.isBrightOutside();
        if (Werewolf.aLuaNãoLhePega(quem)) {
            // o amuleto segura a forma onde ela estiver — e o que ela dá continua a valer
            if (oQueÉ.forma().éBicho()) Werewolf.vira(level, quem, oQueÉ.forma());
            return;
        }

        if (oQueÉ.forma().éBicho()) {
            if (cheia) {
                Werewolf.vira(level, quem, oQueÉ.forma());
                return;
            }
            // a lua passou: ele volta a ser gente, queira ou não
            Werewolf.forma(quem, Werewolf.Forma.GENTE);
            estouro(level, quem, SoundEvents.FIRE_EXTINGUISH);
            return;
        }

        if (!cheia) return;
        // de gente, em lua cheia: vira lobo
        Werewolf.forma(quem, Werewolf.Forma.LOBO);
        Werewolf.vira(level, quem, Werewolf.Forma.LOBO);
        estouro(level, quem, SoundEvents.RAVAGER_ROAR);
    }

    /**
     * Arruma o corpo para a forma em que ele está.
     *
     * <p>É chamado sempre que o grau ou a forma mudam, e é o {@code initCurrentShift} do original: os
     * modificadores saem e entram de uma vez, para a vida e a velocidade nunca ficarem a meio.
     */
    public static void arruma(ServerLevel level, Player quem) {
        WerewolfStats.põe(quem);
    }

    private static void estouro(ServerLevel level, Player quem, net.minecraft.sounds.SoundEvent som) {
        level.sendParticles(ParticleTypes.EXPLOSION, quem.getX(), quem.getY() + 1.0, quem.getZ(),
                8, 0.5, 0.5, 0.5, 0.0);
        level.playSound(null, quem.blockPosition(), som, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
