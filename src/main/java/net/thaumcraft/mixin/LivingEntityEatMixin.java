package net.thaumcraft.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.forbidden.ForbiddenDrops;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * O {@code onEat} do Forbidden Magic 0.575: quem termina de comer no Nether deixa cair, duas vezes em dez, um
 * Fragmento da Gula. O gancho é na hora em que a comida acaba de ser comida, com ela ainda na mão.
 */
@Mixin(LivingEntity.class)
public class LivingEntityEatMixin {
    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void thaumcraft$gluttony(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player player) ForbiddenDrops.onEat(player, self.getUseItem().copy());
    }
}
