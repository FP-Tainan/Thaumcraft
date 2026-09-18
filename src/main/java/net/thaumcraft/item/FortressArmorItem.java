package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.thaumcraft.registry.TCComponents;

import java.util.function.Consumer;

/**
 * A armadura de fortaleza de táumio: o {@code ItemFortressArmor} da 4.2.3.5. O desenho é o
 * {@code FortressArmorRenderer}; o elmo aceita, por infusão, os óculos da revelação e uma de três máscaras.
 */
public class FortressArmorItem extends Item {
    public FortressArmorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        if (Boolean.TRUE.equals(stack.get(TCComponents.FORTRESS_GOGGLES))) {
            lines.accept(Component.translatable("item.thaumcraft.goggles").withStyle(ChatFormatting.DARK_PURPLE));
        }
        Integer mask = stack.get(TCComponents.FORTRESS_MASK);
        if (mask != null) lines.accept(Component.translatable("item.thaumcraft.fortress_helmet.mask." + mask).withStyle(ChatFormatting.GOLD));
    }
}
