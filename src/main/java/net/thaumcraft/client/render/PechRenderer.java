package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.PechEntity;
import net.thaumcraft.registry.TCItems;

/**
 * O {@code RenderPech} da 4.2.3.5: o modelo com a pele do tipo ({@code pech_forage}, {@code pech_thaum},
 * {@code pech_stalker}) e o que ele tem na mão, preso no braço direito com as mesmas voltas do original para arco,
 * ferramenta (a varinha um pouco mais baixa) e item solto.
 */
public class PechRenderer extends MobRenderer<PechEntity, PechRenderer.State, PechModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("pech"), "main");
    private static final Identifier[] SKINS = {
            Thaumcraft.id("textures/models/pech_forage.png"),
            Thaumcraft.id("textures/models/pech_thaum.png"),
            Thaumcraft.id("textures/models/pech_stalker.png"),
    };

    public static class State extends LivingEntityRenderState {
        int type;
        float mumble;
        boolean passenger;
        boolean crouching;
        float attack;
        final ItemStackRenderState held = new ItemStackRenderState();
        boolean bow;
        boolean wand;
        boolean tool;
        boolean rod;
    }

    public PechRenderer(EntityRendererProvider.Context context) {
        super(context, new PechModel(context.bakeLayer(LAYER)), 0.25f);
        this.addLayer(new HeldItem(this));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(PechEntity pech, State state, float partial) {
        super.extractRenderState(pech, state, partial);
        state.type = Math.max(0, Math.min(2, pech.pechType()));
        state.mumble = pech.mumble;
        state.passenger = pech.isPassenger();
        state.crouching = pech.isCrouching();
        state.attack = pech.getAttackAnim(partial);
        ItemStack stack = pech.getMainHandItem();
        state.bow = stack.is(Items.BOW);
        state.wand = stack.is(TCItems.WAND);
        state.rod = stack.is(Items.FISHING_ROD);
        // o isFull3D do jogo de então: espadas, machados, picaretas, pás, enxadas, varas e a varinha
        state.tool = state.wand || state.rod || stack.is(net.minecraft.tags.ItemTags.SWORDS) || stack.is(net.minecraft.tags.ItemTags.AXES)
                || stack.is(net.minecraft.tags.ItemTags.PICKAXES) || stack.is(net.minecraft.tags.ItemTags.SHOVELS)
                || stack.is(net.minecraft.tags.ItemTags.HOES);
        this.itemModelResolver.updateForLiving(state.held, stack, ItemDisplayContext.NONE, pech);
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return SKINS[state.type];
    }

    /** O {@code renderEquippedItems} do {@code RenderPech}. */
    static class HeldItem extends RenderLayer<State, PechModel> {
        HeldItem(PechRenderer parent) {
            super(parent);
        }

        @Override
        public void submit(PoseStack pose, SubmitNodeCollector collector, int light, State state, float yRot, float xRot) {
            if (state.held.isEmpty()) return;
            pose.pushPose();
            this.getParentModel().rightArm.translateAndRotate(pose);
            pose.translate(-0.0625f, 0.3375f, 0.0625f);
            if (state.bow) {
                float s = 0.625f;
                pose.translate(0.0f, 0.125f, 0.3125f);
                pose.mulPose(Axis.YP.rotationDegrees(-20.0f));
                pose.scale(s, -s, s);
                pose.mulPose(Axis.XP.rotationDegrees(-100.0f));
                pose.mulPose(Axis.YP.rotationDegrees(45.0f));
            } else if (state.tool) {
                float s = 0.625f;
                if (state.wand) pose.translate(0.0f, -0.125f, 0.0f);
                if (state.rod) {
                    // a vara de pesca gira em volta de si ao ser desenhada (o shouldRotateAroundWhenRendering)
                    pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
                    pose.translate(0.0f, -0.125f, 0.0f);
                }
                pose.translate(0.0f, 0.1875f, 0.0f);
                pose.scale(s, -s, s);
                pose.mulPose(Axis.XP.rotationDegrees(-100.0f));
                pose.mulPose(Axis.YP.rotationDegrees(45.0f));
            } else {
                float s = 0.375f;
                pose.translate(0.25f, 0.1875f, -0.1875f);
                pose.scale(s, s, s);
                pose.mulPose(Axis.ZP.rotationDegrees(60.0f));
                pose.mulPose(Axis.XP.rotationDegrees(-90.0f));
                pose.mulPose(Axis.ZP.rotationDegrees(20.0f));
            }
            // o item de 2014 era desenhado de (0, 0, 0) a (1, 1, -1/16); o de hoje, centrado no meio
            pose.translate(0.5f, 0.5f, -0.03125f);
            state.held.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
            pose.popPose();
        }
    }
}
