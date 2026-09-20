package net.thaumcraft.maleficium;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.wands.VisDiscountGear;
import net.thaumcraft.research.WarpEvents;
import org.jetbrains.annotations.Nullable;

/**
 * As roupas do Maleficium que só têm números: os dois óculos e as duas armaduras de fortaleza.
 *
 * <p>Todas se consertam sozinhas um ponto por segundo (o {@code onUpdate} do original), descontam vis e distorcem
 * quem as veste. Os óculos revelam o que está por trás do mundo pela etiqueta {@code thaumcraft:revealing}.
 */
public class MaleficiumGear extends Item implements VisDiscountGear, WarpEvents.WarpingGear {
    private final int discount;
    private final int warp;
    private final boolean selfRepair;

    public MaleficiumGear(int discount, int warp, boolean selfRepair, Properties properties) {
        super(properties);
        this.discount = discount;
        this.warp = warp;
        this.selfRepair = selfRepair;
    }

    @Override
    public int visDiscount(ItemStack stack, Player player, @Nullable Aspect aspect) {
        return this.discount;
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return this.warp;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!this.selfRepair || entity.tickCount % 20 != 0 || !stack.isDamaged()) return;
        stack.setDamageValue(stack.getDamageValue() - 1);
    }
}
