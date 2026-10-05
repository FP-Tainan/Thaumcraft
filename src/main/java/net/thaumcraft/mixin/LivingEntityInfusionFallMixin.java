package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.thaumcraft.occulta.infusion.Infusions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * <b>A queda de quem está infundido.</b>
 *
 * <p>Só a <b>Infusão do Mundo</b> a usa, e usa-a para fazer da queda um poder: cair mais de três blocos em
 * terra mole rebenta o chão ou arranca o bloco de baixo, e a queda deixa de doer.
 *
 * <p>Como o do lobisomem e o dos poderes de bicho, mexe na <b>distância</b> e não no dano. É também o único
 * poder de infusão que <b>não precisa da Mão de Bruxa</b>: cair não é coisa que se faça com as mãos.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityInfusionFallMixin {
    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double thaumcraft$quedaDeInfusão(double quanto, double mesma, float quanta,
                                             DamageSource fonte) {
        if (!(((Object) this) instanceof ServerPlayer quem)) return quanto;
        if (!(quem.level() instanceof ServerLevel level)) return quanto;
        return Infusions.de(quem).cai(level, quem, quanto);
    }
}
