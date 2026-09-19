package net.thaumcraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.item.FocusSwap;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.item.WandItem;
import net.thaumcraft.registry.TCComponents;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A tecla de trocar foco e o menu radial: o {@code KeyHandler} e o {@code REHWandHandler.handleFociRadial} da
 * 4.2.3.5.
 *
 * <p>Com a varinha na mão, segurar F abre o menu: duas rodas giram em sentidos opostos, o foco preso fica no
 * centro e os outros — do inventário e das bolsas — em círculo, na ordem das letras de cada um. O que o mouse toca
 * cresce; soltar a tecla em cima dele, ou clicar, o escolhe. Agachado, F tira o foco preso.
 *
 * <p>No original o jogo só soltava o mouse; no de hoje, clicar com o mouse solto sem tela o prende de novo, então
 * o menu vive numa tela transparente enquanto a tecla está apertada, e o encolher de quando ela é solta segue pelo
 * mostrador.
 */
public final class FocusRadial {
    private static final Identifier RADIAL = Thaumcraft.id("textures/misc/radial.png");
    private static final Identifier RADIAL2 = Thaumcraft.id("textures/misc/radial2.png");
    public static final KeyMapping KEY_F = new KeyMapping("key.thaumcraft.focus", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F, KeyMapping.Category.MISC);

    private static boolean pressed;
    /** O {@code radialLock}: depois de escolher, não escolhe de novo até soltar a tecla. */
    private static boolean lock;
    static float hudScale;
    private static long lastTime;
    static final TreeMap<String, ItemStack> foci = new TreeMap<>();
    static final Map<String, Float> scale = new HashMap<>();
    static final Map<String, Boolean> hover = new HashMap<>();

    private FocusRadial() {
    }

    public static void init() {
        KeyMappingHelper.registerKeyMapping(KEY_F);
        ClientTickEvents.START_CLIENT_TICK.register(FocusRadial::tick);
        HudElementRegistry.addLast(Thaumcraft.id("focus_radial"), (graphics, tracker) -> {
            Minecraft minecraft = Minecraft.getInstance();
            // o encolher depois de solta a tecla: sem a tela, o mostrador termina o desenho
            if (!(minecraft.gui.screen() instanceof RadialScreen) && hudScale > 0.0f) {
                render(graphics, -10000, -10000, false);
            }
        });
    }

    private static void tick(Minecraft minecraft) {
        Player player = minecraft.player;
        if (player == null) return;
        boolean down = KEY_F.isDown() || minecraft.gui.screen() instanceof RadialScreen && InputConstants.isKeyDown(minecraft.getWindow(),
                KeyMappingHelper.getBoundKeyOf(KEY_F).getValue());
        if (down) {
            if (!pressed) lock = false;
            ItemStack held = player.getMainHandItem();
            // o cetro não troca de foco (o KeyHandler do original o deixa de fora)
            if (!lock && held.getItem() instanceof WandItem && !WandItem.isSceptre(held) && minecraft.gui.screen() == null) {
                if (player.isShiftKeyDown()) {
                    ClientPlayNetworking.send(new FocusSwap.Change(FocusSwap.REMOVE));
                    lock = true;
                } else {
                    open(minecraft, player);
                }
            }
            pressed = true;
        } else {
            if (minecraft.gui.screen() instanceof RadialScreen radial) radial.release();
            pressed = false;
        }
    }

    private static void open(Minecraft minecraft, Player player) {
        foci.clear();
        scale.clear();
        hover.clear();
        foci.putAll(FocusSwap.available(player));
        for (String key : foci.keySet()) {
            scale.put(key, 1.0f);
            hover.put(key, false);
        }
        if (foci.isEmpty()) return;
        minecraft.gui.setScreen(new RadialScreen());
    }

