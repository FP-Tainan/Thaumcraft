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

        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) return;
            // o gesto do original: só aparece enquanto se segura o agachar
            boolean crouching = com.mojang.blaze3d.platform.InputConstants.isKeyDown(
                            minecraft.getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT)
                    || com.mojang.blaze3d.platform.InputConstants.isKeyDown(
                            minecraft.getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT);
            if (!crouching) return;

            AspectList aspects = ObjectAspects.of(stack);
            if (aspects.isEmpty()) return;

            PlayerKnowledge knowledge = Knowledges.of(minecraft.player);
            StringBuilder known = new StringBuilder();
            int unknown = 0;
            for (Aspect aspect : aspects.getAspectsSortedAmount()) {
                if (!knowledge.hasDiscovered(aspect)) {
                    unknown++;
                    continue;
                }
                if (!known.isEmpty()) known.append("  ");
                known.append(aspect.name().getString()).append(' ').append(aspects.getAmount(aspect));
            }

            if (!known.isEmpty()) {
                // cada aspecto na cor dele seria o ideal; numa linha só, a cor do mais forte serve de tom
                Aspect strongest = aspects.getAspectsSortedAmount().get(0);
                lines.add(Component.literal(known.toString())
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(strongest.color()))));
            }
            if (unknown > 0) {
                lines.add(Component.translatable("tc.tooltip.unknown", unknown)
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        });
    }
}
