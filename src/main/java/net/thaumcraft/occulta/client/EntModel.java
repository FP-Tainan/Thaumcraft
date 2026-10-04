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

/**
 * O <b>Ent</b>, caixa por caixa: o {@code MoCModelEnt} do Mo'Creatures.
 *
 * <p><b>Esta é uma troca de gosto, e está declarada como tal.</b> O Ent do Witchery é um tronco de dezesseis
 * caixas — um paralelepípedo com quatro borrões de folha por cima, duas tábuas por braços e oito palitos por
 * raízes. Funciona, mas não é uma árvore que anda: é uma caixa com cara.
 *
 * <p>O do Mo'Creatures são <b>quarenta e quatro peças</b> e é outra coisa:
 *
 * <ul>
 *   <li>braços em <b>cinco pedaços</b> cada — ombro, braço, pulso, mão e <b>dedos</b> —, o que lhes dá um
 *       cotovelo e um punho que se veem dobrar;</li>
 *   <li>pernas em cinco também — perna, coxa, joelho, tornozelo e um <b>pé</b> que se inclina quinze graus
 *       para a frente, como uma raiz que pisa;</li>
 *   <li>uma <b>cara</b> de verdade: pescoço inclinado, rosto, testa, <b>nariz</b> e uma boca de um pixel de
 *       grosso;</li>
 *   <li>e uma <b>copa</b> de dezesseis blocos de folha arrumados em dois andares à volta de um tronco, em
 *       vez de dois cubos grandes.</li>
 * </ul>
 *
 * <p>E ela <b>anda</b>: braços e pernas em compasso oposto, os pulsos com um balanço lento tirado do relógio
 * do mundo, e a <b>copa inteira vira com a cabeça</b> — dezesseis blocos de folha a rodar quando ele olha
 * para o lado. É isso que o faz parecer uma árvore viva em vez de um bloco com pernas.
 *
 * <p>A folha de textura é a do Mo'Creatures, de cento e vinte e oito por duzentos e cinquenta e seis, porque
 * um corpo novo pede o desenho que foi feito para ele.
 */
public class EntModel extends EntityModel<LivingEntityRenderState> {
    /** As quarenta e quatro peças, pela ordem em que o original as desenha. */
    public static final java.util.List<String> PEÇAS = java.util.List.of(
            "Body", "LShoulder", "LArm", "LWrist", "LHand", "LFingers",
            "RShoulder", "RArm", "RWrist", "RHand", "RFingers",
            "LLeg", "LThigh", "LKnee", "LAnkle", "LFoot",
            "RLeg", "RThigh", "RKnee", "RAnkle", "RFoot",
            "Neck", "Face", "Head", "Nose", "Mouth", "TreeBase",
            "Leave1", "Leave2", "Leave3", "Leave4", "Leave5", "Leave6", "Leave7", "Leave8",
            "Leave9", "Leave10", "Leave11", "Leave12", "Leave13", "Leave14", "Leave15", "Leave16");

    /** O balanço dos pulsos, que vem do relógio do mundo e não do passo. */
    public static final float BALANÇO = 0.09f;
    public static final float QUANTO = 0.05f;

    /** O que os braços e as pernas abrem a andar, e a inclinação fixa do pé. */
    public static final float BRAÇOS = 1.0f;
    public static final float PERNAS = 1.0f;
    public static final float ABERTURA = 10.0f;
    public static final float O_PÉ = 15.0f;

    private final java.util.Map<String, ModelPart> partes = new java.util.HashMap<>();

    public EntModel(ModelPart raiz) {
        super(raiz);
        for (String nome : PEÇAS) this.partes.put(nome, raiz.getChild(nome));
    }

    private ModelPart p(String nome) {
        return this.partes.get(nome);
    }

