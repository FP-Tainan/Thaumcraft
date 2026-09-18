package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.wands.VisDiscountGear;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Os mantos do taumaturgo: o {@code ItemRobeArmor} da 4.2.3.5. Pouca proteção, tingíveis como o couro, e um
 * desconto de vis — dois por cento no peito e na calça, um nas botas. Consertam com tecido encantado.
 */
public class RobeItem extends Item implements VisDiscountGear {
    private final ArmorType type;

    public RobeItem(ArmorType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public int visDiscount(ItemStack stack, Player player, @Nullable Aspect aspect) {
        return this.type == ArmorType.BOOTS ? 1 : 2;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("tc.visdiscount").append(": " + this.visDiscount(stack, null, null) + "%")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
