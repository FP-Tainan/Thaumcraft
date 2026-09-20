package net.thaumcraft.baubles;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * O lado do servidor do Baubles: o tique das peças vestidas e a queda delas na morte.
 */
public final class BaublesEvents {
    private BaublesEvents() {
    }

    public static void init() {
        Baubles.init();
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) Baubles.tick(player);
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer player
                    && !player.level().getGameRules().get(GameRules.KEEP_INVENTORY)) {
                Baubles.dropOnDeath(player);
            }
        });
    }
}
