package net.thaumcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.occulta.familiar.Familiars;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * O golpe que o familiar leva por quem o tem.
 *
 * <p>Um por cento de longe, <b>dez por cento</b> a menos de vinte e quatro blocos — e o que ele leva sai do
 * que a pessoa levaria. É o {@code handlePlayerHurt} do Witchery, que lá é um ouvinte do {@code LivingHurtEvent}
 * e aqui é o mesmo ponto do jogo.
 *
 * <p>Isto convive com os outros mixins que mexem no dano: o jogo aplica uma mudança depois da outra, que é o
 * que o original também faz ao ter vários ganchos no mesmo evento.
 */
@Mixin(Player.class)
public abstract class PlayerFamiliarMixin {
    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
    private float thaumcraft$familiarTakesSome(float dano, ServerLevel level, DamageSource fonte) {
        return Familiars.desvia(level, (Player) (Object) this, fonte, dano);
    }
}
