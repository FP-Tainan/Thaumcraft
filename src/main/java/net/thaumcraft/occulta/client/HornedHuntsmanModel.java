package net.thaumcraft.occulta.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;

/**
 * O Caçador Cornudo desenhado: o {@code ModelHornedAvatar} do Witchery, caixa por caixa.
 *
 * <p>Ele não é um gigante: é um <b>homem com as proporções erradas</b>, e é nisso que está o susto. O
 * <b>peito</b> mede vinte de largura — mais do que o corpo inteiro de um aldeão — e os <b>antebraços e as
 * canelas são mais grossos do que os braços e as coxas</b>, como se o que ele tem de carne estivesse todo nas
 * pontas.
 *
 * <p>Os <b>chifres</b> são uma caixa <b>chata</b>, de vinte por dezessete e <b>zero de fundo</b>: uma folha de
 * duas faces presa à cabeça. É o jeito de 2014 de desenhar galhada, e funciona porque ela acompanha o olhar.
 *
 * <p>E a <b>lança</b> é uma peça própria presa ao antebraço direito, com o cabo de cinquenta de comprido e
 * <b>duas pontas chatas cruzadas</b> — uma em x, outra em z —, que de qualquer ângulo parecem uma só.
 *
 * <p>As pernas vêm dobradas de fábrica — joelho para trás, pé para a frente — e ficam assim mesmo paradas: o
 * Caçador <b>nunca está de pé direito</b>.
 */
public final class HornedHuntsmanModel extends EntityModel<HornedHuntsmanRenderer.State> {
    public static final ModelLayerLocation HUNTSMAN =
            new ModelLayerLocation(Thaumcraft.id("horned_huntsman"), "main");

    /** O passo dele: treze de compasso nas pernas e dez na pancada. */
    public static final float COMPASSO = 13.0f;
    public static final float COMPASSO_DA_PANCADA = 10.0f;

    private final ModelPart cabeça;
    private final ModelPart pernaDireita;
    private final ModelPart pernaEsquerda;
    private final ModelPart canelaDireita;
    private final ModelPart canelaEsquerda;
    private final ModelPart péDireito;
    private final ModelPart péEsquerdo;
    private final ModelPart braçoDireito;
    private final ModelPart braçoEsquerdo;

    public HornedHuntsmanModel(ModelPart raiz) {
        super(raiz);
        this.cabeça = raiz.getChild("head");
        this.pernaDireita = raiz.getChild("leg_right");
        this.pernaEsquerda = raiz.getChild("leg_left");
        this.canelaDireita = this.pernaDireita.getChild("shin_right");
        this.canelaEsquerda = this.pernaEsquerda.getChild("shin_left");
        this.péDireito = this.canelaDireita.getChild("foot_right");
        this.péEsquerdo = this.canelaEsquerda.getChild("foot_left");
        this.braçoDireito = raiz.getChild("arm_right");
        this.braçoEsquerdo = raiz.getChild("arm_left");
    }

