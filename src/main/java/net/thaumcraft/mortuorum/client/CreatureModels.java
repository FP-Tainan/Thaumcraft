package net.thaumcraft.mortuorum.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import java.util.List;

/**
 * Os modelos das criaturas do Ars Mortuorum: os {@code ModelBase} do Necromancy, caixa por caixa.
 *
 * <p><b>Arquivo gerado</b> por {@code scratchpad/am-criaturas.js} a partir do jar original — não se escreve à
 * mão. Cada peça traz o ponto de giro, o giro parado, se é espelhada e as caixas dela, e a lista de nomes diz
 * em que ordem o original as desenha.
 */
public final class CreatureModels {
    private CreatureModels() {
    }

    /** O {@code ModelNightCrawler}: 18 peças numa folha de 64 por 32. */
    public static final List<String> NIGHT_CRAWLER_PARTS = List.of("midBody", "upperBody", "neck", "lowerBody", "Shape1", "Shape2", "Shape7", "Shape8", "Shape9", "Shape10", "Shape11", "Shape12", "Shape13", "Shape14", "Shape15", "headset", "leftarmset", "rightarmset");

    public static LayerDefinition nightCrawler() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition raiz = mesh.getRoot();
        PartDefinition parte_midBody = raiz.addOrReplaceChild("midBody", CubeListBuilder.create()
                .mirror()
                .texOffs(49, 9).addBox(-4f, 0f, -2f, 4f, 5f, 3f)
                , PartPose.offsetAndRotation(2f, 14.46667f, -1.13333f, 0.18589f, 0f, 0f));
        PartDefinition parte_upperBody = raiz.addOrReplaceChild("upperBody", CubeListBuilder.create()
                .mirror()
                .texOffs(45, 18).addBox(0f, 0f, 0f, 6f, 4f, 3f)
                , PartPose.offsetAndRotation(-3f, 12.46667f, -4.66667f, 0.53795f, 0f, 0f));
        PartDefinition parte_neck = raiz.addOrReplaceChild("neck", CubeListBuilder.create()
                .mirror()
                .texOffs(57, 14).addBox(0f, 0f, 0f, 2f, 1f, 1f)
                , PartPose.offsetAndRotation(-1f, 11.33333f, -4f, 0.66922f, 0f, 0f));
        PartDefinition parte_lowerBody = raiz.addOrReplaceChild("lowerBody", CubeListBuilder.create()
                .mirror()
                .texOffs(53, 23).addBox(0f, 0f, 0f, 2f, 5f, 3f)
                , PartPose.offsetAndRotation(-1f, 19f, -2.4f, 0f, 0f, 0f));
        PartDefinition parte_Shape1 = raiz.addOrReplaceChild("Shape1", CubeListBuilder.create()
                .mirror()
                .texOffs(54, 27).addBox(0f, 0f, 0f, 1f, 0f, 3f)
                , PartPose.offsetAndRotation(0f, 23f, 0f, -0.26025f, 0f, 0f));
        PartDefinition parte_Shape2 = raiz.addOrReplaceChild("Shape2", CubeListBuilder.create()
                .mirror()
                .texOffs(54, 28).addBox(0f, 0f, 0f, 1f, 0f, 2f)
                , PartPose.offsetAndRotation(-1f, 23f, 0f, -0.48332f, 0f, 0f));
        PartDefinition parte_Shape7 = raiz.addOrReplaceChild("Shape7", CubeListBuilder.create()
                .mirror()
                .texOffs(54, 30).addBox(0f, 0f, 0f, 1f, 0f, 1f)
                , PartPose.offsetAndRotation(0f, 22f, 0.26667f, -0.92947f, 0f, 0f));
        PartDefinition parte_Shape8 = raiz.addOrReplaceChild("Shape8", CubeListBuilder.create()
                .mirror()
                .texOffs(54, 27).addBox(0f, 0f, 0f, 1f, 0f, 3f)
                , PartPose.offsetAndRotation(1f, 23f, -1.6f, 0f, 0.22307f, 0.81793f));
        PartDefinition parte_Shape9 = raiz.addOrReplaceChild("Shape9", CubeListBuilder.create()
                .mirror()
                .texOffs(54, 28).addBox(0f, 0f, 0f, 1f, 0f, 2f)
                , PartPose.offsetAndRotation(-1f, 23f, -1.86667f, 0f, -0.22307f, 2.24931f));
        PartDefinition parte_Shape10 = raiz.addOrReplaceChild("Shape10", CubeListBuilder.create()
                .mirror()
                .texOffs(55, 27).addBox(0f, 0f, 0f, 1f, 1f, 0f)
                , PartPose.offsetAndRotation(-1f, 21f, -0.53333f, 0f, -2.28648f, 0f));
        PartDefinition parte_Shape11 = raiz.addOrReplaceChild("Shape11", CubeListBuilder.create()
                .mirror()
                .texOffs(54, 28).addBox(0f, 0f, 0f, 1f, 1f, 0f)
                , PartPose.offsetAndRotation(1f, 20f, -2f, 0f, -0.92947f, 0f));
        PartDefinition parte_Shape12 = raiz.addOrReplaceChild("Shape12", CubeListBuilder.create()
                .mirror()
                .texOffs(58, 27).addBox(0f, 0f, 0f, 0f, 1f, 2f)
                , PartPose.offsetAndRotation(-1f, 23f, -1f, 0f, -0.11154f, 0.18589f));
        PartDefinition parte_Shape13 = raiz.addOrReplaceChild("Shape13", CubeListBuilder.create()
                .mirror()
                .texOffs(58, 27).addBox(0f, 0f, 0f, 0f, 1f, 2f)
                , PartPose.offsetAndRotation(1f, 23f, 0f, 0f, 0.18589f, -0.37179f));
        PartDefinition parte_Shape14 = raiz.addOrReplaceChild("Shape14", CubeListBuilder.create()
                .mirror()
                .texOffs(56, 28).addBox(0f, 0f, 0f, 0f, 1f, 1f)
                , PartPose.offsetAndRotation(1f, 23f, -2f, 0f, 0.92947f, 0f));
        PartDefinition parte_Shape15 = raiz.addOrReplaceChild("Shape15", CubeListBuilder.create()
                .mirror()
                .texOffs(59, 28).addBox(0f, 0f, 0f, 0f, 1f, 1f)
                , PartPose.offsetAndRotation(-1f, 23f, -2f, 0f, -0.83652f, 0f));
        PartDefinition parte_headset = raiz.addOrReplaceChild("headset", CubeListBuilder.create()
                .mirror()
                .texOffs(17, 0).addBox(-2f, -9f, -4f, 4f, 5f, 5f)
                .texOffs(52, 19).addBox(1f, -4f, 1f, 1f, 2f, 0f)
                .texOffs(52, 19).addBox(-2f, -4f, 1f, 1f, 2f, 0f)
                .texOffs(52, 19).addBox(2f, -4f, 0f, 0f, 2f, 1f)
                .texOffs(52, 19).addBox(-2f, -4f, 0f, 0f, 2f, 1f)
                .texOffs(18, 13).addBox(-2f, -2f, -3f, 4f, 1f, 4f)
                .texOffs(0, 18).addBox(-1.93333f, -2.46667f, -2f, 0f, 1f, 1f)
                .texOffs(0, 18).addBox(1.86667f, -2.46667f, -2f, 0f, 1f, 1f)
                .texOffs(0, 18).addBox(0.46667f, -2.46667f, -2.93333f, 1f, 1f, 0f)
                .texOffs(0, 18).addBox(-1.46667f, -2.46667f, -2.93333f, 1f, 1f, 0f)
                .texOffs(0, 18).addBox(0.73333f, -4.8f, -3.53333f, 1f, 1f, 0f)
                .texOffs(0, 18).addBox(-1.66667f, -4.8f, -3.46667f, 1f, 1f, 0f)
                , PartPose.offsetAndRotation(0f, 12.26667f, -4.06667f, 0f, 0f, 0f));
        PartDefinition parte_leftarmset = raiz.addOrReplaceChild("leftarmset", CubeListBuilder.create()
                .mirror()
                .texOffs(40, 16).addBox(3f, 10f, -4f, 1f, 9f, 1f)
                .texOffs(0, 0).addBox(4f, 19f, -4f, 0f, 2f, 1f)
                .texOffs(0, 0).addBox(3f, 19f, -4f, 0f, 2f, 1f)
                , PartPose.offsetAndRotation(0f, 2f, 1f, 0f, 0f, 0f));
        PartDefinition parte_rightarmset = raiz.addOrReplaceChild("rightarmset", CubeListBuilder.create()
                .mirror()
                .texOffs(40, 16).addBox(-4f, 10f, -3f, 1f, 9f, 1f)
                .texOffs(0, 0).addBox(-3f, 19f, -3f, 0f, 2f, 1f)
                .texOffs(0, 0).addBox(-4f, 19f, -3f, 0f, 2f, 1f)
                , PartPose.offsetAndRotation(0f, 2f, 0f, 0f, 0f, 0f));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** O {@code ModelTeddy}: 8 peças numa folha de 64 por 32. */
    public static final List<String> TEDDY_PARTS = List.of("pawFrontRight", "pawFrontLeft", "pawBackRight", "pawBackLeft", "Belly", "Head", "earRight", "earLeft");

