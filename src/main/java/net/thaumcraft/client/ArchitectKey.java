package net.thaumcraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.thaumcraft.item.Architect;
import net.thaumcraft.item.WandItem;
import org.lwjgl.glfw.GLFW;

/**
 * A tecla G, o {@code keyG} ("Misc Wand Toggle") do {@code KeyHandler} da 4.2.3.5: com a varinha na mão, manda ao
 * servidor o número 1 do {@code PacketItemKeyToServer}, uma vez por aperto.
 */
public final class ArchitectKey {
    public static final KeyMapping KEY_G = new KeyMapping("key.thaumcraft.misc", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G, KeyMapping.Category.MISC);
    private static boolean pressed;

    private ArchitectKey() {
    }

    public static void init() {
        KeyMappingHelper.registerKeyMapping(KEY_G);
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            if (KEY_G.isDown()) {
                if (!pressed && minecraft.player != null && minecraft.gui.screen() == null
                        && minecraft.player.getMainHandItem().getItem() instanceof WandItem) {
                    ClientPlayNetworking.send(new Architect.Key(1));
                }
                pressed = true;
            } else {
                pressed = false;
            }
        });
    }
}
