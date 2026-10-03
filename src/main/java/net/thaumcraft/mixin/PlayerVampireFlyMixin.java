package net.thaumcraft.mixin;

import net.minecraft.world.entity.player.Player;
import net.thaumcraft.occulta.vampire.VampirePowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <b>As asas de morcego, batida a batida</b>: o {@code Shapeshift.updatePlayerState} do Witchery.
 *
 * <p>Duas coisas, e as duas a cada batida:
 *
 * <ul>
 *   <li>as asas se <b>repõem</b>, porque qualquer coisa no jogo que mexa nas licenças de voo — morrer,
 *       trocar de mundo, sair do criativo — as apagaria, e um morcego sem asas cai do céu;</li>
 *   <li>e <b>voando, a queda não conta</b>: a conta zera enquanto ele estiver no ar.</li>
 * </ul>
 *
 * <p>É a segunda que faz do morcego o que ele é. Ele não é imune a cair: ele é imune <b>enquanto voa</b>. Um
 * morcego que se desligue a cem blocos de altura cai de onde estava e não de onde subiu, e o chão lhe cobra
 * o resto. Sair da forma, no alto, é uma decisão — e é a única maneira de morrer com este poder.
 */
@Mixin(Player.class)
public abstract class PlayerVampireFlyMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void thaumcraft$asas(CallbackInfo info) {
        VampirePowers.voa((Player) (Object) this);
    }
}
