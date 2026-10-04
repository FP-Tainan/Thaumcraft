package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.thaumcraft.Thaumcraft;

/**
 * O <b>Baú de Sanguessugas</b>, caixa por caixa: o {@code ModelLeechChest} do Witchery.
 *
 * <p>Oito peças. Um <b>corpo</b> de catorze por nove, <b>quatro quartos de tampa</b> de seis por cinco — e
 * três <b>sacos de sangue</b> de tamanhos diferentes na frente.
 *
 * <p>A tampa em quatro quartos é a ideia inteira do bloco: ela não <b>dobra</b>, ela se <b>abre</b>, com os
 * quatro pedaços afastando-se uns dos outros em três eixos ao mesmo tempo. Um baú comum range; este
 * <b>floresce</b>.
 *
 * <p>E os sacos aparecem <b>um por nome</b> que o baú guarda. Um baú com três sacos na frente está dizendo,
 * a quem souber ler, que três pessoas já o abriram — o que torna esta armadilha a mais honesta do mod.
 */
public final class LeechChestModel {
    public static final ModelLayerLocation BAÚ =
            new ModelLayerLocation(Thaumcraft.id("leech_chest"), "main");

    private LeechChestModel() {
    }

    /** Sessenta e quatro por sessenta e quatro de textura. */
    public static LayerDefinition baú() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("below", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 14.0f, 9.0f, 14.0f),
                PartPose.offset(1.0f, 7.0f, 1.0f));

        raiz.addOrReplaceChild("lidbl", CubeListBuilder.create()
                        .texOffs(28, 24).addBox(-6.0f, -5.0f, -6.0f, 6.0f, 5.0f, 6.0f),
                PartPose.offset(14.0f, 7.0f, 14.0f));
        raiz.addOrReplaceChild("lidfr", CubeListBuilder.create()
                        .texOffs(0, 36).addBox(0.0f, -5.0f, 0.0f, 6.0f, 5.0f, 6.0f),
                PartPose.offset(2.0f, 7.0f, 2.0f));
        raiz.addOrReplaceChild("lidbr", CubeListBuilder.create()
                        .texOffs(0, 24).addBox(0.0f, -5.0f, -6.0f, 6.0f, 5.0f, 6.0f),
                PartPose.offset(2.0f, 7.0f, 14.0f));
        raiz.addOrReplaceChild("lidfl", CubeListBuilder.create()
                        .texOffs(28, 36).addBox(-6.0f, -5.0f, 0.0f, 6.0f, 5.0f, 6.0f),
                PartPose.offset(14.0f, 7.0f, 2.0f));

        // e os três sacos, um por nome guardado
        raiz.addOrReplaceChild("sac1", CubeListBuilder.create()
                        .texOffs(0, 8).addBox(0.0f, 0.0f, 0.0f, 2.0f, 3.0f, 1.0f),
                PartPose.offset(3.0f, 8.0f, 0.0f));
        raiz.addOrReplaceChild("sac2", CubeListBuilder.create()
                        .texOffs(0, 3).addBox(0.0f, 0.0f, 0.0f, 3.0f, 2.0f, 1.0f),
                PartPose.offset(9.0f, 13.0f, 0.0f));
        raiz.addOrReplaceChild("sac3", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 2.0f, 1.0f, 1.0f),
                PartPose.offset(9.0f, 9.0f, 0.0f));

        return LayerDefinition.create(malha, 64, 64);
    }
}
