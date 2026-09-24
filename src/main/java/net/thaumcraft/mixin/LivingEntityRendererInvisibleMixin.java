package net.thaumcraft.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.thaumcraft.naturalis.NaturalisItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * O {@code IRevealInvisible} do Magia Naturalis 0.5.0: com os Óculos de Cristal Escuro no rosto, o contorno de
 * quem está invisível aparece — o desenho passa a ser feito, ainda que apagado, em vez de ser pulado.
 */
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererInvisibleMixin {
    @Inject(method = "isBodyVisible", at = @At("HEAD"), cancellable = true)
    private void thaumcraft$darkCrystalGoggles(LivingEntityRenderState state, CallbackInfoReturnable<Boolean> back) {
        if (!state.isInvisible) return;
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!player.getItemBySlot(EquipmentSlot.HEAD).is(NaturalisItems.DARK_CRYSTAL_GOGGLES)) return;
        back.setReturnValue(true);
    }
}
