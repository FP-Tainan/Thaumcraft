package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.thaumcraft.arcana.AffinityEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A Afinidade no dano, no pulo, na queda e na morte: os quatro eventos do {@code AffinityHelper} do Ars Magica
 * 2 que o Forge de 2014 tinha e o jogo de hoje não tem.
 *
 * <p>O {@code onEntityHurt} entra onde o {@code LivingHurtEvent} entrava — no dano que chega, antes da
 * armadura —, e vale para os dois lados: para quem leva e para quem bateu de mão vazia. O jogador tem o seu
 * próprio {@code actuallyHurt}, e lá o gancho está no {@link PlayerAffinityMixin}.
 *
 * <p>O {@code onEntityFall} mexe na distância de queda antes de ela virar dano, que é o que o
 * {@code LivingFallEvent} fazia; o {@code onEntityJump} soma velocidade depois do pulo; e o
 * {@code onEntityDeath} cobra de quem é de Vida o preço de matar.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityAffinityMixin {
    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
    private float thaumcraft$affinityHurt(float damage, ServerLevel level, DamageSource source) {
        return AffinityEffects.hurt((LivingEntity) (Object) this, source, damage);
    }

    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void thaumcraft$affinityJump(CallbackInfo ci) {
        AffinityEffects.jump((LivingEntity) (Object) this);
    }

    @ModifyVariable(method = "causeFallDamage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private double thaumcraft$affinityFall(double distance) {
        return AffinityEffects.fall((LivingEntity) (Object) this, (float) distance);
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void thaumcraft$affinityDeath(DamageSource source, CallbackInfo ci) {
        AffinityEffects.killed((LivingEntity) (Object) this, source);
    }
}
