package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.thaumcraft.arcana.ArcanaEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Os quatro lugares por onde um efeito do Ars Arcana mexe em quem o tem.
 *
 * <p>No original isto é o {@code AMEventHandler}, um punhado de métodos gigantes que olham para todos os
 * efeitos de uma vez. Aqui é o mesmo ponto do jogo — a <b>batida</b>, o <b>dano</b>, a <b>queda</b> e o
 * <b>pulo</b> —, e o que cada efeito faz está junto dele, no {@link ArcanaEffects}.
 *
 * <p>Repare que isto convive com o mixin da Afinidade sem se pisarem: os dois mudam o dano, e o jogo aplica
 * uma mudança depois da outra, que é o que o original também faz ao ter dois ganchos no mesmo evento.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityArcanaEffectsMixin {
    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
    private float thaumcraft$arcanaShield(float dano, ServerLevel level, DamageSource fonte) {
        return ArcanaEffects.hurt((LivingEntity) (Object) this, fonte, dano);
    }

    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double thaumcraft$arcanaFall(double distância) {
        return ArcanaEffects.fall((LivingEntity) (Object) this, (float) distância);
    }

    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void thaumcraft$arcanaJump(CallbackInfo ci) {
        ArcanaEffects.jump((LivingEntity) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void thaumcraft$arcanaTick(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level() instanceof ServerLevel level) ArcanaEffects.tick(level, self);
    }

    /**
     * E quando ela sai do mundo, arruma o que ficou.
     *
     * <p>Quem morre iluminado deixaria um bloco de luz invisível aceso para sempre, e ninguém saberia que ele
     * estava lá nem como o tirar.
     */
    @Inject(method = "remove", at = @At("HEAD"))
    private void thaumcraft$arcanaRemoved(net.minecraft.world.entity.Entity.RemovalReason porquê,
                                          CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level() instanceof ServerLevel level) ArcanaEffects.esquece(level, self);
    }
}
