package net.thaumcraft.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.item.FortressArmorItem;
import net.thaumcraft.registry.TCComponents;

/**
 * As máscaras do elmo de fortaleza no combate: o trecho delas no {@code EventHandlerRunic.entityHurt} da 4.2.3.5.
 *
 * <p>O demônio que bebe (2) devolve um de vida a quem bate, com chance de dano/12; o fantasma irado (1) seca quem
 * acerta quem o usa, quatro segundos de Wither, com chance de dano/10. O diabo sorridente (0) atenua a Distorção,
 * que chega com ela.
 */
public final class FortressMasks {
    private FortressMasks() {
    }

    public static void init() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
            if (source.getEntity() instanceof Player leecher && mask(leecher) == 2
                    && leecher.getRandom().nextFloat() < baseDamage / 12.0f) {
                leecher.heal(1.0f);
            }
            if (entity instanceof Player player && source.getEntity() instanceof LivingEntity attacker
                    && mask(player) == 1 && player.getRandom().nextFloat() < baseDamage / 10.0f) {
                attacker.addEffect(new MobEffectInstance(MobEffects.WITHER, 80));
            }
        });
    }

    /** A máscara do elmo de fortaleza que a pessoa veste, ou −1. */
    public static int mask(Player player) {
        ItemStack helm = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(helm.getItem() instanceof FortressArmorItem)) return -1;
        Integer mask = helm.get(TCComponents.FORTRESS_MASK);
        return mask == null ? -1 : mask;
    }
}
