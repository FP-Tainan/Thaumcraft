package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.thaumcraft.arcana.Contingency;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * O que acorda uma Contingência: os quatro pontos do {@code AMEventHandler} do Ars Magica 2 onde ela dispara.
 *
 * <p>Duas delas ficam no dano — a de <b>Dano</b>, que dispara em qualquer pancada, e a de <b>Vida Baixa</b>,
 * que espera a vida cair a um terço. As outras duas ficam na <b>morte</b> e no <b>tique</b>, que é onde o
 * original olha se a pessoa está ardendo ou caindo.
 *
 * <p>A de morte entra <b>antes</b> de o jogo matar a pessoa, que é o que faz dela uma segunda vida.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityContingencyMixin {
    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void thaumcraft$contingencyHurt(ServerLevel level, DamageSource fonte, float dano,
                                            CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        Contingency.proc(level, self, Contingency.Kind.DAMAGE_TAKEN);
        if (self.getHealth() <= self.getMaxHealth() * Contingency.LOW_HEALTH) {
            Contingency.proc(level, self, Contingency.Kind.HEALTH_LOW);
        }
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void thaumcraft$contingencyDeath(DamageSource fonte, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level() instanceof ServerLevel level) {
            Contingency.proc(level, self, Contingency.Kind.DEATH);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void thaumcraft$contingencyTick(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self.level() instanceof ServerLevel level)) return;
        net.thaumcraft.arcana.Contingencies.tick(level, self);
    }
}
