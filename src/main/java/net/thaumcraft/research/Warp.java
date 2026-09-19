package net.thaumcraft.research;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.net.TCNetwork;

/**
 * A distorção (o <em>warp</em>) da 4.2.3.5: o {@code addWarpToPlayer} e o {@code addStickyWarpToPlayer} do
 * {@code Thaumcraft}. Há três: a permanente, a que gruda (que o sabão e os sais tiram) e a temporária (que some um ponto
 * a cada evento). Mexer em qualquer uma avisa quem a ganhou e reinicia o contador que puxa os eventos.
 */
public final class Warp {
    public static final int PERMANENT = 0;
    public static final int STICKY = 1;
    public static final int TEMPORARY = 2;

    private Warp() {
    }

    /** Distorção permanente ou temporária; a permanente não se tira por aqui. */
    public static void add(Player player, int amount, boolean temporary) {
        if (!(player instanceof ServerPlayer server)) return;
        if (!temporary && amount < 0 || amount == 0) return;
        PlayerKnowledge knowledge = Knowledges.of(player);
        if (temporary) {
            if (amount < 0 && knowledge.warpTemp() <= 0) return;
            knowledge.addWarpTemp(amount);
        } else {
            knowledge.addWarpPerm(amount);
        }
        knowledge.setWarpCounter(knowledge.warpTotal());
        Knowledges.save(player, knowledge);
        TCNetwork.warpMessage(server, temporary ? TEMPORARY : PERMANENT, amount);
    }

    /** A distorção que gruda. */
    public static void addSticky(Player player, int amount) {
        if (!(player instanceof ServerPlayer server) || amount == 0) return;
        PlayerKnowledge knowledge = Knowledges.of(player);
        if (amount < 0 && knowledge.warpSticky() <= 0) return;
        knowledge.addWarpSticky(amount);
        knowledge.setWarpCounter(knowledge.warpTotal());
        Knowledges.save(player, knowledge);
        TCNetwork.warpMessage(server, STICKY, amount);
    }
}
