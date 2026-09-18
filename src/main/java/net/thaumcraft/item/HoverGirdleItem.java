package net.thaumcraft.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.baubles.BaubleItem;
import net.thaumcraft.api.baubles.BaubleType;

/**
 * O cinturão taumostático: o {@code ItemGirdleHover} da 4.2.3.5. Vai no cinto; amortece a queda (um terço de bloco
 * por tique) e, com o arreio, deixa o voo mais rápido e a Potentia render mais (veja o {@code Hover}).
 */
public class HoverGirdleItem extends Item implements BaubleItem {
    public HoverGirdleItem(Properties properties) {
        super(properties);
    }

    @Override
    public BaubleType baubleType(ItemStack stack) {
        return BaubleType.BELT;
    }

    @Override
    public void onWornTick(ItemStack stack, LivingEntity wearer) {
        if (wearer.fallDistance > 0.0) wearer.fallDistance -= 0.33f;
    }
}
