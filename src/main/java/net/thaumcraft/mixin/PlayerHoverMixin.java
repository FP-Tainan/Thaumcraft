package net.thaumcraft.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.thaumcraft.event.Hover;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;

/**
 * O {@code breakSpeedEvent} do {@code EventHandlerEntity}: quem paira com o arreio quebra blocos no ar cinco vezes mais
 * depressa, desfazendo o castigo de quem não pisa no chão.
 */
@Mixin(Player.class)
public abstract class PlayerHoverMixin {
    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void thaumcraft$hoverBreakSpeed(BlockState state, CallbackInfoReturnable<Float> info) {
        Player player = (Player) (Object) this;
        if (!player.onGround() && Hover.getHover(player)) info.setReturnValue(info.getReturnValue() * 5.0f);
    }
}
