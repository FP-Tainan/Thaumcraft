package net.thaumcraft.client.render.taint;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/**
 * Os modelos próprios da mácula: o {@code ModelSlime} de 1.7.10 (o miolo e a casca), o {@code ModelTaintSpore}, o
 * {@code ModelTaintSporeSwarmer} e o {@code ModelTaintacle} (os gomos que diminuem 12% a cada um, com a bolinha e a
 * cabeça na ponta).
 */
public final class TaintOddModels {
    private TaintOddModels() {
    }

    public static class State extends LivingEntityRenderState {
        public float size, squish;
        /** O esporo: o tamanho que se vê e o pulsar. */
        public float sporeSize, pulse;
        public boolean hurt;
        /** O tentáculo: agitado, debatendo-se, ferido, atacando; a altura. */
        public boolean agitated;
        public float flail;
        public int hurtTicks, attackTicks;
        public float height;
        public int tickCount;
    }

    // ------------------------------------------------------------------------------------------------ o slime

    public static LayerDefinition slimeInner() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 16).addBox(-3, 17, -3, 6, 6, 6), PartPose.ZERO);
        root.addOrReplaceChild("right_eye", CubeListBuilder.create().texOffs(32, 0).addBox(-3.25f, 18, -3.5f, 2, 2, 2), PartPose.ZERO);
        root.addOrReplaceChild("left_eye", CubeListBuilder.create().texOffs(32, 4).addBox(1.25f, 18, -3.5f, 2, 2, 2), PartPose.ZERO);
        root.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(32, 8).addBox(0, 21, -3.5f, 1, 1, 1), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition slimeOuter() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-4, 16, -4, 8, 8, 8), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static class Plain extends EntityModel<State> {
        public Plain(ModelPart root) {
            super(root);
        }
    }

    // ------------------------------------------------------------------------------------------------ os esporos

    public static LayerDefinition spore() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-6, 2, -6, 12, 12, 12)
                .texOffs(0, 0).addBox(-8, 0, -8, 16, 16, 16), PartPose.offset(0, 24, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** O balanço do esporo (mais forte quando ferido). */
    public static class Spore extends EntityModel<State> {
        private final ModelPart cube;

        public Spore(ModelPart root) {
            super(root);
            this.cube = root.getChild("cube");
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            float intensity = state.hurt ? 0.04f : 0.02f;
            this.cube.xRot = intensity * Mth.sin(state.ageInTicks * 0.05f);
            this.cube.zRot = intensity * Mth.sin(state.ageInTicks * 0.1f);
        }
    }

    /** O enxameador: o cubo de dentro (aceso, pulsando) e o de fora. */
    public static LayerDefinition swarmerInner() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(-8, 0, -8, 16, 16, 16), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition swarmerOuter() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 32).addBox(-8, -8, -8, 16, 16, 16), PartPose.offset(0, 16, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    // ------------------------------------------------------------------------------------------------ o tentáculo

    /** Quanto cada gomo encolhe em relação ao de trás. */
    static final float SHRINK = 0.88f;

    /**
     * O {@code ModelTaintacle} de tantos gomos: a raiz em (0, 12, 0), cada gomo oito acima do anterior e 12% menor (o
     * {@code glScalef} que o {@code ModelRendererTaintacle} põe antes de cada filho), e na ponta a bolinha e a cabeça.
     */
    public static LayerDefinition taintacle(int length) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition parent = root.addOrReplaceChild("tentacle", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -4, -4, 8, 8, 8),
                PartPose.offset(0, 12, 0));
        for (int k = 0; k < length - 1; k++) {
            parent = parent.addOrReplaceChild("tent" + k, CubeListBuilder.create().texOffs(0, 16).addBox(-4, -4, -4, 8, 8, 8),
                    PartPose.offset(0, -8 * SHRINK, 0).scaled(SHRINK));
        }
        parent.addOrReplaceChild("orb", CubeListBuilder.create().texOffs(0, 56).addBox(-2, -2, -2, 4, 4, 4),
                PartPose.offset(0, -8 * SHRINK, 0).scaled(SHRINK));
        parent.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 32).addBox(-6, -6, -6, 12, 12, 12),
                PartPose.offset(0, -8 * SHRINK, 0).scaled(SHRINK));
        return LayerDefinition.create(mesh, 64, 64);
    }

    /**
     * O tentáculo: {@code tips} escolhe a metade que se desenha — os gomos (sem a ponta) ou só a bolinha e a cabeça, que
     * o original desenha com a luz cheia.
     */
    public static class Taintacle extends EntityModel<State> {
        private final ModelPart[] tents;
        private final ModelPart orb, head;
        private final int length;

        public Taintacle(ModelPart root, int length, boolean tips) {
            super(root);
            this.length = length;
            this.tents = new ModelPart[length - 1];
            ModelPart part = root.getChild("tentacle");
            part.skipDraw = tips;
            for (int k = 0; k < length - 1; k++) {
                part = part.getChild("tent" + k);
                part.skipDraw = tips;
                this.tents[k] = part;
            }
            this.orb = part.getChild("orb");
            this.head = part.getChild("head");
            this.orb.visible = tips;
            this.head.visible = tips;
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            // o par6 do setRotationAngles de então é a escala do modelo (1/16), não o tique parcial
            float mod = 0.0625f * 0.2f;
            float fs = state.agitated ? 3.0f : 1.0f - mod;
            float fi = state.flail + (state.hurtTicks <= 0 && state.attackTicks <= 0 ? -mod : mod);
            for (int k = 0; k < this.length - 1; k++) {
                this.tents[k].xRot = 0.15f * fi * Mth.sin(state.ageInTicks * 0.1f * fs - k / 2.0f);
                this.tents[k].zRot = 0.1f / fi * Mth.sin(state.ageInTicks * 0.15f - k / 2.0f);
            }
        }
    }
}
