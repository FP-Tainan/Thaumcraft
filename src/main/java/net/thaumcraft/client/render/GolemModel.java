package net.thaumcraft.client.render;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * O corpo do golem: o {@code ModelGolem} da 4.2.3.5, caixa por caixa, com as peças 30 unidades abaixo (o
 * {@code ModelGolem(false)} do {@code RenderGolemBase}) e tudo encolhido a quatro décimos dentro do modelo, como o
 * {@code glScaled(0.4)} do original. As poses são as do {@code setRotationAngles} e do {@code setLivingAnimations}:
 * cabeça baixa sem núcleo (ou algemado), a cabeça subindo no despertar, os braços no passo, ao carregar, ao bater, e os
 * braços abertos do golem alquimista conforme o tamanho do jarro.
 */
public class GolemModel extends EntityModel<GolemRenderer.State> {
    private static final float F2 = 30.0f;
    private final ModelPart golem;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public GolemModel(ModelPart root) {
        super(root);
        this.golem = root.getChild("golem");
        this.head = this.golem.getChild("head");
        this.rightArm = this.golem.getChild("right_arm");
        this.leftArm = this.golem.getChild("left_arm");
        this.rightLeg = this.golem.getChild("right_leg");
        this.leftLeg = this.golem.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition g = mesh.getRoot().addOrReplaceChild("golem", CubeListBuilder.create(), PartPose.ZERO.scaled(0.4f));
        g.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -11.0f, -5.5f, 8, 9, 8),
                PartPose.offset(0.0f, F2, -2.0f));
        g.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 40).addBox(-8.0f, -2.0f, -6.0f, 16, 12, 11)
                        .texOffs(0, 70).addBox(-4.5f, 10.0f, -3.0f, 9, 5, 6, new CubeDeformation(0.5f)),
                PartPose.offset(0.0f, F2, 0.0f));
        g.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(60, 21).addBox(-12.0f, -2.5f, -3.0f, 4, 25, 6),
                PartPose.offset(0.0f, F2, 0.0f));
        g.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror().texOffs(60, 21).addBox(8.0f, -2.5f, -3.0f, 4, 25, 6),
                PartPose.offset(0.0f, F2, 0.0f));
        g.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(37, 0).addBox(-3.5f, -3.0f, -3.0f, 6, 16, 5),
                PartPose.offset(-4.0f, 18.0f + F2, 0.0f));
        g.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror().texOffs(37, 0).addBox(-3.5f, -3.0f, -3.0f, 6, 16, 5),
                PartPose.offset(5.0f, 18.0f + F2, 0.0f));
        return LayerDefinition.create(mesh, 128, 128);
    }

    /** O {@code func_78172_a}: a onda triangular do passo, de −1 a 1. */
    static float tri(float a, float b) {
        return (Math.abs(a % b - b * 0.5f) - b * 0.25f) / (b * 0.25f);
    }

    /** A parte que encolhe tudo (para quem desenha coisas presas ao braço saber onde ele está). */
    public ModelPart golem() {
        return this.golem;
    }

    public ModelPart rightArm() {
        return this.rightArm;
    }

    @Override
    public void setupAnim(GolemRenderer.State s) {
        super.setupAnim(s);
        // o setLivingAnimations: os braços
        float swing = s.walkAnimationPos, amount = s.walkAnimationSpeed, partial = s.ageInTicks - Mth.floor(s.ageInTicks);
        if (s.actionTimer > 0) {
            this.rightArm.xRot = -2.0f + 1.5f * tri(s.actionTimer - partial, 5.0f);
            this.leftArm.xRot = -2.0f + 1.5f * tri(s.actionTimer - partial, 5.0f);
        } else if (s.leftArm <= 0 && s.rightArm <= 0) {
            if (!s.carrying && !s.bucket) {
                this.rightArm.xRot = (-0.2f + 1.5f * tri(swing, 13.0f)) * amount;
                this.leftArm.xRot = (-0.2f - 1.5f * tri(swing, 13.0f)) * amount;
            } else {
                this.rightArm.xRot = -1.0f;
                this.leftArm.xRot = -1.0f;
            }
        } else {
            if (s.leftArm > 0) this.leftArm.xRot = -2.0f + 1.5f * tri(s.leftArm - partial, 20.0f);
            if (s.rightArm > 0) this.rightArm.xRot = -2.0f + 1.5f * tri(s.rightArm - partial, 20.0f);
        }
        // o setRotationAngles: a cabeça, as pernas e os braços do alquimista
        if (s.core != -1 && !(s.bootup < 0.0f)) {
            if (s.inactive) {
                this.head.yRot = 0.0f;
                this.head.xRot = 0.57595867f;
            } else if (s.bootup > 0.0f) {
                this.head.yRot = 0.0f;
                this.head.xRot = s.bootup * Mth.DEG_TO_RAD;
            } else {
                this.head.yRot = s.yRot * Mth.DEG_TO_RAD;
                this.head.xRot = s.xRot * Mth.DEG_TO_RAD;
            }
            this.rightLeg.xRot = -1.5f * tri(swing, 13.0f) * amount;
            this.leftLeg.xRot = 1.5f * tri(swing, 13.0f) * amount;
            this.leftArm.zRot = 0.0f;
            this.rightArm.zRot = 0.0f;
            if (s.core == 6) {
                float sp = (1.0f - (0.5f + Math.min(64, s.carryLimit) / 128.0f)) * 25.0f;
                this.leftArm.zRot = sp * Mth.DEG_TO_RAD;
                this.rightArm.zRot = -sp * Mth.DEG_TO_RAD;
            }
        } else {
            this.head.yRot = 0.0f;
            this.head.xRot = 0.57595867f;
            this.rightLeg.xRot = 0.0f;
            this.leftLeg.xRot = 0.0f;
            this.rightArm.xRot = 0.0f;
            this.leftArm.xRot = 0.0f;
        }
    }
}
