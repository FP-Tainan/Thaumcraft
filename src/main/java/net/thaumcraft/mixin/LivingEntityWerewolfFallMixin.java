package net.thaumcraft.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.thaumcraft.occulta.wolf.WerewolfPowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * <b>A queda que perdoa</b>: o {@code LivingFallEvent} do Witchery, onde o {@code updateFallState} corre.
 *
 * <p>O original mexe na <b>distância</b> e não no dano, e é a diferença que importa: perdoando a distância,
 * tudo o que o jogo conta em cima dela — o dano, o barulho, o pó, a Queda Suave — continua a bater certo.
 * Um lobo de grau dez cai cinco blocos e não <b>caiu</b> de todo.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityWerewolfFallMixin {
    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double thaumcraft$quedaDeBicho(double quanto, double mesma, float quanta, DamageSource fonte) {
        if (!(((Object) this) instanceof Player quem)) return quanto;
        return WerewolfPowers.queda(quem, quanto);
    }
}
