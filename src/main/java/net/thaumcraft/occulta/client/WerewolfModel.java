package net.thaumcraft.occulta.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * O <b>Lobisomem</b>, caixa por caixa: o {@code MoCModelWere} do Mo'Creatures.
 *
 * <p><b>Esta é uma troca de gosto, e está declarada como tal.</b> O lobisomem do Witchery é um boneco de
 * jogador com um focinho: o mesmo corpo de seis caixas que todo biped tem, com uma cabeça de lobo por cima.
 * De longe não se distingue de um zumbi de chapéu.
 *
 * <p>O do Mo'Creatures são <b>quarenta e quatro peças</b>, e a lista diz por quê:
 *
 * <ul>
 *   <li>uma cabeça em <b>nove</b> — crânio, focinho, nariz, <b>dentes de cima e de baixo</b>, boca, duas
 *       orelhas e duas <b>suíças</b> viradas para fora;</li>
 *   <li><b>pescoço em dois pedaços</b> inclinados, que é o que lhe dá a corcunda;</li>
 *   <li>peito e barriga separados, cada um com a sua inclinação;</li>
 *   <li><b>rabo em quatro segmentos</b>;</li>
 *   <li>braços e pernas em <b>três pedaços</b> cada — e <b>cinco dedos</b> em cada mão.</li>
 * </ul>
 *
 * <p>Dez dedos. É o detalhe que diz tudo: o do Witchery tem dois cubos por mãos.
 *
 * <p>E ele tem <b>duas posturas</b> no original — de pé e <b>encurvado</b> —, e o que muda entre elas não
 * são rotações: são os <b>pontos de giro</b> da cabeça, do pescoço, do peito, da barriga, dos braços, das
 * pernas e do rabo, todos mudados ao mesmo tempo. Este porte usa a de <b>pé</b>, que é a do lobisomem do
 * Witchery; os números da encurvada ficam escritos aqui para quando alguém a quiser.
 */
public class WerewolfModel extends EntityModel<LivingEntityRenderState> {
    /** As quarenta e quatro peças, pela ordem em que o original as desenha. */
    public static final List<String> PEÇAS = List.of(
            "Head", "Nose", "Snout", "TeethU", "TeethL", "Mouth", "LEar", "REar",
            "Neck", "Neck2", "SideburnL", "SideburnR", "Chest", "Abdomen",
            "TailA", "TailC", "TailB", "TailD",
            "RLegA", "RFoot", "RLegB", "RLegC", "LLegB", "LFoot", "LLegC", "LLegA",
            "RArmB", "RArmC", "LArmB", "RHand", "RArmA", "LArmA", "LArmC", "LHand",
            "RFinger1", "RFinger2", "RFinger3", "RFinger4", "RFinger5",
            "LFinger1", "LFinger2", "LFinger3", "LFinger4", "LFinger5");

    /** O que anda com a cabeça, com os braços, com as pernas e com o rabo. */
    private static final List<String> DA_CABEÇA = List.of(
            "Nose", "Snout", "TeethU", "TeethL", "Mouth", "LEar", "REar", "SideburnL", "SideburnR");
    private static final List<String> DOS_BRAÇOS = List.of(
            "LArmB", "LArmC", "LHand", "LFinger1", "LFinger2", "LFinger3", "LFinger4", "LFinger5",
            "RArmA", "RArmB", "RArmC", "RHand",
            "RFinger1", "RFinger2", "RFinger3", "RFinger4", "RFinger5");
    private static final List<String> DAS_PERNAS = List.of(
            "RLegA", "RLegB", "RLegC", "RFoot", "LLegB", "LLegC", "LFoot");
    private static final List<String> DO_RABO = List.of("TailB", "TailC", "TailD");
    private static final List<String> DEDOS_E = List.of(
            "LFinger1", "LFinger2", "LFinger3", "LFinger4", "LFinger5");
    private static final List<String> DEDOS_D = List.of(
            "RFinger1", "RFinger2", "RFinger3", "RFinger4", "RFinger5");

