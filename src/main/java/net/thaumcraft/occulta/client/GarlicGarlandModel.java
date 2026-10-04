package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.thaumcraft.Thaumcraft;

import java.util.List;

/**
 * A Guirlanda de Alho, caixa por caixa: o {@code ModelGarlicGarland} do Witchery.
 *
 * <p>São <b>cinco cabeças de alho</b> e <b>quatro pedaços de cordel</b>, numa chapa de trinta e dois por
 * trinta e dois — e a graça dela está em como uma cabeça de alho é feita.
 *
 * <p>Cada cabeça é um <b>talo</b> de um pixel de grosso com <b>quatro chapas penduradas nele</b>, cada uma
 * mais larga do que a de cima: três, cinco, sete e quatro. É a silhueta de um bolbo visto de fora — estreito
 * no pescoço, bojudo no meio, fechado em baixo — feita com quatro caixas e nada mais. Não há uma única
 * rotação nelas.
 *
 * <p>As cinco penduram-se em <b>ziguezague</b>: as das pontas e a do meio altas, as outras duas um pixel e
 * meio mais baixas. E os quatro cordéis ligam-nas em <b>V</b>, inclinados trinta e um graus para um lado e
 * para o outro, com as caixas <b>encolhidas quatro décimos</b> — que é o truque de 2014 para um fio parecer
 * um fio e não uma tábua.
 */
public final class GarlicGarlandModel {
    public static final ModelLayerLocation GUIRLANDA =
            new ModelLayerLocation(Thaumcraft.id("garlic_garland"), "main");

    /** Os cinco talos, que são as peças de raiz. */
    public static final List<String> CABEÇAS = List.of("a", "b", "c", "d", "e");

    /** E os quatro cordéis. */
    public static final List<String> CORDÉIS = List.of("string1", "string2", "string3", "string4");

    /** A inclinação de um cordel, e o quanto a caixa dele encolhe. */
    public static final float O_CORDEL = 0.5462881f;
    public static final float FINO = -0.4f;

    private GarlicGarlandModel() {
    }

    /** Trinta e dois por trinta e dois de textura. */
    public static LayerDefinition guirlanda() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        cabeça(raiz, "a", -5.0f, 0.0f);
        cabeça(raiz, "b", -2.5f, 1.5f);
        cabeça(raiz, "c", 0.0f, 0.0f);
        cabeça(raiz, "d", 2.5f, 1.5f);
        cabeça(raiz, "e", 5.0f, 0.0f);

        cordel(raiz, "string1", -5.4f, -0.3f, O_CORDEL);
        cordel(raiz, "string2", -3.0f, 1.8f, -O_CORDEL);
        cordel(raiz, "string3", -0.4f, -0.3f, O_CORDEL);
        cordel(raiz, "string4", 2.0f, 1.8f, -O_CORDEL);

        return LayerDefinition.create(malha, 32, 32);
    }

    /**
     * Uma cabeça de alho: o talo, e as quatro chapas penduradas nele.
     *
     * <p>As quatro são filhas do talo, e por isso mexer no talo mexe na cabeça inteira. É o que o original
     * faz com vinte {@code addChild}, e é a única hierarquia que este modelo tem.
     */
    private static void cabeça(PartDefinition raiz, String nome, float x, float y) {
        PartDefinition talo = raiz.addOrReplaceChild(nome, CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-0.5f, 0.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offset(x, y, 7.0f));

        talo.addOrReplaceChild("um", CubeListBuilder.create()
                        .texOffs(0, 3).addBox(-1.5f, 2.0f, -1.5f, 3.0f, 1.0f, 3.0f),
                PartPose.ZERO);
        talo.addOrReplaceChild("dois", CubeListBuilder.create()
                        .texOffs(0, 7).addBox(-2.5f, 3.0f, -2.5f, 5.0f, 1.0f, 5.0f),
                PartPose.ZERO);
        talo.addOrReplaceChild("três", CubeListBuilder.create()
                        .texOffs(0, 13).addBox(-3.5f, 4.0f, -3.5f, 7.0f, 3.0f, 7.0f),
                PartPose.ZERO);
        talo.addOrReplaceChild("quatro", CubeListBuilder.create()
                        .texOffs(0, 23).addBox(-2.0f, 7.0f, -2.0f, 4.0f, 1.0f, 4.0f),
                PartPose.ZERO);
    }

    /** E um cordel: uma caixa de quatro por um, encolhida até dar um fio, e inclinada. */
    private static void cordel(PartDefinition raiz, String nome, float x, float y, float giro) {
        raiz.addOrReplaceChild(nome, CubeListBuilder.create()
                        .texOffs(6, 0).addBox(0.0f, -0.5f, -0.5f, 4.0f, 1.0f, 1.0f,
                                new CubeDeformation(FINO)),
                PartPose.offsetAndRotation(x, y, 7.0f, 0.0f, 0.0f, giro));
    }
}
