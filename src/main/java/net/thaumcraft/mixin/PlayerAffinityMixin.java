package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.arcana.AffinityEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * A Afinidade no dano que chega ao jogador.
 *
 * <p>O jogador tem o seu próprio {@code actuallyHurt}, e por isso o gancho do {@link LivingEntityAffinityMixin}
 * não o alcança. É aqui que a Terra amortece, o Arcano amplifica e a Natureza e o Gelo devolvem — e é também
 * aqui que um jogador que bate noutro de mão vazia põe fogo nele.
 */
@Mixin(Player.class)
public abstract class PlayerAffinityMixin {
    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
    private float thaumcraft$affinityHurt(float damage, ServerLevel level, DamageSource source) {
        return AffinityEffects.hurt((Player) (Object) this, source, damage);
    }
}
