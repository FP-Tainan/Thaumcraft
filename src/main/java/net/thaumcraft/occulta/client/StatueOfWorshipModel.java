package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.thaumcraft.Thaumcraft;

/**
 * A malha da <b>Estátua de Adoração</b>: o {@code ModelBiped} que o original lhe empresta.
 *
 * <p>Não há modelo próprio: o original desenha a estátua com o <b>mesmo boneco</b> com que desenha toda a
 * gente, e a graça está em o desenhar <b>duas vezes</b> — uma com a pele do dono, outra com uma folha de
 * pedra por cima. Aqui a malha é escrita à mão, com as medidas e as casas de folha do {@code ModelBiped} da
 * 1.7.10, que são as do boneco de sempre.
 *
 * <h2>Duas folhas, duas contas</h2>
 *
 * <p>A mesma malha é assada <b>duas vezes</b>, com tamanhos de folha diferentes:
 *
 * <ul>
 *   <li>a <b>pedra</b> é a folha do Witchery, de <b>sessenta e quatro por trinta e dois</b>, que é o
 *       formato de pele de 2014;</li>
 *   <li>a <b>pele</b> de quem jogar é de <b>sessenta e quatro por sessenta e quatro</b>, que é o de hoje.</li>
 * </ul>
 *
 * <p>E elas encaixam porque o formato antigo é, à letra, a <b>metade de cima</b> do novo: a cabeça, o
 * corpo, o braço e a perna direitos estão nas mesmas casas nos dois. O que o formato antigo não tem é braço
 * e perna <b>esquerdos</b> próprios — ele espelha os direitos —, e é assim que a estátua fica. É o que o
 * original faz, e com uma pele de hoje nota-se: a manga esquerda da estátua é a direita ao espelho.
 */
public final class StatueOfWorshipModel {
    /** A folha de pedra, que é a do Witchery. */
    public static final Identifier PEDRA = Thaumcraft.id("textures/block/statue_of_worship.png");

    /** A malha com a folha de pedra, de sessenta e quatro por trinta e dois. */
    public static final ModelLayerLocation PEDRA_MALHA =
            new ModelLayerLocation(Thaumcraft.id("statue_of_worship"), "stone");

    /** E a mesma malha com a folha de pele, de sessenta e quatro por sessenta e quatro. */
    public static final ModelLayerLocation PELE_MALHA =
            new ModelLayerLocation(Thaumcraft.id("statue_of_worship"), "skin");

    /** As peças, na ordem em que o original as desenha. */
    public static final String[] PEÇAS = {
        "head", "headwear", "body", "right_arm", "left_arm", "right_leg", "left_leg",
    };

    private StatueOfWorshipModel() {
    }

    public static LayerDefinition pedra() {
        return LayerDefinition.create(malha(), 64, 32);
    }

    public static LayerDefinition pele() {
        return LayerDefinition.create(malha(), 64, 64);
    }

    /** O boneco de sempre, com as medidas do {@code ModelBiped}. */
    private static MeshDefinition malha() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.ZERO);
        raiz.addOrReplaceChild("headwear", CubeListBuilder.create().texOffs(32, 0)
                .addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f,
                        new net.minecraft.client.model.geom.builders.CubeDeformation(0.5f)),
                PartPose.ZERO);
        raiz.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16)
                .addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f), PartPose.ZERO);

        raiz.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16)
                .addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(-5.0f, 2.0f, 0.0f));
        raiz.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror().texOffs(40, 16)
                .addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(5.0f, 2.0f, 0.0f));

        raiz.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(-1.9f, 12.0f, 0.0f));
        raiz.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror().texOffs(0, 16)
                .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(1.9f, 12.0f, 0.0f));

        return malha;
    }
}
