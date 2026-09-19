package net.thaumcraft.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.thaumcraft.entity.PrimalArrowEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A flecha primordial ({@code ItemPrimalArrow}): uma por primário, oito de cada vez na bancada arcana. Qualquer arco a
 * atira; o arco de osso, mais forte.
 */
public class PrimalArrowItem extends ArrowItem {
    private final int type;

    public PrimalArrowItem(int type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        return new PrimalArrowEntity(level, shooter, ammo.copyWithCount(1), weapon, this.type);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        return new PrimalArrowEntity(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1), this.type);
    }
}
