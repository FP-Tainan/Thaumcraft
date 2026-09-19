package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.thaumcraft.event.Champions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Os campeões no dano e no tique: o {@code LivingHurtEvent} (o dano que chega, antes da armadura) e o
 * {@code LivingUpdateEvent} da 4.2.3.5. O jogador tem o seu próprio {@code actuallyHurt}, e lá o gancho está no
 * {@link PlayerRunicMixin}.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityChampionMixin {
    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
    private float thaumcraft$champion(float damage, ServerLevel level, DamageSource source) {
        return Champions.hurt((LivingEntity) (Object) this, source, damage);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void thaumcraft$championTick(CallbackInfo ci) {
        Champions.tick((LivingEntity) (Object) this);
    }
}
