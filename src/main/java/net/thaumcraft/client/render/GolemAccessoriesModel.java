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
 * Os acessórios do golem: o {@code ModelGolemAccessories(0, 30)} da 4.2.3.5 — barrete, cartola (com a aba), óculos,
 * visor, gravata, blindagem, lança-dardos e braço de maça; e, no golem avançado, o cérebro no jarro por cima da cabeça
 * e a cara malvada. A cartola e o barrete sobem um nada no golem avançado, para não brigar com o jarro.
 */
public class GolemAccessoriesModel extends EntityModel<GolemRenderer.State> {
    private static final float P2 = 30.0f;
    private final ModelPart fez, glasses, hat, hatRim, bowtie, dartgun, mace, visor, plate, plateLeft, plateRight, jar, brain, evilHead;
    private final ModelPart[] headParts;

    public GolemAccessoriesModel(ModelPart root) {
        super(root);
        ModelPart g = root.getChild("golem");
        this.fez = g.getChild("fez");
        this.glasses = g.getChild("glasses");
        this.hat = g.getChild("hat");
        this.hatRim = g.getChild("hat_rim");
        this.bowtie = g.getChild("bowtie");
        this.dartgun = g.getChild("dartgun");
        this.mace = g.getChild("mace");
        this.visor = g.getChild("visor");
        this.plate = g.getChild("plate");
        this.plateLeft = g.getChild("plate_left");
        this.plateRight = g.getChild("plate_right");
        this.jar = g.getChild("jar");
        this.brain = g.getChild("brain");
        this.evilHead = g.getChild("evil_head");
        this.headParts = new ModelPart[]{this.fez, this.glasses, this.jar, this.brain, this.evilHead, this.visor, this.hat, this.hatRim};
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition g = mesh.getRoot().addOrReplaceChild("golem", CubeListBuilder.create(), PartPose.ZERO.scaled(0.4f));
        PartPose headPivot = PartPose.offset(0.0f, P2, -2.0f);
        PartPose bodyPivot = PartPose.offset(0.0f, P2, 0.0f);
        g.addOrReplaceChild("fez", CubeListBuilder.create().texOffs(0, 94).addBox(-4.5f, -15.0f, -6.0f, 9, 7, 9), headPivot);
        g.addOrReplaceChild("plate", CubeListBuilder.create().texOffs(32, 40).addBox(-6.5f, -1.0f, -7.0f, 13, 12, 13), bodyPivot);
        g.addOrReplaceChild("plate_left", CubeListBuilder.create().texOffs(0, 44).addBox(-8.5f, -4.0f, -6.5f, 3, 6, 12), bodyPivot);
        g.addOrReplaceChild("plate_right", CubeListBuilder.create().mirror().texOffs(0, 44).addBox(5.5f, -4.0f, -6.5f, 3, 6, 12), bodyPivot);
        g.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 110).addBox(-4.5f, -17.0f, -6.0f, 9, 9, 9), headPivot);
        g.addOrReplaceChild("glasses", CubeListBuilder.create().texOffs(0, 80).addBox(-4.5f, -8.0f, -6.0f, 9, 4, 9), headPivot);
        g.addOrReplaceChild("visor", CubeListBuilder.create().texOffs(0, 70).addBox(-5.0f, -8.0f, -6.0f, 10, 5, 5), headPivot);
        g.addOrReplaceChild("hat_rim", CubeListBuilder.create().texOffs(36, 114).addBox(-6.5f, -9.0f, -8.0f, 13, 1, 13), headPivot);
        g.addOrReplaceChild("dartgun", CubeListBuilder.create().texOffs(80, 80).addBox(7.9f, 7.5f, -3.5f, 6, 16, 7), bodyPivot);
        g.addOrReplaceChild("mace", CubeListBuilder.create().texOffs(80, 26).addBox(-13.0f, 15.0f, -5.0f, 6, 8, 10), bodyPivot);
        g.addOrReplaceChild("bowtie", CubeListBuilder.create().texOffs(0, 0).addBox(-8.5f, -2.0f, -6.5f, 17, 4, 12), bodyPivot);
        g.addOrReplaceChild("jar", CubeListBuilder.create().texOffs(96, 56).addBox(-4.0f, -15.0f, -5.5f, 8, 4, 8), headPivot);
        g.addOrReplaceChild("brain", CubeListBuilder.create().texOffs(96, 70).addBox(-3.5f, -14.0f, -5.0f, 7, 3, 7), headPivot);
        g.addOrReplaceChild("evil_head", CubeListBuilder.create().texOffs(64, 65).addBox(-4.0f, -9.0f, -5.5f, 8, 7, 8),
                PartPose.offset(0.0f, P2, -2.0f).scaled(1.01f, 1.0f, 1.01f));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(GolemRenderer.State s) {
        super.setupAnim(s);
        String deco = s.decoration;
        this.dartgun.visible = deco.contains("R");
        this.fez.visible = deco.contains("F");
        this.hat.visible = deco.contains("H");
        this.hatRim.visible = deco.contains("H");
        this.bowtie.visible = deco.contains("B");
        this.plate.visible = this.plateLeft.visible = this.plateRight.visible = deco.contains("P");
        this.glasses.visible = deco.contains("G");
        this.visor.visible = deco.contains("V");
        this.mace.visible = deco.contains("M");
        this.brain.visible = s.advanced;
        this.jar.visible = s.advanced;
        this.evilHead.visible = s.advanced && s.core >= 0;
        // no golem avançado o barrete e a cartola sobem um centésimo (o glTranslatef do original)
        if (s.advanced) {
            this.fez.y -= 0.16f * (deco.contains("F") ? 1 : 0);
            float up = 0.16f * ((deco.contains("F") ? 1 : 0) + 1);
            this.hat.y -= up;
            this.hatRim.y -= up;
        }
        float yaw, pitch;
        if (s.core == -1 || s.bootup < 0.0f || s.inactive) {
            yaw = 0.0f;
            pitch = 0.57595867f;
        } else if (s.bootup > 0.0f) {
            yaw = 0.0f;
            pitch = s.bootup * Mth.DEG_TO_RAD;
        } else {
            yaw = s.yRot * Mth.DEG_TO_RAD;
            pitch = s.xRot * Mth.DEG_TO_RAD;
        }
        for (ModelPart p : this.headParts) {
            p.yRot = yaw;
            p.xRot = pitch;
        }
        float swing = s.walkAnimationPos, amount = s.walkAnimationSpeed, partial = s.ageInTicks - Mth.floor(s.ageInTicks);
        if (s.actionTimer > 0) {
            this.dartgun.xRot = -2.0f + 1.5f * GolemModel.tri(s.actionTimer - partial, 10.0f);
            this.mace.xRot = -2.0f + 1.5f * GolemModel.tri(s.actionTimer - partial, 10.0f);
        } else if (s.leftArm <= 0 && s.rightArm <= 0) {
            if (s.carrying) {
                this.dartgun.xRot = -1.0f;
                this.mace.xRot = -1.0f;
            } else {
                this.dartgun.xRot = (-0.2f - 1.5f * GolemModel.tri(swing, 13.0f)) * amount;
                this.mace.xRot = (-0.2f + 1.5f * GolemModel.tri(swing, 13.0f)) * amount;
            }
        } else {
            if (s.leftArm > 0) this.dartgun.xRot = -2.0f + 1.5f * GolemModel.tri(s.leftArm - partial, 10.0f);
            if (s.rightArm > 0) this.mace.xRot = -2.0f + 1.5f * GolemModel.tri(s.rightArm - partial, 10.0f);
        }
    }
}
