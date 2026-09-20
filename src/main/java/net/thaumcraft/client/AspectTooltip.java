package net.thaumcraft.client;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.AspectList;
import net.thaumcraft.api.aspects.ObjectAspects;
import net.thaumcraft.research.Knowledges;
import net.thaumcraft.research.PlayerKnowledge;

/**
 * De que a coisa na mão é feita, escrito na dica do item.
 *
 * <p>É um dos gestos mais conhecidos do Thaumcraft: segurar o agachar sobre qualquer item mostra os
 * aspectos dele. E vale a mesma regra do thaumômetro — só se lê o que já se descobriu; o resto aparece
 * como um risco de interrogação.
 */
public final class AspectTooltip {
    private AspectTooltip() {
    }

    public static void init() {
        // o jarro cheio diz o que leva, sempre, como o ItemJarFilled do original: "Nome x quantidade", e o
        // rótulo em roxo. Aspecto que o jogador não descobriu aparece como desconhecido
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            net.thaumcraft.item.JarContents jar = stack.get(net.thaumcraft.registry.TCComponents.JAR_CONTENTS);
            Minecraft minecraft = Minecraft.getInstance();
            if (jar == null || minecraft.player == null) return;
            PlayerKnowledge knowledge = Knowledges.of(minecraft.player);
            Aspect held = jar.heldAspect();
            if (held != null) {
                lines.add(knowledge.hasDiscovered(held)
                        ? held.name().copy().append(" x " + jar.amount())
                        : Component.translatable("tc.aspect.unknown"));
            }
            Aspect label = jar.labelAspect();
            if (label != null) {
                lines.add((knowledge.hasDiscovered(label) ? label.name().copy()
                        : Component.translatable("tc.aspect.unknown")).withStyle(ChatFormatting.DARK_PURPLE));
            }
        });

        // o rótulo marcado diz o aspecto que leva, em roxo como o do jarro
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            String marked = stack.get(net.thaumcraft.registry.TCComponents.LABEL_ASPECT);
            Minecraft minecraft = Minecraft.getInstance();
            if (marked == null || minecraft.player == null) return;
            Aspect aspect = Aspect.of(marked);
            if (aspect == null) return;
            lines.add((Knowledges.of(minecraft.player).hasDiscovered(aspect) ? aspect.name().copy()
                    : Component.translatable("tc.aspect.unknown")).withStyle(ChatFormatting.DARK_PURPLE));
        });

        // o gesto do original: agachando sobre uma casa de qualquer tela, os símbolos do que aquilo é feito
        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> container)) return;
            net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.afterExtract(screen)
                    .register((self, graphics, mouseX, mouseY, partial) -> aspectsOnCursor(container, graphics, mouseX, mouseY));
        });
    }

    /** O {@code renderAspectsInGui} do {@code ClientTickEventsFML}: uma fileira de símbolos acima e à direita do cursor. */
    private static void aspectsOnCursor(net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen,
                                        net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        boolean shift = com.mojang.blaze3d.platform.InputConstants.isKeyDown(minecraft.getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT)
                || com.mojang.blaze3d.platform.InputConstants.isKeyDown(minecraft.getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT);
        if (!shift) return;
        var slot = ((net.thaumcraft.mixin.ContainerScreenHoverMixin) screen).thaumcraft$hoveredSlot();
        if (slot == null || !slot.hasItem()) return;
        net.minecraft.world.item.ItemStack stack = slot.getItem();
        PlayerKnowledge knowledge = Knowledges.of(minecraft.player);
        // como no original, só se lê o que já foi examinado com o thaumômetro
        if (!knowledge.hasScanned(net.thaumcraft.research.ScanManager.keyOf(stack))) return;
        AspectList aspects = ObjectAspects.of(stack);
        if (aspects.isEmpty()) return;
        int ticks = minecraft.player.tickCount;
        int index = 0;
        for (Aspect aspect : aspects.getAspectsSortedAmount()) {
            int x = mouseX + 9 + index * 18;
            int y = mouseY - 34;
            var pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(x - 2, y - 2);
            pose.scale(1.25f, 1.25f);
            graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, BACK, 0, 0, 0, 0, 16, 16, 16, 16);
            pose.popMatrix();
            if (knowledge.hasDiscovered(aspect)) {
                net.thaumcraft.client.gui.AspectTags.draw(graphics, minecraft.font, x, y, aspect, aspects.getAmount(aspect), 0, 1.0f, false, ticks);
            } else {
                graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, UNKNOWN, x, y, 0, 0, 16, 16, 16, 16);
            }
            index++;
        }
    }

    private static final net.minecraft.resources.Identifier BACK = net.thaumcraft.Thaumcraft.id("textures/aspects/_back.png");
    private static final net.minecraft.resources.Identifier UNKNOWN = net.thaumcraft.Thaumcraft.id("textures/aspects/_unknown.png");
}
