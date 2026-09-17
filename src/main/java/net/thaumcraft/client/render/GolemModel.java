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
 * O corpo do golem, refeito do {@code ModelGolem} do Thaumcraft 4.2.3.5.
 *
 * <p>Cada caixa e cada canto de textura aqui foram lidos no próprio {@code ModelGolem} do mod — cabeça de
 * oito por nove por oito no canto (0,0), tronco de dezesseis por doze por onze em (0,40), braços de
 * quatro por vinte e cinco em (60,21), pernas de seis por dezesseis em (37,0), e a saia em (0,70). A
 * folha de textura é de cento e vinte e oito por cento e vinte e oito, como a do original.
 *
 * <p>O golem do original é atarracado e de braços compridos, quase batendo no chão; é essa silhueta que
 * o faz ser reconhecido de longe, e é por isso que as medidas não foram arredondadas.
 */
public class GolemModel extends EntityModel<GolemRenderer.State> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart skirt;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public GolemModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.skirt = root.getChild("skirt");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-4.0f, -11.0f, -5.5f, 8, 9, 8), PartPose.offset(0.0f, 0.0f, 0.0f));

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 40)
                .addBox(-8.0f, -2.0f, -6.0f, 16, 12, 11), PartPose.offset(0.0f, 0.0f, 0.0f));

        root.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(0, 70)
                .addBox(-4.5f, 10.0f, -3.0f, 9, 5, 6, new CubeDeformation(0.5f)),
                PartPose.offset(0.0f, 0.0f, 0.0f));

        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(60, 21)
                .addBox(-12.0f, -2.5f, -3.0f, 4, 25, 6), PartPose.offset(0.0f, 0.0f, 0.0f));

        root.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror()
                .texOffs(60, 21)
                .addBox(8.0f, -2.5f, -3.0f, 4, 25, 6), PartPose.offset(0.0f, 0.0f, 0.0f));

        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(37, 0)
                .addBox(-3.5f, -3.0f, -3.0f, 6, 16, 5), PartPose.offset(-4.0f, 18.0f, 0.0f));

        root.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror()
                .texOffs(37, 0)
                .addBox(-3.5f, -3.0f, -3.0f, 6, 16, 5), PartPose.offset(5.0f, 18.0f, 0.0f));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(GolemRenderer.State state) {
        super.setupAnim(state);

        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;

        // o gingado: pernas e braços em contratempo, no compasso do passo
        float swing = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;
        this.rightLeg.xRot = Mth.cos(swing * 0.6662f) * 1.4f * speed;
        this.leftLeg.xRot = Mth.cos(swing * 0.6662f + (float) Math.PI) * 1.4f * speed;
        this.rightArm.xRot = Mth.cos(swing * 0.6662f + (float) Math.PI) * 0.8f * speed;
        this.leftArm.xRot = Mth.cos(swing * 0.6662f) * 0.8f * speed;

        // e uma respirada de leve no tronco, para ele não parecer uma estátua parada
        this.body.y = Mth.sin(state.ageInTicks * 0.09f) * 0.2f;
        this.skirt.y = this.body.y;
    }
}
