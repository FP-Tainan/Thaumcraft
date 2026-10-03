package net.thaumcraft.mixin;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.thaumcraft.occulta.vampire.VampirePowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * <b>Um morcego cabe onde nada cabe</b>: o ramo do {@code EntitySizeInfo} que é do vampiro.
 *
 * <p>Em forma de morcego o jogador mede <b>três décimos por seis décimos</b> — pouco mais de meio bloco de
 * altura e metade da largura de gente. É a menor caixa que o mod dá a um jogador, e com ela ele passa por um
 * buraco de um bloco, entra por uma fenda de meia parede e se enfia numa gruta por onde nem um lobo entra.
 *
 * <p>Os olhos ficam a <b>oito décimos</b> da altura, e isso muda o jogo mais do que parece: a câmara desce
 * para meio metro do chão, e o mundo visto de lá <b>é outro mundo</b>. É a melhor razão para virar morcego,
 * e não é um número de combate — como no lobo, {@linkplain PlayerWerewolfSizeMixin que mede oito décimos}.
 *
 * <p>E as duas formas nunca se encontram: não se é lobo e morcego ao mesmo tempo, e por isso as duas
 * costuras no mesmo lugar não se pisam.
 */
@Mixin(net.minecraft.world.entity.Avatar.class)
public abstract class PlayerVampireSizeMixin {
    @Inject(method = "getDefaultDimensions", at = @At("RETURN"), cancellable = true)
    private void thaumcraft$tamanhoDeMorcego(Pose pose, CallbackInfoReturnable<EntityDimensions> info) {
        if (!(((Object) this) instanceof Player self)) return;
        if (!VampirePowers.emMorcego(self)) return;
        info.setReturnValue(
                EntityDimensions.scalable(VampirePowers.MORCEGO_LARGURA, VampirePowers.MORCEGO_ALTURA)
                        .withEyeHeight(VampirePowers.MORCEGO_ALTURA * VampirePowers.MORCEGO_OLHOS));
    }
}
