package net.thaumcraft.occulta.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;

/**
 * O Lobisomem do Witchery desenhado: o {@code ModelWolfman}, caixa por caixa.
 *
 * <p><b>O jogo não usa mais esta malha</b>, e é uma escolha declarada: o corpo que se vê é o do
 * {@link WerewolfModel}, tirado do Mo'Creatures, porque este tem dez peças e aquele tem quarenta e quatro.
 * O que fica daqui é a <b>camada</b> {@link #WOLFMAN}, que é onde o lobisomem mora.
 *
 * <p>Ela fica escrita por inteiro de propósito. É o porte fiel do original, e quem quiser o lobisomem do
 * Witchery de volta troca uma linha no {@code ThaumcraftClient} e tem-no outra vez.
 *
 * <p>Dez partes, e a graça delas é que ele <b>não é um homem com cabeça de lobo</b>: o tronco inclina-se para
 * a frente, as pernas são de bicho — coxa e canela em dois pedaços, dobradas ao contrário —, os braços caem
 * até ao chão e há uma cauda. Ele corre como um lobo e levanta-se como um homem, e é isso que o faz assustar.
 *
 * <p>As duas orelhas e o focinho são caixas <b>somadas à cabeça</b>, e por isso acompanham o olhar. E o
 * espelho do original está onde vale: na perna e no braço esquerdos, ligados <b>antes</b> das caixas.
 *
 * <p>As pernas têm <b>um teto na dobra</b> — {@code Math.max(..., -0.8)} —, que é o que impede o lobisomem de
 * abrir as pernas para trás ao correr depressa. Fica.
 */
public final class WolfmanModel extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation WOLFMAN = new ModelLayerLocation(Thaumcraft.id("wolfman"), "main");

    /** A inclinação de repouso do tronco e das coxas: o mesmo número, e não é acaso. */
    public static final float INCLINADO = 0.4098033f;

    /** E o teto da dobra das pernas. */
    public static final float TETO_DA_PERNA = -0.8f;

    private final ModelPart cabeça;
    private final ModelPart tronco;
    private final ModelPart coxaDireita;
    private final ModelPart coxaEsquerda;
    private final ModelPart braçoDireito;
    private final ModelPart braçoEsquerdo;

    public WolfmanModel(ModelPart raiz) {
        super(raiz);
        this.cabeça = raiz.getChild("head");
        this.tronco = raiz.getChild("body_upper");
        this.coxaDireita = raiz.getChild("leg_right_upper");
        this.coxaEsquerda = raiz.getChild("leg_left_upper");
        this.braçoDireito = raiz.getChild("arm_right");
        this.braçoEsquerdo = raiz.getChild("arm_left");
    }

    /** Sessenta e quatro por sessenta e quatro de textura, como no original. */
    public static LayerDefinition wolfman() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        // a cabeça, com as duas orelhas e o focinho presos a ela
        raiz.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0f, -6.0f, -2.0f, 6.0f, 6.0f, 4.0f, new CubeDeformation(0.05f))
                        .texOffs(16, 14).addBox(-3.0f, -8.0f, 1.0f, 2.0f, 2.0f, 1.0f)
                        .texOffs(16, 14).addBox(1.0f, -8.0f, 1.0f, 2.0f, 2.0f, 1.0f)
                        .texOffs(0, 10).addBox(-1.5f, -3.1f, -5.0f, 3.0f, 3.0f, 4.0f),
                PartPose.offset(0.0f, 0.0f, -2.0f));

        PartDefinition tronco = raiz.addOrReplaceChild("body_upper", CubeListBuilder.create()
                        .texOffs(0, 35).addBox(-5.0f, 0.0f, -3.9f, 10.0f, 7.0f, 8.0f),
                PartPose.offsetAndRotation(0.0f, -0.1f, -2.0f, INCLINADO, 0.0f, 0.0f));

        tronco.addOrReplaceChild("body_lower", CubeListBuilder.create()
                        .texOffs(3, 50).addBox(-4.0f, 2.0f, -2.3f, 8.0f, 7.0f, 5.0f),
                PartPose.offset(0.0f, 5.0f, -1.5f));

        raiz.addOrReplaceChild("tail", CubeListBuilder.create()
                        .texOffs(55, 52).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 10.0f, 2.0f),
                PartPose.offsetAndRotation(0.0f, 11.9f, 3.6f, 0.59184116f, 0.0f, 0.0f));

        PartDefinition coxaEsquerda = raiz.addOrReplaceChild("leg_left_upper", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(38, 0).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 7.0f, 4.0f),
                PartPose.offsetAndRotation(2.0f, 12.0f, 0.0f, -INCLINADO, 0.0f, 0.0f));
        coxaEsquerda.addOrReplaceChild("leg_left_lower", CubeListBuilder.create()
                        .texOffs(38, 13).addBox(-2.0f, 3.5f, 2.0f, 4.0f, 8.0f, 4.0f),
                PartPose.ZERO);

        PartDefinition coxaDireita = raiz.addOrReplaceChild("leg_right_upper", CubeListBuilder.create()
                        .texOffs(38, 0).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 7.0f, 4.0f),
                PartPose.offsetAndRotation(-2.0f, 12.0f, 0.0f, -INCLINADO, 0.0f, 0.0f));
        coxaDireita.addOrReplaceChild("leg_right_lower", CubeListBuilder.create()
                        .texOffs(38, 13).addBox(-2.0f, 3.5f, 2.0f, 4.0f, 8.0f, 4.0f),
                PartPose.ZERO);

        raiz.addOrReplaceChild("arm_left", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(38, 46).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 14.0f, 4.0f),
                PartPose.offset(6.0f, 2.0f, 0.0f));

        // e o braço direito nasce meio bloco mais para dentro do que o esquerdo: é do original
        raiz.addOrReplaceChild("arm_right", CubeListBuilder.create()
                        .texOffs(38, 46).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 14.0f, 4.0f),
                PartPose.offset(-5.8f, 2.0f, 0.0f));

        return LayerDefinition.create(malha, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState estado) {
        super.setupAnim(estado);
        float passo = estado.walkAnimationPos;
        float força = estado.walkAnimationSpeed;

        this.cabeça.yRot = estado.yRot * Mth.DEG_TO_RAD;
        this.cabeça.xRot = estado.xRot * Mth.DEG_TO_RAD;

        this.coxaDireita.xRot = Math.max(
                -INCLINADO + Mth.cos(passo * 0.6662f) * 1.4f * força, TETO_DA_PERNA);
        this.coxaEsquerda.xRot = Math.max(
                -INCLINADO + Mth.cos(passo * 0.6662f + Mth.PI) * 1.4f * força, TETO_DA_PERNA);

        this.braçoDireito.xRot = Mth.cos(passo * 0.6662f + Mth.PI) * 2.0f * força * 0.5f;
        this.braçoEsquerdo.xRot = Mth.cos(passo * 0.6662f) * 2.0f * força * 0.5f;
        this.braçoDireito.zRot = 0.0f;
        this.braçoEsquerdo.zRot = 0.0f;
        this.tronco.xRot = INCLINADO;
    }
}
