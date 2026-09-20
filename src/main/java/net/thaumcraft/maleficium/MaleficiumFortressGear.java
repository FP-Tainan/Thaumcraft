package net.thaumcraft.maleficium;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.wands.VisDiscountGear;
import net.thaumcraft.item.FortressArmorItem;
import net.thaumcraft.research.WarpEvents;
import org.jetbrains.annotations.Nullable;

/**
 * As duas armaduras de fortaleza do Maleficium — a do vazio e a das sombras.
 *
 * <p>No original elas herdam o {@code ItemFortressArmor} do Thaumcraft, e por isso usam o mesmo modelo, aceitam as
 * mesmas máscaras e os mesmos óculos por infusão e contam no conjunto. Aqui é o mesmo: só muda a folha de textura,
 * o desconto de vis, a distorção e o conserto sozinho.
 */
public class MaleficiumFortressGear extends FortressArmorItem implements VisDiscountGear, WarpEvents.WarpingGear {
    private final int discount;
    private final int warp;

    public MaleficiumFortressGear(int discount, int warp, Properties properties) {
        super(properties);
        this.discount = discount;
        this.warp = warp;
    }

    @Override
    public int visDiscount(ItemStack stack, Player player, @Nullable Aspect aspect) {
        return this.discount;
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return this.warp;
    }

    /** O {@code onUpdate} do original: um ponto de conserto por segundo. */
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (entity.tickCount % 20 != 0 || !stack.isDamaged()) return;
        stack.setDamageValue(stack.getDamageValue() - 1);
    }
}
