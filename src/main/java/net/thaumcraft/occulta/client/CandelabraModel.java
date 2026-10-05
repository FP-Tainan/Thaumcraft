package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.thaumcraft.Thaumcraft;

/**
 * O <b>Candelabro</b>, caixa por caixa: o {@code ModelCandelabra} do Witchery.
 *
 * <p>Catorze peças em trinta e dois por trinta e dois de textura. Duas <b>travessas</b> cruzadas que
 * formam o braço, cinco <b>pratinhos</b> nas pontas e no meio, cinco <b>velas</b> em cima deles, e um
 * <b>pé</b> de duas peças.
 *
 * <p>A vela do meio é <b>mais comprida</b> que as outras quatro — treze contra oito —, e o pratinho dela
 * fica <b>dois mais abaixo</b>, de modo que ela sobressai pelo dobro. É o que dá ao candelabro aquela
 * silhueta de cruz com um espigão no meio que se reconhece de longe.
 */
public final class CandelabraModel {
    public static final ModelLayerLocation CANDELABRO =
            new ModelLayerLocation(Thaumcraft.id("candelabra"), "main");

    private CandelabraModel() {
    }

    /** Trinta e dois por trinta e dois de textura. */
    public static LayerDefinition candelabro() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        // as quatro velas de fora, todas da mesma caixa de textura
        raiz.addOrReplaceChild("candleLeft", vela(8), PartPose.offset(-6.0f, 11.0f, -1.0f));
        raiz.addOrReplaceChild("candleRight", vela(8), PartPose.offset(4.0f, 11.0f, -1.0f));
        raiz.addOrReplaceChild("candleFront", vela(8), PartPose.offset(-1.0f, 11.0f, -6.0f));
        raiz.addOrReplaceChild("candleBack", vela(8), PartPose.offset(-1.0f, 11.0f, 4.0f));
        // e a do meio, que é cinco mais comprida
        raiz.addOrReplaceChild("candleMiddle", vela(13), PartPose.offset(-1.0f, 9.0f, -1.0f));

        // as duas travessas cruzadas
        raiz.addOrReplaceChild("supportLR", CubeListBuilder.create()
                        .texOffs(0, 17).addBox(0.0f, 0.0f, 0.0f, 12.0f, 1.0f, 2.0f),
                PartPose.offset(-6.0f, 19.0f, -1.0f));
        raiz.addOrReplaceChild("supportFB", CubeListBuilder.create()
                        .texOffs(0, 4).addBox(0.0f, 0.0f, 0.0f, 2.0f, 1.0f, 12.0f),
                PartPose.offset(-1.0f, 19.0f, -6.0f));

        // os cinco pratinhos
        raiz.addOrReplaceChild("sconceLeft", prato(), PartPose.offset(-6.5f, 17.0f, -1.5f));
        raiz.addOrReplaceChild("sconceRight", prato(), PartPose.offset(3.5f, 17.0f, -1.5f));
        raiz.addOrReplaceChild("sconceFront", prato(), PartPose.offset(-1.5f, 17.0f, -6.5f));
        raiz.addOrReplaceChild("sconceBack", prato(), PartPose.offset(-1.5f, 17.0f, 3.5f));
        raiz.addOrReplaceChild("sconceMiddle", prato(), PartPose.offset(-1.5f, 15.0f, -1.5f));

        // e o pé
        raiz.addOrReplaceChild("baseTop", CubeListBuilder.create()
                        .texOffs(12, 20).addBox(0.0f, 0.0f, 0.0f, 3.0f, 1.0f, 3.0f),
                PartPose.offset(-1.5f, 22.0f, -1.5f));
        raiz.addOrReplaceChild("baseBottom", CubeListBuilder.create()
                        .texOffs(8, 24).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 1.0f, 5.0f),
                PartPose.offset(0.0f, 23.0f, 0.0f));

        return LayerDefinition.create(malha, 32, 32);
    }

    private static CubeListBuilder vela(int alto) {
        return CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 2.0f, alto, 2.0f);
    }

    private static CubeListBuilder prato() {
        return CubeListBuilder.create().texOffs(0, 20).addBox(0.0f, 0.0f, 0.0f, 3.0f, 1.0f, 3.0f);
    }
}
