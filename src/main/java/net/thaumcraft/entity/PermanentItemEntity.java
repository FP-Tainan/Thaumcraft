package net.thaumcraft.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thaumcraft.registry.TCEntities;

/**
 * O item que não some: o {@code EntityPermanentItem} da 4.2.3.5 — o mesmo item boiando do crisol, mas sem prazo de
 * validade. É como a tábua rúnica espera na sala da chave das Terras de Fora.
 */
public class PermanentItemEntity extends SpecialItemEntity {
    public PermanentItemEntity(EntityType<? extends PermanentItemEntity> type, Level level) {
        super(type, level);
        this.setUnlimitedLifetime();
    }

    public PermanentItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        this(TCEntities.PERMANENT_ITEM, level);
        this.setPos(x, y, z);
        this.setItem(stack);
        this.setYRot((float) (Math.random() * 360.0));
        this.setDeltaMovement(Math.random() * 0.2f - 0.1f, 0.2f, Math.random() * 0.2f - 0.1f);
    }
}
