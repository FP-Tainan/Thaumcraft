package net.thaumcraft.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.registry.TCItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Os braços que seguram o thaumômetro.
 *
 * <p>O jogo só desenha braço para quem está de mão vazia; com item na mão desenha só o item. No Thaumcraft
 * original o aparelho aparece erguido entre as duas mãos, como quem abre um mapa, e é isso que este remendo
 * traz de volta: baixado, o braço que o segura entra na cena; erguido, as duas mãos seguram a moldura.
 */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Shadow
    private void renderPlayerArm(PoseStack pose, SubmitNodeCollector collector, int light,
                                 float equipProgress, float swing, HumanoidArm arm) {
        throw new AssertionError();
    }

    @Shadow
    private void renderMapHand(PoseStack pose, SubmitNodeCollector collector, int light, HumanoidArm arm) {
        throw new AssertionError();
    }

    @Shadow
    private float calculateMapTilt(float pitch) {
        throw new AssertionError();
    }

    @Inject(method = "submitArmWithItem", at = @At("HEAD"))
    private void thaumcraft$armHoldingTheScanner(AbstractClientPlayer player, float partial, float pitch,
                                                 InteractionHand hand, float swing, ItemStack stack,
                                                 float equip, PoseStack pose, SubmitNodeCollector collector,
                                                 int light, CallbackInfo info) {
        if (!stack.is(TCItems.THAUMOMETER) || player.isInvisible()) return;

        if (player.isUsingItem() && player.getUsedItemHand() == hand) {
            // erguido ao olho: as duas mãos sobem pelos cantos e seguram a moldura, como no mod original
            for (HumanoidArm lado : HumanoidArm.values()) {
                float sinal = lado == HumanoidArm.RIGHT ? 1.0f : -1.0f;
                pose.pushPose();
                pose.translate(sinal * 0.30f, 0.10f, -0.95f);
                pose.mulPose(Axis.YP.rotationDegrees(90.0f));
                renderMapHand(pose, collector, light, lado);
                pose.popPose();
            }
            return;
        }

        // baixado: só o braço da mão que o segura
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        pose.pushPose();
        renderPlayerArm(pose, collector, light, equip, swing, arm);
        pose.popPose();
    }
}
