package net.thaumcraft.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.research.Warp;
import net.thaumcraft.research.WarpItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** O {@code onCrafting} do {@code EventHandlerWorld}: fabricar certas coisas deixa distorção que gruda. */
@Mixin(ItemStack.class)
public abstract class ItemStackWarpMixin {
    @Inject(method = "onCraftedBy", at = @At("HEAD"))
    private void thaumcraft$warp(Player player, int amount, CallbackInfo info) {
        int warp = WarpItems.of((ItemStack) (Object) this);
        if (warp > 0 && !player.level().isClientSide()) Warp.addSticky(player, warp);
    }
}