    /** Cento e vinte e oito por cento e vinte e oito de textura, como no original. */
    public static LayerDefinition huntsman() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        // a cabeça, com a galhada chata presa a ela
        PartDefinition cabeça = raiz.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(4, 112).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f),
                PartPose.offset(0.0f, -16.0f, 0.0f));
        cabeça.addOrReplaceChild("horns", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(0, 88).addBox(-10.0f, -24.0f, 0.0f, 20.0f, 17.0f, 0.0f),
                PartPose.ZERO);

        raiz.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(12, 61).addBox(-6.0f, 0.0f, -3.0f, 12.0f, 8.0f, 6.0f),
                PartPose.offset(0.0f, -8.0f, 0.0f));

        raiz.addOrReplaceChild("chest", CubeListBuilder.create()
                        .texOffs(0, 43).addBox(0.0f, 0.0f, 0.0f, 20.0f, 8.0f, 10.0f),
                PartPose.offset(-10.0f, -16.0f, -5.0f));

        raiz.addOrReplaceChild("hips", CubeListBuilder.create()
                        .texOffs(8, 75).addBox(-7.0f, 0.0f, -4.0f, 14.0f, 4.0f, 8.0f),
                PartPose.ZERO);

        // os braços: braço, antebraço grosso, e a lança no direito
        PartDefinition braçoDireito = raiz.addOrReplaceChild("arm_right", CubeListBuilder.create()
                        .texOffs(72, 50).addBox(-4.0f, -2.0f, -2.0f, 4.0f, 13.0f, 4.0f),
                PartPose.offset(-10.0f, -13.0f, 0.0f));
        PartDefinition antebraçoDireito = braçoDireito.addOrReplaceChild("forearm_right",
                CubeListBuilder.create()
                        .texOffs(68, 67).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 14.0f, 6.0f),
                PartPose.offsetAndRotation(-2.0f, 10.0f, 0.0f, -Mth.PI / 6.0f, 0.0f, 0.0f));
        antebraçoDireito.addOrReplaceChild("spear", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(61, 14).addBox(-0.5f, -30.0f, -0.5f, 1.0f, 50.0f, 1.0f)
                        .texOffs(60, 8).addBox(-1.5f, -35.0f, 0.0f, 3.0f, 6.0f, 0.0f)
                        .texOffs(60, 5).addBox(0.0f, -35.0f, -1.5f, 0.0f, 6.0f, 3.0f),
                PartPose.offsetAndRotation(0.0f, 12.0f, 0.0f, 1.5f, 0.0f, 0.0f));

        PartDefinition braçoEsquerdo = raiz.addOrReplaceChild("arm_left", CubeListBuilder.create()
                        .texOffs(72, 50).addBox(0.0f, -2.0f, -2.0f, 4.0f, 13.0f, 4.0f),
                PartPose.offset(10.0f, -13.0f, 0.0f));
        braçoEsquerdo.addOrReplaceChild("forearm_left", CubeListBuilder.create()
                        .texOffs(68, 67).addBox(-3.0f, 0.0f, -3.0f, 6.0f, 14.0f, 6.0f),
                PartPose.offsetAndRotation(2.0f, 10.0f, 0.0f, -Mth.PI / 6.0f, 0.0f, 0.0f));

        // e as pernas, dobradas de fábrica
        perna(raiz, "right", -4.0f);
        perna(raiz, "left", 4.0f);

        return LayerDefinition.create(malha, 128, 128);
    }

    /**
     * Uma perna: coxa, canela e pé, cada um preso ao de cima.
     *
     * <p>Os três ângulos de repouso são os do original — a coxa trinta graus para a frente, a canela
     * <b>nove décimos para trás</b> e o pé meio para a frente —, e é esse joelho dobrado que faz dele uma
     * coisa que anda como bicho.
     */
    private static void perna(PartDefinition raiz, String lado, float x) {
        PartDefinition coxa = raiz.addOrReplaceChild("leg_" + lado, CubeListBuilder.create()
                        .texOffs(72, 0).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 10.0f, 4.0f),
                PartPose.offsetAndRotation(x, 3.0f, -1.0f, Mth.PI / 6.0f, 0.0f, 0.0f));
        PartDefinition canela = coxa.addOrReplaceChild("shin_" + lado, CubeListBuilder.create()
                        .texOffs(68, 14).addBox(-3.0f, -2.0f, -3.0f, 6.0f, 14.0f, 6.0f),
                PartPose.offsetAndRotation(0.0f, 10.0f, 0.0f, -0.9f, 0.0f, 0.0f));
        canela.addOrReplaceChild("foot_" + lado, CubeListBuilder.create()
                        .texOffs(69, 34).addBox(-2.0f, 0.0f, -5.0f, 4.0f, 3.0f, 7.0f),
                PartPose.offsetAndRotation(0.0f, 10.0f, 0.0f, 0.5f, 0.0f, 0.0f));
    }

    /**
     * A onda triangular do original: o {@code func_78172_a}.
     *
     * <p>Ela sobe e desce em linha reta em vez de em curva, e é por isso que o passo dele é <b>duro</b> — o
     * jogo usa um seno, e o Caçador não.
     */
    public static float onda(float onde, float compasso) {
        return (Math.abs(onde % compasso - compasso * 0.5f) - compasso * 0.25f) / (compasso * 0.25f);
    }

    @Override
    public void setupAnim(HornedHuntsmanRenderer.State estado) {
        super.setupAnim(estado);
        float passo = estado.walkAnimationPos;
        float força = estado.walkAnimationSpeed;

        this.cabeça.yRot = estado.yRot * Mth.DEG_TO_RAD;
        this.cabeça.xRot = estado.xRot * Mth.DEG_TO_RAD;

        this.pernaEsquerda.xRot = -1.3f * onda(passo, COMPASSO) * força + 0.5f;
        this.pernaDireita.xRot = 1.3f * onda(passo, COMPASSO) * força + 0.5f;
        /*
         * E as canelas vêm cruzadas: a esquerda segue a coxa direita e a direita segue a esquerda. É o
         * original, e não é descuido que se note — a perna dobra ao contrário de qualquer jeito.
         */
        this.canelaEsquerda.xRot = 0.8f * (this.pernaDireita.xRot - 0.5f) - 1.0f;
        this.canelaDireita.xRot = 0.8f * (this.pernaEsquerda.xRot - 0.5f) - 1.0f;
        this.péEsquerdo.xRot = Math.max(1.4f * (this.pernaEsquerda.xRot - 0.5f) + 0.5f, 0.2f);
        this.péDireito.xRot = Math.max(1.4f * (this.pernaDireita.xRot - 0.5f) + 0.5f, 0.2f);
        this.pernaEsquerda.yRot = 0.0f;
        this.pernaDireita.yRot = 0.0f;

        // a pancada: o braço direito sobe por cima do ombro e desce
        if (estado.braço > 0.0f) {
            this.braçoDireito.xRot = -2.0f + 1.5f * onda(estado.braço, COMPASSO_DA_PANCADA);
            return;
        }
        this.braçoDireito.xRot = (-0.2f + 1.5f * onda(passo, COMPASSO)) * força * 0.2f;
        this.braçoEsquerdo.xRot = (-0.2f - 1.5f * onda(passo, COMPASSO)) * força * 0.2f;
    }
}
