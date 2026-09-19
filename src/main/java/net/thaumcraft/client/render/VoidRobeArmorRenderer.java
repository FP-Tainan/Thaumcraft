package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.item.VoidRobeItem;

/**
 * O manto do vazio no corpo: o {@code getArmorModel} do {@code ItemVoidRobeArmor} da 4.2.3.5 — o {@code ModelRobe} do
 * robe dos cultistas. Como o {@code getArmorTexture} dele devolve a {@code void_robe_armor_overlay.png} no passe sem tipo
 * (o que o Forge de então tinge com o {@code getColor}) e a {@code void_robe_armor.png} no passe "overlay", o pano
 * cinza é o que leva a cor, e os enfeites escuros e dourados vão por cima, sem tinta.
 */
public class VoidRobeArmorRenderer extends CultistArmorRenderer {
    private static final Identifier CLOTH = Thaumcraft.id("textures/models/void_robe_armor_overlay.png");

    public VoidRobeArmorRenderer(EntityRendererProvider.Context context) {
        super(context, Thaumcraft.id("textures/models/void_robe_armor.png"), ROBE_INNER, ROBE_OUTER, CultistArmorRenderer::robeSway);
    }

    @Override
    public void render(PoseStack pose, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState state, EquipmentSlot slot,
                       int light, HumanoidModel<HumanoidRenderState> context) {
        Model model = slot == EquipmentSlot.CHEST || slot == EquipmentSlot.FEET ? this.outer : this.inner;
        model.slot = slot;
        DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
        int colour = 0xFF000000 | (dyed != null ? dyed.rgb() : VoidRobeItem.UNDYED);
        collector.submitModel(model, state, pose, RenderTypes.armorCutoutNoCull(CLOTH), light, OverlayTexture.NO_OVERLAY, colour, null, 0, null);
        collector.submitModel(model, state, pose, RenderTypes.armorCutoutNoCull(this.texture), light, OverlayTexture.NO_OVERLAY, -1, null, 0, null);
        if (stack.hasFoil()) {
            collector.submitModel(model, state, pose, RenderTypes.armorEntityGlint(), light, OverlayTexture.NO_OVERLAY, -1, null, 0, null);
        }
    }
}
