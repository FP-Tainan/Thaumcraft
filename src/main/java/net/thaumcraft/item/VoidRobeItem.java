package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.wands.VisDiscountGear;
import net.thaumcraft.research.WarpEvents;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A armadura de manto do vazio: o {@code ItemVoidRobeArmor} da 4.2.3.5. A proteção do metal do vazio num manto de
 * tecido encantado, tingível (roxo de fábrica); cinco por cento de desconto de vis por peça, dois de distorção, conserta
 * sozinha um ponto por segundo, e o capuz tem os óculos da revelação embutidos.
 */
public class VoidRobeItem extends Item implements VisDiscountGear, WarpEvents.WarpingGear {
    /** O {@code getColor} sem tinta: 6961280. */
    public static final int UNDYED = 0xFF000000 | 6961280;

    public VoidRobeItem(Properties properties) {
        super(properties);
    }

    @Override
    public int visDiscount(ItemStack stack, Player player, @Nullable Aspect aspect) {
        return 5;
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return 2;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (stack.isDamaged() && entity instanceof LivingEntity && entity.tickCount % 20 == 0) stack.setDamageValue(stack.getDamageValue() - 1);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("tc.visdiscount").append(": " + this.visDiscount(stack, null, null) + "%")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
