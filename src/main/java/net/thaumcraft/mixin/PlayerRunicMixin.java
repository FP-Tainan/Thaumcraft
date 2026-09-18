package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.event.RunicShield;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * O escudo rúnico entra onde o {@code LivingHurtEvent} do Forge entrava: no dano que chega ao jogador, antes da
 * armadura.
 */
@Mixin(Player.class)
public abstract class PlayerRunicMixin {
    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
    private float thaumcraft$runicShield(float damage, ServerLevel level, DamageSource source) {
        return RunicShield.absorb((Player) (Object) this, source, damage);
    }
}
