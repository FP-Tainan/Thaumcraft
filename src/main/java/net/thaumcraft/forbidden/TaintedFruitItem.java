package net.thaumcraft.forbidden;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thaumcraft.registry.TCEffects;
import net.thaumcraft.research.Warp;

/**
 * O Fruto Maculado: o {@code ItemFruitTainted} do Forbidden Magic 0.575.
 *
 * <p>Enche a barriga (quatro e oito décimos), mas o preço é caro: um ponto de distorção que gruda, meio minuto de
 * mácula e de fome — e, quatro vezes em dez, a taumarreia, com o aviso no chat.
 */
public class TaintedFruitItem extends Item {
    public TaintedFruitItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            Warp.addSticky(player, 1);
            player.addEffect(new MobEffectInstance(TCEffects.FLUX_TAINT, 600, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0, false, true));
            if (level.getRandom().nextFloat() < 0.4f) {
                player.sendSystemMessage(Component.translatable("warp.text.15").withStyle(ChatFormatting.DARK_PURPLE));
                player.addEffect(new MobEffectInstance(TCEffects.THAUMARHIA, 600, 0, true, true));
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
