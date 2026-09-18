package net.thaumcraft.client;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.client.fx.ShieldRunesFx;
import net.thaumcraft.client.fx.Sparkle;
import net.thaumcraft.event.RunicShield;
import org.joml.Matrix3x2fStack;

/**
 * O lado de quem joga do escudo rúnico: a barra ({@code renderRunicArmorBar} do {@code ClientTickEventsFML}), o
 * clarão ({@code PacketFXShield}) e a linha "Escudo rúnico +N" nas peças rúnicas.
 *
 * <p>A barra fica sobre a de armadura: um escudinho da folha de partículas por décimo de carga, cada um com uma runa
 * pequena por cima, dourada, pulsando.
 */
public final class RunicHud {
    private static int charge, max;

    private RunicHud() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(RunicShield.Charge.TYPE, (payload, context) -> {
            charge = payload.charge();
            max = payload.max();
        });
        ClientPlayNetworking.registerGlobalReceiver(RunicShield.Flash.TYPE, (payload, context) ->
                context.client().execute(() -> ShieldRunesFx.spawn(payload.source(), payload.target())));
        HudElementRegistry.addLast(Thaumcraft.id("runic_shield"), (graphics, tracker) -> draw(graphics));
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            int runic = RunicShield.finalCharge(stack);
            if (runic > 0) {
                lines.add(Math.min(1, lines.size()), Component.translatable("item.runic.charge").append(" +" + runic)
                        .withStyle(ChatFormatting.GOLD));
            }
        });
    }

    private static void draw(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || charge <= 0 || max <= 0 || player.isCreative() || player.isSpectator()) return;
        float fill = charge / (float) max;
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(graphics.guiWidth() / 2.0f - 91.0f, graphics.guiHeight() - 39.0f);
        for (int a = 0; a < fill * 10.0f; a++) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, Sparkle.PARTICLES, a * 8, 0, 160, 16, 9, 9, 256, 256);
            pose.pushMatrix();
            pose.scale(0.5f, 0.5f);
            float alpha = Mth.sin(player.tickCount / 4.0f + a) * 0.4f + 0.6f;
            int colour = (int) (Mth.clamp(alpha, 0.0f, 1.0f) * 255.0f) << 24 | 0xFFBF3D;
            graphics.blit(RenderPipelines.GUI_TEXTURED, Sparkle.PARTICLES, a * 16, 0, a * 16, 96, 16, 16, 256, 256, colour);
            pose.popMatrix();
        }
        pose.popMatrix();
    }
}