    /** A postura de pé, que é a do lobisomem do Witchery. */
    public static final float PESCOÇO = -34.0f;
    public static final float PEITO = 36.0f;
    public static final float BARRIGA = 15.0f;

    /** As dobras fixas das pernas, em radianos, que lhe dão o joelho de bicho. */
    public static final float PERNA_A = -0.8126625f;
    public static final float PERNA_B = -0.8445741f;
    public static final float PERNA_C = -0.2860688f;

    /** E as dos braços. */
    public static final float BRAÇO_B = 0.3490659f;
    public static final float BRAÇO_C = -0.3490659f;

    /** O balanço dos ombros, que vem do relógio do mundo e não do passo. */
    public static final float BALANÇO = 0.09f;
    public static final float QUANTO = 0.05f;

    /** A inclinação do nariz, da boca e das suíças, que andam com a cabeça. */
    public static final float O_NARIZ = 0.2792527f;
    public static final float A_BOCA = 2.530727f;
    public static final float A_SUÍÇA = -0.2094395f;
    public static final float A_SUÍÇA_ABERTA = 0.418879f;

    private final Map<String, ModelPart> partes = new HashMap<>();

    public WerewolfModel(ModelPart raiz) {
        super(raiz);
        for (String nome : PEÇAS) this.partes.put(nome, raiz.getChild(nome));
    }

    private ModelPart p(String nome) {
        return this.partes.get(nome);
    }