    /** O {@code renderFocusRadialHUD}, com o mouse na tela (ou fora dela, encolhendo). */
    static void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean active) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || foci.isEmpty()) return;
        ItemStack wand = player.getMainHandItem();
        if (!(wand.getItem() instanceof WandItem)) {
            hudScale = 0.0f;
            return;
        }
        long now = net.minecraft.util.Util.getMillis();
        if (now > lastTime) {
            for (String key : hover.keySet()) {
                float s = scale.get(key);
                if (hover.get(key)) {
                    if (s < 1.3f) scale.put(key, s + 0.025f);
                } else if (s > 1.0f) {
                    scale.put(key, s - 0.025f);
                }
            }
            hudScale = Mth.clamp(hudScale + (active ? 0.05f : -0.05f), 0.0f, 1.0f);
            lastTime = now + 5L;
        }
        if (hudScale <= 0.0f) return;

        int cx = graphics.guiWidth() / 2, cy = graphics.guiHeight() / 2;
        float ticks = player.tickCount % 720 / 2.0f + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float width = 16.0f + foci.size() * 2.5f;
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(cx, cy);
        wheel(graphics, RADIAL, ticks, width * 2.75f * hudScale);
        wheel(graphics, RADIAL2, -ticks, width * 2.55f * hudScale);

        ItemStack tooltip = null;
        String had = wand.get(TCComponents.WAND_FOCUS);
        if (had != null) {
            ItemStack held = net.thaumcraft.item.WandItem.focusStack(wand);
            if (!held.isEmpty()) {
                graphics.item(held, -8, -8);
                if (Math.abs(mouseX - cx) <= 10 && Math.abs(mouseY - cy) <= 10) tooltip = held;
            }
        }

        pose.scale(hudScale, hudScale);
        float rot = -90.0f * hudScale;
        float slice = 360.0f / foci.size();
        for (Map.Entry<String, ItemStack> entry : foci.entrySet()) {
            String key = entry.getKey();
            double xx = Mth.cos(rot / 180.0f * (float) Math.PI) * width;
            double yy = Mth.sin(rot / 180.0f * (float) Math.PI) * width;
            rot += slice;
            float s = scale.get(key);
            pose.pushMatrix();
            pose.translate((float) xx, (float) yy);
            pose.scale(s, s);
            graphics.item(entry.getValue(), -8, -8);
            pose.popMatrix();
            if (active && !lock) {
                double mx = (mouseX - cx) / (double) hudScale - xx, my = (mouseY - cy) / (double) hudScale - yy;
                boolean over = mx >= -10 && mx <= 10 && my >= -10 && my <= 10;
                hover.put(key, over);
                if (over) tooltip = entry.getValue();
            }
        }
        pose.popMatrix();
        if (tooltip != null && active) {
            graphics.setComponentTooltipForNextFrame(minecraft.font,
                    java.util.List.of(tooltip.getHoverName()), cx - 4, cy + 20);
        }
    }

    private static void wheel(GuiGraphicsExtractor graphics, Identifier texture, float degrees, float size) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.rotate((float) Math.toRadians(degrees));
        int half = (int) (size / 2.0f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, -half, -half, 0, 0, half * 2, half * 2, half * 2, half * 2, 0x80FFFFFF);
        pose.popMatrix();
    }

    /** Escolhe o foco sob o mouse e manda o pedido; depois disso, trava até soltar a tecla. */
    static void choose() {
        if (lock) return;
        for (Map.Entry<String, Boolean> entry : hover.entrySet()) {
            if (!entry.getValue()) continue;
            ClientPlayNetworking.send(new FocusSwap.Change(entry.getKey()));
            lock = true;
            return;
        }
    }

    /** A tela transparente do menu, que prende o mouse solto enquanto F está apertada. */
    static class RadialScreen extends Screen {
        RadialScreen() {
            super(Component.translatable("key.thaumcraft.focus"));
        }

        void release() {
            choose();
            this.onClose();
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
            render(graphics, mouseX, mouseY, true);
        }

        @Override
        public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (event.button() == 0) {
                choose();
                this.onClose();
                return true;
            }
            return super.mouseClicked(event, doubleClick);
        }

        @Override
        public boolean keyReleased(KeyEvent event) {
            if (KEY_F.matches(event)) {
                this.release();
                return true;
            }
            return super.keyReleased(event);
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
