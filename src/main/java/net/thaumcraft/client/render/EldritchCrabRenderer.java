package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.eldritch.EldritchCrabEntity;

/**
 * O caranguejo eldritch: o {@code RenderEldritchCrab} e o {@code ModelEldritchCrab} da 4.2.3.5. A carapaça é o elmo dos
 * cavaleiros enquanto ele o tem, e a cauda nua depois; por cima da pele vai a {@code craboverlay.png} acesa (luz 200) e
 * translúcida. As patas batem aos pares ao andar e as pinças abrem e fecham sem parar.
 */
public class EldritchCrabRenderer extends MobRenderer<EldritchCrabEntity, EldritchCrabRenderer.State, EldritchCrabRenderer.Model> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("eldritch_crab"), "main");
    private static final Identifier SKIN = Thaumcraft.id("textures/models/crab.png");
    private static final Identifier OVERLAY = Thaumcraft.id("textures/models/craboverlay.png");

    public static class State extends LivingEntityRenderState {
        boolean helm;
    }

    public EldritchCrabRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(LAYER)), 1.0f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack pose, SubmitNodeCollector collector, int light, State state, float yRot, float xRot) {
                if (state.isInvisible) return;
                collector.submitModel(this.getParentModel(), state, pose, RenderTypes.entityTranslucent(OVERLAY), 200,
                        OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
            }
        });
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EldritchCrabEntity crab, State state, float partial) {
        super.extractRenderState(crab, state, partial);
        state.helm = crab.hasHelm();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return SKIN;
    }

    public static class Model extends EntityModel<State> {
        private final ModelPart tailHelm, tailBare, rClaw1, rClaw2, lClaw1, lClaw2;
        private final ModelPart rrLeg0, rrLeg1, rfLeg0, rfLeg1, lrLeg0, lrLeg1, lfLeg0, lfLeg1;

        public Model(ModelPart root) {
            super(root);
            this.tailHelm = root.getChild("tailhelm");
            this.tailBare = root.getChild("tailbare");
            this.rClaw1 = root.getChild("rclaw1");
            this.rClaw2 = root.getChild("rclaw2");
            this.lClaw1 = root.getChild("lclaw1");
            this.lClaw2 = root.getChild("lclaw2");
            this.rrLeg0 = root.getChild("rrleg0");
            this.rrLeg1 = root.getChild("rrleg1");
            this.rfLeg0 = root.getChild("rfleg0");
            this.rfLeg1 = root.getChild("rfleg1");
            this.lrLeg0 = root.getChild("lrleg0");
            this.lrLeg1 = root.getChild("lrleg1");
            this.lfLeg0 = root.getChild("lfleg0");
            this.lfLeg1 = root.getChild("lfleg1");
        }

        private static void rotation(ModelPart part, float x, float y, float z) {
            part.xRot = x;
            part.yRot = y;
            part.zRot = z;
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            this.tailHelm.visible = state.helm;
            this.tailBare.visible = !state.helm;
            rotation(this.rrLeg1, 0.0f, 0.2094395f, 0.4363323f);
            rotation(this.rfLeg1, 0.0f, -0.2094395f, 0.4363323f);
            rotation(this.lrLeg1, 0.0f, -0.2094395f, -0.4363323f);
            rotation(this.lfLeg1, 0.0f, 0.2094395f, -0.4363323f);
            rotation(this.rrLeg0, 0.0f, 0.2094395f, 0.4363323f);
            rotation(this.rfLeg0, 0.0f, -0.2094395f, 0.4363323f);
            rotation(this.lfLeg0, 0.0f, 0.2094395f, -0.4363323f);
            rotation(this.lrLeg0, 0.0f, -0.2094395f, -0.4363323f);
            float swing = state.walkAnimationPos, amount = state.walkAnimationSpeed;
            float f9 = -(Mth.cos(swing * 0.6662f * 2.0f) * 0.4f) * amount;
            float f10 = -(Mth.cos(swing * 0.6662f * 2.0f + Mth.PI) * 0.4f) * amount;
            for (ModelPart p : new ModelPart[]{this.rrLeg1, this.rrLeg0}) {
                p.yRot += f9;
                p.zRot += f9;
            }
            for (ModelPart p : new ModelPart[]{this.lrLeg1, this.lrLeg0}) {
                p.yRot -= f9;
                p.zRot -= f9;
            }
            for (ModelPart p : new ModelPart[]{this.rfLeg1, this.rfLeg0}) {
                p.yRot += f10;
                p.zRot += f10;
            }
            for (ModelPart p : new ModelPart[]{this.lfLeg1, this.lfLeg0}) {
                p.yRot -= f10;
                p.zRot -= f10;
            }
            this.tailBare.yRot = this.tailHelm.yRot = Mth.cos(swing * 0.6662f) * 2.0f * amount * 0.125f;
            this.tailBare.zRot = this.tailHelm.zRot = Mth.cos(swing * 0.6662f) * amount * 0.125f;
            float t = state.ageInTicks;
            this.rClaw2.xRot = 0.3141593f - Mth.sin(t / 4.0f) * 0.25f;
            this.lClaw2.xRot = 0.3141593f + Mth.sin(t / 4.1f) * 0.25f;
            this.rClaw1.xRot = Mth.sin(t / 4.0f) * 0.125f;
            this.lClaw1.xRot = -Mth.sin(t / 4.1f) * 0.125f;
        }
    }
}
