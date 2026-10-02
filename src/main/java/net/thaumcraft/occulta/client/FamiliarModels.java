package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.thaumcraft.Thaumcraft;

/**
 * Os modelos dos familiares: o {@code ModelToad} e o {@code ModelOwl} do Witchery, caixa por caixa.
 *
 * <p><b>Atenção ao espelho.</b> Nos dois modelos o original liga o {@code mirror} em cada parte, mas nem sempre
 * no lugar que faz diferença: ligado <b>antes</b> de uma caixa ele vale, ligado <b>depois</b> não faz nada — é
 * o espelho morto que este porte já encontrou às centenas nos modelos de 2014. Aqui o espelho está só onde ele
 * de fato valia: na cabeça e nas pernas do sapo, e na cabeça da coruja.
 */
public final class FamiliarModels {
    public static final ModelLayerLocation TOAD = new ModelLayerLocation(Thaumcraft.id("toad"), "main");
    public static final ModelLayerLocation OWL = new ModelLayerLocation(Thaumcraft.id("owl"), "main");

    private FamiliarModels() {
    }

    /** O sapo: trinta e dois por trinta e dois de textura, seis caixas. */
    public static LayerDefinition toad() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("head", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 5).addBox(-2.0f, -1.0f, -4.0f, 4.0f, 2.0f, 4.0f)
                        .texOffs(0, 0).addBox(-2.5f, -3.0f, -3.0f, 2.0f, 2.0f, 2.0f)
                        .texOffs(8, 0).addBox(0.5f, -3.0f, -3.0f, 2.0f, 2.0f, 2.0f),
                PartPose.offset(0.0f, 20.0f, -1.0f));

        raiz.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-2.0f, -1.0f, 0.0f, 4.0f, 2.0f, 5.0f),
                PartPose.offsetAndRotation(0.0f, 20.0f, -1.0f, -0.4833219f, 0.0f, 0.0f));

        raiz.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(13, 26).addBox(-1.0f, 0.0f, 0.0f, 1.0f, 4.0f, 1.0f),
                PartPose.offsetAndRotation(-2.0f, 20.0f, -1.0f, -0.3346075f, 0.0f, 0.0f));

        raiz.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(18, 26).addBox(0.0f, 0.0f, 0.0f, 1.0f, 4.0f, 1.0f),
                PartPose.offsetAndRotation(2.0f, 20.0f, -1.0f, -0.3346075f, 0.0f, 0.0f));

        raiz.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 20).addBox(-2.0f, -1.0f, -2.0f, 2.0f, 2.0f, 3.0f)
                        .texOffs(0, 26).addBox(-3.0f, 1.0f, -4.0f, 3.0f, 0.0f, 3.0f),
                PartPose.offset(-2.0f, 23.0f, 3.0f));

        raiz.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(11, 20).addBox(0.0f, -1.0f, -2.0f, 2.0f, 2.0f, 3.0f)
                        .texOffs(0, 26).addBox(0.0f, 1.0f, -4.0f, 3.0f, 0.0f, 3.0f),
                PartPose.offset(2.0f, 23.0f, 3.0f));

        return LayerDefinition.create(malha, 32, 32);
    }

    /**
     * A coruja: sessenta e quatro por trinta e dois, oito caixas.
     *
     * <p>As pernas dela são <b>filhas do corpo</b> — o original as prende ao tronco depois de as ter feito, e
     * por isso elas andam com ele.
     */
    public static LayerDefinition owl() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("head", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(37, 0).addBox(-5.0f, -7.0f, -1.0f, 1.0f, 3.0f, 2.0f)
                        .texOffs(37, 0).addBox(4.0f, -7.0f, -1.0f, 1.0f, 3.0f, 2.0f)
                        .texOffs(30, 0).addBox(-1.0f, -3.0f, -4.0f, 2.0f, 3.0f, 1.0f)
                        .texOffs(0, 0).addBox(-4.0f, -6.0f, -3.0f, 8.0f, 6.0f, 6.0f),
                PartPose.offset(0.0f, 15.0f, 0.0f));

        PartDefinition corpo = raiz.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-3.0f, 0.0f, -2.0f, 6.0f, 8.0f, 4.0f),
                PartPose.offset(0.0f, 15.0f, 0.0f));

        raiz.addOrReplaceChild("right_wing", CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-1.0f, -1.0f, -2.0f, 1.0f, 8.0f, 4.0f),
                PartPose.offset(-3.0f, 16.0f, 0.0f));

        raiz.addOrReplaceChild("left_wing", CubeListBuilder.create()
                        .texOffs(40, 16).addBox(0.0f, -1.0f, -2.0f, 1.0f, 8.0f, 4.0f),
                PartPose.offset(3.0f, 16.0f, 0.0f));

        // as pernas vão presas ao corpo, com a posição que o original lhes dá por último
        corpo.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-1.0f, 0.0f, -2.0f, 2.0f, 1.0f, 4.0f),
                PartPose.offset(-2.0f, 8.0f, -1.0f));

        corpo.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-1.0f, 0.0f, -2.0f, 2.0f, 1.0f, 4.0f),
                PartPose.offset(2.0f, 8.0f, -1.0f));

        return LayerDefinition.create(malha, 64, 32);
    }
}
