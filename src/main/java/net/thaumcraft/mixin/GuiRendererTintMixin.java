package net.thaumcraft.mixin;

import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.thaumcraft.client.render.TintedItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Cola o item na tela com a cor que ele trouxe (o branco de sempre, se não trouxe nenhuma). */
@Mixin(GuiRenderer.class)
public class GuiRendererTintMixin {
    @ModifyConstant(method = "submitBlitFromItemAtlas", constant = @Constant(intValue = -1))
    private int thaumcraft$tint(int color, GuiItemRenderState itemState, GuiItemAtlas.SlotView slotView) {
        return (Object) itemState instanceof TintedItems.Tinted tinted ? tinted.thaumcraft$tint() : color;
    }
}