    public static LayerDefinition ent() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("Body", CubeListBuilder.create()
                        .texOffs(68, 36).addBox(-7.5f, -12.5f, -4.5f, 15.0f, 25.0f, 9.0f),
                PartPose.offset(0.0f, -31.0f, 0.0f));
        raiz.addOrReplaceChild("LShoulder", CubeListBuilder.create()
                        .texOffs(48, 108).addBox(6.0f, -14.0f, -4.8f, 9.0f, 7.0f, 7.0f),
                PartPose.offsetAndRotation(0.0f, -31.0f, 0.0f, 0.0f, 0.0f, -0.17453f));
        raiz.addOrReplaceChild("LArm", CubeListBuilder.create()
                        .texOffs(80, 108).addBox(0.0f, -4.0f, -5.0f, 6.0f, 24.0f, 6.0f),
                PartPose.offsetAndRotation(10.0f, -42.0f, 1.0f, 0.0f, 0.0f, -0.17453f));
        raiz.addOrReplaceChild("LWrist", CubeListBuilder.create()
                        .texOffs(0, 169).addBox(2.0f, 17.0f, -6.0f, 8.0f, 15.0f, 8.0f),
                PartPose.offset(10.0f, -42.0f, 1.0f));
        raiz.addOrReplaceChild("LHand", CubeListBuilder.create()
                        .texOffs(88, 241).addBox(1.0f, 28.0f, -7.0f, 10.0f, 5.0f, 10.0f),
                PartPose.offset(10.0f, -42.0f, 1.0f));
        raiz.addOrReplaceChild("LFingers", CubeListBuilder.create()
                        .texOffs(88, 176).addBox(1.0f, 33.0f, -7.0f, 10.0f, 15.0f, 10.0f),
                PartPose.offset(10.0f, -42.0f, 1.0f));
        raiz.addOrReplaceChild("RShoulder", CubeListBuilder.create()
                        .texOffs(48, 122).addBox(-15.0f, -14.0f, -4.8f, 9.0f, 7.0f, 7.0f),
                PartPose.offsetAndRotation(0.0f, -31.0f, 0.0f, 0.0f, 0.0f, 0.17453f));
        raiz.addOrReplaceChild("RArm", CubeListBuilder.create()
                        .texOffs(104, 108).addBox(-6.0f, -4.0f, -5.0f, 6.0f, 24.0f, 6.0f),
                PartPose.offsetAndRotation(-10.0f, -42.0f, 1.0f, 0.0f, 0.0f, 0.17453f));
        raiz.addOrReplaceChild("RWrist", CubeListBuilder.create()
                        .texOffs(32, 169).addBox(-10.0f, 17.0f, -6.0f, 8.0f, 15.0f, 8.0f),
                PartPose.offset(-10.0f, -42.0f, 1.0f));
        raiz.addOrReplaceChild("RHand", CubeListBuilder.create()
                        .texOffs(88, 226).addBox(-11.0f, 28.0f, -7.0f, 10.0f, 5.0f, 10.0f),
                PartPose.offset(-10.0f, -42.0f, 1.0f));
        raiz.addOrReplaceChild("RFingers", CubeListBuilder.create()
                        .texOffs(88, 201).addBox(-11.0f, 33.0f, -7.0f, 10.0f, 15.0f, 10.0f),
                PartPose.offset(-10.0f, -42.0f, 1.0f));
        raiz.addOrReplaceChild("LLeg", CubeListBuilder.create()
                        .texOffs(0, 90).addBox(3.0f, 0.0f, -3.0f, 6.0f, 20.0f, 6.0f),
                PartPose.offset(0.0f, -21.0f, 0.0f));
        raiz.addOrReplaceChild("LThigh", CubeListBuilder.create()
                        .texOffs(24, 64).addBox(2.5f, 4.0f, -3.5f, 7.0f, 12.0f, 7.0f),
                PartPose.offset(0.0f, -21.0f, 0.0f));
        raiz.addOrReplaceChild("LKnee", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(2.0f, 20.0f, -4.0f, 8.0f, 24.0f, 8.0f),
                PartPose.offset(0.0f, -21.0f, 0.0f));
        raiz.addOrReplaceChild("LAnkle", CubeListBuilder.create()
                        .texOffs(32, 29).addBox(1.5f, 25.0f, -4.5f, 9.0f, 20.0f, 9.0f),
                PartPose.offset(0.0f, -21.0f, 0.0f));
        raiz.addOrReplaceChild("LFoot", CubeListBuilder.create()
                        .texOffs(0, 206).addBox(1.5f, 38.0f, -23.5f, 9.0f, 5.0f, 9.0f),
                PartPose.offsetAndRotation(0.0f, -21.0f, 0.0f, 0.2618f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("RLeg", CubeListBuilder.create()
                        .texOffs(0, 64).addBox(-9.0f, 0.0f, -3.0f, 6.0f, 20.0f, 6.0f),
                PartPose.offset(0.0f, -21.0f, 0.0f));
        raiz.addOrReplaceChild("RThigh", CubeListBuilder.create()
                        .texOffs(24, 83).addBox(-9.5f, 4.0f, -3.5f, 7.0f, 12.0f, 7.0f),
                PartPose.offset(0.0f, -21.0f, 0.0f));
        raiz.addOrReplaceChild("RKnee", CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-10.0f, 20.0f, -4.0f, 8.0f, 24.0f, 8.0f),
                PartPose.offset(0.0f, -21.0f, 0.0f));
        raiz.addOrReplaceChild("RAnkle", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-10.5f, 25.0f, -4.5f, 9.0f, 20.0f, 9.0f),
                PartPose.offset(0.0f, -21.0f, 0.0f));
        raiz.addOrReplaceChild("RFoot", CubeListBuilder.create()
                        .texOffs(0, 192).addBox(-10.5f, 38.0f, -23.5f, 9.0f, 5.0f, 9.0f),
                PartPose.offsetAndRotation(0.0f, -21.0f, 0.0f, 0.2618f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("Neck", CubeListBuilder.create()
                        .texOffs(52, 90).addBox(-4.0f, -8.0f, -5.8f, 8.0f, 10.0f, 8.0f),
                PartPose.offsetAndRotation(0.0f, -44.0f, 0.0f, 0.5236f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("Face", CubeListBuilder.create()
                        .texOffs(52, 70).addBox(-4.5f, -11.0f, -9.0f, 9.0f, 7.0f, 8.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Head", CubeListBuilder.create()
                        .texOffs(84, 88).addBox(-6.0f, -20.5f, -9.5f, 12.0f, 10.0f, 10.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Nose", CubeListBuilder.create()
                        .texOffs(82, 88).addBox(-1.5f, -12.0f, -12.0f, 3.0f, 7.0f, 3.0f),
                PartPose.offsetAndRotation(0.0f, -44.0f, 0.0f, -0.12217f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("Mouth", CubeListBuilder.create()
                        .texOffs(77, 36).addBox(-3.0f, -8.0f, -6.8f, 6.0f, 2.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, -44.0f, 0.0f, 0.5236f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("TreeBase", CubeListBuilder.create()
                        .texOffs(0, 136).addBox(-10.0f, -31.5f, -11.5f, 20.0f, 13.0f, 20.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Leave1", CubeListBuilder.create()
                        .texOffs(0, 224).addBox(-16.0f, -45.0f, -17.0f, 16.0f, 16.0f, 16.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Leave2", CubeListBuilder.create()
                        .texOffs(0, 224).addBox(0.0f, -45.0f, -17.0f, 16.0f, 16.0f, 16.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Leave3", CubeListBuilder.create()
                        .texOffs(0, 224).addBox(0.0f, -45.0f, -1.0f, 16.0f, 16.0f, 16.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Leave4", CubeListBuilder.create()
                        .texOffs(0, 224).addBox(-16.0f, -45.0f, -1.0f, 16.0f, 16.0f, 16.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Leave5", CubeListBuilder.create()
                        .texOffs(0, 224).addBox(-16.0f, -45.0f, -33.0f, 16.0f, 16.0f, 16.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Leave6", CubeListBuilder.create()
                        .texOffs(0, 224).addBox(0.0f, -45.0f, -33.0f, 16.0f, 16.0f, 16.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Leave7", CubeListBuilder.create()
                        .texOffs(0, 224).addBox(16.0f, -45.0f, -17.0f, 16.0f, 16.0f, 16.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Leave8", CubeListBuilder.create()
                        .texOffs(0, 224).addBox(16.0f, -45.0f, -1.0f, 16.0f, 16.0f, 16.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Leave9", CubeListBuilder.create()
                        .texOffs(0, 224).addBox(0.0f, -45.0f, 15.0f, 16.0f, 16.0f, 16.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Leave10", CubeListBuilder.create()
                        .texOffs(0, 224).addBox(-16.0f, -45.0f, 15.0f, 16.0f, 16.0f, 16.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Leave11", CubeListBuilder.create()
                        .texOffs(0, 224).addBox(-32.0f, -45.0f, -1.0f, 16.0f, 16.0f, 16.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Leave12", CubeListBuilder.create()
                        .texOffs(0, 224).addBox(-32.0f, -45.0f, -17.0f, 16.0f, 16.0f, 16.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Leave13", CubeListBuilder.create()
                        .texOffs(0, 224).addBox(-16.0f, -61.0f, -17.0f, 16.0f, 16.0f, 16.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Leave14", CubeListBuilder.create()
                        .texOffs(0, 224).addBox(0.0f, -61.0f, -17.0f, 16.0f, 16.0f, 16.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Leave15", CubeListBuilder.create()
                        .texOffs(0, 224).addBox(0.0f, -61.0f, -1.0f, 16.0f, 16.0f, 16.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        raiz.addOrReplaceChild("Leave16", CubeListBuilder.create()
                        .texOffs(0, 224).addBox(-16.0f, -61.0f, -1.0f, 16.0f, 16.0f, 16.0f),
                PartPose.offset(0.0f, -44.0f, 0.0f));
        return LayerDefinition.create(malha, 128, 256);
    }

    /**
     * O passo dele, que é o do original.
     *
     * <p>Os braços andam ao contrário das pernas, e cada braço leva o pulso, a mão e os dedos consigo — eles
     * não dobram sozinhos, dobram <b>com</b> o braço. O que dobra sozinho é o pulso, com um balanço de cinco
     * centésimos tirado do relógio do mundo: é o que faz as mãos dele parecerem pesadas.
     *
     * <p>E a <b>copa inteira</b> — tronco e dezesseis folhas — vira com o pescoço. Nenhuma outra árvore deste
     * porte faz isso.
     */
    @Override
    public void setupAnim(LivingEntityRenderState estado) {
        super.setupAnim(estado);

        float passo = estado.walkAnimationPos;
        float força = estado.walkAnimationSpeed;
        float relógio = estado.ageInTicks;
        float grau = (float) (180.0 / Math.PI);

        float direito = Mth.cos(passo * 0.6662f + (float) Math.PI) * 2.0f * força * 0.5f;
        float esquerdo = Mth.cos(passo * 0.6662f) * 2.0f * força * 0.5f;
        float pernaD = Mth.cos(passo * 0.6662f) * PERNAS * força;
        float pernaE = Mth.cos(passo * 0.6662f + (float) Math.PI) * PERNAS * força;

        float balanço = Mth.cos(relógio * BALANÇO) * QUANTO;

        this.p("LWrist").zRot = balanço - QUANTO;
        this.p("LWrist").xRot = esquerdo;
        this.p("RWrist").zRot = -balanço + QUANTO;
        this.p("RWrist").xRot = direito;

        for (String qual : new String[]{"LHand", "LFingers", "LArm"}) {
            this.p(qual).xRot = this.p("LWrist").xRot;
        }
        this.p("LHand").zRot = this.p("LWrist").zRot;
        this.p("LFingers").zRot = this.p("LWrist").zRot;
        this.p("LArm").zRot = -ABERTURA / grau + this.p("LWrist").zRot;

        for (String qual : new String[]{"RHand", "RFingers", "RArm"}) {
            this.p(qual).xRot = this.p("RWrist").xRot;
        }
        this.p("RHand").zRot = this.p("RWrist").zRot;
        this.p("RFingers").zRot = this.p("RWrist").zRot;
        this.p("RArm").zRot = ABERTURA / grau + this.p("RWrist").zRot;

        this.p("RLeg").xRot = pernaD;
        this.p("LLeg").xRot = pernaE;
        for (String qual : new String[]{"LThigh", "LKnee", "LAnkle"}) {
            this.p(qual).xRot = this.p("LLeg").xRot;
        }
        for (String qual : new String[]{"RThigh", "RKnee", "RAnkle"}) {
            this.p(qual).xRot = this.p("RLeg").xRot;
        }
        this.p("LFoot").xRot = O_PÉ / grau + this.p("LLeg").xRot;
        this.p("RFoot").xRot = O_PÉ / grau + this.p("RLeg").xRot;

        // e a copa inteira vira com a cabeça
        float olhar = estado.yRot * ((float) Math.PI / 180.0f);
        this.p("Neck").yRot = olhar;
        for (String qual : PEÇAS) {
            if (qual.startsWith("Leave") || qual.equals("TreeBase") || qual.equals("Head")
                    || qual.equals("Face") || qual.equals("Nose") || qual.equals("Mouth")) {
                this.p(qual).yRot = olhar;
            }
        }
    }
}