    public static LayerDefinition lobisomem() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("Head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0f, -3.0f, -6.0f, 8.0f, 8.0f, 6.0f),
                PartPose.offset(0.0f, -8.0f, -6.0f));
        raiz.addOrReplaceChild("Nose", CubeListBuilder.create()
                        .texOffs(44, 33).addBox(-1.5f, -1.7f, -12.3f, 3.0f, 2.0f, 7.0f),
                PartPose.offsetAndRotation(0.0f, -8.0f, -6.0f, 0.27925f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("Snout", CubeListBuilder.create()
                        .texOffs(0, 25).addBox(-2.0f, 2.0f, -12.0f, 4.0f, 2.0f, 6.0f),
                PartPose.offset(0.0f, -8.0f, -6.0f));
        raiz.addOrReplaceChild("TeethU", CubeListBuilder.create()
                        .texOffs(46, 18).addBox(-2.0f, 4.01f, -12.0f, 4.0f, 2.0f, 5.0f),
                PartPose.offset(0.0f, -8.0f, -6.0f));
        raiz.addOrReplaceChild("TeethL", CubeListBuilder.create()
                        .texOffs(20, 109).addBox(-1.5f, -12.5f, 2.01f, 3.0f, 5.0f, 2.0f),
                PartPose.offsetAndRotation(0.0f, -8.0f, -6.0f, 2.53073f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("Mouth", CubeListBuilder.create()
                        .texOffs(42, 69).addBox(-1.5f, -12.5f, 0.0f, 3.0f, 9.0f, 2.0f),
                PartPose.offsetAndRotation(0.0f, -8.0f, -6.0f, 2.53073f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("LEar", CubeListBuilder.create()
                        .texOffs(13, 14).addBox(0.5f, -7.5f, -1.0f, 3.0f, 5.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, -8.0f, -6.0f, 0.0f, 0.0f, 0.17453f));
        raiz.addOrReplaceChild("REar", CubeListBuilder.create()
                        .texOffs(22, 0).addBox(-3.5f, -7.5f, -1.0f, 3.0f, 5.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, -8.0f, -6.0f, 0.0f, 0.0f, -0.17453f));
        raiz.addOrReplaceChild("Neck", CubeListBuilder.create()
                        .texOffs(28, 0).addBox(-3.5f, -3.0f, -7.0f, 7.0f, 8.0f, 7.0f),
                PartPose.offsetAndRotation(0.0f, -5.0f, -2.0f, -0.6025f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("Neck2", CubeListBuilder.create()
                        .texOffs(0, 14).addBox(-1.5f, -2.0f, -5.0f, 3.0f, 4.0f, 7.0f),
                PartPose.offsetAndRotation(0.0f, -1.0f, -6.0f, -0.45379f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("SideburnL", CubeListBuilder.create()
                        .texOffs(28, 33).addBox(3.0f, 0.0f, -2.0f, 2.0f, 6.0f, 6.0f),
                PartPose.offsetAndRotation(0.0f, -8.0f, -6.0f, -0.20944f, 0.41888f, -0.08727f));
        raiz.addOrReplaceChild("SideburnR", CubeListBuilder.create()
                        .texOffs(28, 45).addBox(-5.0f, 0.0f, -2.0f, 2.0f, 6.0f, 6.0f),
                PartPose.offsetAndRotation(0.0f, -8.0f, -6.0f, -0.20944f, -0.41888f, 0.08727f));
        raiz.addOrReplaceChild("Chest", CubeListBuilder.create()
                        .texOffs(20, 15).addBox(-4.0f, 0.0f, -7.0f, 8.0f, 8.0f, 10.0f),
                PartPose.offsetAndRotation(0.0f, -6.0f, -2.5f, 0.64133f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("Abdomen", CubeListBuilder.create()
                        .texOffs(0, 40).addBox(-3.0f, -8.0f, -8.0f, 6.0f, 14.0f, 8.0f),
                PartPose.offsetAndRotation(0.0f, 4.5f, 5.0f, 0.26954f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("TailA", CubeListBuilder.create()
                        .texOffs(52, 42).addBox(-1.5f, -1.0f, -2.0f, 3.0f, 4.0f, 3.0f),
                PartPose.offsetAndRotation(0.0f, 9.5f, 6.0f, 1.06465f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("TailC", CubeListBuilder.create()
                        .texOffs(48, 59).addBox(-2.0f, 6.8f, -4.6f, 4.0f, 6.0f, 4.0f),
                PartPose.offsetAndRotation(0.0f, 9.5f, 6.0f, 1.09956f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("TailB", CubeListBuilder.create()
                        .texOffs(48, 49).addBox(-2.0f, 2.0f, -2.0f, 4.0f, 6.0f, 4.0f),
                PartPose.offsetAndRotation(0.0f, 9.5f, 6.0f, 0.75049f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("TailD", CubeListBuilder.create()
                        .texOffs(52, 69).addBox(-1.5f, 9.8f, -4.1f, 3.0f, 5.0f, 3.0f),
                PartPose.offsetAndRotation(0.0f, 9.5f, 6.0f, 1.09956f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("RLegA", CubeListBuilder.create()
                        .texOffs(12, 64).addBox(-2.5f, -1.5f, -3.5f, 3.0f, 8.0f, 5.0f),
                PartPose.offsetAndRotation(-3.0f, 9.5f, 3.0f, -0.81266f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("RFoot", CubeListBuilder.create()
                        .texOffs(14, 93).addBox(-2.50667f, 12.5f, -5.0f, 3.0f, 2.0f, 3.0f),
                PartPose.offset(-3.0f, 9.5f, 3.0f));
        raiz.addOrReplaceChild("RLegB", CubeListBuilder.create()
                        .texOffs(14, 76).addBox(-1.9f, 4.2f, 0.5f, 2.0f, 2.0f, 5.0f),
                PartPose.offsetAndRotation(-3.0f, 9.5f, 3.0f, -0.84457f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("RLegC", CubeListBuilder.create()
                        .texOffs(14, 83).addBox(-2.0f, 6.2f, 0.5f, 2.0f, 8.0f, 2.0f),
                PartPose.offsetAndRotation(-3.0f, 9.5f, 3.0f, -0.28607f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("LLegB", CubeListBuilder.create()
                        .texOffs(0, 76).addBox(-0.1f, 4.2f, 0.5f, 2.0f, 2.0f, 5.0f),
                PartPose.offsetAndRotation(3.0f, 9.5f, 3.0f, -0.84457f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("LFoot", CubeListBuilder.create()
                        .texOffs(0, 93).addBox(-0.50667f, 12.5f, -5.0f, 3.0f, 2.0f, 3.0f),
                PartPose.offset(3.0f, 9.5f, 3.0f));
        raiz.addOrReplaceChild("LLegC", CubeListBuilder.create()
                        .texOffs(0, 83).addBox(0.0f, 6.2f, 0.5f, 2.0f, 8.0f, 2.0f),
                PartPose.offsetAndRotation(3.0f, 9.5f, 3.0f, -0.28607f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("LLegA", CubeListBuilder.create()
                        .texOffs(0, 64).addBox(-0.5f, -1.5f, -3.5f, 3.0f, 8.0f, 5.0f),
                PartPose.offsetAndRotation(3.0f, 9.5f, 3.0f, -0.81266f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("RArmB", CubeListBuilder.create()
                        .texOffs(48, 77).addBox(-3.5f, 1.0f, -1.5f, 4.0f, 8.0f, 4.0f),
                PartPose.offsetAndRotation(-4.0f, -4.0f, -2.0f, 0.2618f, 0.0f, 0.34907f));
        raiz.addOrReplaceChild("RArmC", CubeListBuilder.create()
                        .texOffs(48, 112).addBox(-6.0f, 5.0f, 3.0f, 4.0f, 7.0f, 4.0f),
                PartPose.offsetAndRotation(-4.0f, -4.0f, -2.0f, -0.34907f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("LArmB", CubeListBuilder.create()
                        .texOffs(48, 89).addBox(-0.5f, 1.0f, -1.5f, 4.0f, 8.0f, 4.0f),
                PartPose.offsetAndRotation(4.0f, -4.0f, -2.0f, 0.2618f, 0.0f, -0.34907f));
        raiz.addOrReplaceChild("RHand", CubeListBuilder.create()
                        .texOffs(32, 118).addBox(-6.0f, 12.5f, -1.5f, 4.0f, 3.0f, 4.0f),
                PartPose.offset(-4.0f, -4.0f, -2.0f));
        raiz.addOrReplaceChild("RArmA", CubeListBuilder.create()
                        .texOffs(0, 108).addBox(-5.0f, -3.0f, -2.0f, 5.0f, 5.0f, 5.0f),
                PartPose.offsetAndRotation(-4.0f, -4.0f, -2.0f, 0.63204f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("LArmA", CubeListBuilder.create()
                        .texOffs(0, 98).addBox(0.0f, -3.0f, -2.0f, 5.0f, 5.0f, 5.0f),
                PartPose.offsetAndRotation(4.0f, -4.0f, -2.0f, 0.63204f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("LArmC", CubeListBuilder.create()
                        .texOffs(48, 101).addBox(2.0f, 5.0f, 3.0f, 4.0f, 7.0f, 4.0f),
                PartPose.offsetAndRotation(4.0f, -4.0f, -2.0f, -0.34907f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("LHand", CubeListBuilder.create()
                        .texOffs(32, 111).addBox(2.0f, 12.5f, -1.5f, 4.0f, 3.0f, 4.0f),
                PartPose.offset(4.0f, -4.0f, -2.0f));
        raiz.addOrReplaceChild("RFinger1", CubeListBuilder.create()
                        .texOffs(8, 120).addBox(-0.5f, 0.0f, -0.5f, 1.0f, 3.0f, 1.0f),
                PartPose.offset(-4.0f, -4.0f, -2.0f));
        raiz.addOrReplaceChild("RFinger2", CubeListBuilder.create()
                        .texOffs(12, 124).addBox(-3.5f, 15.5f, -1.5f, 1.0f, 3.0f, 1.0f),
                PartPose.offset(-4.0f, -4.0f, -2.0f));
        raiz.addOrReplaceChild("RFinger3", CubeListBuilder.create()
                        .texOffs(12, 119).addBox(-4.8f, 15.5f, -1.5f, 1.0f, 4.0f, 1.0f),
                PartPose.offset(-4.0f, -4.0f, -2.0f));
        raiz.addOrReplaceChild("RFinger4", CubeListBuilder.create()
                        .texOffs(16, 119).addBox(-6.0f, 15.5f, -0.5f, 1.0f, 4.0f, 1.0f),
                PartPose.offset(-4.0f, -4.0f, -2.0f));
        raiz.addOrReplaceChild("RFinger5", CubeListBuilder.create()
                        .texOffs(16, 124).addBox(-6.0f, 15.5f, 1.0f, 1.0f, 3.0f, 1.0f),
                PartPose.offset(-4.0f, -4.0f, -2.0f));
        raiz.addOrReplaceChild("LFinger1", CubeListBuilder.create()
                        .texOffs(8, 124).addBox(2.0f, 15.5f, 1.0f, 1.0f, 3.0f, 1.0f),
                PartPose.offset(4.0f, -4.0f, -2.0f));
        raiz.addOrReplaceChild("LFinger2", CubeListBuilder.create()
                        .texOffs(0, 124).addBox(2.5f, 15.5f, -1.5f, 1.0f, 3.0f, 1.0f),
                PartPose.offset(4.0f, -4.0f, -2.0f));
        raiz.addOrReplaceChild("LFinger3", CubeListBuilder.create()
                        .texOffs(0, 119).addBox(3.8f, 15.5f, -1.5f, 1.0f, 4.0f, 1.0f),
                PartPose.offset(4.0f, -4.0f, -2.0f));
        raiz.addOrReplaceChild("LFinger4", CubeListBuilder.create()
                        .texOffs(4, 119).addBox(5.0f, 15.5f, -0.5f, 1.0f, 4.0f, 1.0f),
                PartPose.offset(4.0f, -4.0f, -2.0f));
        raiz.addOrReplaceChild("LFinger5", CubeListBuilder.create()
                        .texOffs(4, 124).addBox(5.0f, 15.5f, 1.0f, 1.0f, 3.0f, 1.0f),
                PartPose.offset(4.0f, -4.0f, -2.0f));
        return LayerDefinition.create(malha, 64, 128);
    }

    /**
     * O passo dele, que é o do original na postura de pé.
     *
     * <p>Os braços andam <b>ao contrário</b> das pernas — o esquerdo com a perna direita — e cada braço leva
     * consigo os três pedaços, a mão e os cinco dedos. As pernas levam a coxa, o joelho e o pé, cada um com
     * a sua dobra fixa por cima: é dessa soma que sai o joelho dobrado para trás de um bicho, e não uma
     * perna de gente.
     *
     * <p>E os ombros balançam cinco centésimos ao compasso do relógio do mundo, parado ou não. É o que o faz
     * respirar.
     */
    @Override
    public void setupAnim(LivingEntityRenderState estado) {
        super.setupAnim(estado);

        float passo = estado.walkAnimationPos;
        float força = estado.walkAnimationSpeed;
        float grau = (float) (180.0 / Math.PI);

        float pernaD = Mth.cos(passo * 0.6662f + (float) Math.PI) * 0.8f * força;
        float pernaE = Mth.cos(passo * 0.6662f) * 0.8f * força;

        // a cabeça olha, e tudo o que é dela olha com ela
        this.p("Head").yRot = estado.yRot * ((float) Math.PI / 180.0f);
        this.p("Head").xRot = estado.xRot * ((float) Math.PI / 180.0f);
        this.p("Head").y = -8.0f;
        this.p("Head").z = -6.0f;

        this.p("Neck").xRot = PESCOÇO / grau;
        this.p("Neck").y = -5.0f;
        this.p("Neck").z = -2.0f;
        this.p("Neck2").y = -1.0f;
        this.p("Neck2").z = -6.0f;
        this.p("Chest").y = -6.0f;
        this.p("Chest").z = -2.5f;
        this.p("Chest").xRot = PEITO / grau;
        this.p("Abdomen").xRot = BARRIGA / grau;
        this.p("LLegA").z = 3.0f;
        this.p("LArmA").y = -4.0f;
        this.p("LArmA").z = -2.0f;
        this.p("TailA").y = 9.5f;
        this.p("TailA").z = 6.0f;

        for (String qual : DA_CABEÇA) {
            this.p(qual).y = this.p("Head").y;
            this.p(qual).z = this.p("Head").z;
            this.p(qual).yRot = this.p("Head").yRot;
            this.p(qual).xRot = this.p("Head").xRot;
        }
        this.p("TeethL").xRot = this.p("Head").xRot + A_BOCA;
        this.p("Mouth").xRot = this.p("Head").xRot + A_BOCA;
        this.p("Nose").xRot = O_NARIZ + this.p("Head").xRot;
        this.p("SideburnL").xRot = A_SUÍÇA + this.p("Head").xRot;
        this.p("SideburnL").yRot = A_SUÍÇA_ABERTA + this.p("Head").yRot;
        this.p("SideburnR").xRot = A_SUÍÇA + this.p("Head").xRot;
        this.p("SideburnR").yRot = -A_SUÍÇA_ABERTA + this.p("Head").yRot;

        for (String qual : DOS_BRAÇOS) {
            this.p(qual).y = this.p("LArmA").y;
            this.p(qual).z = this.p("LArmA").z;
        }
        for (String qual : DAS_PERNAS) this.p(qual).z = this.p("LLegA").z;
        for (String qual : DO_RABO) {
            this.p(qual).y = this.p("TailA").y;
            this.p(qual).z = this.p("TailA").z;
        }

        // as pernas, com as três dobras do bicho por cima do passo
        this.p("RLegA").xRot = PERNA_A + pernaD;
        this.p("RLegB").xRot = PERNA_B + pernaD;
        this.p("RLegC").xRot = PERNA_C + pernaD;
        this.p("RFoot").xRot = pernaD;
        this.p("LLegA").xRot = PERNA_A + pernaE;
        this.p("LLegB").xRot = PERNA_B + pernaE;
        this.p("LLegC").xRot = PERNA_C + pernaE;
        this.p("LFoot").xRot = pernaE;

        // e os braços, ao contrário das pernas, com o balanço dos ombros
        float balanço = Mth.cos(estado.ageInTicks * BALANÇO) * QUANTO;
        this.p("RArmA").zRot = -balanço + QUANTO;
        this.p("LArmA").zRot = balanço - QUANTO;
        this.p("RArmA").xRot = pernaE;
        this.p("LArmA").xRot = pernaD;

        this.p("RArmB").zRot = BRAÇO_B + this.p("RArmA").zRot;
        this.p("LArmB").zRot = -BRAÇO_B + this.p("LArmA").zRot;
        this.p("RArmB").xRot = (float) (Math.PI / 12) + this.p("RArmA").xRot;
        this.p("LArmB").xRot = (float) (Math.PI / 12) + this.p("LArmA").xRot;
        this.p("RArmC").zRot = this.p("RArmA").zRot;
        this.p("LArmC").zRot = this.p("LArmA").zRot;
        this.p("RArmC").xRot = BRAÇO_C + this.p("RArmA").xRot;
        this.p("LArmC").xRot = BRAÇO_C + this.p("LArmA").xRot;
        this.p("RHand").zRot = this.p("RArmA").zRot;
        this.p("LHand").zRot = this.p("LArmA").zRot;
        this.p("RHand").xRot = this.p("RArmA").xRot;
        this.p("LHand").xRot = this.p("LArmA").xRot;

        for (String dedo : DEDOS_D) {
            this.p(dedo).xRot = this.p("RArmA").xRot;
            this.p(dedo).zRot = this.p("RArmA").zRot;
        }
        for (String dedo : DEDOS_E) {
            this.p(dedo).xRot = this.p("LArmA").xRot;
            this.p(dedo).zRot = this.p("LArmA").zRot;
        }
    }
}
