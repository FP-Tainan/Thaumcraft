package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.thaumcraft.Thaumcraft;

/**
 * O Caixão, caixa por caixa: o {@code ModelCoffin} do Witchery.
 *
 * <p>São <b>seis peças</b> e uma tampa de três andares, numa chapa de cento e vinte e oito por sessenta e
 * quatro:
 *
 * <ul>
 *   <li>um <b>fundo</b> raso que assenta no chão, e um <b>corpo</b> de cinco de altura por cima dele;</li>
 *   <li>duas <b>paredes laterais</b> de um de grosso — a esquerda é a direita <b>espelhada</b>, que era o
 *       jeito de poupar textura em 2014 e aqui é o {@code mirror()};</li>
 *   <li>uma <b>parede de topo</b>, que o desenhista vira ao contrário na metade dos pés, de modo que as
 *       duas metades fechem o caixão pelas duas pontas;</li>
 *   <li>e a <b>tampa</b>, que são três chapas uma sobre a outra, cada uma mais estreita — e é esse degrau
 *       que lhe dá o perfil de tampo de caixão em vez de tábua.</li>
 * </ul>
 *
 * <p>A tampa gira em volta de um ponto na <b>borda esquerda</b>, sete para o lado e cinco para cima: ela
 * não levanta, ela <b>abre de lado</b>, como uma porta deitada.
 *
 * <p>E ele é construído <b>de cabeça para baixo</b>, como tudo o que vem desse tempo: quem o põe de pé é o
 * meio-giro em Z do desenhista.
 */
public final class CoffinModel {
    public static final ModelLayerLocation CAIXÃO =
            new ModelLayerLocation(Thaumcraft.id("coffin"), "main");

    private CoffinModel() {
    }

    /** Cento e vinte e oito por sessenta e quatro de textura. */
    public static LayerDefinition caixão() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("base_lower", CubeListBuilder.create()
                        .texOffs(0, 20).addBox(-8.0f, 0.0f, -8.0f, 16.0f, 1.0f, 16.0f),
                PartPose.offset(0.0f, 8.0f, 0.0f));
        raiz.addOrReplaceChild("base", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-7.0f, 0.0f, -8.0f, 14.0f, 5.0f, 15.0f),
                PartPose.offset(0.0f, 3.0f, 0.0f));
        raiz.addOrReplaceChild("side_right", CubeListBuilder.create()
                        .texOffs(0, 37).addBox(-7.0f, 0.0f, -8.0f, 1.0f, 7.0f, 15.0f),
                PartPose.offset(0.0f, -4.0f, 0.0f));
        raiz.addOrReplaceChild("side_left", CubeListBuilder.create()
                        .texOffs(0, 37).mirror().addBox(6.0f, 0.0f, -8.0f, 1.0f, 7.0f, 15.0f),
                PartPose.offset(0.0f, -4.0f, 0.0f));
        raiz.addOrReplaceChild("side_end", CubeListBuilder.create()
                        .texOffs(33, 51).addBox(-6.0f, 0.0f, 6.0f, 12.0f, 7.0f, 1.0f),
                PartPose.offset(0.0f, -4.0f, 0.0f));

        PartDefinition tampa = raiz.addOrReplaceChild("lid", CubeListBuilder.create()
                        .texOffs(60, 0).addBox(-1.0f, 0.0f, -8.0f, 16.0f, 1.0f, 16.0f),
                PartPose.offset(-7.0f, -5.0f, 0.0f));
        tampa.addOrReplaceChild("lid_mid", CubeListBuilder.create()
                        .texOffs(64, 18).addBox(0.0f, -1.0f, -8.0f, 14.0f, 1.0f, 15.0f),
                PartPose.ZERO);
        tampa.addOrReplaceChild("lid_top", CubeListBuilder.create()
                        .texOffs(67, 35).addBox(1.0f, -2.0f, -8.0f, 12.0f, 1.0f, 14.0f),
                PartPose.ZERO);

        return LayerDefinition.create(malha, 128, 64);
    }
}
