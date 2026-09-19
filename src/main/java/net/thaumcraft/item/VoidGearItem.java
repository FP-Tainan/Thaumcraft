package net.thaumcraft.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.research.WarpEvents;

/**
 * O equipamento de metal do vazio ({@code ItemVoidSword}, {@code ItemVoidPickaxe}, {@code ItemVoidArmor}...): conserta-se
 * sozinho um ponto por segundo, distorce quem o usa (um por peça) e as ferramentas enfraquecem o que acertam (a espada por
 * três segundos, as outras por quatro).
 */
public class VoidGearItem extends Item implements WarpEvents.WarpingGear {
    private final int weakness;

    public VoidGearItem(Properties properties, int weakness) {
        super(properties);
        this.weakness = weakness;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (stack.isDamaged() && entity instanceof LivingEntity && entity.tickCount % 20 == 0) stack.setDamageValue(stack.getDamageValue() - 1);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        if (this.weakness > 0 && target.level() instanceof ServerLevel level && (!(target instanceof Player) || level.isPvpAllowed())) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, this.weakness));
        }
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return 1;
    }
}
