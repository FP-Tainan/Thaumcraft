package net.thaumcraft.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.occulta.wolf.WerewolfPowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <b>O salto de um bicho</b>: o {@code LivingJumpEvent} do Witchery, que é onde o {@code updateJump} corre.
 *
 * <p>Um lobisomem pula <b>mais alto</b>, e correndo o pulo também o atira <b>para a frente</b>. As duas
 * coisas se somam ao impulso do pulo <b>de uma vez</b> — o original soma e larga —, e é isso que faz do
 * quinto degrau, que pede dez monstros mortos no ar, uma coisa que se consegue.
 *
 * <p>Corre <b>dos dois lados</b>, e tem de correr: quem manda no movimento de um jogador é o cliente dele, e
 * quem conta a altura de que ele caiu é o servidor. O original punha isto só no cliente porque na 1.7.10 o
 * servidor não olhava; hoje olha, e um salto que só o cliente visse seria um salto que o servidor desfaz.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityWerewolfJumpMixin {
    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void thaumcraft$saltoDeBicho(CallbackInfo info) {
        if (((Object) this) instanceof Player quem) WerewolfPowers.pula(quem);
    }
}