    public static LayerDefinition teddy() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition raiz = mesh.getRoot();
        PartDefinition parte_pawFrontRight = raiz.addOrReplaceChild("pawFrontRight", CubeListBuilder.create()
                .mirror()
                .texOffs(0, 5).addBox(-2f, 0f, 0f, 2f, 3f, 2f)
                , PartPose.offsetAndRotation(-1.5f, 16f, 2.5f, -1.15192f, 0f, 0f));
        PartDefinition parte_pawFrontLeft = raiz.addOrReplaceChild("pawFrontLeft", CubeListBuilder.create()
                .mirror()
                .texOffs(4, 5).addBox(0f, 0f, 0f, 2f, 3f, 2f)
                , PartPose.offsetAndRotation(1.5f, 16f, 2.5f, -1.15192f, 0f, 0f));
        PartDefinition parte_pawBackRight = raiz.addOrReplaceChild("pawBackRight", CubeListBuilder.create()
                .mirror()
                .texOffs(0, 14).addBox(0f, 0f, 0f, 2f, 3f, 2f)
                , PartPose.offsetAndRotation(-2.2f, 21f, 2f, 0f, 0f, 0f));
        PartDefinition parte_pawBackLeft = raiz.addOrReplaceChild("pawBackLeft", CubeListBuilder.create()
                .mirror()
                .texOffs(0, 9).addBox(0f, 0f, 0f, 2f, 3f, 2f)
                , PartPose.offsetAndRotation(0.2f, 21f, 2f, 0f, 0f, 0f));
        PartDefinition parte_Belly = raiz.addOrReplaceChild("Belly", CubeListBuilder.create()
                .mirror()
                .texOffs(10, 0).addBox(0f, 0f, 0f, 4f, 7f, 3f)
                , PartPose.offsetAndRotation(-2f, 15f, 1f, 0.14871f, 0f, 0f));
        PartDefinition parte_Head = raiz.addOrReplaceChild("Head", CubeListBuilder.create()
                .mirror()
                .texOffs(0, 0).addBox(-2f, -3f, -1f, 3f, 3f, 2f)
                , PartPose.offsetAndRotation(0.5f, 16f, 1f, 0f, 0f, 0f));
        PartDefinition parte_earRight = raiz.addOrReplaceChild("earRight", CubeListBuilder.create()
                .mirror()
                .texOffs(8, 10).addBox(-0.5f, -1f, 0f, 1f, 1f, 1f)
                , PartPose.offsetAndRotation(-1f, 13.2f, 0.1f, 0.11154f, 0f, -0.22307f));
        PartDefinition parte_earLeft = raiz.addOrReplaceChild("earLeft", CubeListBuilder.create()
                .mirror()
                .texOffs(8, 12).addBox(-0.5f, -1f, 0f, 1f, 1f, 1f)
                , PartPose.offsetAndRotation(1f, 13.2f, 0.1f, 0.11154f, 0f, 0.22307f));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** O {@code ModelIsaacHead}: 5 peças numa folha de 64 por 32. */
    public static final List<String> ISAAC_HEAD_PARTS = List.of("neck4", "neck3", "neck2", "head", "neck1");

