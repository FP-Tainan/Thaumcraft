package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.thaumcraft.Thaumcraft;

/**
 * A vassoura desenhada: o {@code ModelBroom} do Witchery, caixa por caixa.
 *
 * <p>São <b>dez</b>: o cabo, e <b>nove cerdas</b> espetadas em leque na ponta — cada uma com a sua inclinação,
 * nenhuma igual à outra. É o que faz a ponta parecer um molho de palha e não um bloco.
 *
 * <p><b>O cabo e as cerdas desenham-se separados</b>, e não por capricho: no original só as <b>cerdas</b>
 * levam a tinta, e o cabo fica sempre da cor da madeira. Por isso são duas partes, e não uma.
 *
 * <p>O espelho do original é o <b>morto</b> de sempre: ele liga o {@code mirror} nas dez partes, mas sempre
 * <b>depois</b> das caixas, onde não faz nada. Nenhuma delas o leva aqui.
 */
public final class BroomModel {
    public static final ModelLayerLocation BROOM = new ModelLayerLocation(Thaumcraft.id("broom"), "main");

    private BroomModel() {
    }

    /** Trinta e dois por trinta e dois de textura, como no original. */
    public static LayerDefinition broom() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("handle", CubeListBuilder.create()
                        .texOffs(24, 0).addBox(-1.0f, -10.0f, -1.0f, 2.0f, 24.0f, 2.0f),
                PartPose.offsetAndRotation(0.0f, 11.0f, -5.0f, 1.570796f, 0.0f, 0.0f));

        PartDefinition cerdas = raiz.addOrReplaceChild("bristles", CubeListBuilder.create(),
                PartPose.ZERO);

        // as nove, na ordem do original — e cada uma com o seu canto e a sua inclinação
        cerdas.addOrReplaceChild("bristle1", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 10.0f),
                PartPose.offsetAndRotation(-1.0f, 10.0f, 9.0f, 0.1858931f, -0.1487144f, 0.0f));
        cerdas.addOrReplaceChild("bristle2", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 10.0f),
                PartPose.offsetAndRotation(1.0f, 12.0f, 9.0f, -0.1487144f, 0.1858931f, 0.0f));
        cerdas.addOrReplaceChild("bristle3", CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 9.0f),
                PartPose.offsetAndRotation(1.0f, 10.0f, 9.0f, 0.2230717f, 0.1858931f, 0.0f));
        cerdas.addOrReplaceChild("bristle4", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 10.0f),
                PartPose.offsetAndRotation(0.0f, 10.0f, 9.0f, 0.2230717f, 0.0743572f, 0.0f));
        cerdas.addOrReplaceChild("bristle5", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 10.0f),
                PartPose.offsetAndRotation(-1.0f, 12.0f, 9.0f, -0.2230717f, -0.1487144f, 0.0f));
        cerdas.addOrReplaceChild("bristle6", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 10.0f),
                PartPose.offsetAndRotation(0.0f, 11.0f, 9.0f, -0.0371786f, 0.0743572f, 0.0f));
        cerdas.addOrReplaceChild("bristle7", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 10.0f),
                PartPose.offsetAndRotation(1.0f, 11.0f, 9.0f, -0.0371786f, 0.2230717f, 0.0f));
        cerdas.addOrReplaceChild("bristle8", CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 9.0f),
                PartPose.offsetAndRotation(-1.0f, 11.0f, 9.0f, -0.0743572f, -0.1487144f, 0.0f));
        // a nona é a única que o original desloca de meio-terço de bloco, e o engano dele fica
        cerdas.addOrReplaceChild("bristle9", CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-0.5333334f, -0.5f, 0.0f, 1.0f, 1.0f, 9.0f),
                PartPose.offsetAndRotation(0.0f, 12.0f, 9.0f, -0.1858931f, 0.0f, 0.0f));

        return LayerDefinition.create(malha, 32, 32);
    }

    /** As duas partes de que o desenhista precisa. */
    public record Partes(ModelPart cabo, ModelPart cerdas) {
        public static Partes de(ModelPart raiz) {
            return new Partes(raiz.getChild("handle"), raiz.getChild("bristles"));
        }
    }
}
