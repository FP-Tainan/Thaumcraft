package net.thaumcraft.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.client.render.ThaumometerFirstPerson;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * O thaumômetro na mão principal, em primeira pessoa: em vez do item comum, o desenhista do original
 * ({@link ThaumometerFirstPerson}), com as duas mãos na moldura e a mira escrita no vidro.
 */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void thaumcraft$thaumometer(AbstractClientPlayer player, float partial, float pitch,
                                        InteractionHand hand, float swing, ItemStack stack,
                                        float equip, PoseStack pose, SubmitNodeCollector collector,
                                        int light, CallbackInfo info) {
        if (hand != InteractionHand.MAIN_HAND || !ThaumometerFirstPerson.is(stack) || player.isScoping()) return;
        ThaumometerFirstPerson.submit(player, partial, swing, equip, pose, collector, light);
        info.cancel();
    }
}
