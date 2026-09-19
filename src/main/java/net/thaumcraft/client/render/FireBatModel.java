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
 * O {@code ModelFireBat} da 4.2.3.5 — o morcego do jogo de então, antes do de hoje: cabeça de seis com as orelhas,
 * corpo de seis por doze com a cauda, e as asas de duas partes, numa folha de 64 por 64. Pendurado, fica de
 * ponta-cabeça com as asas fechadas; voando, bate as asas e balança o corpo.
 */
public class FireBatModel extends EntityModel<FireBatRenderer.State> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart outerRightWing;
    private final ModelPart outerLeftWing;

    public FireBatModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightWing = this.body.getChild("right_wing");
        this.leftWing = this.body.getChild("left_wing");
        this.outerRightWing = this.rightWing.getChild("outer_right_wing");
        this.outerLeftWing = this.leftWing.getChild("outer_left_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0f, -3.0f, -3.0f, 6, 6, 6)
                .texOffs(24, 0).addBox(-4.0f, -6.0f, -2.0f, 3, 4, 1)
                .mirror().texOffs(24, 0).addBox(1.0f, -6.0f, -2.0f, 3, 4, 1), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-3.0f, 4.0f, -3.0f, 6, 12, 6)
                .texOffs(0, 34).addBox(-5.0f, 16.0f, 0.0f, 10, 6, 1), PartPose.ZERO);
        PartDefinition right = body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(42, 0).addBox(-12.0f, 1.0f, 1.5f, 10, 16, 1), PartPose.ZERO);
        right.addOrReplaceChild("outer_right_wing", CubeListBuilder.create()
                .texOffs(24, 16).addBox(-8.0f, 1.0f, 0.0f, 8, 12, 1), PartPose.offset(-12.0f, 1.0f, 1.5f));
        PartDefinition left = body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .mirror().texOffs(42, 0).addBox(2.0f, 1.0f, 1.5f, 10, 16, 1), PartPose.ZERO);
        left.addOrReplaceChild("outer_left_wing", CubeListBuilder.create()
                .mirror().texOffs(24, 16).addBox(0.0f, 1.0f, 0.0f, 8, 12, 1), PartPose.offset(12.0f, 1.0f, 1.5f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(FireBatRenderer.State state) {
        super.setupAnim(state);
        if (state.hanging) {
            this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
            this.head.yRot = (float) Math.PI - state.yRot * Mth.DEG_TO_RAD;
            this.head.zRot = (float) Math.PI;
            this.head.setPos(0.0f, -2.0f, 0.0f);
            this.rightWing.setPos(-3.0f, 0.0f, 3.0f);
            this.leftWing.setPos(3.0f, 0.0f, 3.0f);
            this.body.xRot = (float) Math.PI;
            this.rightWing.xRot = (float) (-Math.PI / 20);
            this.rightWing.yRot = (float) (-Math.PI * 2.0 / 5.0);
            this.outerRightWing.yRot = -1.7278761f;
            this.leftWing.xRot = this.rightWing.xRot;
            this.leftWing.yRot = -this.rightWing.yRot;
            this.outerLeftWing.yRot = -this.outerRightWing.yRot;
        } else {
            this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
            this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
            this.head.zRot = 0.0f;
            this.head.setPos(0.0f, 0.0f, 0.0f);
            this.rightWing.setPos(0.0f, 0.0f, 0.0f);
            this.leftWing.setPos(0.0f, 0.0f, 0.0f);
            this.body.xRot = (float) (Math.PI / 4) + Mth.cos(state.ageInTicks * 0.1f) * 0.15f;
            this.body.yRot = 0.0f;
            this.rightWing.yRot = Mth.cos(state.ageInTicks * 1.3f) * (float) Math.PI * 0.25f;
            this.leftWing.yRot = -this.rightWing.yRot;
            this.outerRightWing.yRot = this.rightWing.yRot * 0.5f;
            this.outerLeftWing.yRot = -this.rightWing.yRot * 0.5f;
        }
    }
}
