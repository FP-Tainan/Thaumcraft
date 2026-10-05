package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.thaumcraft.Thaumcraft;

/**
 * O <b>Cálice</b>, caixa por caixa: o {@code ModelChalice} do Witchery.
 *
 * <p>A taça é feita de <b>caixas chatas</b> — quatro paredes de espessura zero e um fundo de espessura zero
 * —, que é como se fazia um copo oco antes de haver jeito melhor. Uma caixa de espessura zero desenha as
 * duas faces no mesmo lugar, uma virada para cada lado, e o resultado é uma parede que se vê por dentro e
 * por fora.
 *
 * <p>O <b>líquido</b> é outra chapa chata, solta da taça, e só se desenha quando o cálice está cheio.
 *
 * <p>E as caixas de textura do original são <b>negativas</b> — {@code (0, -5)}, {@code (-5, 4)},
 * {@code (-4, 18)}. Não é engano: a folha se repete nas duas direções, e ler cinco acima do topo de uma
 * folha de trinta e dois é o mesmo que ler na linha vinte e sete. Ficam como estão, porque mudá-las para o
 * número de dentro daria o mesmo desenho e deixaria de se parecer com o original.
 */
public final class ChaliceModel {
    public static final ModelLayerLocation CÁLICE =
            new ModelLayerLocation(Thaumcraft.id("chalice"), "main");

    private ChaliceModel() {
    }

    /** Trinta e dois por trinta e dois de textura. */
    public static LayerDefinition cálice() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("chalice", CubeListBuilder.create()
                        .texOffs(0, -5).addBox(4.0f, -6.0f, -1.0f, 0.0f, 4.0f, 5.0f)
                        .texOffs(0, -5).addBox(-1.0f, -6.0f, -1.0f, 0.0f, 4.0f, 5.0f)
                        .texOffs(0, 0).addBox(-1.0f, -6.0f, 4.0f, 5.0f, 4.0f, 0.0f)
                        .texOffs(0, 0).addBox(-1.0f, -6.0f, -1.0f, 5.0f, 4.0f, 0.0f)
                        .texOffs(-5, 4).addBox(-1.0f, -2.0f, -1.0f, 5.0f, 0.0f, 5.0f)
                        .texOffs(4, 10).addBox(1.0f, -2.0f, 1.0f, 1.0f, 2.0f, 1.0f)
                        .texOffs(0, 13).addBox(0.0f, 0.0f, 0.0f, 3.0f, 1.0f, 3.0f),
                PartPose.offset(-1.0f, 23.0f, -1.0f));

        raiz.addOrReplaceChild("liquid", CubeListBuilder.create()
                        .texOffs(-4, 18).addBox(0.0f, 0.0f, 0.0f, 5.0f, 0.0f, 5.0f),
                PartPose.offset(-2.0f, 19.0f, -2.0f));

        return LayerDefinition.create(malha, 32, 32);
    }
}
