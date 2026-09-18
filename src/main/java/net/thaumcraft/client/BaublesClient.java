package net.thaumcraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.thaumcraft.baubles.Baubles;
import net.thaumcraft.baubles.BaublesNetwork;
import net.thaumcraft.client.gui.BaublesScreen;
import org.lwjgl.glfw.GLFW;

/**
 * O lado de quem joga do Baubles: a tecla B ({@code KeyHandler}), o botão no inventário ({@code GuiEvents}) e o tique
 * das peças vestidas.
 */
public final class BaublesClient {
    public static final KeyMapping KEY = new KeyMapping("key.thaumcraft.baubles", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B, KeyMapping.Category.INVENTORY);

    private BaublesClient() {
    }

    public static void init() {
        KeyMappingHelper.registerKeyMapping(KEY);
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            if (minecraft.player == null) return;
            if (!minecraft.isPaused()) Baubles.tick(minecraft.player);
            while (KEY.consumeClick()) {
                if (minecraft.gui.screen() == null) toggle(true);
            }
        });
        // o botão do inventário de sempre, no canto de cima do quadro do jogador, como no original
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof InventoryScreen) {
                Screens.getWidgets(screen).add(new BaublesScreen.ToggleButton((width - 176) / 2 + 66, (height - 166) / 2 + 9, true));
            }
        });
    }

    /** Troca entre o inventário de sempre e o expandido. */
    public static void toggle(boolean toBaubles) {
        Minecraft minecraft = Minecraft.getInstance();
        if (toBaubles) {
            ClientPlayNetworking.send(new BaublesNetwork.Open(true));
        } else {
            minecraft.gui.setScreen(new InventoryScreen(minecraft.player));
            ClientPlayNetworking.send(new BaublesNetwork.Open(false));
        }
    }
}
