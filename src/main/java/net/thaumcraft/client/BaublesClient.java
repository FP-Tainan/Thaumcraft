package net.thaumcraft.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.thaumcraft.baubles.Baubles;

/**
 * O lado de quem joga do Baubles: o tique das peças vestidas.
 *
 * <p>O inventário expandido do original, com a tecla B e o botãozinho que alternava as duas telas, saiu: as quatro
 * casas agora ficam no inventário do próprio jogo ({@code InventoryMenuBaublesMixin}), a pedido de quem joga.
 */
public final class BaublesClient {
    private BaublesClient() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            if (minecraft.player != null && !minecraft.isPaused()) Baubles.tick(minecraft.player);
        });
    }
}
