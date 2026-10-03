package net.thaumcraft.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.thaumcraft.occulta.vampire.Vampire;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <b>Comida não alimenta um vampiro</b>: o que a {@link net.thaumcraft.occulta.vampire.VampireHunger}
 * descreve.
 *
 * <p>Este é o lugar exato onde um alimento <b>conta</b>, e é só isso que se atalha. O que a comida faz
 * <i>além</i> de alimentar — curar, dar um efeito, tocar um som — continua a acontecer, porque hoje isso
 * mora noutro lado. Uma maçã dourada ainda cura um vampiro; o que ela deixa de fazer é sustentá-lo.
 *
 * <p>O sangue não passa por aqui: ele entra pela {@code VampireHunger}, direto na barra.
 */
@Mixin(FoodProperties.class)
public abstract class FoodPropertiesVampireMixin {
    @Inject(method = "onConsume", at = @At("HEAD"), cancellable = true)
    private void thaumcraft$oVampiroNaoSeAlimenta(net.minecraft.world.level.Level level, LivingEntity quem,
                                                  net.minecraft.world.item.ItemStack oquê,
                                                  net.minecraft.world.item.component.Consumable como,
                                                  CallbackInfo info) {
        if (Vampire.é(quem)) info.cancel();
    }
}
