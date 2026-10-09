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
 * O <b>Treefyd</b>, caixa por caixa: o {@code ModelTreefyd} do Witchery.
 *
 * <p>É uma <b>flor carnívora com pernas</b>, e o desenho diz isso melhor do que qualquer texto: um talo fino
 * de dois por dois, uma <b>cabeça</b> cúbica com uma coroa de <b>pétalas</b> chata por trás e uma
 * <b>língua</b> que sai dela para a frente, duas chapas de <b>folha</b> cruzadas em x no meio do talo, uma
 * <b>base</b> quadrada e <b>quatro pernas</b> cúbicas.
 *
 * <p>As duas chapas de folha estão <b>as duas a quarenta e cinco graus</b>, e é isso que as faz cruzar: uma
 * tem a largura em x e a outra em z, de modo que rodadas pelo mesmo ângulo ficam perpendiculares. É o truque
 * mais barato de fazer uma folhagem com duas faces, e é o que o original usa.
 *
 * <p>As pernas andam em compasso oposto, duas a duas, e a <b>cabeça segue o olhar</b>. Mais nada se mexe: o
 * resto dele é um pau.
 */
public class TreefydModel extends EntityModel<LivingEntityRenderState> {
    /** O compasso das pernas e o quanto elas abrem, que são os do original. */
    public static final float COMPASSO = 0.6662f;
    public static final float ABERTURA = 1.4f;

    private final ModelPart cabeça;
    private final ModelPart perna1;
    private final ModelPart perna2;
    private final ModelPart perna3;
    private final ModelPart perna4;

    public TreefydModel(ModelPart raiz) {
        super(raiz);
        this.cabeça = raiz.getChild("head");
        this.perna1 = raiz.getChild("leg1");
        this.perna2 = raiz.getChild("leg2");
        this.perna3 = raiz.getChild("leg3");
        this.perna4 = raiz.getChild("leg4");
    }

    /** A folha de textura é a do original: sessenta e quatro por trinta e dois. */
    public static LayerDefinition treefyd() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        // a cabeça: o cubo da cara, a coroa de pétalas por trás e a língua à frente
        raiz.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-2.0f, -4.0f, -2.0f, 4.0f, 4.0f, 4.0f)
                        .texOffs(0, 0).addBox(-5.0f, -7.0f, 0.0f, 10.0f, 10.0f, 0.0f)
                        .texOffs(25, 18).addBox(0.0f, -3.0f, -6.0f, 0.0f, 10.0f, 4.0f),
                PartPose.offset(0.0f, 3.0f, 0.0f));

        // o talo
        raiz.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(16, 14).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 16.0f, 2.0f),
                PartPose.offset(0.0f, 3.0f, 0.0f));

        // as duas chapas de folha, cruzadas
        raiz.addOrReplaceChild("leavesV", CubeListBuilder.create()
                        .texOffs(40, 6).addBox(0.0f, 0.0f, -6.0f, 0.0f, 14.0f, 12.0f),
                PartPose.offsetAndRotation(0.0f, 6.0f, 0.0f, 0.0f, Mth.PI / 4.0f, 0.0f));
        raiz.addOrReplaceChild("leavesH", CubeListBuilder.create()
                        .texOffs(40, 0).addBox(-6.0f, 0.0f, 0.0f, 12.0f, 14.0f, 0.0f),
                PartPose.offsetAndRotation(0.0f, 6.0f, 0.0f, 0.0f, Mth.PI / 4.0f, 0.0f));

        // a base e as quatro pernas
        raiz.addOrReplaceChild("base", CubeListBuilder.create()
                        .texOffs(15, 6).addBox(0.0f, 0.0f, 0.0f, 6.0f, 1.0f, 6.0f),
                PartPose.offset(-3.0f, 19.0f, -3.0f));
        perna(raiz, "leg3", -2.0f, -4.0f);
        perna(raiz, "leg4", 2.0f, -4.0f);
        perna(raiz, "leg1", -2.0f, 4.0f);
        perna(raiz, "leg2", 2.0f, 4.0f);

        return LayerDefinition.create(malha, 64, 32);
    }

    private static void perna(PartDefinition raiz, String nome, float x, float z) {
        raiz.addOrReplaceChild(nome, CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 4.0f, 4.0f),
                PartPose.offset(x, 20.0f, z));
    }

    @Override
    public void setupAnim(LivingEntityRenderState estado) {
        super.setupAnim(estado);
        this.cabeça.yRot = estado.yRot * Mth.DEG_TO_RAD;
        this.cabeça.xRot = estado.xRot * Mth.DEG_TO_RAD;

        float passo = estado.walkAnimationPos * COMPASSO;
        float quanto = estado.walkAnimationSpeed;
        this.perna1.xRot = Mth.cos(passo) * ABERTURA * quanto;
        this.perna4.xRot = this.perna1.xRot;
        this.perna2.xRot = Mth.cos(passo + Mth.PI) * ABERTURA * quanto;
        this.perna3.xRot = this.perna2.xRot;
    }
}
