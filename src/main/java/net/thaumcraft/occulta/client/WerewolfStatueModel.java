package net.thaumcraft.occulta.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.thaumcraft.Thaumcraft;

import java.util.List;

/**
 * A Estátua do Lobisomem, caixa por caixa: o {@code ModelWolfAltar} do Witchery.
 *
 * <p>É o modelo mais cheio deste porte — <b>trinta e oito peças</b> — e vale dizer o que elas são, porque a
 * estátua conta uma história:
 *
 * <ul>
 *   <li>um <b>pedestal de três lajes</b>, uma por cima da outra e cada uma mais estreita;</li>
 *   <li>um <b>degrau</b> de quatro por dois à frente, onde se põe o que se traz;</li>
 *   <li>o <b>senhor</b>, de pé: tronco de gente, <b>cabeça de lobo</b> com as duas orelhas e o focinho, o
 *       braço esquerdo caído e o direito atrás das costas, segurando uma <b>lança de quarenta de comprido</b>.
 *       Uma das pernas dele está dobrada para trás, como quem acabou de parar de andar;</li>
 *   <li>e <b>dois lobos</b>, um de cada lado, cada um com corpo, juba, cabeça de quatro peças, quatro pernas
 *       e cauda — e <b>virados para fora</b>, cada um por um ângulo diferente, como dois cães que guardam.</li>
 * </ul>
 *
 * <p>O senhor é desenhado a <b>sete décimos</b> do tamanho e os lobos a <b>metade</b>, cada um em volta do
 * próprio ponto de giro. É o que o original faz com três {@code glScaled} em volta de três
 * {@code glTranslate}, e aqui é o que as três escalas da peça fazem — a mesma conta, e muito menos linhas.
 *
 * <p>E ela é construída <b>de cabeça para baixo</b>: as contas crescem para o chão, e quem a põe de pé é o
 * meio-giro em Z do desenhista. É o jeito de 2014 de desenhar uma estátua, e as contas são as dele.
 */
public final class WerewolfStatueModel {
    public static final ModelLayerLocation STATUE =
            new ModelLayerLocation(Thaumcraft.id("werewolf_statue"), "main");

    /** Quanto o senhor e os lobos encolhem, cada um em volta do próprio ponto. */
    public static final float SENHOR = 0.7f;
    public static final float LOBO = 0.5f;

    /** As sete peças de raiz, pela ordem em que o original as desenha. */
    public static final List<String> RAÍZES = List.of(
            "lord", "step", "plinth_top", "plinth_base", "wolf_right", "plinth_middle", "wolf_left");

    private WerewolfStatueModel() {
    }

