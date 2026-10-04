package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.thaumcraft.Thaumcraft;

/**
 * A <b>armadilha de urso</b>, caixa por caixa: o {@code ModelBeartrap} do Witchery.
 *
 * <p>Vinte peças numa chapa de trinta e dois. A <b>base</b> é uma barra de dez; os dois <b>discos</b> são as
 * molas; a <b>placa</b> no meio é o gatilho, e ela <b>afunda</b> quando a armadilha dispara. E os dois
 * <b>arcos</b> — o da frente e o de trás — levam cada um as suas duas hastes e os seus <b>cinco dentes</b>.
 *
 * <p>Os dentes e as hastes são <b>filhos dos arcos</b>, e é por isso que ela funciona: basta girar o arco e
 * tudo o que está pregado nele gira junto. Armada, os arcos ficam deitados no chão e os dentes apontam para
 * cima; disparada, eles se levantam <b>1,2 radiano</b> cada um e os dentes se encontram no meio.
 *
 * <p>Nada disto se vê quando ela está armada e é de outra pessoa — ela fica a <b>três décimos de opaca</b>, o
 * que é o mais perto de invisível que um bloco do original chega a ficar.
 */
public final class BeartrapModel {
    public static final ModelLayerLocation ARMADILHA =
            new ModelLayerLocation(Thaumcraft.id("beartrap"), "main");

    /** Onde ficam os cinco dentes de cada arco, de uma ponta à outra. */
    private static final float[] DENTES = {-4.5f, -2.5f, -0.5f, 1.5f, 3.5f};

    private BeartrapModel() {
    }

    /** Trinta e dois por trinta e dois de textura, e o chão do modelo no vinte e quatro. */
    public static LayerDefinition armadilha() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("base", CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-5.0f, -1.0f, -0.5f, 10.0f, 1.0f, 1.0f),
                PartPose.offset(0.0f, 23.99f, 0.0f));

        // as duas molas
        raiz.addOrReplaceChild("diskLeft", CubeListBuilder.create()
                        .texOffs(19, 3).addBox(3.7f, -2.0f, -1.0f, 1.0f, 2.0f, 2.0f),
                PartPose.offset(0.0f, 24.0f, 0.0f));
        raiz.addOrReplaceChild("diskRight", CubeListBuilder.create()
                        .texOffs(19, 3).addBox(-4.7f, -2.0f, -1.0f, 1.0f, 2.0f, 2.0f),
                PartPose.offset(0.0f, 24.0f, 0.0f));

        // o gatilho, que afunda ao disparar
        raiz.addOrReplaceChild("plate", CubeListBuilder.create()
                        .texOffs(1, 0).addBox(-2.0f, -1.5f, -2.0f, 4.0f, 1.0f, 4.0f),
                PartPose.offset(0.0f, 24.0f, 0.0f));

        arco(raiz, "armFront", -7.0f, -6.0f);
        arco(raiz, "armBack", 6.0f, 0.0f);

        return LayerDefinition.create(malha, 32, 32);
    }

    /**
     * Um arco, com as duas hastes e os cinco dentes pregados nele.
     *
     * @param zArco  onde fica a barra do arco e a fileira de dentes
     * @param zHaste onde começam as duas hastes que o ligam à base
     */
    private static void arco(PartDefinition raiz, String qual, float zArco, float zHaste) {
        PartDefinition arco = raiz.addOrReplaceChild(qual, CubeListBuilder.create()
                        .texOffs(0, 9).addBox(-4.5f, -1.0f, zArco, 9.0f, 1.0f, 1.0f),
                PartPose.offset(0.0f, 23.99f, 0.0f));

        arco.addOrReplaceChild("left", CubeListBuilder.create()
                        .texOffs(0, 12).addBox(3.5f, -1.0f, zHaste, 1.0f, 1.0f, 6.0f),
                PartPose.ZERO);
        arco.addOrReplaceChild("right", CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-4.5f, -1.0f, zHaste, 1.0f, 1.0f, 6.0f),
                PartPose.ZERO);

        for (int qual1 = 0; qual1 < DENTES.length; qual1++) {
            arco.addOrReplaceChild("tooth" + (qual1 + 1), CubeListBuilder.create()
                            .texOffs(0, 0).addBox(DENTES[qual1], -2.0f, zArco, 1.0f, 1.0f, 1.0f),
                    PartPose.ZERO);
        }
    }
}
