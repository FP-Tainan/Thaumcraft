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
        PlayerKnowledge knowledge = Knowledges.of(player);
        if (add(player, knowledge, amount, temporary)) Knowledges.save(player, knowledge);
    }

    /** O mesmo, num caderno que quem chama vai guardar depois. */
    public static boolean add(Player player, PlayerKnowledge knowledge, int amount, boolean temporary) {
        if (!(player instanceof ServerPlayer server)) return false;
        if (!temporary && amount < 0 || amount == 0) return false;
        if (temporary) {
            if (amount < 0 && knowledge.warpTemp() <= 0) return false;
            knowledge.addWarpTemp(amount);
        } else {
            knowledge.addWarpPerm(amount);
        }
        knowledge.setWarpCounter(knowledge.warpTotal());
        TCNetwork.warpMessage(server, temporary ? TEMPORARY : PERMANENT, amount);
        return true;
    }

    /** A distorção que gruda. */
    public static void addSticky(Player player, int amount) {
        PlayerKnowledge knowledge = Knowledges.of(player);
        if (addSticky(player, knowledge, amount)) Knowledges.save(player, knowledge);
    }

    public static boolean addSticky(Player player, PlayerKnowledge knowledge, int amount) {
        if (!(player instanceof ServerPlayer server) || amount == 0) return false;
        if (amount < 0 && knowledge.warpSticky() <= 0) return false;
        knowledge.addWarpSticky(amount);
        knowledge.setWarpCounter(knowledge.warpTotal());
        TCNetwork.warpMessage(server, STICKY, amount);
        return true;
    }
}
