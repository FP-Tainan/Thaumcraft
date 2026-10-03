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
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;

/**
 * Lilith desenhada: o {@code ModelLilith} do Witchery, caixa por caixa.
 *
 * <p>Ela é uma figura alta e estreita — um metro e meio de cintura para cima num corpo de dois e meio — com
 * <b>chifres</b> virados para trás, dois <b>dentes</b> que saem da boca, um <b>nariz</b> de um pixel, e duas
 * <b>asas chatas</b> presas aos braços que descem até abaixo dos pés.
 *
 * <p>A <b>saia</b> são duas peças iguais no mesmo lugar, e é o truque mais bonito do modelo: uma segue a
 * perna que está <b>mais atrás</b> e a outra a que está <b>mais à frente</b>, de modo que andando a saia se
 * abre sozinha. Parado, a de trás fica em dois décimos e a saia fecha.
 *
 * <p>E os braços dela nunca param: mesmo imóvel, há um balanço de cinco centésimos no cotovelo e no ombro,
 * tirado do relógio do mundo e não do passo. É o que a faz parecer <b>viva</b> em vez de posta.
 */
public final class LilithModel extends EntityModel<LilithRenderer.State> {
    public static final ModelLayerLocation LILITH = new ModelLayerLocation(Thaumcraft.id("lilith"), "main");

    /** A inclinação de repouso das pernas, e o teto da dobra. */
    public static final float PERNA_PARADA = -0.27314404f;
    public static final float TETO_DA_PERNA = -0.8f;

    /** E o mínimo em que a saia da frente fica, parada. */
    public static final float SAIA_FECHADA = 0.2f;

    /** Quanto dura a pancada no braço dela. */
    public static final float COMPASSO_DA_PANCADA = 10.0f;

    private final ModelPart cabeça;
    private final ModelPart braçoDireito;
    private final ModelPart braçoEsquerdo;
    private final ModelPart pernaDireita;
    private final ModelPart pernaEsquerda;
    private final ModelPart saiaDeTrás;
    private final ModelPart saiaDaFrente;

    public LilithModel(ModelPart raiz) {
        super(raiz);
        this.cabeça = raiz.getChild("head");
        this.braçoDireito = raiz.getChild("arm_right");
        this.braçoEsquerdo = raiz.getChild("arm_left");
        this.pernaDireita = raiz.getChild("leg_right");
        this.pernaEsquerda = raiz.getChild("leg_left");
        this.saiaDeTrás = raiz.getChild("skirt_back");
        this.saiaDaFrente = raiz.getChild("skirt_front");
    }

