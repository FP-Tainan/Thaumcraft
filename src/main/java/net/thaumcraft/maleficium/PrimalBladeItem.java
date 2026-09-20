package net.thaumcraft.maleficium;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.registry.TCSounds;
import net.thaumcraft.research.WarpEvents;

/**
 * A Lâmina Primordial: o {@code ItemPrimalBlade} do Tainted Magic 8.1.1. Fere como nenhuma outra, põe quem ela acerta
 * a definhar e a enfraquecer, conserta-se sozinha um ponto por segundo — e, com o clique direito seguro, abre um
 * redemoinho que puxa para si tudo o que estiver a quinze blocos, ferindo de três em três o que chegar perto.
 */
public class PrimalBladeItem extends Item implements WarpEvents.WarpingGear {
    public PrimalBladeItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
        attacker.level().playSound(null, attacker, TCSounds.SWING.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    /** O {@code onUsingTick}: o redemoinho de quinze blocos. */
    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
        if (player.tickCount % 10 == 0) {
            level.playSound(null, player, TCSounds.BRAIN.value(), SoundSource.PLAYERS, 0.05f, 0.5f);
        }
        for (Entity entity : level.getEntities(player, player.getBoundingBox().inflate(15.0))) {
            if (entity == player || !entity.isAlive() || entity.isInvulnerable()) continue;
            if (level instanceof ServerLevel server && entity.position().distanceTo(player.position()) < 2.0) {
                entity.hurtServer(server, level.damageSources().magic(), 3.0f);
            }
            Vec3 pull = player.position().add(0.5, 0.5, 0.5).subtract(entity.position()).scale(1.0 / 20.0);
            double length = pull.length();
            double force = 1.0 - length;
            if (force <= 0.0 || length == 0.0) continue;
            force *= force;
            entity.setDeltaMovement(entity.getDeltaMovement().add(
                    pull.x / length * force * 0.2, pull.y / length * force * 0.3, pull.z / length * force * 0.2));
            entity.hurtMarked = true;
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (stack.isDamaged() && entity.tickCount % 20 == 0) stack.setDamageValue(stack.getDamageValue() - 1);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display,
                                java.util.function.Consumer<net.minecraft.network.chat.Component> lines,
                                net.minecraft.world.item.TooltipFlag flag) {
        lines.accept(net.minecraft.network.chat.Component.translatable("text.sapprimal")
                .withStyle(net.minecraft.ChatFormatting.GOLD));
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return 5;
    }
}
