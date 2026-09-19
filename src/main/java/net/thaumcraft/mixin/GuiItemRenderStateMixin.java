package net.thaumcraft.mixin;

import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.thaumcraft.client.render.TintedItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Guarda no item a cor pedida pelo {@link TintedItems} na hora em que ele entra na tela. */
@Mixin(GuiItemRenderState.class)
public class GuiItemRenderStateMixin implements TintedItems.Tinted {
    @Unique
    private int thaumcraft$tint = -1;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void thaumcraft$remember(CallbackInfo ci) {
        this.thaumcraft$tint = TintedItems.current;
    }

    @Override
    public int thaumcraft$tint() {
        return this.thaumcraft$tint;
    }
}
