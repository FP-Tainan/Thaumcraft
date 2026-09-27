package net.thaumcraft.occulta.spirit;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * O relógio do fantasma: o pedaço do {@code updatePlayerEffects} do Witchery que conta a manifestação.
 *
 * <p>O <b>Rito da Manifestação</b> não abre porta nenhuma: ele dá <b>crédito</b>. Cento e cinquenta segundos de
 * corpo no mundo de cá, que só se gastam quando se atravessa o Portal do Espírito. Enquanto o fantasma anda, a
 * conta desce de cinco em cinco segundos; aos sessenta, aos trinta e aos quinze ele é avisado; e no zero é
 * puxado de volta para o outro lado, queira ou não.
 */
public final class SpiritManifest {
    /** Quantos segundos o rito dá: o {@code 150} do original. */
    public static final int GRANTED = 150;
    /** De quantos em quantos tiques a conta desce, e quanto ela desce: cinco segundos. */
    public static final int EVERY = 100;
    public static final int STEP = 5;
    /** E em que contas ele é avisado. */
    public static final int[] WARNINGS = {60, 30, 15};
    /** Abaixo disto o portal não deixa passar: é o {@code canPlayerManifest}. */
    public static final int FLOOR = 5;

    private SpiritManifest() {
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
        SpiritWalk era = SpiritWalk.of(quem);
        if (!era.ghost()) return;

        int resta = Math.max(0, era.manifest() - STEP);
        if (resta == 0) {
            SpiritWalk.set(quem, era.withManifest(0));
            SpiritWorld.unmanifest(quem);
            return;
        }
        SpiritWalk.set(quem, era.withManifest(resta));
        for (int aviso : WARNINGS) {
            if (resta > aviso || resta <= aviso - STEP) continue;
            quem.sendSystemMessage(Component.translatable("tc.rite.manifest.countdown", resta)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            break;
        }
    }

    /** Quanto crédito de manifestação aquela pessoa tem. */
    public static int credit(ServerPlayer quem) {
        return SpiritWalk.of(quem).manifest();
    }

    /** Dá-lhe crédito: é o que o Rito da Manifestação faz, e ele soma ao que já houver. */
    public static void grant(ServerPlayer quem, int segundos) {
        SpiritWalk era = SpiritWalk.of(quem);
        SpiritWalk.set(quem, era.withManifest(era.manifest() + segundos));
    }

    /** E se ela pode atravessar o portal: o {@code canPlayerManifest}. */
    public static boolean canManifest(ServerPlayer quem) {
        return credit(quem) >= FLOOR;
    }
}