    /** Cento e vinte e oito por cento e vinte e oito de textura. */
    public static LayerDefinition statue() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        // ---------------------------------------------------------- o pedestal e o degrau
        raiz.addOrReplaceChild("plinth_base", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 16.0f, 2.0f, 16.0f),
                PartPose.offset(-8.0f, 22.0f, -8.0f));
        raiz.addOrReplaceChild("plinth_middle", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 14.0f, 1.0f, 14.0f),
                PartPose.offset(-7.0f, 21.0f, -7.0f));
        raiz.addOrReplaceChild("plinth_top", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 16.0f, 1.0f, 16.0f),
                PartPose.offset(-8.0f, 20.0f, -8.0f));
        raiz.addOrReplaceChild("step", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 4.0f, 2.0f, 4.0f),
                PartPose.offset(-3.9f, 18.0f, -6.5f));

        // ---------------------------------------------------------- o senhor
        PartDefinition senhor = raiz.addOrReplaceChild("lord", CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f),
                PartPose.offset(0.0f, 3.1f, 0.0f));

        // a cabeça de lobo dele, com as duas orelhas e o focinho
        PartDefinition cabeça = senhor.addOrReplaceChild("lord_head", CubeListBuilder.create()
                        .texOffs(0, 42).addBox(-3.0f, -6.0f, -2.0f, 6.0f, 6.0f, 5.0f),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.13665928f, 0.13665928f, 0.0f));
        cabeça.addOrReplaceChild("lord_ear_right", CubeListBuilder.create()
                        .texOffs(16, 14).addBox(-3.0f, -8.0f, 2.0f, 2.0f, 2.0f, 1.0f),
                PartPose.ZERO);
        cabeça.addOrReplaceChild("lord_ear_left", CubeListBuilder.create()
                        .texOffs(16, 14).addBox(1.0f, -8.0f, 2.0f, 2.0f, 2.0f, 1.0f),
                PartPose.ZERO);
        cabeça.addOrReplaceChild("lord_snout", CubeListBuilder.create()
                        .texOffs(0, 10).addBox(-1.5f, -3.1f, -5.0f, 3.0f, 3.0f, 4.0f),
                PartPose.ZERO);

        senhor.addOrReplaceChild("lord_arm_left", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(40, 16).addBox(4.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offsetAndRotation(0.0f, 2.0f, 0.0f, -0.4553564f, -0.13665928f, 0.0f));

        // e o direito, atrás das costas, com a lança presa a ele
        PartDefinition braçoDireito = senhor.addOrReplaceChild("lord_arm_right", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(40, 16).addBox(-8.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offsetAndRotation(0.0f, 2.0f, 0.0f, -2.4586453f, 0.4098033f, 0.0f));
        braçoDireito.addOrReplaceChild("lord_spear", CubeListBuilder.create()
                        .texOffs(0, 33).addBox(-6.5f, 8.0f, -17.9f, 1.0f, 1.0f, 40.0f),
                PartPose.ZERO);

        senhor.addOrReplaceChild("lord_leg_left", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 16).addBox(0.0f, 12.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.ZERO);

        // a perna direita vem dobrada, em duas peças: é a que diz que ele acabou de parar
        PartDefinition pernaDireita = senhor.addOrReplaceChild("lord_leg_right", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 16).addBox(-4.0f, 7.4f, 8.0f, 4.0f, 6.0f, 4.0f),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -(float) (Math.PI / 3.0), 0.0f, 0.0f));
        pernaDireita.addOrReplaceChild("lord_shin_right", CubeListBuilder.create()
                        .texOffs(18, 0).addBox(-4.0f, 15.7f, -6.2f, 4.0f, 6.0f, 4.0f),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.95609134f, 0.0f, 0.0f));

        // ---------------------------------------------------------- e os dois lobos
        lobo(raiz, "wolf_right", -5.0f, 3.5f, 0.27314404f, true);
        lobo(raiz, "wolf_left", 5.5f, -2.0f, 0.31869712f, false);

        return LayerDefinition.create(malha, 128, 128);
    }

    /**
     * Um dos dois lobos.
     *
     * <p>Os dois são a mesma coisa com dois ângulos e dois lugares diferentes, com uma exceção: a
     * <b>cabeça</b>. O da direita tem a cabeça <b>virada e baixada</b> — e com a juba virada com ela —, o da
     * esquerda tem-na direita. Um olha para quem chega e o outro olha para longe, e é isso que faz os dois
     * parecerem vivos em vez de iguais.
     */
    private static void lobo(PartDefinition raiz, String nome, float x, float z, float giro, boolean olhando) {
        PartDefinition corpo = raiz.addOrReplaceChild(nome, CubeListBuilder.create()
                        .texOffs(18, 14).addBox(-3.0f, -2.0f, -3.0f, 6.0f, 6.0f, 9.0f),
                PartPose.offsetAndRotation(x, 14.0f, z, 0.0f, giro, 0.0f));

        corpo.addOrReplaceChild(nome + "_mane", CubeListBuilder.create()
                        .texOffs(21, 0).addBox(-4.0f, -3.0f, -9.0f, 8.0f, 7.0f, 6.0f),
                olhando ? PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, -0.13665928f, 0.0f)
                        : PartPose.ZERO);

        PartDefinition cabeça = corpo.addOrReplaceChild(nome + "_head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0f, -2.5f, -13.0f, 6.0f, 6.0f, 4.0f),
                olhando
                        ? PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.22759093f, -0.22759093f, 0.0f)
                        : PartPose.ZERO);
        cabeça.addOrReplaceChild(nome + "_ear_right", CubeListBuilder.create()
                        .texOffs(16, 14).addBox(-3.0f, -4.5f, -10.0f, 2.0f, 2.0f, 1.0f),
                PartPose.ZERO);
        cabeça.addOrReplaceChild(nome + "_ear_left", CubeListBuilder.create()
                        .texOffs(16, 14).addBox(1.0f, -4.5f, -10.0f, 2.0f, 2.0f, 1.0f),
                PartPose.ZERO);
        cabeça.addOrReplaceChild(nome + "_snout", CubeListBuilder.create()
                        .texOffs(0, 10).addBox(-1.5f, 0.5f, -17.1f, 3.0f, 3.0f, 4.0f),
                PartPose.ZERO);

        corpo.addOrReplaceChild(nome + "_tail", CubeListBuilder.create()
                        .texOffs(9, 18).addBox(-1.0f, 0.0f, 4.0f, 2.0f, 8.0f, 2.0f),
                PartPose.offsetAndRotation(0.0f, 0.6f, 0.0f, 0.31869712f, 0.0f, 0.0f));

        // as quatro patas: as duas de trás viradas um pouco, no lobo que olha
        corpo.addOrReplaceChild(nome + "_leg_1", CubeListBuilder.create()
                        .texOffs(0, 18).addBox(-2.5f, 4.0f, 3.0f, 2.0f, 8.0f, 2.0f),
                PartPose.ZERO);
        corpo.addOrReplaceChild(nome + "_leg_2", CubeListBuilder.create()
                        .texOffs(0, 18).addBox(0.5f, 4.0f, 3.0f, 2.0f, 8.0f, 2.0f),
                PartPose.ZERO);
        corpo.addOrReplaceChild(nome + "_leg_3", CubeListBuilder.create()
                        .texOffs(0, 18).addBox(-2.5f, 4.0f, -8.0f, 2.0f, 8.0f, 2.0f),
                olhando ? PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, -0.091106184f, 0.0f)
                        : PartPose.ZERO);
        corpo.addOrReplaceChild(nome + "_leg_4", CubeListBuilder.create()
                        .texOffs(0, 18).addBox(0.5f, 4.0f, -8.0f, 2.0f, 8.0f, 2.0f),
                olhando ? PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 0.0f, -0.091106184f, 0.0f)
                        : PartPose.ZERO);
    }

    /** Põe as três escalas que o original faz com {@code glScaled}. */
    public static void encolhe(ModelPart raiz) {
        escala(raiz.getChild("lord"), SENHOR);
        escala(raiz.getChild("wolf_right"), LOBO);
        escala(raiz.getChild("wolf_left"), LOBO);
    }

    private static void escala(ModelPart qual, float quanto) {
        qual.xScale = quanto;
        qual.yScale = quanto;
        qual.zScale = quanto;
    }
}
