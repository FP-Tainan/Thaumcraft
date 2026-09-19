package net.thaumcraft.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.eldritch.EldritchGuardianEntity;
import net.thaumcraft.world.OuterLands;

/**
 * O guardião eldritch: o {@code RenderEldritchGuardian} e o {@code ModelEldritchGuardian} da 4.2.3.5. Fora das Terras de
 * Fora é um vulto: a sessenta por cento até dezesseis blocos, sumindo até os trinta e dois (vinte e quatro no difícil).
 * O capuz segue o olhar, os braços sobem no golpe e no grito, e a saia, o manto e as abas balançam sem parar.
 */
public class EldritchGuardianRenderer extends MobRenderer<EldritchGuardianEntity, EldritchGuardianRenderer.State, EldritchGuardianRenderer.Model> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("eldritch_guardian"), "main");
    private static final Identifier SKIN = Thaumcraft.id("textures/models/eldritch_guardian.png");

    public static class State extends LivingEntityRenderState {
        float alpha = 1.0f;
        float armLiftL, armLiftR;
    }

    public EldritchGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(LAYER)), 0.5f);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EldritchGuardianEntity guardian, State state, float partial) {
        super.extractRenderState(guardian, state, partial);
        state.armLiftL = guardian.armLiftL;
        state.armLiftR = guardian.armLiftR;
        var viewer = Minecraft.getInstance().getCameraEntity();
        if (OuterLands.is(guardian.level()) || viewer == null) {
            state.alpha = 1.0f;
        } else {
            float d6 = guardian.level().getDifficulty() == Difficulty.HARD ? 576.0f : 1024.0f;
            float d7 = 256.0f;
            double d8 = guardian.distanceToSqr(viewer.getX(), viewer.getY(), viewer.getZ());
            state.alpha = d8 < 256.0 ? 0.6f : (float) (1.0 - Math.min(d6 - d7, d8 - d7) / (d6 - d7)) * 0.6f;
        }
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return SKIN;
    }

    @Override
    protected RenderType getRenderType(State state, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderTypes.entityTranslucent(SKIN);
    }

    @Override
    protected int getModelTint(State state) {
        return ARGB.colorFromFloat(state.alpha, 1.0f, 1.0f, 1.0f);
    }

    public static class Model extends EntityModel<State> {
        private final ModelPart hood1, hoodEye, armL1, armR1, legpanelC1, legpanelC2, legpanelC3, cloak1, cloak2, cloak3;
        private final ModelPart sidepanelL2, sidepanelL3, sidepanelL4, sidepanelR2, sidepanelR3, sidepanelR4;

        public Model(ModelPart root) {
            super(root);
            this.hood1 = root.getChild("hood1");
            this.hoodEye = root.getChild("hoodeye");
            this.armL1 = root.getChild("arml1");
            this.armR1 = root.getChild("armr1");
            this.legpanelC1 = root.getChild("legpanelc1");
            this.legpanelC2 = this.legpanelC1.getChild("legpanelc2");
            this.legpanelC3 = this.legpanelC2.getChild("legpanelc3");
            this.cloak1 = root.getChild("cloak1");
            this.cloak2 = this.cloak1.getChild("cloak2");
            this.cloak3 = this.cloak2.getChild("cloak3");
            this.sidepanelL2 = root.getChild("sidepanell2");
            this.sidepanelL3 = this.sidepanelL2.getChild("sidepanell3");
            this.sidepanelL4 = this.sidepanelL3.getChild("sidepanell4");
            this.sidepanelR2 = root.getChild("sidepanelr2");
            this.sidepanelR3 = this.sidepanelR2.getChild("sidepanelr3");
            this.sidepanelR4 = this.sidepanelR3.getChild("sidepanelr4");
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            // o olho do capuz é só do guardião-mor
            this.hoodEye.visible = false;
            this.hood1.yRot = state.yRot * Mth.DEG_TO_RAD;
            this.hood1.xRot = state.xRot * Mth.DEG_TO_RAD;
            this.hoodEye.xRot = this.hood1.xRot;
            this.hoodEye.yRot = this.hood1.yRot;
            float t = state.ageInTicks;
            this.armL1.xRot = -1.0f - state.armLiftL + Mth.sin((t + 20.0f) / 10.0f) * 0.08f;
            this.armR1.xRot = -1.0f - state.armLiftR + Mth.sin(t / 10.0f) * 0.08f;
            this.legpanelC1.xRot = -0.15f + Mth.sin(t / 8.0f) * 0.12f;
            this.legpanelC2.xRot = Mth.sin((t - 5.0f) / 8.0f) * 0.13f;
            this.legpanelC3.xRot = Mth.sin((t - 10.0f) / 8.0f) * 0.14f;
            this.cloak1.xRot = 0.2f + Mth.sin(t / 7.0f) * 0.08f;
            this.cloak2.xRot = Mth.sin((t - 5.0f) / 7.0f) * 0.1f;
            this.cloak3.xRot = Mth.sin((t - 10.0f) / 7.0f) * 0.12f;
            this.sidepanelL2.zRot = -0.2f + Mth.sin((t + 10.0f) / 8.0f) * 0.12f;
            this.sidepanelL3.zRot = Mth.sin((t + 5.0f) / 8.0f) * 0.13f;
            this.sidepanelL4.zRot = Mth.sin(t / 8.0f) * 0.14f;
            this.sidepanelR2.zRot = 0.2f + Mth.sin((t - 5.0f) / 8.0f) * 0.12f;
            this.sidepanelR3.zRot = Mth.sin((t - 10.0f) / 8.0f) * 0.13f;
            this.sidepanelR4.zRot = Mth.sin((t - 15.0f) / 8.0f) * 0.14f;
        }
    }
}
