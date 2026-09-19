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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.thaumcraft.Thaumcraft;

import java.util.function.BiConsumer;

/**
 * As armaduras de modelo próprio do Culto Carmesim no corpo: o {@code getArmorModel} dos itens e o {@code render} do
 * {@code ModelRobe}, do {@code ModelKnightArmor} e do {@code ModelLeaderArmor} da 4.2.3.5. O elmo e a calça usam a versão
 * de dentro do modelo; o peito, a de fora. Cada peça mostra só a sua parte do corpo, o elmo um pouco maior, e as abas de
 * pano balançam com o passo.
 */
public class CultistArmorRenderer implements ArmorRenderer {
    public static final ModelLayerLocation ROBE_INNER = layer("cultist_robe_inner");
    public static final ModelLayerLocation ROBE_OUTER = layer("cultist_robe_outer");
    public static final ModelLayerLocation PLATE_INNER = layer("cultist_plate_inner");
    public static final ModelLayerLocation PLATE_OUTER = layer("cultist_plate_outer");
    public static final ModelLayerLocation LEADER_INNER = layer("cultist_leader_inner");
    public static final ModelLayerLocation LEADER_OUTER = layer("cultist_leader_outer");

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(Thaumcraft.id(name), "main");
    }

    private final Identifier texture;
    private final Model inner, outer;

    public CultistArmorRenderer(EntityRendererProvider.Context context, Identifier texture, ModelLayerLocation inner,
                                ModelLayerLocation outer, BiConsumer<Model, Float> sway) {
        this.texture = texture;
        this.inner = new Model(context.bakeLayer(inner), sway);
        this.outer = new Model(context.bakeLayer(outer), sway);
    }

    /** O balanço do robe: as abas da frente com o passo, as de trás ao contrário. */
    public static void robeSway(Model m, Float c) {
        m.rot("body", "frontclothr1", c - 0.1047198f);
        m.rot("body", "frontclothl1", c - 0.1047198f);
        m.rot("body", "frontclothr2", c - 0.3316126f);
        m.rot("body", "frontclothl2", c - 0.3316126f);
        m.rot("body", "clothbackr1", -c + 0.1047198f);
        m.rot("body", "clothbackl1", -c + 0.1047198f);
        for (String s : new String[]{"clothbackr2", "clothbackl2", "clothbackr3", "clothbackl3"}) m.rot("body", s, -c + 0.2268928f);
    }

    /** O balanço da placa: as abas da frente e a capa em três pedaços, pela metade. */
    public static void plateSway(Model m, Float c) {
        m.rot("body", "frontcloth1", c - 0.1047198f);
        m.rot("body", "frontcloth2", c - 0.3316126f);
        capeSway(m, c);
    }

    /** O balanço do pretor: as abas das pernas cada uma com a sua perna, e a capa. */
    public static void leaderSway(Model m, Float c) {
        capeSway(m, c);
    }

    private static void capeSway(Model m, float c) {
        m.rot("body", "cloak1", -c / 2.0f + 0.1396263f);
        m.rot("body", "cloak2", -c / 2.0f + 0.3069452f);
        m.rot("body", "cloak3", -c / 2.0f + 0.4465716f);
    }

    @Override
    public void render(PoseStack pose, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState state,
                       EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> context) {
        Model model = slot == EquipmentSlot.CHEST || slot == EquipmentSlot.FEET ? this.outer : this.inner;
        model.slot = slot;
        collector.submitModel(model, state, pose, RenderTypes.armorCutoutNoCull(this.texture), light, OverlayTexture.NO_OVERLAY, -1, null, 0, null);
        if (stack.hasFoil()) {
            collector.submitModel(model, state, pose, RenderTypes.armorEntityGlint(), light, OverlayTexture.NO_OVERLAY, -1, null, 0, null);
        }
    }

    /** O modelo de uma peça: mostra o que é dela e balança as abas. */
    public static class Model extends HumanoidModel<HumanoidRenderState> {
        EquipmentSlot slot = EquipmentSlot.CHEST;
        private final BiConsumer<Model, Float> sway;

        Model(ModelPart root, BiConsumer<Model, Float> sway) {
            super(root);
            this.sway = sway;
        }

        void rot(String parent, String part, float x) {
            ModelPart p = parent.equals("body") ? this.body : this.head;
            if (p.hasChild(part)) p.getChild(part).xRot = x;
        }

        @Override
        public void setupAnim(HumanoidRenderState state) {
            super.setupAnim(state);
            this.head.visible = this.hat.visible = this.slot == EquipmentSlot.HEAD;
            this.hat.visible = false;
            this.body.visible = this.slot == EquipmentSlot.CHEST || this.slot == EquipmentSlot.LEGS;
            this.rightArm.visible = this.leftArm.visible = this.slot == EquipmentSlot.CHEST;
            this.rightLeg.visible = this.leftLeg.visible = this.slot == EquipmentSlot.LEGS;
            this.head.xScale = this.head.yScale = this.head.zScale = 1.01f;
            float a = Mth.cos(state.walkAnimationPos * 0.6662f) * 1.4f * state.walkAnimationSpeed;
            float b = Mth.cos(state.walkAnimationPos * 0.6662f + (float) Math.PI) * 1.4f * state.walkAnimationSpeed;
            float c = Math.min(a, b);
            this.sway.accept(this, c);
            // as abas das pernas do pretor seguem cada perna
            this.rot("body", "legclothr", a - 0.1047198f);
            this.rot("body", "legclothl", b - 0.1047198f);
        }
    }
}
