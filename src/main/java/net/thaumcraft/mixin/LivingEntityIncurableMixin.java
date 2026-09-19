package net.thaumcraft.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.thaumcraft.research.Incurable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/** O leite (e tudo que limpa os efeitos de uma vez) deixa os efeitos sem cura do {@link Incurable}. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityIncurableMixin {
    @Unique
    private List<MobEffectInstance> thaumcraft$kept;

    @Inject(method = "removeAllEffects", at = @At("HEAD"))
    private void thaumcraft$keep(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) return;
        this.thaumcraft$kept = new ArrayList<>();
        for (MobEffectInstance effect : self.getActiveEffects()) {
            if (Incurable.marked(self, effect.getEffect())) this.thaumcraft$kept.add(new MobEffectInstance(effect));
        }
    }

    @Inject(method = "removeAllEffects", at = @At("RETURN"))
    private void thaumcraft$restore(CallbackInfoReturnable<Boolean> cir) {
        if (this.thaumcraft$kept == null) return;
        LivingEntity self = (LivingEntity) (Object) this;
        for (MobEffectInstance effect : this.thaumcraft$kept) self.addEffect(effect);
        this.thaumcraft$kept = null;
    }
}