    public static LayerDefinition isaacHead() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition raiz = mesh.getRoot();
        PartDefinition parte_neck4 = raiz.addOrReplaceChild("neck4", CubeListBuilder.create()
                .mirror()
                .texOffs(0, 0).addBox(0f, 0f, 0f, 1f, 3f, 1f)
                , PartPose.offsetAndRotation(1f, 2f, 1f, 0f, 0f, 0f));
        PartDefinition parte_neck3 = raiz.addOrReplaceChild("neck3", CubeListBuilder.create()
                .mirror()
                .texOffs(0, 0).addBox(0f, 0f, 0f, 1f, 1f, 1f)
                , PartPose.offsetAndRotation(0f, 2f, 1f, 0f, 0f, 0f));
        PartDefinition parte_neck2 = raiz.addOrReplaceChild("neck2", CubeListBuilder.create()
                .mirror()
                .texOffs(0, 0).addBox(0f, 0f, 0f, 1f, 1f, 1f)
                , PartPose.offsetAndRotation(0f, 2f, 0f, 0f, 0f, 0f));
        PartDefinition parte_head = raiz.addOrReplaceChild("head", CubeListBuilder.create()
                .mirror()
                .texOffs(0, 0).addBox(-4f, -8f, -4f, 10f, 9f, 8f)
                , PartPose.offsetAndRotation(0f, 1f, 0f, 0f, 0f, 0f));
        PartDefinition parte_neck1 = raiz.addOrReplaceChild("neck1", CubeListBuilder.create()
                .mirror()
                .texOffs(0, 0).addBox(0f, 0f, 0f, 1f, 1f, 1f)
                , PartPose.offsetAndRotation(1f, 2f, -1f, 0f, 0f, 0f));
        return LayerDefinition.create(mesh, 64, 32);
    }

}
