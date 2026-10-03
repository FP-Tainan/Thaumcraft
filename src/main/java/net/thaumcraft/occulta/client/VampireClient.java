package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.vampire.Vampire;
import net.thaumcraft.occulta.vampire.VampireHooks;
import net.thaumcraft.occulta.vampire.VampirePowers;
import org.lwjgl.glfw.GLFW;

/**
 * O que um vampiro <b>vê</b>: a barra de sangue e a tecla que escolhe o poder.
 *
 * <p>O original desenha a barra de sangue no canto e o nome do poder escolhido por cima dela, e os dois são
 * o painel de comando inteiro de um vampiro — ele não tem menu, não tem livro aberto, não tem roda. Tem um
 * número que desce e uma palavra que diz o que o clique vai fazer.
 *
 * <p>A barra <b>só aparece a quem é vampiro</b>, e some no instante em que ele deixa de ser.
 */
public final class VampireClient {
    /** A tecla que passa ao poder seguinte, e a mesma com Ctrl que liga a visão. */
    public static final KeyMapping TECLA = new KeyMapping("key.thaumcraft.vampire_power",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, KeyMapping.Category.MISC);

    /** Onde a barra fica, e de que tamanho. */
    public static final int MARGEM = 8;
    public static final int LARGURA = 80;
    public static final int ALTURA = 6;

    /** As cores dela: o sangue, o que falta, e a moldura. */
    public static final int SANGUE = 0xFF8B0000;
    public static final int VAZIO = 0xFF200000;
    public static final int MOLDURA = 0xFF000000;

    private VampireClient() {
    }

    public static void init() {
        KeyMappingHelper.registerKeyMapping(TECLA);
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            while (TECLA.consumeClick()) {
                if (minecraft.player == null || minecraft.gui.screen() != null) continue;
                if (!Vampire.é(minecraft.player)) continue;
                boolean visão = InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)
                        || InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL);
                ClientPlayNetworking.send(new VampireHooks.Escolha(visão));
            }
        });
        HudElementRegistry.addLast(Thaumcraft.id("vampire_blood"), (graphics, tracker) -> barra(graphics));
    }

    /** A barra de sangue, e o poder escolhido por cima dela. */
    private static void barra(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        var quem = minecraft.player;
        if (quem == null || !Vampire.é(quem)) return;

        int teto = Vampire.tetoDoSangue(quem);
        int tem = Vampire.sangueDe(quem);
        int cheio = teto <= 0 ? 0 : Math.round(LARGURA * (float) tem / teto);

        int x = MARGEM;
        int y = graphics.guiHeight() - MARGEM - ALTURA;
        graphics.fill(x - 1, y - 1, x + LARGURA + 1, y + ALTURA + 1, MOLDURA);
        graphics.fill(x, y, x + LARGURA, y + ALTURA, VAZIO);
        if (cheio > 0) graphics.fill(x, y, x + cheio, y + ALTURA, SANGUE);

        var poder = VampirePowers.escolhido(quem);
        Component diz = Component.translatable("tc.vampirepower." + poder.getSerializedName())
                .withStyle(ChatFormatting.DARK_RED);
        graphics.text(minecraft.font, diz, x, y - 11, 0xFFFFFFFF);
    }
}
