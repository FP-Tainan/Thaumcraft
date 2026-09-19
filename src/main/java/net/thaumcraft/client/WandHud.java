package net.thaumcraft.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.api.wands.WandParts;
import net.thaumcraft.item.FocusItem;
import net.thaumcraft.item.Focuses;
import net.thaumcraft.item.WandItem;
import org.joml.Matrix3x2fStack;

/**
 * O mostrador de vis da varinha. É o {@code renderCastingWandHud} do {@code ClientTickEventsFML} da
 * 4.2.3.5, descompilado.
 *
 * <p>No canto de cima à esquerda fica um anel de latão, e em volta dele, em leque, seis frascos de vidro —
 * um por aspecto primário, de vinte e quatro em vinte e quatro graus. Cada frasco enche na cor do seu
 * aspecto conforme o que a varinha guarda. No meio do anel, o foco preso.
 *
 * <p>Sobre cada frasco podem aparecer marcas: um pingo laranja quando o foco gasta aquele aspecto, e uma
 * seta — verde subindo, vermelha descendo — quando o frasco mudou desde o último segundo, que é o que se
 * vê ao beber de um nó ou ao lançar uma magia. Agachado, aparecem os números: quanto há, e quanto o foco
 * cobra, já com o desconto das pontas.
 *
 * <p>Eu tinha feito isto como uma fileira de símbolos no canto de baixo, achando que era assim no
 * original. Não era.
 */
public final class WandHud {
    private static final Identifier HUD = Thaumcraft.id("textures/gui/hud.png");
    private static final int SHEET = 256;

    /** O que cada frasco tinha no último segundo, para as setas; e quando ele foi medido. */
    private static AspectList before;
    private static int beforeSlot = -1;
    private static long nextSync;

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
        if (!(stack.getItem() instanceof WandItem)) return;

        int max = WandItem.maxVis(stack);
        if (max <= 0) return;
        AspectList vis = WandItem.vis(stack);

        // o que havia no último segundo, para as setas de subindo e descendo
        long now = net.minecraft.util.Util.getMillis();
        int slot = player.getInventory().getSelectedSlot();
        if (before == null || slot != beforeSlot || nextSync <= now) {
            before = new AspectList(vis);
            beforeSlot = slot;
            nextSync = now + 1000L;
        }

        FocusItem focus = Focuses.on(stack);
        WandParts.Cap cap = WandItem.cap(stack);
        Matrix3x2fStack pose = graphics.pose();

        pose.pushMatrix();
        // o anel
        pose.pushMatrix();
        pose.scale(0.5f, 0.5f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, HUD, 0, 0, 0, 0, 64, 64, SHEET, SHEET);
        pose.popMatrix();
        pose.translate(16.0f, 16.0f);

        int count = 0;
        for (Aspect aspect : Aspects.primals()) {
            int amount = vis.getAmount(aspect);
            pose.pushMatrix();
            // o mostrador no alto: o leque gira um quarto de volta para descer pela borda da tela
            pose.rotate((float) Math.toRadians(90.0f));
            pose.rotate((float) Math.toRadians(-15.0f + count * 24.0f));
            pose.translate(0.0f, -32.0f);
            pose.scale(0.5f, 0.5f);

            // o que o frasco tem, na cor do aspecto
            int level = (int) (30.0f * amount / max);
            if (level > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, HUD, -4, 35 - level, 104, 0, 8, level, SHEET, SHEET,
                        0xCC000000 | aspect.color());
            }
            // o vidro por cima
            graphics.blit(RenderPipelines.GUI_TEXTURED, HUD, -8, -3, 72, 0, 16, 42, SHEET, SHEET);

            int shift = 0;
            if (focus != null && focus.cost(WandItem.focusStack(stack)).getAmount(aspect) > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, HUD, -4, -8, 136, 0, 8, 8, SHEET, SHEET);
                shift = 8;
            }
            int earlier = before.getAmount(aspect);
            if (earlier > amount) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, HUD, -4, -8 - shift, 128, 0, 8, 8, SHEET, SHEET);
            } else if (earlier < amount) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, HUD, -4, -8 - shift, 120, 0, 8, 8, SHEET, SHEET);
            }

            if (player.isShiftKeyDown()) {
                pose.pushMatrix();
                pose.rotate((float) Math.toRadians(-90.0f));
                graphics.text(minecraft.font, Integer.toString(amount / WandItem.VIS_UNIT), -32, -4, 0xFFFFFFFF);
                if (focus != null && focus.cost(WandItem.focusStack(stack)).getAmount(aspect) > 0) {
                    float each = focus.cost(WandItem.focusStack(stack)).getAmount(aspect) * WandItem.focusModifier(stack, player, aspect) / 100.0f;
                    graphics.text(minecraft.font, new java.text.DecimalFormat("#######.##").format(each),
                            8, -4, 0xFFFFFFFF);
                }
                pose.popMatrix();
            }
            pose.popMatrix();
            count++;
        }
        pose.popMatrix();

        // o foco preso, no meio do anel
        if (focus != null) {
            graphics.item(new ItemStack(focus), 8, 8);
        }
    }
}
