package net.thaumcraft.occulta.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.thaumcraft.Thaumcraft;

/**
 * O <b>Demônio</b>, caixa por caixa: o {@code ModelDemon} do Witchery.
 *
 * <p>Ele é um golem de ferro com um corpo de demônio por cima — e a lista de peças conta isso:
 *
 * <ul>
 *   <li>uma <b>cara</b> de oito por oito com <b>dois chifres</b> de um pixel que sobem oito, <b>dois
 *       dentes</b> que descem do lábio de cima, um <b>focinho</b> de dois e um <b>lábio de baixo</b>;</li>
 *   <li>um corpo de oito por catorze, braços de <b>vinte</b> de comprido que lhe chegam abaixo dos
 *       joelhos, e pernas de vinte;</li>
 *   <li>e <b>duas asas chatas</b> de catorze por vinte e um, presas às costas e abertas em ângulos
 *       diferentes — uma a trinta e oito graus do corpo, a outra virada ao contrário.</li>
 * </ul>
 *
 * <p>As asas são <b>chapas</b>, sem grossura nenhuma, e nunca se mexem. Ele não voa: elas estão ali para
 * dizer o que ele é.
 *
 * <p>E ele anda <b>como um golem</b>: pernas e braços balançam na curva de triângulo do {@code triangleWave},
 * que é a do golem de ferro e não a de gente — um passo duro, de coisa pesada. Os braços levam um
 * <b>desconto de dois décimos</b> que os deixa um dedo à frente do corpo, pendurados, mesmo parado.
 *
 * <p>No golpe, o <b>braço direito</b> sai de dois radianos atrás e desce — é o mesmo martelo do golem de
 * ferro, com os mesmos dez quadros.
 */
public class DemonModel extends EntityModel<DemonRenderer.State> {
    public static final ModelLayerLocation DEMÔNIO =
            new ModelLayerLocation(Thaumcraft.id("demon"), "main");

    /** O quanto perna e braço abrem a andar, que é o número do golem. */
    public static final float A_PERNA = 1.5f;
    public static final float O_BRAÇO = 1.5f;
    public static final float PENDE = 0.2f;
    public static final float O_PASSO = 13.0f;

    /** E o martelo: de dois radianos atrás, em dez quadros. */
    public static final float O_MARTELO = 2.0f;
    public static final float QUADROS_DO_GOLPE = 10.0f;

    private final ModelPart cabeça;
    private final ModelPart corpo;
    private final ModelPart braçoD;
    private final ModelPart braçoE;
    private final ModelPart pernaD;
    private final ModelPart pernaE;
    private final ModelPart asaD;
    private final ModelPart asaE;

    public DemonModel(ModelPart raiz) {
        super(raiz);
        this.cabeça = raiz.getChild("head");
        this.corpo = raiz.getChild("body");
        this.braçoD = raiz.getChild("rightarm");
        this.braçoE = raiz.getChild("leftarm");
        this.pernaD = raiz.getChild("rightleg");
        this.pernaE = raiz.getChild("leftleg");
        this.asaD = raiz.getChild("wingRight");
        this.asaE = raiz.getChild("wingLeft");
    }

