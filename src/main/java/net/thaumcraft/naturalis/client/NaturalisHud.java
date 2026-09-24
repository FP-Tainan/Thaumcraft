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
 * O que os Óculos escrevem na tela: o {@code renderSpectaclesHUD} do Magia Naturalis 0.5.0.
 *
 * <p>Com eles no rosto e sem taumômetro na mão, o que estiver na mira ganha um nome no meio da tela — o tipo e o
 * feitio do nó de aura, ou de quem é o baú arcano.
 */
public final class NaturalisHud {
    private NaturalisHud() {
    }

    public static void init() {
        HudElementRegistry.addLast(Thaumcraft.id("spectacles"), (graphics, tracker) -> draw(graphics));
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
}
