package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.thaumcraft.Thaumcraft;

/**
 * O <b>Apanha-Erva</b>, caixa por caixa: o {@code ModelGrassper} do Witchery.
 *
 * <p>Dez peças numa chapa de sessenta e quatro: <b>quatro folhas chatas</b> de oito por oito deitadas no
 * chão, cada uma inclinada trinta graus para o seu lado, dois pedaços de <b>caule</b> tortos em sentidos
 * opostos — que é o que lhe dá o jeito de planta vergada —, e <b>quatro pétalas</b> de um pixel abrindo em
 * volta da boca.
 *
 * <p>As folhas são chapas sem grossura nenhuma. Vistas de cima são uma estrela de quatro pontas; vistas de
 * lado, quase desaparecem — e é por isso que um Apanha-Erva no chão parece uma moita até alguém lhe pôr
 * alguma coisa na boca.
 */
public final class GrassperModel {
    public static final ModelLayerLocation APANHA_ERVA =
            new ModelLayerLocation(Thaumcraft.id("grassper"), "main");

    private GrassperModel() {
    }

    /** Sessenta e quatro por sessenta e quatro de textura. */
    public static LayerDefinition apanhaErva() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        // os dois pedaços de caule, tortos para lados contrários
        raiz.addOrReplaceChild("stalktop", CubeListBuilder.create()
                        .texOffs(0, 4).addBox(-1.0f, -4.0f, -1.0f, 2.0f, 4.0f, 2.0f),
                PartPose.offsetAndRotation(2.0f, 21.0f, 0.0f, 0.0f, 0.0f, (float) (-Math.PI / 6)));
        raiz.addOrReplaceChild("stalkbottom", CubeListBuilder.create()
                        .texOffs(0, 10).addBox(-1.0f, -4.0f, -1.0f, 2.0f, 4.0f, 2.0f),
                PartPose.offsetAndRotation(0.0f, 24.0f, 0.0f, 0.0f, 0.0f, (float) (Math.PI / 6)));

        // as quatro folhas, cada uma caída para o seu lado
        raiz.addOrReplaceChild("leafright", CubeListBuilder.create()
                        .texOffs(0, 8).addBox(0.0f, 0.0f, -4.0f, 8.0f, 0.0f, 8.0f),
                PartPose.offsetAndRotation(0.0f, 24.0f, 0.0f, 0.0f, 3.141593f, (float) (Math.PI / 6)));
        raiz.addOrReplaceChild("leafleft", CubeListBuilder.create()
                        .texOffs(0, 8).addBox(0.0f, 0.0f, -4.0f, 8.0f, 0.0f, 8.0f),
                PartPose.offsetAndRotation(0.0f, 24.0f, 0.0f, 0.0f, 0.0f, (float) (-Math.PI / 6)));
        raiz.addOrReplaceChild("leaffront", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0f, 0.0f, -8.0f, 8.0f, 0.0f, 8.0f),
                PartPose.offsetAndRotation(0.0f, 24.0f, 0.0f, (float) (-Math.PI / 6), 0.0f, 0.0f));
        raiz.addOrReplaceChild("leafback", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0f, 0.0f, -8.0f, 8.0f, 0.0f, 8.0f),
                PartPose.offsetAndRotation(0.0f, 24.0f, 0.0f, (float) (-Math.PI / 6), -3.141593f, 0.0f));

        // e as quatro pétalas em volta da boca
        raiz.addOrReplaceChild("petalbackright", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0f, -2.0f, 0.0f, 1.0f, 2.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, 18.0f, 0.0f,
                        (float) (-Math.PI / 6), 0.0f, (float) (-Math.PI / 4)));
        raiz.addOrReplaceChild("petalfrontright", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0f, -2.0f, -1.0f, 1.0f, 2.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, 18.0f, 0.0f,
                        (float) (Math.PI / 6), 0.0f, (float) (-Math.PI / 4)));
        raiz.addOrReplaceChild("petalbackleft", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(0.0f, -2.0f, 0.0f, 1.0f, 2.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, 18.0f, 0.0f,
                        -0.3490659f, 0.0f, (float) (Math.PI / 12)));
        raiz.addOrReplaceChild("petalfrontleft", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(0.0f, -2.0f, -1.0f, 1.0f, 2.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, 18.0f, 0.0f,
                        0.3490659f, 0.0f, (float) (Math.PI / 12)));

        return LayerDefinition.create(malha, 64, 64);
    }
}
