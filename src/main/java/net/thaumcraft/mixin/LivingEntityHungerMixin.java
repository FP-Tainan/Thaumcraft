package net.thaumcraft.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.registry.TCEffects;
import net.thaumcraft.registry.TCItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A fome estranha na hora de comer (o {@code finishedUsingItem} do {@code EventHandlerEntity}): carne podre ou cérebro de
 * zumbi a aliviam (um nível e trinta segundos a menos); qualquer outra comida só lembra que não adianta.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityHungerMixin {
    @Shadow
    protected ItemStack useItem;

    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void thaumcraft$unnaturalHunger(CallbackInfo info) {
        if (!((Object) this instanceof Player player) || player.level().isClientSide() || this.useItem.isEmpty()) return;
        MobEffectInstance pe = player.getEffect(TCEffects.UNNATURAL_HUNGER);
        if (pe == null) return;
        if (this.useItem.is(Items.ROTTEN_FLESH) || this.useItem.is(TCItems.ZOMBIE_BRAIN)) {
            int amp = pe.getAmplifier() - 1;
            int duration = pe.getDuration() - 600;
            player.removeEffect(TCEffects.UNNATURAL_HUNGER);
            if (duration > 0 && amp >= 0) net.thaumcraft.research.Incurable.add(player, new MobEffectInstance(TCEffects.UNNATURAL_HUNGER, duration, amp, true, true));
            player.sendSystemMessage(Component.translatable("warp.text.hunger.2").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.ITALIC));
        } else if (this.useItem.has(DataComponents.FOOD)) {
            player.sendSystemMessage(Component.translatable("warp.text.hunger.1").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
        }
    }
}
