package net.thaumcraft.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.thaumcraft.research.WarpEvents;

import java.util.function.Consumer;

/**
 * A lâmina carmesim: o {@code ItemCrimsonSword} da 4.2.3.5, a espada dos cultistas. O golpe enfraquece (três segundos) e
 * dá fome (seis) — a "Grande Sangria" —, conserta-se sozinha um ponto por segundo e distorce quem a carrega (dois).
 */
public class CrimsonSwordItem extends Item implements WarpEvents.WarpingGear {
    public CrimsonSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (target.level() instanceof ServerLevel level && (!(target instanceof Player) || !(attacker instanceof Player) || level.isPvpAllowed())) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60));
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (stack.isDamaged() && entity instanceof LivingEntity && entity.tickCount % 20 == 0) stack.setDamageValue(stack.getDamageValue() - 1);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("enchantment.special.sapgreat").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return 2;
    }
}
