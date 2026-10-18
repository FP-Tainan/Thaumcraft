package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.thaumcraft.Thaumcraft;

import java.util.List;

/**
 * A malha da <b>Estátua da Deusa</b>: o {@code ModelGoddess} do Witchery.
 *
 * <p>É o boneco de sempre com <b>três peças a mais</b>: o <b>decote</b>, que é uma caixa deitada por cima
 * do peito, e as <b>duas saias</b> — uma por cima da outra, a de baixo mais larga — que lhe cobrem as
 * pernas do joelho para baixo. As pernas estão lá por baixo, inteiras, e não se veem: é assim no original.
 *
 * <p><b>E nenhuma peça é espelhada.</b> O original liga o espelho das nove <b>depois</b> de
 * declarar a caixa de cada uma, e em 2014 isso não faz nada — o {@code mirror} só vale para as caixas
 * declaradas a seguir. São nove linhas mortas, e ficam de fora.
 *
 * <p>E os <b>braços abertos</b> são a pose, e não a animação: o {@code setRotationAngles} dela escreve os
 * mesmos seis números a cada quadro e nunca os muda. Aqui eles vão na malha, que é onde as coisas que não
 * mudam devem ir.
 */
public final class GoddessStatueModel {
    public static final ModelLayerLocation STATUE =
            new ModelLayerLocation(Thaumcraft.id("goddess_statue"), "main");

    /** As nove peças, na ordem em que o original as desenha. */
    public static final List<String> PEÇAS = List.of(
            "head", "cleave", "body", "right_arm", "left_arm", "right_leg", "left_leg",
            "dress_lower", "dress_middle");

    private GoddessStatueModel() {
    }

    /** Sessenta e quatro por sessenta e quatro de folha. */
    public static LayerDefinition statue() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f),
                PartPose.ZERO);

        // o decote, deitado um oitavo de volta para trás
        raiz.addOrReplaceChild("cleave", CubeListBuilder.create()
                        .texOffs(35, 0).addBox(0.0f, -2.0f, -2.0f, 8.0f, 4.0f, 4.0f),
                PartPose.offsetAndRotation(-4.0f, 3.0f, -2.0f, -net.minecraft.util.Mth.HALF_PI / 2.0f,
                        0.0f, 0.0f));

        raiz.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f),
                PartPose.ZERO);

        // os braços abertos: a pose que o original repõe a cada quadro
        raiz.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offsetAndRotation(-5.0f, 2.0f, 0.0f, -0.6f, 0.6f, 0.1f));
        raiz.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offsetAndRotation(5.0f, 2.0f, 0.0f, -0.6f, -0.6f, -0.1f));

        raiz.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(-2.0f, 12.0f, 0.0f));
        raiz.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(2.0f, 12.0f, 0.0f));

        // e as duas saias, a de baixo mais larga
        raiz.addOrReplaceChild("dress_lower", CubeListBuilder.create()
                        .texOffs(0, 45).addBox(-6.0f, 0.0f, -4.0f, 12.0f, 7.0f, 8.0f),
                PartPose.offset(0.0f, 17.0f, 0.0f));
        raiz.addOrReplaceChild("dress_middle", CubeListBuilder.create()
                        .texOffs(0, 33).addBox(-5.0f, 0.0f, -3.0f, 10.0f, 5.0f, 6.0f),
                PartPose.offset(0.0f, 12.0f, 0.0f));

        return LayerDefinition.create(malha, 64, 64);
    }
}
