package net.thaumcraft.naturalis.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * O {@code TaintBreederModel} do Magia Naturalis 0.5.0: uma aranha grande de oito pernas esticadas, com dois
 * olhos altos, duas presas e um bojo de duas peças atrás. Numa folha de 64 por 64.
 *
 * <p>Como no original, as pernas não andam: o que se move é o bojo, que balança conforme a aranha vai sendo
 * ferida — quanto menos vida ela tem, mais ele sacode —, e as presas junto com ele.
 */
public class TaintBreederModel extends EntityModel<TaintBreederRenderer.State> {
    /** A inclinação do bojo e das presas, do original. */
    private static final float REAR_TILT = 0.3490659f;
    private static final float CLAW_TILT = -0.0194155f;
    private static final float CLAW2_TILT = 0.0400703f;

    private final ModelPart rear;
    private final ModelPart rearEnd;
    private final ModelPart claw;
    private final ModelPart claw2;

    public TaintBreederModel(ModelPart root) {
        super(root);
        this.rear = root.getChild("rear");
        this.rearEnd = root.getChild("rear_end");
        this.claw = root.getChild("claw");
        this.claw2 = root.getChild("claw2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(32, 4).addBox(-2.5f, -4.0f, -5.0f, 5, 6, 6),
                PartPose.offsetAndRotation(0.0f, 20.0f, -3.0f, 0.0698132f, 0.0f, 0.0f));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0f, -3.0f, -3.0f, 6, 6, 8),
                PartPose.offsetAndRotation(0.0f, 20.0f, 0.0f, 0.1396263f, 0.0f, 0.0f));
        root.addOrReplaceChild("rear", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-5.0f, -4.0f, 0.0f, 10, 9, 12),
                PartPose.offsetAndRotation(0.0f, 19.0f, 3.0f, REAR_TILT, 0.0f, 0.0f));
        root.addOrReplaceChild("rear_end", CubeListBuilder.create()
                .texOffs(8, 35).addBox(-3.5f, -3.0f, 12.0f, 7, 7, 2),
                PartPose.offsetAndRotation(0.0f, 19.0f, 3.0f, REAR_TILT, 0.0f, 0.0f));

        // as quatro pernas de cada lado, na mesma altura, uma atrás da outra
        float[] recuo = {-1.0f, 0.0f, 1.0f, 2.0f};
        for (int i = 0; i < 4; i++) {
            root.addOrReplaceChild("right_leg" + i, CubeListBuilder.create()
                    .texOffs(21, 0).addBox(-1.0f, -1.0f, -1.0f, 16, 2, 2),
                    PartPose.offset(4.0f, 20.0f, recuo[i]));
            root.addOrReplaceChild("left_leg" + i, CubeListBuilder.create()
                    .mirror().texOffs(21, 0).addBox(-15.0f, -1.0f, -1.0f, 16, 2, 2),
                    PartPose.offset(-4.0f, 20.0f, recuo[i]));
        }

        root.addOrReplaceChild("claw", CubeListBuilder.create()
                .texOffs(32, 16).addBox(-1.5f, -2.5f, -11.0f, 3, 2, 7),
                PartPose.offsetAndRotation(0.0f, 20.0f, -3.0f, CLAW_TILT, 0.0f, 0.0f));
        root.addOrReplaceChild("claw2", CubeListBuilder.create()
                .mirror().texOffs(44, 26).addBox(-1.5f, -0.5f, -11.0f, 3, 2, 7),
                PartPose.offsetAndRotation(0.0f, 20.0f, -3.0f, CLAW2_TILT, 0.0f, 0.0f));

        // os dois olhos, altos na cabeça
        root.addOrReplaceChild("right_eye", CubeListBuilder.create()
                .mirror().texOffs(0, 35).addBox(-3.5f, -5.0f, -3.0f, 2, 4, 2),
                PartPose.offsetAndRotation(0.0f, 20.0f, -3.0f, 0.0698132f, 0.0f, 0.0f));
        root.addOrReplaceChild("left_eye", CubeListBuilder.create()
                .texOffs(0, 35).addBox(1.5f, -5.0f, -3.0f, 2, 4, 2),
                PartPose.offsetAndRotation(0.0f, 20.0f, -3.0f, 0.0698132f, 0.0f, 0.0f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(TaintBreederRenderer.State state) {
        super.setupAnim(state);
        // o balanço do bojo: a conta do original, sobre a vida que ela já perdeu
        float perdida = Math.max(state.maxHealth - state.health, 1.0f) * 0.5f;
        float balanco = Mth.sin(state.ageInTicks) * 0.0133f * perdida;
        this.rear.yRot = balanco;
        this.rearEnd.yRot = balanco;
        this.rear.xRot = REAR_TILT + balanco;
        this.rearEnd.xRot = REAR_TILT + balanco;
        this.claw.xRot = CLAW_TILT + balanco * 0.5f;
        this.claw2.xRot = -CLAW_TILT - balanco * 0.25f;
    }
}