    /** Sessenta e quatro por sessenta e quatro de textura, como no original. */
    public static LayerDefinition lilith() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        // o pescoço, o peito inclinado, os ombros e a cintura
        raiz.addOrReplaceChild("neck", CubeListBuilder.create()
                        .texOffs(24, 0).addBox(-1.5f, -1.5f, -1.5f, 3.0f, 2.0f, 3.0f),
                PartPose.offset(0.0f, -13.0f, 0.0f));
        raiz.addOrReplaceChild("body_chest", CubeListBuilder.create()
                        .texOffs(17, 17).addBox(-4.0f, -1.5f, -1.5f, 8.0f, 3.0f, 3.0f),
                PartPose.offsetAndRotation(0.0f, -9.8f, -1.9f, Mth.PI / 4.0f, 0.0f, 0.0f));
        raiz.addOrReplaceChild("body_shoulders", CubeListBuilder.create()
                        .texOffs(15, 6).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 6.0f, 4.0f),
                PartPose.offset(0.0f, -12.7f, 0.0f));
        raiz.addOrReplaceChild("body_waist", CubeListBuilder.create()
                        .texOffs(20, 24).addBox(-3.0f, 0.0f, -1.0f, 6.0f, 10.0f, 2.0f),
                PartPose.offset(0.0f, -7.5f, 0.0f));

        // as duas saias, iguais e no mesmo lugar
        raiz.addOrReplaceChild("skirt_back", CubeListBuilder.create()
                        .texOffs(0, 49).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 10.0f, 5.0f),
                PartPose.offset(0.0f, -0.9f, 0.0f));
        raiz.addOrReplaceChild("skirt_front", CubeListBuilder.create()
                        .texOffs(0, 49).addBox(-4.5f, 0.0f, -2.5f, 9.0f, 10.0f, 5.0f),
                PartPose.offset(0.0f, -0.9f, 0.0f));

        // a cabeça, com os chifres, o nariz, os dentes e o focinho de duas peças
        PartDefinition cabeça = raiz.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(40, 0).addBox(-3.0f, -6.0f, -3.0f, 6.0f, 6.0f, 6.0f),
                PartPose.offset(0.0f, -13.5f, 0.0f));
        cabeça.addOrReplaceChild("horn_left", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(52, 30).addBox(1.0f, -12.3f, 0.0f, 6.0f, 10.0f, 0.0f),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.18203785f, 0.0f, 0.0f));
        cabeça.addOrReplaceChild("horn_right", CubeListBuilder.create()
                        .texOffs(52, 30).addBox(-7.0f, -12.3f, 0.0f, 6.0f, 10.0f, 0.0f),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.18203785f, 0.0f, 0.0f));
        cabeça.addOrReplaceChild("nose", CubeListBuilder.create()
                        .texOffs(41, 0).addBox(-0.5f, -3.6f, -4.0f, 1.0f, 2.0f, 1.0f),
                PartPose.ZERO);
        cabeça.addOrReplaceChild("tooth_left", CubeListBuilder.create()
                        .mirror(true)
                        .texOffs(20, 0).addBox(0.5f, -1.6f, -3.6f, 1.0f, 3.0f, 1.0f,
                                new CubeDeformation(-0.35f)),
                PartPose.ZERO);
        cabeça.addOrReplaceChild("tooth_right", CubeListBuilder.create()
                        .texOffs(20, 0).addBox(-1.5f, -1.6f, -3.6f, 1.0f, 3.0f, 1.0f,
                                new CubeDeformation(-0.35f)),
                PartPose.ZERO);
        PartDefinition cabeça2 = cabeça.addOrReplaceChild("head2", CubeListBuilder.create()
                        .texOffs(42, 12).addBox(-2.5f, -5.5f, 1.0f, 5.0f, 5.0f, 5.0f),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.18203785f, 0.0f, 0.0f));
        cabeça2.addOrReplaceChild("head3", CubeListBuilder.create()
                        .texOffs(44, 22).addBox(-2.0f, -4.7f, 5.6f, 4.0f, 4.0f, 4.0f),
                PartPose.ZERO);

        // os braços, com o antebraço e a asa presos a cada um
        braço(raiz, "arm_right", -4.5f, -2.5f, false);
        braço(raiz, "arm_left", 4.4f, -0.5f, true);

        // e as pernas, em duas peças
        perna(raiz, "leg_right", -2.1f, false);
        perna(raiz, "leg_left", 2.1f, true);

        return LayerDefinition.create(malha, 64, 64);
    }

    /** Um braço: braço, antebraço e a asa que desce dele. */
    private static void braço(PartDefinition raiz, String nome, float x, float caixa, boolean espelho) {
        PartDefinition braço = raiz.addOrReplaceChild(nome, CubeListBuilder.create()
                        .mirror(espelho)
                        .texOffs(0, 0).addBox(caixa, -1.5f, -1.5f, 3.0f, 13.0f, 3.0f),
                PartPose.offset(x, -11.5f, 0.0f));
        braço.addOrReplaceChild(nome + "_lower", CubeListBuilder.create()
                        .mirror(espelho)
                        .texOffs(8, 25).addBox(espelho ? -0.5f : -2.5f, 9.8f, 0.8f, 3.0f, 13.0f, 3.0f),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, -0.22759093f, 0.0f, 0.0f));
        braço.addOrReplaceChild(nome + "_wing", CubeListBuilder.create()
                        .mirror(espelho)
                        .texOffs(0, 13).addBox(espelho ? 1.0f : -1.0f, -19.6f, -12.7f,
                                0.0f, 30.0f, 4.0f),
                PartPose.offsetAndRotation(0.0f, 0.0f, 0.0f, 2.5497515f,
                        espelho ? Mth.PI / 18.0f : -Mth.PI / 18.0f, 0.0f));
    }

    /** E uma perna: coxa e canela. */
    private static void perna(PartDefinition raiz, String nome, float x, boolean espelho) {
        PartDefinition coxa = raiz.addOrReplaceChild(nome, CubeListBuilder.create()
                        .mirror(espelho)
                        .texOffs(36, 30).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 13.0f, 4.0f),
                PartPose.offsetAndRotation(x, 2.5f, 0.0f, PERNA_PARADA, 0.0f, 0.0f));
        coxa.addOrReplaceChild(nome + "_lower", CubeListBuilder.create()
                        .mirror(espelho)
                        .texOffs(48, 47).addBox(-2.0f, 8.0f, 2.0f, 4.0f, 13.0f, 4.0f),
                PartPose.ZERO);
    }

    @Override
    public void setupAnim(LilithRenderer.State estado) {
        super.setupAnim(estado);
        float passo = estado.walkAnimationPos;
        float força = estado.walkAnimationSpeed;
        float relógio = estado.ageInTicks;

        this.cabeça.yRot = estado.yRot * Mth.DEG_TO_RAD;
        this.cabeça.xRot = estado.xRot * Mth.DEG_TO_RAD;

        this.braçoDireito.xRot = Mth.cos(passo * 0.6662f + Mth.PI) * 2.0f * força * 0.5f;
        this.braçoEsquerdo.xRot = Mth.cos(passo * 0.6662f) * 2.0f * força * 0.5f;
        this.braçoDireito.yRot = 0.0f;
        this.braçoEsquerdo.yRot = 0.0f;

        this.pernaDireita.xRot = Math.max(
                Mth.cos(passo * 0.6662f) * 1.4f * força + PERNA_PARADA, TETO_DA_PERNA);
        this.pernaEsquerda.xRot = Math.max(
                Mth.cos(passo * 0.6662f + Mth.PI) * 1.4f * força + PERNA_PARADA, TETO_DA_PERNA);
        this.pernaDireita.yRot = 0.0f;
        this.pernaEsquerda.yRot = 0.0f;

        // as duas saias seguem as duas pernas, e é por isso que ela anda bem
        this.saiaDeTrás.xRot = Math.min(this.pernaDireita.xRot, this.pernaEsquerda.xRot);
        this.saiaDaFrente.xRot = Math.max(
                Math.max(this.pernaDireita.xRot, this.pernaEsquerda.xRot), SAIA_FECHADA);

        // e o balanço que ela tem mesmo parada
        this.braçoDireito.zRot = Mth.cos(relógio * 0.09f) * 0.05f + 0.05f;
        this.braçoEsquerdo.zRot = -(Mth.cos(relógio * 0.09f) * 0.05f + 0.05f);
        this.braçoDireito.xRot += Mth.sin(relógio * 0.067f) * 0.05f;
        this.braçoEsquerdo.xRot -= Mth.sin(relógio * 0.067f) * 0.05f;

        if (estado.braço <= 0.0f) return;
        this.braçoDireito.xRot = -2.0f + 1.5f * onda(estado.braço, COMPASSO_DA_PANCADA);
    }

    /** A onda triangular do original, a mesma do Caçador. */
    public static float onda(float onde, float compasso) {
        return (Math.abs(onde % compasso - compasso * 0.5f) - compasso * 0.25f) / (compasso * 0.25f);
    }
}
