package net.thaumcraft.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.entity.BrainyZombieEntity;
import net.thaumcraft.registry.TCItems;
import net.thaumcraft.registry.TCResources;

/**
 * O resto do {@code livingDrops} do {@code EventHandlerEntity} da 4.2.3.5: o zumbi (que não o zangado) batido por alguém
 * deixa o cérebro uma vez em dez (mais com pilhagem), e o aldeão, uma moeda de ouro uma vez em dez.
 */
public final class MobDrops {
    private MobDrops() {
    }

    public static void init() {
        ServerLivingEntityEvents.AFTER_DEATH.register(MobDrops::drops);
    }

    private static void drops(LivingEntity entity, DamageSource source) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        int looting = 0;
        if (source.getEntity() instanceof LivingEntity killer) {
            var enchantments = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
            looting = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                    enchantments.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING), killer.getMainHandItem());
        }
        boolean recentlyHit = entity.getLastHurtByPlayer() != null;
        if (entity instanceof Zombie && !(entity instanceof BrainyZombieEntity) && recentlyHit && level.getRandom().nextInt(10) - looting < 1) {
            drop(level, entity, new ItemStack(TCItems.ZOMBIE_BRAIN));
        }
        if (entity instanceof Villager && level.getRandom().nextInt(10) - looting < 1) {
            drop(level, entity, new ItemStack(TCResources.get("gold_coin")));
        }
    }

    private static void drop(ServerLevel level, LivingEntity entity, ItemStack stack) {
        level.addFreshEntity(new ItemEntity(level, entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ(), stack));
    }
}
