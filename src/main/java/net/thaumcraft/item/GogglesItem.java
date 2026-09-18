package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.wands.VisDiscountGear;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Os óculos da revelação: o {@code ItemGoggles} da 4.2.3.5. Mostram os nós e o que guardam os recipientes, e dão
 * cinco por cento de desconto de vis.
 */
public class GogglesItem extends Item implements VisDiscountGear {
    public GogglesItem(Properties properties) {
        super(properties);
    }

    @Override
    public int visDiscount(ItemStack stack, @Nullable Player player, @Nullable Aspect aspect) {
        return 5;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("tc.visdiscount").append(": " + this.visDiscount(stack, null, null) + "%")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