    /** Cento e vinte e oito por trinta e dois de textura. */
    public static LayerDefinition demônio() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        /*
         * A cabeça são sete caixas com coordenadas de textura próprias — o original lhes dá nome, uma a uma,
         * que era o jeito de então de pôr sete desenhos numa peça só.
         */
        raiz.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f)
                        .texOffs(0, 16).addBox(4.0f, -12.0f, -0.5f, 1.0f, 8.0f, 1.0f)
                        .texOffs(0, 16).addBox(-5.0f, -12.0f, -0.5f, 1.0f, 8.0f, 1.0f)
                        .texOffs(4, 16).addBox(1.0f, -4.0f, -5.0f, 1.0f, 2.0f, 1.0f)
                        .texOffs(4, 16).addBox(-2.0f, -4.0f, -5.0f, 1.0f, 2.0f, 1.0f)
                        .texOffs(8, 16).addBox(-2.0f, -2.0f, -6.0f, 4.0f, 1.0f, 2.0f)
                        .texOffs(20, 16).addBox(-1.0f, -6.0f, -6.0f, 2.0f, 3.0f, 2.0f),
                PartPose.offset(0.0f, -9.0f, 0.0f));

        raiz.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(64, 0).addBox(-4.0f, 0.0f, -3.0f, 8.0f, 14.0f, 6.0f),
                PartPose.offset(0.0f, -9.0f, 0.0f));
        raiz.addOrReplaceChild("rightarm", CubeListBuilder.create()
                        .texOffs(48, 0).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 20.0f, 4.0f),
                PartPose.offset(-5.0f, -7.0f, 0.0f));
        raiz.addOrReplaceChild("leftarm", CubeListBuilder.create()
                        .texOffs(48, 0).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 20.0f, 4.0f),
                PartPose.offset(5.0f, -7.0f, 0.0f));
        raiz.addOrReplaceChild("rightleg", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 20.0f, 4.0f),
                PartPose.offset(-2.0f, 4.0f, 0.0f));
        raiz.addOrReplaceChild("leftleg", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 20.0f, 4.0f),
                PartPose.offset(2.0f, 4.0f, 0.0f));

        /*
         * As duas asas saem da mesma esquina da chapa e nenhuma delas é espelhada: o original liga o espelho
         * depois de criar as caixas, e depois de criada a caixa o espelho não faz mais nada. As duas levam
         * então o mesmo desenho, e o que as distingue é só o ângulo.
         */
        raiz.addOrReplaceChild("wingRight", CubeListBuilder.create()
                        .texOffs(93, 0).addBox(0.0f, 0.0f, 0.0f, 14.0f, 21.0f, 0.0f),
                PartPose.offsetAndRotation(1.0f, -8.0f, 3.0f, 0.3047198f, -0.6698132f, -0.6283185f));
        raiz.addOrReplaceChild("wingLeft", CubeListBuilder.create()
                        .texOffs(93, 0).addBox(0.0f, 0.0f, 0.0f, 14.0f, 21.0f, 0.0f),
                PartPose.offsetAndRotation(-1.0f, -8.0f, 3.0f, -0.3047198f, 3.811406f, 0.6283185f));

        return LayerDefinition.create(malha, 128, 32);
    }

    /**
     * O passo dele: a cabeça olha, e as quatro pontas andam na <b>curva de triângulo</b> do golem.
     *
     * <p>Enquanto o golpe corre, o braço direito deixa o passo e faz o <b>martelo</b>: ele vem de dois
     * radianos atrás e desce na mesma curva, só que medida nos dez quadros do golpe em vez dos treze do
     * passo. O braço esquerdo fica onde estava — o original não o toca, e é por isso que o golem parece
     * bater com um lado só.
     */
    @Override
    public void setupAnim(DemonRenderer.State estado) {
        super.setupAnim(estado);

        this.cabeça.yRot = estado.yRot * ((float) Math.PI / 180.0f);
        this.cabeça.xRot = estado.xRot * ((float) Math.PI / 180.0f);

        float onda = triângulo(estado.walkAnimationPos, O_PASSO);
        this.pernaE.xRot = -A_PERNA * onda * estado.walkAnimationSpeed;
        this.pernaD.xRot = A_PERNA * onda * estado.walkAnimationSpeed;
        this.pernaE.yRot = 0.0f;
        this.pernaD.yRot = 0.0f;

        if (estado.golpe > 0.0f) {
            this.braçoD.xRot = -O_MARTELO + O_BRAÇO * triângulo(estado.golpe, QUADROS_DO_GOLPE);
        } else {
            this.braçoD.xRot = (-PENDE + O_BRAÇO * onda) * estado.walkAnimationSpeed;
            this.braçoE.xRot = (-PENDE - O_BRAÇO * onda) * estado.walkAnimationSpeed;
        }
    }

    /**
     * A curva de triângulo do golem de ferro: o {@code triangleWave} do jogo de então.
     *
     * <p>Ela sobe e desce em linha reta em vez de em seno, e é isso que dá ao golem — e ao demônio — aquele
     * passo <b>duro</b>, de coisa que não dobra.
     */
    public static float triângulo(float passo, float largura) {
        return (Math.abs(passo % largura - largura * 0.5f) - largura * 0.25f) / (largura * 0.25f);
    }
}
