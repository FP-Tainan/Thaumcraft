package net.thaumcraft.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.thaumcraft.registry.TCEntities;

/**
 * A coisa que sai do crisol: o {@code EntitySpecialItem} da 4.2.3.5 — um item que não cai (a subida se desfaz aos
 * poucos e o peso é anulado a cada tique) e que explosão nenhuma destrói.
 */
public class SpecialItemEntity extends ItemEntity {
    public SpecialItemEntity(EntityType<? extends SpecialItemEntity> type, Level level) {
        super(type, level);
    }

    public SpecialItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        this(TCEntities.SPECIAL_ITEM, level);
        this.setPos(x, y, z);
        this.setItem(stack);
        this.setYRot((float) (Math.random() * 360.0));
        this.setDeltaMovement(Math.random() * 0.2f - 0.1f, 0.2f, Math.random() * 0.2f - 0.1f);
    }

    @Override
    public void tick() {
        Vec3 m = this.getDeltaMovement();
        double y = m.y > 0.0 ? m.y * 0.9f : m.y;
        this.setDeltaMovement(m.x, y + 0.04f, m.z);
        super.tick();
    }

    @Override
    public boolean ignoreExplosion(net.minecraft.world.level.Explosion explosion) {
        return true;
    }
}
