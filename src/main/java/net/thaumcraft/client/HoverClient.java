package net.thaumcraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.client.fx.LightningBolt;
import net.thaumcraft.client.fx.Sparkle;
import net.thaumcraft.event.Hover;
import net.thaumcraft.item.HoverHarnessItem;
import net.thaumcraft.item.JarContents;
import net.thaumcraft.registry.TCComponents;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.research.Knowledges;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/**
 * O lado de quem joga do arreio taumostático: a tecla H ({@code KeyHandler}), o {@code handleHoverArmor} do jogador
 * local (o voo mais lento, o zumbido), as faíscas que saltam das costas de quem paira ({@code ModelHoverHarness}), o
 * mostrador de Potentia à esquerda ({@code renderHoverHUD}) e as linhas da dica.
 */
public final class HoverClient {
    public static final KeyMapping KEY = new KeyMapping("key.thaumcraft.hover", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H, KeyMapping.Category.MISC);

    private static long humUntil;
    private static final Map<Integer, Long> SHOCK = new HashMap<>();
    private static boolean known;

    private HoverClient() {
    }

    public static void init() {
        KeyMappingHelper.registerKeyMapping(KEY);
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            LocalPlayer player = minecraft.player;
            if (player == null) {
                known = false;
                return;
            }
            if (minecraft.isPaused()) return;
            Hover.checkWorn(player);
            ItemStack armor = player.getItemBySlot(EquipmentSlot.CHEST);
            while (KEY.consumeClick()) {
                if (minecraft.gui.screen() == null && armor.getItem() instanceof HoverHarnessItem) toggle(player, armor);
            }
            if (armor.getItem() instanceof HoverHarnessItem && !player.getAbilities().instabuild) tick(player, armor);
            sparks(minecraft);
        });
        HudElementRegistry.addLast(Thaumcraft.id("hover_harness"), (graphics, tracker) -> hud(graphics));
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            if (!(stack.getItem() instanceof HoverHarnessItem harness)) return;
            Minecraft minecraft = Minecraft.getInstance();
            JarContents jar = stack.getOrDefault(TCComponents.HARNESS_JAR, ItemStack.EMPTY).get(TCComponents.JAR_CONTENTS);
            Aspect held = jar == null ? null : jar.heldAspect();
            if (held != null && minecraft.player != null) {
                lines.add(Knowledges.of(minecraft.player).hasDiscovered(held)
                        ? held.name().copy().append(" x " + jar.amount())
                        : Component.translatable("tc.aspect.unknown"));
            }
            lines.add(Component.translatable("tc.visdiscount").append(": " + harness.visDiscount(stack, null, null) + "%")
                    .withStyle(ChatFormatting.DARK_PURPLE));
            lines.add(Component.translatable("tc.visdiscount").append(" (Aer): " + harness.visDiscount(stack, null, Aspects.AIR) + "%")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        });
    }

    /** O {@code toggleHover} do lado de quem joga: avisa o servidor e toca o som de ligar ou desligar. */
    private static void toggle(Player player, ItemStack armor) {
        if (!Hover.toggleHover(player, armor)) return;
        boolean hover = Hover.getHover(player);
        ClientPlayNetworking.send(new Hover.Fly(hover));
        player.level().playLocalSound(player.getX(), player.getY(), player.getZ(),
                (hover ? TCSounds.HHON : TCSounds.HHOFF).value(), SoundSource.PLAYERS, 0.33f, 1.0f, false);
    }

    /** O {@code handleHoverArmor} do jogador local. */
    private static void tick(LocalPlayer player, ItemStack armor) {
        if (!known && armor.has(TCComponents.HOVER)) {
            boolean hover = Boolean.TRUE.equals(armor.get(TCComponents.HOVER));
            Hover.setHover(player, hover);
            ClientPlayNetworking.send(new Hover.Fly(hover));
        }
        known = true;
        boolean hover = Hover.getHover(player);
        player.getAbilities().flying = hover;
        if (hover && Hover.fuel(armor) > 0) {
            long now = System.currentTimeMillis();
            if (humUntil < now) {
                humUntil = now + 1200L;
                player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), TCSounds.JACOBS.value(),
                        SoundSource.PLAYERS, 0.05f, 1.0f + player.getRandom().nextFloat() * 0.05f, false);
            }
            float mod = Hover.speed(player);
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x * mod, motion.y, motion.z * mod);
        } else if (hover) {
            toggle(player, armor);
        }
    }

    /**
     * As faíscas do {@code ModelHoverHarness}: de quem paira, a cada 50 a 100 ms, um raio fino das costas até um bloco
     * a até seis blocos, numa direção qualquer para trás.
     */
    private static void sparks(Minecraft minecraft) {
        if (minecraft.level == null) return;
        long now = System.currentTimeMillis();
        RandomSource random = minecraft.level.getRandom();
        for (Player player : minecraft.level.players()) {
            ItemStack armor = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!(armor.getItem() instanceof HoverHarnessItem) || !Boolean.TRUE.equals(armor.get(TCComponents.HOVER))) continue;
            if (SHOCK.getOrDefault(player.getId(), 0L) >= now) continue;
            SHOCK.put(player.getId(), now + 50L + random.nextInt(50));
            float mod = player.isCrouching() ? 0.075f : 0.0f;
            // no 1.7.10 o y do jogador é o do olho, a 1,62 do chão
            double y = player.getY() + 1.62 - 0.45 - mod;
            float yaw = player.yBodyRot - 90.0f - random.nextInt(180);
            float pitch = -80 + random.nextInt(160);
            Vec3 from = new Vec3(player.getX(), y, player.getZ());
            Vec3 to = from.add(Vec3.directionFromRotation(pitch, yaw).scale(6.0));
            BlockHitResult hit = minecraft.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (hit.getType() != HitResult.Type.BLOCK) continue;
            Vec3 at = hit.getLocation();
            float body = (player.yBodyRot + 90.0f) / 180.0f * (float) Math.PI;
            LightningBolt bolt = new LightningBolt(player.getX() - Mth.cos(body) * 0.5f, y, player.getZ() - Mth.sin(body) * 0.5f,
                    at.x, at.y, at.z, random.nextLong(), 1, 2.0f, 3);
            bolt.defaultFractal();
            bolt.setType(6);
            bolt.setWidth(0.015f);
            bolt.finalizeBolt();
        }
    }

    /** O {@code renderHoverHUD}: o tubo de Potentia à esquerda, o ícone do arreio e, pairando, o brilho girando. */
    private static void hud(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.gui.screen() != null) return;
        ItemStack armor = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(armor.getItem() instanceof HoverHarnessItem)) return;
        int l = graphics.guiHeight();
        int level = Math.round(Hover.fuel(armor) / 64.0f * 48.0f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Sparkle.PARTICLES, 6, l / 2 + 24 - level, 224, 48 - level, 8, level, 256, 256, 0xFF00FFBF);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Sparkle.PARTICLES, 5, l / 2 - 28, 240, 0, 10, 56, 256, 256);
        if (Boolean.TRUE.equals(armor.get(TCComponents.HOVER))) {
            int frame = (int) (System.currentTimeMillis() % 700L) / 50;
            graphics.blit(RenderPipelines.GUI_TEXTURED, Sparkle.PARTICLES, 2, l / 2 - 43, 16 * frame, 32, 16, 16, 256, 256, 0xA8FFFFFF);
        }
        graphics.item(armor, 2, l / 2 - 43);
    }
}
