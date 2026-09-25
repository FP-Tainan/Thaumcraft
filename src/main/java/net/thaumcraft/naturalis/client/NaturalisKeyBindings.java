package net.thaumcraft.naturalis.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.naturalis.NaturalisKeys;
import org.lwjgl.glfw.GLFW;

/**
 * As quatro teclas do Magia Naturalis 0.5.0 ({@code MNKeyBindings}), com as letras do original: N aumenta a área,
 * J diminui, B passa à forma seguinte — e, com o Ctrl, ao jeito seguinte — e o botão do meio do mouse marca o
 * bloco da mira. Todas só valem com o Foco de Construção na varinha da mão.
 */
public final class NaturalisKeyBindings {
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Thaumcraft.id("naturalis"));

    public static final KeyMapping INCREASE = key("increase_size", GLFW.GLFW_KEY_N);
    public static final KeyMapping DECREASE = key("decrease_size", GLFW.GLFW_KEY_J);
    public static final KeyMapping MISC = key("misc", GLFW.GLFW_KEY_B);
    public static final KeyMapping PICK = new KeyMapping("key.thaumcraft.naturalis.pick_block",
            InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, CATEGORY);

    private static boolean increasePressed;
    private static boolean decreasePressed;
    private static boolean miscPressed;
    private static boolean pickPressed;

    private NaturalisKeyBindings() {
    }

    private static KeyMapping key(String name, int code) {
        return new KeyMapping("key.thaumcraft.naturalis." + name, InputConstants.Type.KEYSYM, code, CATEGORY);
    }

    public static void init() {
        for (KeyMapping mapping : new KeyMapping[]{INCREASE, DECREASE, MISC, PICK}) {
            KeyMappingHelper.registerKeyMapping(mapping);
        }
        ClientTickEvents.END_CLIENT_TICK.register(NaturalisKeyBindings::tick);
    }

    /** O {@code playerTick} do original: uma vez por aperto, e só com o foco na mão. */
    private static void tick(Minecraft minecraft) {
        boolean vale = minecraft.player != null && minecraft.gui.screen() == null && holdingBuilder(minecraft);

        if (PICK.isDown()) {
            if (!pickPressed && vale) pickBlock(minecraft);
            pickPressed = true;
        } else {
            pickPressed = false;
        }
        if (DECREASE.isDown()) {
            if (!decreasePressed && vale) ClientPlayNetworking.send(new NaturalisKeys.Key(NaturalisKeys.DECREASE));
            decreasePressed = true;
        } else {
            decreasePressed = false;
        }
        if (INCREASE.isDown()) {
            if (!increasePressed && vale) ClientPlayNetworking.send(new NaturalisKeys.Key(NaturalisKeys.INCREASE));
            increasePressed = true;
        } else {
            increasePressed = false;
        }
        if (MISC.isDown()) {
            if (!miscPressed && vale) {
                boolean jeito = InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)
                        || InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL);
                ClientPlayNetworking.send(new NaturalisKeys.Key(jeito ? NaturalisKeys.MODE : NaturalisKeys.SHAPE));
            }
            miscPressed = true;
        } else {
            miscPressed = false;
        }
    }

    private static boolean holdingBuilder(Minecraft minecraft) {
        ItemStack held = minecraft.player.getMainHandItem();
        if (!(held.getItem() instanceof WandItem)) return false;
        ItemStack focus = WandItem.focusStack(held);
        return focus.getItem() instanceof FocusItem item && "build".equals(item.type());
    }

    /** O {@code PacketPickedBlock}: o bloco da mira passa a ser o que o foco constrói. */
    private static void pickBlock(Minecraft minecraft) {
        HitResult mira = Focuses.targetBlock(minecraft.player.level(), minecraft.player);
        if (!(mira instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;
        Block block = minecraft.player.level().getBlockState(hit.getBlockPos()).getBlock();
        ClientPlayNetworking.send(new NaturalisKeys.Picked(BuiltInRegistries.BLOCK.getKey(block).toString()));
    }
}
