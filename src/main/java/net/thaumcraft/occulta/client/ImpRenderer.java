package net.thaumcraft.occulta.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.thaumcraft.Thaumcraft;
import net.thaumcraft.occulta.imp.ImpEntity;

/**
 * O <b>Diabrete</b> no mundo: o {@code ModelImp} e o {@code RenderImp} do Witchery.
 *
 * <p>Uma cabeça grande com dois <b>chifres</b> tortos e um <b>nariz</b> comprido virado para baixo, um
 * corpo baixo, braços compridos que quase tocam o chão e duas pernas curtas. E, atrás, duas <b>asas</b>
 * chapadas que ele nunca usa para voar.
 *
 * <p><b>Ligado, ele engorda.</b> O original estica o boneco uma vez e meia em X e Z — e não em Y. Um
 * Diabrete com poder não fica mais alto: fica mais largo, e é uma piada melhor do que seria a outra.
 */
public final class ImpRenderer {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Thaumcraft.id("imp"), "main");

    public static final Identifier FOLHA = Thaumcraft.id("textures/entity/imp.png");

    /** Quanto ele engorda com o Coração de Demônio: uma vez e meia, de lado. */
    public static final float ENGORDA = 1.5f;

    /** O balanço de braços que todos os bichos deste mod têm. */
    private static final float BALANÇO = 0.067f;
    private static final float ABRE = 0.09f;
    private static final float QUANTO = 0.05f;

    private ImpRenderer() {
    }

    /** O que o desenhista precisa de saber dele. */
    public static class Estado extends LivingEntityRenderState {
        public boolean ligado;
    }

    /**
     * A malha, caixa por caixa.
     *
     * <p><b>Diferença.</b> As duas asas declaram, no original, uma folha de <b>cento e vinte e oito por
     * trinta e dois</b> enquanto todo o resto do boneco usa sessenta e quatro por sessenta e quatro — e
     * em 2014 isso valia peça a peça. Mas a conta não fecha: com trinta e dois de altura, uma asa de
     * vinte e um a partir de vinte e um <b>passa de baixo da folha</b>. O desenho está feito para o
     * sessenta e quatro, e a linha do tamanho é que está errada.
     *
     * <p>Por isso elas vão aqui onde o desenho está: <b>(23, 21)</b> na folha de sessenta e quatro.
     */
    public static LayerDefinition criaCamada() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        PartDefinition cabeça = raiz.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0f, -8.0f, -4.0f, 10.0f, 8.0f, 10.0f),
                PartPose.offset(0.0f, 8.0f, 0.0f));
        cabeça.addOrReplaceChild("horn_left", CubeListBuilder.create()
                        .texOffs(0, 21).addBox(-1.0f, -5.0f, -1.0f, 2.0f, 5.0f, 2.0f),
                PartPose.offsetAndRotation(4.0f, -5.0f, 0.0f, 0.4089647f, 0.0f, 0.7435722f));
        cabeça.addOrReplaceChild("horn_right", CubeListBuilder.create()
                        .texOffs(0, 21).addBox(-1.0f, -5.0f, -1.0f, 2.0f, 5.0f, 2.0f),
                PartPose.offsetAndRotation(-4.0f, -5.0f, 0.0f, 0.4089647f, 0.0f, -0.7435722f));
        cabeça.addOrReplaceChild("nose", CubeListBuilder.create()
                        .texOffs(9, 21).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 4.0f, 2.0f),
                PartPose.offsetAndRotation(0.0f, -4.0f, -3.0f, -0.9666439f, 0.0f, 0.0f));

        raiz.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-4.0f, 0.0f, -4.0f, 8.0f, 9.0f, 7.0f),
                PartPose.offset(0.0f, 9.0f, 0.0f));
        raiz.addOrReplaceChild("chest", CubeListBuilder.create()
                        .texOffs(4, 41).addBox(-4.0f, 0.0f, -2.0f, 6.0f, 2.0f, 4.0f),
                PartPose.offset(1.0f, 8.0f, 0.0f));

        raiz.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(41, 0).addBox(-2.0f, -2.0f, -1.5f, 3.0f, 13.0f, 3.0f),
                PartPose.offset(-5.0f, 11.0f, 0.0f));
        raiz.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(41, 0).addBox(-1.0f, -2.0f, -1.5f, 3.0f, 13.0f, 3.0f),
                PartPose.offset(5.0f, 11.0f, 0.0f));

        /*
         * As pernas, que o original monta em y 18 e repõe em y 12 e z 0,1 a cada quadro. Valem as
         * segundas, como sempre.
         */
        raiz.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(33, 48).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 6.0f, 3.0f),
                PartPose.offset(-1.5f, 12.0f, 0.1f));
        raiz.addOrReplaceChild("left_leg", CubeListBuilder.create()
                        .texOffs(33, 48).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 6.0f, 3.0f),
                PartPose.offset(1.5f, 12.0f, 0.1f));

        raiz.addOrReplaceChild("wing_right", CubeListBuilder.create()
                        .texOffs(23, 21).addBox(0.0f, 0.0f, 0.0f, 14.0f, 21.0f, 0.0f),
                PartPose.offsetAndRotation(-2.0f, 10.0f, -1.0f,
                        0.3047198f, -0.6698132f, -0.6283185f));
        raiz.addOrReplaceChild("wing_left", CubeListBuilder.create()
                        .texOffs(23, 21).addBox(0.0f, 0.0f, 0.0f, 14.0f, 21.0f, 0.0f),
                PartPose.offsetAndRotation(2.0f, 10.0f, -1.0f,
                        -0.3047198f, 3.811406f, 0.6283185f));

        return LayerDefinition.create(malha, 64, 64);
    }

    /** O boneco: o andar de sempre, e o balanço de braços deste mod. */
    public static class Modelo extends EntityModel<Estado> {
        private final ModelPart cabeça;
        private final ModelPart braçoDireito;
        private final ModelPart braçoEsquerdo;
        private final ModelPart pernaDireita;
        private final ModelPart pernaEsquerda;

        public Modelo(ModelPart raiz) {
            super(raiz);
            this.cabeça = raiz.getChild("head");
            this.braçoDireito = raiz.getChild("right_arm");
            this.braçoEsquerdo = raiz.getChild("left_arm");
            this.pernaDireita = raiz.getChild("right_leg");
            this.pernaEsquerda = raiz.getChild("left_leg");
        }

        @Override
        public void setupAnim(Estado estado) {
            super.setupAnim(estado);
            this.cabeça.yRot = estado.yRot * ((float) Math.PI / 180.0f);
            this.cabeça.xRot = estado.xRot * ((float) Math.PI / 180.0f);

            float passo = estado.walkAnimationPos;
            float quanto = estado.walkAnimationSpeed;
            this.braçoDireito.xRot = Mth.cos(passo * 0.6662f + (float) Math.PI) * 2.0f * quanto * 0.5f;
            this.braçoEsquerdo.xRot = Mth.cos(passo * 0.6662f) * 2.0f * quanto * 0.5f;
            this.pernaDireita.xRot = Mth.cos(passo * 0.6662f) * 1.4f * quanto;
            this.pernaEsquerda.xRot = Mth.cos(passo * 0.6662f + (float) Math.PI) * 1.4f * quanto;

            float abre = Mth.cos(estado.ageInTicks * ABRE) * QUANTO + QUANTO;
            float balanço = Mth.sin(estado.ageInTicks * BALANÇO) * QUANTO;
            this.braçoDireito.zRot = abre;
            this.braçoEsquerdo.zRot = -abre;
            this.braçoDireito.xRot += balanço;
            this.braçoEsquerdo.xRot -= balanço;
        }
    }

    /** E o desenhista, que o engorda quando ele está ligado. */
    public static class Diabrete extends MobRenderer<ImpEntity, Estado, EntityModel<Estado>> {
        public Diabrete(EntityRendererProvider.Context contexto) {
            super(contexto, new Modelo(contexto.bakeLayer(LAYER)), 0.4f);
        }

        @Override
        public Estado createRenderState() {
            return new Estado();
        }

        @Override
        public void extractRenderState(ImpEntity bicho, Estado estado, float parcial) {
            super.extractRenderState(bicho, estado, parcial);
            estado.ligado = bicho.ligado();
        }

        @Override
        public Identifier getTextureLocation(Estado estado) {
            return FOLHA;
        }

        @Override
        protected void scale(Estado estado, PoseStack pose) {
            super.scale(estado, pose);
            if (estado.ligado) pose.scale(ENGORDA, 1.0f, ENGORDA);
        }
    }
}
