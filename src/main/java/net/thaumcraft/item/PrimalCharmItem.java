package net.thaumcraft.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.api.aspects.Aspect;
import net.thaumcraft.api.aspects.Aspects;
import net.thaumcraft.entity.AspectOrbEntity;
import org.jetbrains.annotations.Nullable;

/**
 * O amuleto primordial: o {@code ItemResource} de número 15 da 4.2.3.5. Carregado, de vez em quando (20 em 20000 por
 * tique) solta um orbe de um aspecto primordial qualquer.
 */
public class PrimalCharmItem extends Item {
    private static final Aspect[] PRIMALS = {Aspects.AIR, Aspects.EARTH, Aspects.FIRE, Aspects.WATER, Aspects.ORDER, Aspects.ENTROPY};

    public PrimalCharmItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (level.getRandom().nextInt(20000) < 20) {
            Aspect aspect = PRIMALS[level.getRandom().nextInt(6)];
            level.addFreshEntity(new AspectOrbEntity(level, entity.getX(), entity.getY(), entity.getZ(), aspect, 1));
        }
    }
}
