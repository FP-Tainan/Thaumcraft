package net.thaumcraft.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.thaumcraft.entity.PrimalArrowEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** As flechas primordiais de ar, fogo e ordem ferem com o dano próprio delas (o {@code DamageSourceIndirectThaumcraftEntity}). */
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowDamageMixin {
    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSources;arrow(Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/damagesource/DamageSource;"))
    private DamageSource thaumcraft$primalDamage(DamageSources sources, AbstractArrow arrow, Entity owner) {
        if (arrow instanceof PrimalArrowEntity primal) return primal.damageSource(owner);
        return sources.arrow(arrow, owner);
    }
}
