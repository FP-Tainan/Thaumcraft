package net.thaumcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.entity.eldritch.EldritchGolemEntity;

/**
 * O construto eldritch: o {@code RenderEldritchGolem} e o {@code ModelEldritchGolem} da 4.2.3.5. O modelo a 2,15 vezes,
 * com a cabeça (ou, sem ela, o toco do pescoço), o manto e as abas da frente balançando com o passo, e os dois braços
 * descendo juntos no golpe.
 */
public class EldritchGolemRenderer extends MobRenderer<EldritchGolemEntity, EldritchGolemRenderer.State, EldritchGolemRenderer.Model> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("eldritch_golem"), "main");
    private static final Identifier SKIN = Thaumcraft.id("textures/models/eldritch_golem.png");

    public static class State extends LivingEntityRenderState {
        boolean headless;
        int spawnTimer;
        float attack;
    }

    public EldritchGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(LAYER)), 0.5f);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EldritchGolemEntity golem, State state, float partial) {
        super.extractRenderState(golem, state, partial);
        state.headless = golem.isHeadless();
        state.spawnTimer = golem.getSpawnTimer();
        state.attack = golem.getAttackTimer() > 0 ? golem.getAttackTimer() - partial : 0.0f;
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
    protected void scale(State state, PoseStack pose) {
        pose.scale(2.15f, 2.15f, 2.15f);
    }

    public static class Model extends EntityModel<State> {
        private final ModelPart head, head2, legR, legL, armR, armL, front1, front2, cloak1, cloak2, cloak3;

        public Model(ModelPart root) {
            super(root);
            this.head = root.getChild("head");
            this.head2 = root.getChild("head2");
            this.legR = root.getChild("legr");
            this.legL = root.getChild("legl");
            this.armR = root.getChild("armr");
            this.armL = root.getChild("arml");
            this.front1 = root.getChild("frontcloth1");
            this.front2 = root.getChild("frontcloth2");
            this.cloak1 = root.getChild("cloak1");
            this.cloak2 = root.getChild("cloak2");
            this.cloak3 = root.getChild("cloak3");
        }

        private static float doAbs(float a, float b) {
            return (Math.abs(a % b - b * 0.5f) - b * 0.25f) / (b * 0.25f);
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            this.head.visible = !state.headless;
            this.head2.visible = state.headless;
            float swing = state.walkAnimationPos, amount = state.walkAnimationSpeed;
            if (state.spawnTimer > 0) {
                this.head.yRot = 0.0f;
                this.head.xRot = state.spawnTimer / 2 / (180.0f / Mth.PI);
            } else {
                this.head.yRot = state.yRot / 4.0f / (180.0f / Mth.PI);
                this.head.xRot = state.xRot / 2.0f / (180.0f / Mth.PI);
                this.head2.yRot = state.yRot / (180.0f / Mth.PI);
                this.head2.xRot = state.xRot / (180.0f / Mth.PI);
            }
            this.legR.xRot = Mth.cos(swing * 0.4662f) * 1.4f * amount;
            this.legL.xRot = Mth.cos(swing * 0.4662f + Mth.PI) * 1.4f * amount;
            if (state.attack > 0.0f) {
                this.armR.xRot = -2.0f + 1.5f * doAbs(state.attack, 10.0f);
                this.armL.xRot = -2.0f + 1.5f * doAbs(state.attack, 10.0f);
            } else {
                this.armR.xRot = Mth.cos(swing * 0.4f + Mth.PI) * 2.0f * amount * 0.5f;
                this.armL.xRot = Mth.cos(swing * 0.4f) * 2.0f * amount * 0.5f;
            }
            float a = Mth.cos(swing * 0.44f) * 1.4f * amount;
            float b = Mth.cos(swing * 0.44f + Mth.PI) * 1.4f * amount;
            float c = Math.min(a, b);
            this.front1.xRot = c - 0.1047198f;
            this.front2.xRot = c - 0.3316126f;
            this.cloak1.xRot = -c / 3.0f + 0.1396263f;
            this.cloak2.xRot = -c / 3.0f + 0.3069452f;
            this.cloak3.xRot = -c / 3.0f + 0.4465716f;
        }
    }
}
