package net.thaumcraft.maleficium;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.research.Knowledges;

/**
 * O cogumelo mágico: o {@code ItemMagicFunguar} do Tainted Magic 8.1.1. Come-se depressa, mata três de fome, cura por
 * três segundos, apressa por cinco — e ensina um ponto de pesquisa de um dos seis primários, sorteado na hora.
 */
public class MagicFunguarItem extends Item {
    public MagicFunguarItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack rest = super.finishUsingItem(stack, level, entity);
        if (!(entity instanceof Player player)) return rest;
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1));
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 2));
        if (level.isClientSide()) return rest;
        java.util.List<Aspect> primals = Aspects.primals();
        Aspect aspect = primals.get(level.getRandom().nextInt(primals.size()));
        var knowledge = Knowledges.of(player);
        knowledge.pool().add(aspect, 1);
        if (player instanceof ServerPlayer server) {
            net.thaumcraft.net.TCNetwork.aspectPool(server, aspect, 1, knowledge.points(aspect));
        }
        Knowledges.save(player, knowledge);
        return rest;
    }
}
