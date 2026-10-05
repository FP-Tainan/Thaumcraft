package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.thaumcraft.occulta.infusion.beast.CreaturePowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * <b>A queda de quem tem um bicho no bolso.</b>
 *
 * <p>O morcego trava a queda em <b>cinco blocos</b>; o slime, o cubo de magma e o sapo <b>a apagam</b>.
 *
 * <p>Como o do lobisomem, mexe na <b>distância</b> e não no dano — perdoando a distância, tudo o que o jogo
 * conta em cima dela continua a bater certo.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityBeastFallMixin {
    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double thaumcraft$quedaDePoderDeBicho(double quanto, double mesma, float quanta,
                                                  DamageSource fonte) {
        if (!(((Object) this) instanceof ServerPlayer quem)) return quanto;
        var poder = CreaturePowers.dele(quem);
        if (poder == null) return quanto;
        return poder.cai(quem, (float) quanto);
    }
}
