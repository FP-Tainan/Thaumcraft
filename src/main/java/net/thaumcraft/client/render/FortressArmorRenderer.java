package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.item.FortressArmorItem;
import net.thaumcraft.registry.TCComponents;

/**
 * A armadura de fortaleza no corpo: o {@code getArmorModel} do {@code ItemFortressArmor} e o {@code render} do
 * {@code ModelFortressArmor} da 4.2.3.5.
 *
 * <p>Cada peça desenha só a sua parte do corpo; a couraça leva o cinto largo, o peitoral e as costas, e a calça o
 * cinto estreito. Os enfeites crescem com o conjunto: com duas peças aparecem as ombreiras de cima, as abas do elmo,
 * o livro e as placas do meio das pernas; com as três, o pergaminho, a gema e os ornamentos do elmo, e o resto das
 * ombreiras e das pernas. O elmo mostra a máscara e os óculos que tiver.
 */
public class FortressArmorRenderer implements ArmorRenderer {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("fortress_armor"), "main");
    private static final Identifier TEXTURE = Thaumcraft.id("textures/models/fortress_armor.png");

    private final Model helmet, chest, legs;

    public FortressArmorRenderer(EntityRendererProvider.Context context) {
        this.helmet = new Model(context.bakeLayer(LAYER), EquipmentSlot.HEAD);
        this.chest = new Model(context.bakeLayer(LAYER), EquipmentSlot.CHEST);
        this.legs = new Model(context.bakeLayer(LAYER), EquipmentSlot.LEGS);
    }

    @Override
    public void render(PoseStack pose, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState state,
                       EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> context) {
        Model model = switch (slot) {
            case HEAD -> this.helmet;
            case CHEST -> this.chest;
            case LEGS -> this.legs;
            default -> null;
        };
        if (model == null) return;
        collector.submitModel(model, state, pose, RenderTypes.armorCutoutNoCull(TEXTURE), light, OverlayTexture.NO_OVERLAY, -1, null, 0, null);
        if (stack.hasFoil()) {
            collector.submitModel(model, state, pose, RenderTypes.armorEntityGlint(), light, OverlayTexture.NO_OVERLAY, -1, null, 0, null);
        }
    }

    /** O modelo de uma peça: mostra o que é dela e esconde os enfeites conforme o conjunto que a pessoa veste. */
    static class Model extends HumanoidModel<HumanoidRenderState> {
        private final EquipmentSlot slot;
        private final ModelPart[] masks;

        Model(ModelPart root, EquipmentSlot slot) {
            super(root);
            this.slot = slot;
            this.masks = new ModelPart[]{this.head.getChild("mask0"), this.head.getChild("mask1"), this.head.getChild("mask2")};
        }

        @Override
        public void setupAnim(HumanoidRenderState state) {
            super.setupAnim(state);
            this.head.visible = this.slot == EquipmentSlot.HEAD;
            this.hat.visible = false;
            this.body.visible = this.slot == EquipmentSlot.CHEST || this.slot == EquipmentSlot.LEGS;
            this.rightArm.visible = this.leftArm.visible = this.slot == EquipmentSlot.CHEST;
            this.rightLeg.visible = this.leftLeg.visible = this.slot == EquipmentSlot.LEGS;
            // o original aumenta o elmo em um por cento
            this.head.xScale = this.head.yScale = this.head.zScale = 1.01f;

            // o checkSet: quantas peças do conjunto, e a máscara e os óculos do elmo
            int set = 0;
            for (ItemStack piece : new ItemStack[]{state.headEquipment, state.chestEquipment, state.legsEquipment}) {
                if (piece != null && piece.getItem() instanceof FortressArmorItem) set++;
            }
            ItemStack helm = state.headEquipment;
            boolean fortressHelm = helm != null && helm.getItem() instanceof FortressArmorItem;
            Integer mask = fortressHelm ? helm.get(TCComponents.FORTRESS_MASK) : null;
            boolean goggles = fortressHelm && Boolean.TRUE.equals(helm.get(TCComponents.FORTRESS_GOGGLES));
            this.head.getChild("goggles").visible = goggles;
            for (int a = 0; a < 3; a++) this.masks[a].visible = mask != null && mask == a;

            // a couraça e a calça trocam de cinto
            boolean chest = this.slot == EquipmentSlot.CHEST;
            for (String part : new String[]{"beltr", "beltl", "chestplate", "scroll", "backplate", "book"}) this.body.getChild(part).visible = chest;
            for (String part : new String[]{"mbelt", "mbeltl", "mbeltr"}) this.body.getChild(part).visible = !chest;

            if (chest) {
                this.body.getChild("scroll").visible = set >= 3;
                this.body.getChild("book").visible = set >= 2;
            }
            for (String part : new String[]{"ornamentl", "ornamentl2", "ornamentr", "ornamentr2", "gemornament", "gem"}) {
                this.head.getChild(part).visible = set >= 3;
            }
            this.head.getChild("flapl").visible = this.head.getChild("flapr").visible = set >= 2;
            this.leftArm.getChild("shoulderplateltop").visible = this.leftArm.getChild("shoulderplatel1").visible = set >= 2;
            this.leftArm.getChild("shoulderplatel2").visible = this.leftArm.getChild("shoulderplatel3").visible = set >= 3;
            this.rightArm.getChild("shoulderplatertop").visible = this.rightArm.getChild("shoulderplater1").visible = set >= 2;
            this.rightArm.getChild("shoulderplater2").visible = this.rightArm.getChild("shoulderplater3").visible = set >= 3;
            this.rightLeg.getChild("sidepanelr2").visible = set >= 2;
            this.leftLeg.getChild("sidepanell2").visible = set >= 2;
            this.rightLeg.getChild("sidepanelr3").visible = set >= 3;
            this.leftLeg.getChild("sidepanell3").visible = set >= 3;
        }
    }
}
