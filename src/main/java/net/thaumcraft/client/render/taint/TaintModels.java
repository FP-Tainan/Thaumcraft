package net.thaumcraft.client.render.taint;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/**
 * Os modelos dos bichos maculados, com a geometria dos modelos do jogo de 1.7.10 que o Thaumcraft 4.2.3.5 usava (as
 * texturas do mod são desenhadas para eles): o {@code ModelChicken}, o {@code ModelQuadruped} com o {@code ModelCow} e o
 * {@code ModelPig}, o {@code ModelTaintSheep1}/{@code 2} do mod, o {@code ModelCreeper} e o {@code ModelVillager}.
 */
public final class TaintModels {
    private TaintModels() {
    }

    /** O que os bichos maculados precisam para se mexer. */
    public static class State extends LivingEntityRenderState {
        /** A galinha: o giro das asas. */
        public float wing;
        /** A ovelha: a cabeça descendo para pastar e o giro dela. */
        public float headEatPosition, headEatAngle;
        public boolean sheared;
        /** O creeper: o quanto está para estourar. */
        public float swelling;
    }

    static PartPose at(float x, float y, float z) {
        return PartPose.offset(x, y, z);
    }

    // ------------------------------------------------------------------------------------------------ a galinha

    public static LayerDefinition chicken() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2, -6, -2, 4, 6, 3), at(0, 15, -4));
        root.addOrReplaceChild("bill", CubeListBuilder.create().texOffs(14, 0).addBox(-2, -4, -4, 4, 2, 2), at(0, 15, -4));
        root.addOrReplaceChild("chin", CubeListBuilder.create().texOffs(14, 4).addBox(-1, -2, -3, 2, 2, 2), at(0, 15, -4));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 9).addBox(-3, -4, -3, 6, 8, 6), at(0, 16, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(26, 0).addBox(-1, 0, -3, 3, 5, 3), at(-2, 19, 1));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(26, 0).addBox(-1, 0, -3, 3, 5, 3), at(1, 19, 1));
        root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(24, 13).addBox(0, 0, -3, 1, 4, 6), at(-4, 13, 0));
        root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(24, 13).addBox(-1, 0, -3, 1, 4, 6), at(4, 13, 0));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static class Chicken extends EntityModel<State> {
        private final ModelPart head, bill, chin, body, rightLeg, leftLeg, rightWing, leftWing;

        public Chicken(ModelPart root) {
            super(root);
            this.head = root.getChild("head");
            this.bill = root.getChild("bill");
            this.chin = root.getChild("chin");
            this.body = root.getChild("body");
            this.rightLeg = root.getChild("right_leg");
            this.leftLeg = root.getChild("left_leg");
            this.rightWing = root.getChild("right_wing");
            this.leftWing = root.getChild("left_wing");
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
            this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
            this.bill.xRot = this.chin.xRot = this.head.xRot;
            this.bill.yRot = this.chin.yRot = this.head.yRot;
            this.body.xRot = (float) (Math.PI / 2);
            float pos = state.walkAnimationPos, speed = state.walkAnimationSpeed;
            this.rightLeg.xRot = Mth.cos(pos * 0.6662f) * 1.4f * speed;
            this.leftLeg.xRot = Mth.cos(pos * 0.6662f + (float) Math.PI) * 1.4f * speed;
            this.rightWing.zRot = state.wing;
            this.leftWing.zRot = -state.wing;
        }
    }

    // ------------------------------------------------------------------------------------------------ os de quatro patas

    /** O {@code ModelQuadruped}: cabeça, corpo deitado e quatro pernas da altura dada. */
    static PartDefinition quadruped(MeshDefinition mesh, int legHeight, float inflate) {
        PartDefinition root = mesh.getRoot();
        CubeDeformation d = new CubeDeformation(inflate);
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -4, -8, 8, 8, 8, d), at(0, 18 - legHeight, -6));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 8).addBox(-5, -10, -7, 10, 16, 8, d), at(0, 17 - legHeight, 2));
        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, legHeight, 4, d);
        root.addOrReplaceChild("right_hind_leg", leg, at(-3, 24 - legHeight, 7));
        root.addOrReplaceChild("left_hind_leg", leg, at(3, 24 - legHeight, 7));
        root.addOrReplaceChild("right_front_leg", leg, at(-3, 24 - legHeight, -5));
        root.addOrReplaceChild("left_front_leg", leg, at(3, 24 - legHeight, -5));
        return root;
    }

    public static LayerDefinition cow() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = quadruped(mesh, 12, 0.0f);
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -4, -6, 8, 8, 6)
                .texOffs(22, 0).addBox(-5, -5, -4, 1, 3, 1).texOffs(22, 0).addBox(4, -5, -4, 1, 3, 1), at(0, 4, -8));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(18, 4).addBox(-6, -10, -7, 12, 18, 10)
                .texOffs(52, 0).addBox(-2, 2, -8, 4, 6, 1), at(0, 5, 2));
        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4);
        root.addOrReplaceChild("right_hind_leg", leg, at(-4, 12, 7));
        root.addOrReplaceChild("left_hind_leg", leg, at(4, 12, 7));
        root.addOrReplaceChild("right_front_leg", leg, at(-4, 12, -6));
        root.addOrReplaceChild("left_front_leg", leg, at(4, 12, -6));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition pig() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = quadruped(mesh, 6, 0.0f);
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -4, -8, 8, 8, 8)
                .texOffs(16, 16).addBox(-2, 0, -9, 4, 3, 1), at(0, 12, -6));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** O {@code ModelTaintSheep2}: a ovelha sem lã. */
    public static LayerDefinition sheep() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = quadruped(mesh, 12, 0.0f);
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3, -4, -6, 6, 6, 8), at(0, 6, -8));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 8).addBox(-4, -10, -7, 8, 16, 6), at(0, 5, 2));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** O {@code ModelTaintSheep1}: a lã por cima. */
    public static LayerDefinition sheepWool() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = quadruped(mesh, 12, 0.0f);
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3, -4, -4, 6, 6, 6, new CubeDeformation(0.6f)), at(0, 6, -8));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 8).addBox(-4, -10, -7, 8, 16, 6, new CubeDeformation(1.75f)), at(0, 5, 2));
        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 6, 4, new CubeDeformation(0.5f));
        root.addOrReplaceChild("right_hind_leg", leg, at(-3, 12, 7));
        root.addOrReplaceChild("left_hind_leg", leg, at(3, 12, 7));
        root.addOrReplaceChild("right_front_leg", leg, at(-3, 12, -5));
        root.addOrReplaceChild("left_front_leg", leg, at(3, 12, -5));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** O {@code ModelQuadruped} andando; a ovelha ainda abaixa a cabeça para pastar. */
    public static class Quadruped extends EntityModel<State> {
        private final ModelPart head, body, rightHind, leftHind, rightFront, leftFront;
        private final boolean sheep;

        public Quadruped(ModelPart root, boolean sheep) {
            super(root);
            this.head = root.getChild("head");
            this.body = root.getChild("body");
            this.rightHind = root.getChild("right_hind_leg");
            this.leftHind = root.getChild("left_hind_leg");
            this.rightFront = root.getChild("right_front_leg");
            this.leftFront = root.getChild("left_front_leg");
            this.sheep = sheep;
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
            this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
            this.body.xRot = (float) (Math.PI / 2);
            float pos = state.walkAnimationPos, speed = state.walkAnimationSpeed;
            this.rightHind.xRot = Mth.cos(pos * 0.6662f) * 1.4f * speed;
            this.leftHind.xRot = Mth.cos(pos * 0.6662f + (float) Math.PI) * 1.4f * speed;
            this.rightFront.xRot = Mth.cos(pos * 0.6662f + (float) Math.PI) * 1.4f * speed;
            this.leftFront.xRot = Mth.cos(pos * 0.6662f) * 1.4f * speed;
            if (this.sheep) {
                this.head.y = 6.0f + state.headEatPosition * 9.0f;
                this.head.xRot = state.headEatAngle;
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ o creeper

    public static LayerDefinition creeper() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8), at(0, 4, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4), at(0, 4, 0));
        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 6, 4);
        root.addOrReplaceChild("right_hind_leg", leg, at(-2, 16, 4));
        root.addOrReplaceChild("left_hind_leg", leg, at(2, 16, 4));
        root.addOrReplaceChild("right_front_leg", leg, at(-2, 16, -4));
        root.addOrReplaceChild("left_front_leg", leg, at(2, 16, -4));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static class Creeper extends EntityModel<State> {
        private final ModelPart head, rightHind, leftHind, rightFront, leftFront;

        public Creeper(ModelPart root) {
            super(root);
            this.head = root.getChild("head");
            this.rightHind = root.getChild("right_hind_leg");
            this.leftHind = root.getChild("left_hind_leg");
            this.rightFront = root.getChild("right_front_leg");
            this.leftFront = root.getChild("left_front_leg");
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
            this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
            float pos = state.walkAnimationPos, speed = state.walkAnimationSpeed;
            this.rightHind.xRot = Mth.cos(pos * 0.6662f) * 1.4f * speed;
            this.leftHind.xRot = Mth.cos(pos * 0.6662f + (float) Math.PI) * 1.4f * speed;
            this.rightFront.xRot = Mth.cos(pos * 0.6662f + (float) Math.PI) * 1.4f * speed;
            this.leftFront.xRot = Mth.cos(pos * 0.6662f) * 1.4f * speed;
        }
    }

    // ------------------------------------------------------------------------------------------------ o aldeão

    public static LayerDefinition villager() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -10, -4, 8, 10, 8), at(0, 0, 0));
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(24, 0).addBox(-1, -1, -6, 2, 4, 2), at(0, -2, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 20).addBox(-4, 0, -3, 8, 12, 6)
                .texOffs(0, 38).addBox(-4, 0, -3, 8, 18, 6, new CubeDeformation(0.5f)), at(0, 0, 0));
        root.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(44, 22).addBox(-8, -2, -2, 4, 8, 4)
                .texOffs(44, 22).addBox(4, -2, -2, 4, 8, 4).texOffs(40, 38).addBox(-4, 2, -2, 8, 4, 4), at(0, 3, -1));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-2, 0, -2, 4, 12, 4), at(-2, 12, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror().texOffs(0, 22).addBox(-2, 0, -2, 4, 12, 4), at(2, 12, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static class Villager extends EntityModel<State> {
        private final ModelPart head, arms, rightLeg, leftLeg;

        public Villager(ModelPart root) {
            super(root);
            this.head = root.getChild("head");
            this.arms = root.getChild("arms");
            this.rightLeg = root.getChild("right_leg");
            this.leftLeg = root.getChild("left_leg");
        }

        @Override
        public void setupAnim(State state) {
            super.setupAnim(state);
            this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
            this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
            this.arms.y = 3.0f;
            this.arms.z = -1.0f;
            this.arms.xRot = -0.75f;
            float pos = state.walkAnimationPos, speed = state.walkAnimationSpeed;
            this.rightLeg.xRot = Mth.cos(pos * 0.6662f) * 1.4f * speed * 0.5f;
            this.leftLeg.xRot = Mth.cos(pos * 0.6662f + (float) Math.PI) * 1.4f * speed * 0.5f;
            this.rightLeg.yRot = 0.0f;
            this.leftLeg.yRot = 0.0f;
        }
    }
}
