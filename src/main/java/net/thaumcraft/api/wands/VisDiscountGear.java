package net.thaumcraft.api.wands;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.Aspect;
import org.jetbrains.annotations.Nullable;

/**
 * O {@code IVisDiscountGear} da 4.2.3.5: o que, vestido, faz a varinha gastar menos vis — em pontos percentuais,
 * somados de todas as peças e tirados do multiplicador da ponteira.
 */
public interface VisDiscountGear {
    int visDiscount(ItemStack stack, Player player, @Nullable Aspect aspect);
}
