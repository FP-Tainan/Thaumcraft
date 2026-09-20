package net.thaumcraft.maleficium.client;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.maleficium.FortressBladeItem;
import net.thaumcraft.registry.TCComponents;
import org.joml.Matrix3x2fStack;

/**
 * O que o Maleficium desenha por cima da tela: o Medidor Rúnico da lâmina de fortaleza (o {@code renderHUD} do
 * {@code ItemKatana}) e a linha "Tocada pelo Vazio" nas armaduras que o frasco de sangue tocou.
 *
 * <p>São dezesseis runas da folha de escrita do Thaumcraft; elas acendem da esquerda para a direita enquanto o golpe
 * carrega, e da direita para a esquerda enquanto a lâmina descansa.
 */
public final class MaleficiumHud {
    private static final Identifier SCRIPT = Thaumcraft.id("textures/misc/script.png");
    /** Cada runa tem dezesseis por dezesseis numa folha de duzentos e cinquenta e seis por dezesseis. */
    private static final int RUNE = 16;

    private MaleficiumHud() {
    }

    public static void init() {
        HudElementRegistry.addLast(Thaumcraft.id("fortress_blade_gauge"), (graphics, tracker) -> draw(graphics));
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            if (Boolean.TRUE.equals(stack.get(TCComponents.VOID_TOUCHED))) {
                lines.add(Math.min(1, lines.size()),
                        Component.translatable("text.voidtouched").withStyle(ChatFormatting.DARK_PURPLE));
            }
        });
    }

    private static void draw(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof FortressBladeItem)) return;

        int cooldown = FortressBladeItem.cooldown(held);
        boolean resting = cooldown > 0;
        float fill = resting
                ? cooldown / (float) FortressBladeItem.COOLDOWN_TICKS
                : Math.min(player.getUseItemRemainingTicks() > 0
                        ? (held.getUseDuration(player) - player.getUseItemRemainingTicks()) / (float) FortressBladeItem.CHARGE_TICKS
                        : 0.0f, 1.0f);

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(graphics.guiWidth() / 2.0f + 10.0f, graphics.guiHeight() - (player.isCreative() ? 33.0f : 48.0f));
        pose.scale(0.315f, 0.315f);
        for (int rune = 0; rune < 16; rune++) {
            float red = Mth.sin((player.tickCount + rune * 5) / 5.0f) * 0.1f + 0.8f;
            float green = Mth.sin((player.tickCount + rune * 5) / 7.0f) * 0.1f + 0.7f;
            float alpha = Mth.sin((player.tickCount + rune * 5) / 10.0f) * 0.3f;
            graphics.blit(RenderPipelines.GUI_TEXTURED, SCRIPT, rune * RUNE, 0, rune * RUNE, 0, RUNE, RUNE, 256, 16,
                    colour(red, green, 0.4f, alpha + 0.7f));
            if (Math.ceil(16.0f * fill) <= rune) continue;
            // a parte já cheia vai do vermelho ao verde — ao contrário, quando a lâmina está descansando
            float hue = (resting ? 16.0f - rune : rune) / 16.0f * 0.3f;
            int filled = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0xFFFFFF;
            graphics.blit(RenderPipelines.GUI_TEXTURED, SCRIPT, rune * RUNE, 0, rune * RUNE, 0, RUNE, RUNE, 256, 16,
                    (int) (Mth.clamp(0.9f + alpha, 0.0f, 1.0f) * 255.0f) << 24 | filled);
        }
        pose.popMatrix();
    }

    private static int colour(float red, float green, float blue, float alpha) {
        return (int) (Mth.clamp(alpha, 0.0f, 1.0f) * 255.0f) << 24
                | (int) (Mth.clamp(red, 0.0f, 1.0f) * 255.0f) << 16
                | (int) (Mth.clamp(green, 0.0f, 1.0f) * 255.0f) << 8
                | (int) (Mth.clamp(blue, 0.0f, 1.0f) * 255.0f);
    }
}
