package net.thaumcraft.naturalis;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.research.WarpEvents;
import org.jetbrains.annotations.Nullable;

/**
 * A Foice do Vazio: o {@code VoidSickleItem} do Magia Naturalis 0.5.0. Como toda coisa de metal do vazio, ela se
 * conserta sozinha um ponto por segundo e enfraquece por quatro segundos quem ela acerta — e distorce um pouco
 * quem a carrega.
 */
public class VoidSickleItem extends SickleItem implements WarpEvents.WarpingGear {
    public VoidSickleItem(Properties properties) {
        super(properties, 4, 0, false);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (target.level() instanceof ServerLevel level
                && (!(target instanceof Player) || !(attacker instanceof Player) || level.isPvpAllowed())) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (stack.isDamaged() && entity.tickCount % 20 == 0) stack.setDamageValue(stack.getDamageValue() - 1);
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return 1;
    }
}
