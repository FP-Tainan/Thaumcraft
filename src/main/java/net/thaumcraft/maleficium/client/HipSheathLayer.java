package net.thaumcraft.maleficium.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.client.render.FortressBladeRenderer;
import net.thaumcraft.maleficium.FortressBladeItem;

/**
 * A bainha na cintura: o {@code IRenderInventoryItem} do Tainted Magic 8.1.1, que desenha a saya de quem carrega uma
 * lâmina de fortaleza — e a lâmina dentro dela, quando ela não está na mão.
 *
 * <p>Como no original, isto só vale para quem joga: o cliente não conhece o inventário dos outros.
 */
public class HipSheathLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public HipSheathLayer(LivingEntityRenderer<?, AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, AvatarRenderState state,
                       float yRot, float xRot) {
        Player player = Minecraft.getInstance().player;
        if (player == null || player.getId() != state.id) return;
        // o original não desenha nada em quem está invisível
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.INVISIBILITY)) return;
        ItemStack carried = ItemStack.EMPTY;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof FortressBladeItem) {
                carried = stack;
                break;
            }
        }
        if (carried.isEmpty()) return;
        boolean inHand = player.getMainHandItem() == carried || player.getOffhandItem() == carried;
        pose.pushPose();
        this.getParentModel().body.translateAndRotate(pose);
        // na cintura, do lado esquerdo, com a ponta para trás — o mesmo jeito do original
        pose.translate(0.2f, 0.6f, 0.15f);
        pose.mulPose(Axis.XP.rotationDegrees(55.0f));
        pose.mulPose(Axis.YP.rotationDegrees(180.0f));
        pose.scale(0.5f, 0.5f, 0.5f);
        FortressBladeRenderer.sheath(pose, collector, carried, light, inHand);
        pose.popPose();
    }
}
