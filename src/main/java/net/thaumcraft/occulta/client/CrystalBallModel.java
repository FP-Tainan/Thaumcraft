package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.thaumcraft.Thaumcraft;

/**
 * A <b>Bola de Cristal</b>, caixa por caixa: o {@code ModelCrystalBall} do Witchery.
 *
 * <p>Seis peças: um <b>pé</b> de três degraus que vai estreitando — seis, quatro, dois — e uma <b>esfera</b>
 * que são três cubos <b>encaixados uns dentro dos outros</b>, de dois, quatro e seis.
 *
 * <p>É assim que se fazia uma bola de vidro sem poder fazer uma bola: três cascas de vidro translúcido, cada
 * uma com a sua cor, e o que o olho vê é a soma delas. A de dentro <b>pulsa</b> com a hora do mundo — é a
 * única coisa deste modelo que se mexe, e é o que faz a bola parecer viva.
 */
public final class CrystalBallModel {
    public static final ModelLayerLocation BOLA =
            new ModelLayerLocation(Thaumcraft.id("crystal_ball"), "main");

    private CrystalBallModel() {
    }

    /** Trinta e dois por trinta e dois de textura. */
    public static LayerDefinition bola() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        // o pé, de baixo para cima
        raiz.addOrReplaceChild("baseBottom", CubeListBuilder.create()
                        .texOffs(0, 25).addBox(0.0f, 0.0f, 0.0f, 6.0f, 1.0f, 6.0f),
                PartPose.offset(-3.0f, 23.0f, -3.0f));
        raiz.addOrReplaceChild("baseMiddle", CubeListBuilder.create()
                        .texOffs(0, 20).addBox(0.0f, 0.0f, 0.0f, 4.0f, 1.0f, 4.0f),
                PartPose.offset(-2.0f, 22.0f, -2.0f));
        raiz.addOrReplaceChild("baseTop", CubeListBuilder.create()
                        .texOffs(0, 17).addBox(0.0f, 0.0f, 0.0f, 2.0f, 1.0f, 2.0f),
                PartPose.offset(-1.0f, 21.0f, -1.0f));

        // e as três cascas da esfera, de dentro para fora
        raiz.addOrReplaceChild("globeInner", CubeListBuilder.create()
                        .texOffs(4, 0).addBox(0.0f, 0.0f, 0.0f, 2.0f, 2.0f, 2.0f),
                PartPose.offset(-1.0f, 17.0f, -1.0f));
        raiz.addOrReplaceChild("globeMiddle", CubeListBuilder.create()
                        .texOffs(12, 0).addBox(0.0f, 0.0f, 0.0f, 4.0f, 4.0f, 4.0f),
                PartPose.offset(-2.0f, 16.0f, -2.0f));
        raiz.addOrReplaceChild("globeOuter", CubeListBuilder.create()
                        .texOffs(8, 8).addBox(0.0f, 0.0f, 0.0f, 6.0f, 6.0f, 6.0f),
                PartPose.offset(-3.0f, 15.0f, -3.0f));

        return LayerDefinition.create(malha, 32, 32);
    }
}
