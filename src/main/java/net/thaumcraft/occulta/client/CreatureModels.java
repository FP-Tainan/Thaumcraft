package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import java.util.List;

/**
 * Os modelos dos bichos do Ars Occulta: os {@code ModelBase} do Witchery, caixa por caixa.
 *
 * <p><b>Arquivo gerado</b> por {@code scratchpad/wi-criaturas.js} a partir do jar original — não se escreve à
 * mão. Cada peça traz o ponto de giro, o giro parado, se é espelhada e as caixas dela, e a lista de nomes diz
 * em que ordem o original as desenha.
 */
public final class CreatureModels {
    private CreatureModels() {
    }

    /** O {@code ModelMandrake}: 6 peças numa folha de 64 por 32. */
    public static final List<String> MANDRAKE_PARTS = List.of("head", "body", "rightarm", "leftarm", "rightleg", "leftleg");

    public static LayerDefinition mandrake() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition raiz = mesh.getRoot();
        PartDefinition parte_head = raiz.addOrReplaceChild("head", CubeListBuilder.create()
                .mirror(true)
                .texOffs(0, 8).addBox(-2f, -4f, -2f, 4f, 4f, 4f)
                .texOffs(0, 0).addBox(-4f, -12f, 0f, 8f, 8f, 0f)
                , PartPose.offsetAndRotation(0f, 16f, 0f, 0f, 0f, 0f));
        PartDefinition parte_body = raiz.addOrReplaceChild("body", CubeListBuilder.create()
                .mirror(true)
                .texOffs(21, 0).addBox(-2.5f, 0f, -2.5f, 5f, 2f, 5f)
                .texOffs(17, 7).addBox(-3.5f, 2f, -3.5f, 7f, 3f, 7f)
                , PartPose.offsetAndRotation(0f, 16f, 0f, 0f, 0f, 0f));
        PartDefinition parte_rightarm = raiz.addOrReplaceChild("rightarm", CubeListBuilder.create()
                .texOffs(37, 0).addBox(-1f, 0f, -0.5f, 1f, 3f, 1f)
                , PartPose.offsetAndRotation(-2f, 17f, 0f, 0f, 0f, 1.0472f));
        PartDefinition parte_leftarm = raiz.addOrReplaceChild("leftarm", CubeListBuilder.create()
                .texOffs(37, 0).addBox(0f, 0f, -0.5f, 1f, 3f, 1f)
                , PartPose.offsetAndRotation(2f, 17f, 0f, 0f, 0f, -1.0472f));
        PartDefinition parte_rightleg = raiz.addOrReplaceChild("rightleg", CubeListBuilder.create()
                .texOffs(27, 18).addBox(-1f, 0f, -1f, 2f, 3f, 2f)
                , PartPose.offsetAndRotation(-1f, 21f, 0f, 0f, 0f, 0f));
        PartDefinition parte_leftleg = raiz.addOrReplaceChild("leftleg", CubeListBuilder.create()
                .texOffs(27, 18).addBox(-1f, 0f, -1f, 2f, 3f, 2f)
                , PartPose.offsetAndRotation(1f, 21f, 0f, 0f, 0f, 0f));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** O {@code ModelMindrake}: 5 peças numa folha de 64 por 32. */
    public static final List<String> MINEDRAKE_PARTS = List.of("leaves", "bodyTop", "bodyBottom", "legLeft", "legRight");

