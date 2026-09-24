package net.thaumcraft.naturalis;

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
 * Os Óculos: o {@code SpectaclesItem} do Magia Naturalis 0.5.0. Revelam os nós como os Óculos da Revelação, dão
 * seis por cento de desconto de vis e, no rosto, escrevem no meio da tela o que é o bloco para o qual se olha —
 * o tipo do nó, ou de quem é o baú.
 */
public class SpectaclesItem extends Item implements VisDiscountGear {
    public SpectaclesItem(Properties properties) {
        super(properties);
    }

    @Override
    public int visDiscount(ItemStack stack, @Nullable Player player, @Nullable Aspect aspect) {
        return 6;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("tc.visdiscount").append(": " + this.visDiscount(stack, null, null) + "%")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
