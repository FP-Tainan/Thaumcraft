package net.thaumcraft.client.render;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * O {@code ModelPech} da 4.2.3.5: corpo inclinado, cabeça com o queixão (que mexe quando ele resmunga), as duas
 * mochilas nas costas e braços e pernas curtos, numa folha de 128 por 64. O braço e a perna direitos são os espelhados
 * (o original liga o espelho antes de montar a caixa só nesses dois).
 */
public class PechModel extends EntityModel<PechRenderer.State> {
    private final ModelPart body;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart head;
    private final ModelPart jowls;
    private final ModelPart lowerPack;
    private final ModelPart upperPack;
    final ModelPart rightArm;
    private final ModelPart leftArm;

    public PechModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.head = root.getChild("head");
        this.jowls = root.getChild("jowls");
        this.lowerPack = root.getChild("lower_pack");
        this.upperPack = root.getChild("upper_pack");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(34, 12).addBox(-3, 0, 0, 6, 10, 6),
                PartPose.offsetAndRotation(0, 9, -3, 0.3129957f, 0, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().mirror().texOffs(35, 1).addBox(-2.9f, 0, 0, 3, 6, 3),
                PartPose.offset(0, 18, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(35, 1).addBox(-0.1f, 0, 0, 3, 6, 3),
                PartPose.offset(0, 18, 0));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(2, 11).addBox(-3.5f, -5, -5, 7, 5, 5),
                PartPose.offset(0, 8, 0));
        root.addOrReplaceChild("jowls", CubeListBuilder.create().texOffs(1, 21).addBox(-4, -1, -6, 8, 3, 5),
                PartPose.offset(0, 8, 0));
        root.addOrReplaceChild("lower_pack", CubeListBuilder.create().texOffs(0, 0).addBox(-5, 0, 0, 10, 5, 5),
                PartPose.offsetAndRotation(0, 10, 3.5f, 0.3013602f, 0, 0));
        root.addOrReplaceChild("upper_pack", CubeListBuilder.create().texOffs(64, 1).addBox(-7.5f, -14, 0, 15, 14, 11),
                PartPose.offsetAndRotation(0, 10, 3, 0.4537856f, 0, 0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().mirror().texOffs(52, 2).addBox(-2, 0, -1, 2, 6, 2),
                PartPose.offset(-3, 10, -1));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(52, 2).addBox(0, 0, -1, 2, 6, 2),
                PartPose.offset(3, 10, -1));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** O {@code setRotationAngles} do original. */
    @Override
    public void setupAnim(PechRenderer.State state) {
        super.setupAnim(state);
        float swing = state.walkAnimationPos, speed = state.walkAnimationSpeed, age = state.ageInTicks;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.jowls.yRot = this.head.yRot;
        this.jowls.xRot = this.head.xRot + ((float) (Math.PI / 12) + Mth.cos(swing * 0.6662f) * speed * 0.25f)
                + 0.34906587f * Math.abs(Mth.sin(state.mumble));
        this.rightArm.xRot = Mth.cos(swing * 0.6662f + (float) Math.PI) * 2.0f * speed * 0.5f;
        this.leftArm.xRot = Mth.cos(swing * 0.6662f) * 2.0f * speed * 0.5f;
        this.rightArm.zRot = 0.0f;
        this.leftArm.zRot = 0.0f;
        this.rightLeg.xRot = Mth.cos(swing * 0.6662f) * 1.4f * speed;
        this.leftLeg.xRot = Mth.cos(swing * 0.6662f + (float) Math.PI) * 1.4f * speed;
        this.rightLeg.yRot = 0.0f;
        this.leftLeg.yRot = 0.0f;
        this.lowerPack.yRot = Mth.cos(swing * 0.6662f) * 2.0f * speed * 0.125f;
        this.lowerPack.zRot = Mth.cos(swing * 0.6662f) * 2.0f * speed * 0.125f;
        if (state.passenger) {
            this.rightArm.xRot += (float) (-Math.PI / 5);
            this.leftArm.xRot += (float) (-Math.PI / 5);
            this.rightLeg.xRot = (float) (-Math.PI * 2.0 / 5.0);
            this.leftLeg.xRot = (float) (-Math.PI * 2.0 / 5.0);
            this.rightLeg.yRot = (float) (Math.PI / 10);
            this.leftLeg.yRot = (float) (-Math.PI / 10);
        }
        this.rightArm.yRot = 0.0f;
        this.leftArm.yRot = 0.0f;
        float attack = state.attack;
        if (attack > -9990.0f) {
            this.rightArm.yRot += this.body.yRot;
            this.leftArm.yRot += this.body.yRot;
            this.leftArm.xRot += this.body.yRot;
            float f6 = 1.0f - attack;
            f6 *= f6;
            f6 *= f6;
            f6 = 1.0f - f6;
            float f7 = Mth.sin(f6 * (float) Math.PI);
            float f8 = Mth.sin(attack * (float) Math.PI) * -(this.head.xRot - 0.7f) * 0.75f;
            this.rightArm.xRot = (float) (this.rightArm.xRot - (f7 * 1.2 + f8));
            this.rightArm.yRot += this.body.yRot * 2.0f;
            this.rightArm.zRot = Mth.sin(attack * (float) Math.PI) * -0.4f;
        }
        if (state.crouching) {
            this.rightArm.xRot += 0.4f;
            this.leftArm.xRot += 0.4f;
        }
        this.rightArm.zRot += Mth.cos(age * 0.09f) * 0.05f + 0.05f;
        this.leftArm.zRot -= Mth.cos(age * 0.09f) * 0.05f + 0.05f;
        this.rightArm.xRot += Mth.sin(age * 0.067f) * 0.05f;
        this.leftArm.xRot -= Mth.sin(age * 0.067f) * 0.05f;
    }
}