    public static LayerDefinition minedrake() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition raiz = mesh.getRoot();
        PartDefinition parte_leaves = raiz.addOrReplaceChild("leaves", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3f, 0f, 0f, 6f, 6f, 0f)
                , PartPose.offsetAndRotation(0f, 13f, 0f, 0f, 0f, 0f));
        PartDefinition parte_bodyTop = raiz.addOrReplaceChild("bodyTop", CubeListBuilder.create()
                .texOffs(0, 7).addBox(-1.5f, 0f, -1.5f, 3f, 1f, 3f)
                , PartPose.offsetAndRotation(0f, 19f, 0f, 0f, 0f, 0f));
        PartDefinition parte_bodyBottom = raiz.addOrReplaceChild("bodyBottom", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-2f, 0f, -2f, 4f, 3f, 4f)
                , PartPose.offsetAndRotation(0f, 20f, 0f, 0f, 0f, 0f));
        PartDefinition parte_legLeft = raiz.addOrReplaceChild("legLeft", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-0.5f, 0f, -0.5f, 1f, 1f, 1f)
                , PartPose.offsetAndRotation(1f, 23f, 0f, 0f, 0f, 0f));
        PartDefinition parte_legRight = raiz.addOrReplaceChild("legRight", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-0.5f, 0f, -0.5f, 1f, 1f, 1f)
                , PartPose.offsetAndRotation(-1f, 23f, 0f, 0f, 0f, 0f));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** O {@code ModelEnt}: 16 peças numa folha de 256 por 256. */
    public static final List<String> ENT_PARTS = List.of("ArmLeft", "ArmRight", "Body", "Leg8", "Leg6", "Leg4", "Leg2", "Leg7", "Leg5", "Leg3", "Leg1", "LeavesBaseInner", "LeavesTopInner", "LeavesBase", "LeavesTop", "Face");

    public static LayerDefinition ent() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition raiz = mesh.getRoot();
        PartDefinition parte_ArmLeft = raiz.addOrReplaceChild("ArmLeft", CubeListBuilder.create()
                .texOffs(82, 0).addBox(0f, -22f, -3f, 6f, 24f, 6f)
                , PartPose.offsetAndRotation(8f, -4f, 0f, 0f, 0f, 0f));
        PartDefinition parte_ArmRight = raiz.addOrReplaceChild("ArmRight", CubeListBuilder.create()
                .texOffs(82, 0).addBox(-6f, -22f, -3f, 6f, 24f, 6f)
                , PartPose.offsetAndRotation(-8f, -4f, 0f, 0f, 0f, 0f));
        PartDefinition parte_Body = raiz.addOrReplaceChild("Body", CubeListBuilder.create()
                .texOffs(0, 50).addBox(-8f, -46f, -8f, 16f, 48f, 16f)
                , PartPose.offsetAndRotation(0f, 20f, 0f, 0f, 0f, 0f));
        PartDefinition parte_Leg8 = raiz.addOrReplaceChild("Leg8", CubeListBuilder.create()
                .texOffs(18, 0).addBox(-3f, -1f, -1f, 16f, 2f, 2f)
                , PartPose.offsetAndRotation(4f, 20f, -1f, 0f, 0.57596f, 0.19199f));
        PartDefinition parte_Leg6 = raiz.addOrReplaceChild("Leg6", CubeListBuilder.create()
                .texOffs(18, 0).addBox(-3f, -1f, -1f, 16f, 2f, 2f)
                , PartPose.offsetAndRotation(4f, 20f, 0f, 0f, 0.27925f, 0.19199f));
        PartDefinition parte_Leg4 = raiz.addOrReplaceChild("Leg4", CubeListBuilder.create()
                .texOffs(18, 0).addBox(-3f, -1f, -1f, 16f, 2f, 2f)
                , PartPose.offsetAndRotation(4f, 20f, 1f, 0f, -0.27925f, 0.19199f));
        PartDefinition parte_Leg2 = raiz.addOrReplaceChild("Leg2", CubeListBuilder.create()
                .texOffs(18, 0).addBox(-3f, -1f, -1f, 16f, 2f, 2f)
                , PartPose.offsetAndRotation(4f, 20f, 2f, 0f, -0.57596f, 0.19199f));
        PartDefinition parte_Leg7 = raiz.addOrReplaceChild("Leg7", CubeListBuilder.create()
                .texOffs(18, 0).addBox(-13f, -1f, -1f, 16f, 2f, 2f)
                , PartPose.offsetAndRotation(-4f, 20f, -1f, 0f, -0.57596f, -0.19199f));
        PartDefinition parte_Leg5 = raiz.addOrReplaceChild("Leg5", CubeListBuilder.create()
                .texOffs(18, 0).addBox(-13f, -1f, -1f, 16f, 2f, 2f)
                , PartPose.offsetAndRotation(-4f, 20f, 0f, 0f, -0.27925f, -0.19199f));
        PartDefinition parte_Leg3 = raiz.addOrReplaceChild("Leg3", CubeListBuilder.create()
                .texOffs(18, 0).addBox(-13f, -1f, -1f, 16f, 2f, 2f)
                , PartPose.offsetAndRotation(-4f, 20f, 1f, 0f, 0.27925f, -0.19199f));
        PartDefinition parte_Leg1 = raiz.addOrReplaceChild("Leg1", CubeListBuilder.create()
                .texOffs(18, 0).addBox(-13f, -1f, -1f, 16f, 2f, 2f)
                , PartPose.offsetAndRotation(-4f, 20f, 2f, 0f, 0.57596f, -0.19199f));
        PartDefinition parte_LeavesBaseInner = raiz.addOrReplaceChild("LeavesBaseInner", CubeListBuilder.create()
                .texOffs(24, 59).addBox(0f, 0f, 0f, 56f, 14f, 56f)
                , PartPose.offsetAndRotation(-28f, -41f, -28f, 0f, 0f, 0f));
        PartDefinition parte_LeavesTopInner = raiz.addOrReplaceChild("LeavesTopInner", CubeListBuilder.create()
                .texOffs(108, 14).addBox(0f, 0f, 0f, 28f, 14f, 28f)
                , PartPose.offsetAndRotation(-14f, -57f, -14f, 0f, 0f, 0f));
        PartDefinition parte_LeavesBase = raiz.addOrReplaceChild("LeavesBase", CubeListBuilder.create()
                .texOffs(0, 180).addBox(0f, 0f, 0f, 60f, 16f, 60f)
                , PartPose.offsetAndRotation(-30f, -42f, -30f, 0f, 0f, 0f));
        PartDefinition parte_LeavesTop = raiz.addOrReplaceChild("LeavesTop", CubeListBuilder.create()
                .texOffs(56, 130).addBox(0f, 0f, 0f, 32f, 16f, 32f)
                , PartPose.offsetAndRotation(-16f, -58f, -16f, 0f, 0f, 0f));
        PartDefinition parte_Face = raiz.addOrReplaceChild("Face", CubeListBuilder.create()
                .texOffs(0, 116).addBox(-8f, -46f, -9f, 16f, 24f, 16f)
                , PartPose.offsetAndRotation(0f, 20f, 0f, 0f, 0f, 0f));
        return LayerDefinition.create(mesh, 256, 256);
    }

}
