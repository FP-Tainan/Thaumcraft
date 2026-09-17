package net.thaumcraft.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.item.WandItem;

/**
 * Quanto vis a varinha na mão ainda tem.
 *
 * <p>No mod original isso fica no canto de baixo à esquerda: os seis símbolos primários numa fileira, cada
 * um com uma barrinha que enche conforme o que a varinha guarda daquele aspecto. Aqui é a mesma coisa.
 */
public final class WandHud {
    /** O símbolo e a barra de cada aspecto ocupam esta largura. */
    private static final int STEP = 17;
    private static final int ICON = 12;

    private WandHud() {
    }

    public static void init() {
        HudElementRegistry.addLast(Thaumcraft.id("wand_vis"), (graphics, tracker) -> draw(graphics));
    }

    private static void draw(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof WandItem)) stack = player.getOffhandItem();
        if (!(stack.getItem() instanceof WandItem)) return;

        int max = WandItem.maxVis(stack);
        if (max <= 0) return;
        int left = 6;
        int top = graphics.guiHeight() - 24;
        int index = 0;
        for (Aspect aspect : Aspects.primals()) {
            int have = WandItem.vis(stack, aspect);
            int x = left + index * STEP;
            // o símbolo, apagado quando aquele aspecto está no fim
            float share = have / (float) max;
            int shade = 0x40 + (int) (share * 0xBF);
            graphics.blit(RenderPipelines.GUI_TEXTURED, aspect.image(), x, top, 0, 0, ICON, ICON, ICON, ICON,
                    shade << 24 | aspect.color());
            // a barrinha embaixo, que enche com o que a varinha tem
            int height = Math.max(0, Math.round(share * 6));
            graphics.fill(x, top + ICON + 1, x + ICON, top + ICON + 3, 0x80000000);
            if (height > 0) {
                graphics.fill(x, top + ICON + 1, x + Math.round(share * ICON), top + ICON + 3,
                        0xFF000000 | aspect.color());
            }
            index++;
        }
    }
}
