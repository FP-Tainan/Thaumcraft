package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;

import java.util.List;

/**
 * As malhas dos <b>dois marcadores de área</b>: o {@code ModelStatueWolf} e o {@code ModelMandrake} do
 * Witchery, usados os dois como estátua e não como bicho.
 *
 * <p>São duas estatuetas de meio tamanho — o desenhista as encolhe a <b>metade</b> — e cada uma marca um
 * rito: o <b>lobo</b> contra a maldição dirigida, a <b>mandrágora</b> contra o puxão por teleporte.
 *
 * <p><b>E nenhuma peça é espelhada.</b> O original liga o espelho de todas elas <b>depois</b> de declarar
 * a caixa, e em 2014 isso não faz nada.
 */
public final class AreaMarkerModels {
    public static final ModelLayerLocation WOLF =
            new ModelLayerLocation(Thaumcraft.id("wolf_statue"), "main");
    public static final ModelLayerLocation MANDRAKE =
            new ModelLayerLocation(Thaumcraft.id("mandrake_statue"), "main");

    /** As doze peças do lobo, na ordem em que o original as desenha. */
    public static final List<String> LOBO = List.of(
            "head", "body", "mane", "leg1", "leg2", "leg3", "leg4", "tail", "ear1", "ear2", "nose",
            "plinth");

    /** E as seis da mandrágora. */
    public static final List<String> MANDRÁGORA = List.of(
            "head", "body", "right_arm", "left_arm", "right_leg", "left_leg");

    private AreaMarkerModels() {
    }

    /**
     * O <b>lobo sentado</b> num degrau: sessenta e quatro por trinta e dois de folha.
     *
     * <p>O corpo e a juba estão <b>deitados um quarto de volta</b> — é um lobo visto de perfil, construído
     * com as caixas de pé —, e por baixo de tudo há uma laje de oito por treze que é o degrau dele.
     */
    public static LayerDefinition wolf() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0f, -3.0f, -2.0f, 6.0f, 6.0f, 4.0f),
                PartPose.offset(-0.5f, 13.5f, -7.0f));
        raiz.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(18, 14).addBox(-4.0f, -2.0f, -3.0f, 6.0f, 9.0f, 6.0f),
                PartPose.offsetAndRotation(0.5f, 14.0f, 2.0f, Mth.HALF_PI, 0.0f, 0.0f));
        raiz.addOrReplaceChild("mane", CubeListBuilder.create()
                        .texOffs(21, 0).addBox(-4.0f, -3.0f, -3.0f, 8.0f, 6.0f, 7.0f),
                PartPose.offsetAndRotation(-0.5f, 14.0f, -3.0f, Mth.HALF_PI, 0.0f, 0.0f));

        raiz.addOrReplaceChild("leg1", CubeListBuilder.create()
                        .texOffs(0, 18).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f),
                PartPose.offset(-2.0f, 16.0f, 7.0f));
        raiz.addOrReplaceChild("leg2", CubeListBuilder.create()
                        .texOffs(0, 18).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f),
                PartPose.offset(1.0f, 16.0f, 7.0f));
        raiz.addOrReplaceChild("leg3", CubeListBuilder.create()
                        .texOffs(0, 18).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f),
                PartPose.offset(-2.0f, 16.0f, -4.0f));
        raiz.addOrReplaceChild("leg4", CubeListBuilder.create()
                        .texOffs(0, 18).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f),
                PartPose.offset(1.0f, 16.0f, -4.0f));

        raiz.addOrReplaceChild("tail", CubeListBuilder.create()
                        .texOffs(9, 18).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f),
                PartPose.offsetAndRotation(-0.5f, 12.0f, 8.0f, 1.130069f, 0.0f, 0.0f));

        raiz.addOrReplaceChild("ear1", CubeListBuilder.create()
                        .texOffs(16, 14).addBox(-3.0f, -5.0f, 0.0f, 2.0f, 2.0f, 1.0f),
                PartPose.offset(-0.5f, 13.5f, -7.0f));
        raiz.addOrReplaceChild("ear2", CubeListBuilder.create()
                        .texOffs(16, 14).addBox(1.0f, -5.0f, 0.0f, 2.0f, 2.0f, 1.0f),
                PartPose.offset(-0.5f, 13.5f, -7.0f));
        raiz.addOrReplaceChild("nose", CubeListBuilder.create()
                        .texOffs(0, 10).addBox(-2.0f, 0.0f, -5.0f, 3.0f, 3.0f, 4.0f),
                PartPose.offset(0.0f, 13.5f, -7.0f));

        raiz.addOrReplaceChild("plinth", CubeListBuilder.create()
                        .texOffs(22, 18).addBox(0.0f, 0.0f, 0.0f, 8.0f, 1.0f, 13.0f),
                PartPose.offset(-4.5f, 23.0f, -5.0f));

        return LayerDefinition.create(malha, 64, 32);
    }

    /**
     * A <b>mandrágora</b>: a mesma do bicho, com as folhas na cabeça.
     *
     * <p>As <b>folhas</b> são uma caixa de <b>espessura zero</b> — um retângulo chapado de oito por oito
     * atrás da cabeça —, e os dois braços estão abertos a <b>sessenta graus</b> cada.
     */
    public static LayerDefinition mandrake() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 8).addBox(-2.0f, -4.0f, -2.0f, 4.0f, 4.0f, 4.0f)
                        .texOffs(0, 0).addBox(-4.0f, -12.0f, 0.0f, 8.0f, 8.0f, 0.0f),
                PartPose.offset(0.0f, 16.0f, 0.0f));
        raiz.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(21, 0).addBox(-2.5f, 0.0f, -2.5f, 5.0f, 2.0f, 5.0f)
                        .texOffs(17, 7).addBox(-3.5f, 2.0f, -3.5f, 7.0f, 3.0f, 7.0f),
                PartPose.offset(0.0f, 16.0f, 0.0f));

        raiz.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(37, 0).addBox(-1.0f, 0.0f, -0.5f, 1.0f, 3.0f, 1.0f),
                PartPose.offsetAndRotation(-2.0f, 17.0f, 0.0f, 0.0f, 0.0f, 1.047198f));
        raiz.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(37, 0).addBox(0.0f, 0.0f, -0.5f, 1.0f, 3.0f, 1.0f),
                PartPose.offsetAndRotation(2.0f, 17.0f, 0.0f, 0.0f, 0.0f, -1.047198f));

        raiz.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(27, 18).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 3.0f, 2.0f),
                PartPose.offset(-1.0f, 21.0f, 0.0f));
        raiz.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(27, 18).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 3.0f, 2.0f),
                PartPose.offset(1.0f, 21.0f, 0.0f));

        return LayerDefinition.create(malha, 64, 32);
    }
}
