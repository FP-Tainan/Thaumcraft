package net.thaumcraft.entity.taint;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.thaumcraft.registry.TCResources;

/**
 * O que as criaturas da mácula deixam (os {@code dropFewItems} delas): a gosma maculada e o ramo de mácula, soltos na
 * metade da altura de quem morreu.
 */
public final class TaintDrops {
    private TaintDrops() {
    }

    public static void goo(ServerLevel level, LivingEntity entity) {
        entity.spawnAtLocation(level, new ItemStack(TCResources.get("tainted_goo")), entity.getBbHeight() / 2.0f);
    }

    public static void tendril(ServerLevel level, LivingEntity entity) {
        entity.spawnAtLocation(level, new ItemStack(TCResources.get("taint_tendril")), entity.getBbHeight() / 2.0f);
    }

    /** Metade das vezes a gosma, metade o ramo. */
    public static void either(ServerLevel level, LivingEntity entity) {
        if (level.getRandom().nextBoolean()) goo(level, entity);
        else tendril(level, entity);
    }

    /** O nível de pilhagem de quem matou. */
    public static int looting(ServerLevel level, DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity killer)) return 0;
        return EnchantmentHelper.getEnchantmentLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), killer);
    }
}
