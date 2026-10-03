package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.thaumcraft.occulta.OccultaHurt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * O {@code LivingHurtEvent} do Witchery: o lugar onde as poções do ofício mexem no golpe.
 *
 * <p>É o mesmo ponto do jogo que o Ars Arcana usa, e os dois convivem sem se pisarem — o jogo aplica uma
 * mudança depois da outra, que é o que o Forge de então também fazia com dois ganchos no mesmo evento.
 *
 * <p>O que cada poção faz está em {@link OccultaHurt}, junto das outras, porque a ordem entre elas importa.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityOccultaHurtMixin {
    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
    private float thaumcraft$occultaHurt(float dano, ServerLevel level, DamageSource fonte) {
        return OccultaHurt.hurt((LivingEntity) (Object) this, fonte, dano);
    }
}
