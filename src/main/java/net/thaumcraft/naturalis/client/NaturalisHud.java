package net.thaumcraft.naturalis.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.naturalis.NaturalisItems;

/**
 * O que o Magia Naturalis 0.5.0 escreve na tela: o {@code renderSpectaclesHUD} e o {@code renderBuildFocusHUD}.
 *
 * <p>Com os Óculos no rosto e sem taumômetro na mão, o que estiver na mira ganha um nome no meio da tela — o tipo
 * e o feitio do nó de aura. E, com o Foco de Construção na varinha, o canto de cima mostra o bloco que ele vai
 * pôr, quantos ainda há, a forma e o tamanho da área.
 */
public final class NaturalisHud {
    private NaturalisHud() {
    }

    public static void init() {
        HudElementRegistry.addLast(Thaumcraft.id("spectacles"), (graphics, tracker) -> draw(graphics));
        HudElementRegistry.addLast(Thaumcraft.id("builder_focus"), (graphics, tracker) -> builder(graphics));
    }

    private static void draw(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.gui.screen() != null) return;
        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(NaturalisItems.SPECTACLES)) return;
        // com o taumômetro na mão quem fala é ele, como no original
        if (player.getMainHandItem().is(net.thaumcraft.registry.TCItems.THAUMOMETER)) return;
        HitResult hit = minecraft.hitResult;
        if (!(hit instanceof BlockHitResult block) || hit.getType() != HitResult.Type.BLOCK) return;

        var entity = player.level().getBlockEntity(block.getBlockPos());
        Component name = null;
        Component detail = null;
        if (entity instanceof net.thaumcraft.block.entity.NodeBlockEntity node) {
            name = Component.translatable("block.thaumcraft.node");
            String tipo = Component.translatable("tc.nodetype." + node.type().name().toLowerCase()).getString();
            detail = node.modifier() == null ? Component.literal(tipo)
                    : Component.literal(tipo + ", "
                    + Component.translatable("tc.nodemod." + node.modifier().name().toLowerCase()).getString());
        }
        if (name == null) return;

        int middleX = graphics.guiWidth() / 2;
        int middleY = graphics.guiHeight() / 2;
        graphics.text(minecraft.font, name, middleX - minecraft.font.width(name) / 2, middleY + 25, 0xFFF05A3C, true);
        if (detail != null) {
            graphics.text(minecraft.font, detail, middleX - minecraft.font.width(detail) / 2, middleY + 35, -1, true);
        }
    }

    /** O {@code renderBuildFocusHUD}: o bloco, a conta, a forma e o tamanho, no canto de cima. */
    private static void builder(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.gui.screen() != null) return;
        var held = player.getMainHandItem();
        if (!(held.getItem() instanceof net.thaumcraft.item.WandItem)) return;
        var focus = net.thaumcraft.item.WandItem.focusStack(held);
        if (!(focus.getItem() instanceof net.thaumcraft.item.FocusItem item) || !"build".equals(item.type())) return;

        var jeito = net.thaumcraft.naturalis.BuilderFocus.mode(focus);
        net.minecraft.world.level.block.Block bloco = null;
        if (jeito == net.thaumcraft.naturalis.BuilderFocus.Mode.UNIFORM) {
            if (minecraft.hitResult instanceof BlockHitResult mira && mira.getType() == HitResult.Type.BLOCK) {
                bloco = player.level().getBlockState(mira.getBlockPos()).getBlock();
            }
        } else {
            bloco = net.thaumcraft.naturalis.BuilderFocus.picked(focus);
        }

        int x = 49, y = 44;
        if (bloco != null && bloco != net.minecraft.world.level.block.Blocks.AIR) {
            var pilha = new net.minecraft.world.item.ItemStack(bloco);
            graphics.item(pilha, x, y);
            // quantos ainda há na mochila, ou o infinito de quem está no criativo
            Component conta = player.getAbilities().instabuild
                    ? Component.translatable("focus.build.infinite")
                    : Component.literal(String.valueOf(count(player, bloco)));
            graphics.text(minecraft.font, conta, x, y + 18, -1, true);
        } else {
            graphics.text(minecraft.font, Component.literal("?"), x + 6, y + 4, -1, true);
        }
        Component forma = Component.translatable("focus.build.shape")
                .append(": ")
                .append(Component.translatable("focus.build.shape."
                        + net.thaumcraft.naturalis.BuilderFocus.shape(focus).name().toLowerCase()));
        Component tamanho = Component.translatable("focus.build.size")
                .append(": " + net.thaumcraft.naturalis.BuilderFocus.size(focus));
        graphics.text(minecraft.font, forma, x, y - 20, -1, true);
        graphics.text(minecraft.font, tamanho, x, y - 10, -1, true);
        if (jeito == net.thaumcraft.naturalis.BuilderFocus.Mode.UNIFORM) {
            graphics.text(minecraft.font, Component.translatable("focus.build.mode.uniform")
                    .withStyle(ChatFormatting.AQUA), x, y + 28, -1, true);
        }
    }

    /** Quantos desses blocos há na mochila de quem constrói. */
    private static int count(Player player, net.minecraft.world.level.block.Block block) {
        int conta = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            var stack = player.getInventory().getItem(slot);
            if (stack.is(block.asItem())) conta += stack.getCount();
        }
        return conta;
    }
}
