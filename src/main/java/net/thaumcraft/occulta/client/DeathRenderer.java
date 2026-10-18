package net.thaumcraft.occulta.client;

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
import net.thaumcraft.occulta.death.DeathEntity;

/**
 * A <b>Morte</b> no mundo: o {@code ModelDeath} e o {@code RenderDeath} do Witchery.
 *
 * <p>É um boneco comum com três coisas por cima: a <b>cabeça</b> é mais alta do que a de gente (dez em vez
 * de oito, que é o crânio), as <b>pernas</b> são <b>finas</b> — dois por dois em vez de quatro —, e há um
 * <b>manto</b> de oito por vinte e três que a cobre inteira, desenhado por último e <b>cinco por cento
 * mais largo</b> para não entrar no corpo.
 *
 * <p>E há a <b>foice</b>, que é a peça que a faz: um cabo de <b>trinta e cinco</b> de comprimento e uma
 * lâmina chata de treze, presos ao <b>braço direito</b> — que anda sempre <b>um quarto de volta para
 * cima</b>, de modo que ela a leva ao ombro como quem leva uma enxada.
 */
public final class DeathRenderer {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Thaumcraft.id("death"), "main");

    public static final Identifier FOLHA = Thaumcraft.id("textures/entity/death.png");

    /** O compasso do andar, e o quanto braços e pernas abrem. */
    public static final float COMPASSO = 0.6662f;
    public static final float BRAÇO_ABRE = 2.0f;
    public static final float PERNA_ABRE = 1.4f;

    /** O balanço de sempre, que o original soma por cima de tudo. */
    public static final float BALANÇO_OMBRO = 0.09f;
    public static final float BALANÇO_BRAÇO = 0.067f;
    public static final float BALANÇO = 0.05f;

    /** E o quanto o manto sobra: os cinco por cento do original. */
    public static final float MANTO_SOBRA = 1.05f;

    private DeathRenderer() {
    }

    /** A malha, caixa por caixa, na folha de sessenta e quatro por sessenta e quatro. */
    public static LayerDefinition criaCamada() {
        MeshDefinition malha = new MeshDefinition();
        PartDefinition raiz = malha.getRoot();

        raiz.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(27, 43).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 10.0f, 8.0f),
                PartPose.ZERO);
        raiz.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f),
                PartPose.ZERO);

        PartDefinition braçoDireito = raiz.addOrReplaceChild("rightarm", CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(-5.0f, 2.0f, 0.0f));
        raiz.addOrReplaceChild("leftarm", CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f),
                PartPose.offset(5.0f, 2.0f, 0.0f));

        // as pernas finas, que é o que faz dela um esqueleto e não uma pessoa
        raiz.addOrReplaceChild("rightleg", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f),
                PartPose.offset(-2.0f, 12.0f, 0.0f));
        raiz.addOrReplaceChild("leftleg", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 12.0f, 2.0f),
                PartPose.offset(2.0f, 12.0f, 0.0f));

        raiz.addOrReplaceChild("robe", CubeListBuilder.create()
                        .texOffs(0, 33).addBox(-4.0f, 0.0f, -2.5f, 8.0f, 23.0f, 5.0f),
                PartPose.ZERO);

        /*
         * A foice, presa ao braço direito e virada um quarto de volta. Os três deslocamentos que o
         * original lhe dá a cada batida estão aqui, porque são sempre os mesmos.
         */
        braçoDireito.addOrReplaceChild("scythe", CubeListBuilder.create()
                        .texOffs(58, 5).addBox(-0.5f, -16.0f, -0.5f, 1.0f, 35.0f, 1.0f)
                        .texOffs(36, 0).addBox(0.0f, -16.0f, 0.0f, 13.0f, 4.0f, 0.0f),
                PartPose.offsetAndRotation(-0.8f, 8.1f, 0.0f, Mth.HALF_PI, 0.0f, 0.0f));

        return LayerDefinition.create(malha, 64, 64);
    }

    /** O boneco. */
    public static class Modelo extends EntityModel<LivingEntityRenderState> {
        private final ModelPart cabeça;
        private final ModelPart braçoDireito;
        private final ModelPart braçoEsquerdo;
        private final ModelPart pernaDireita;
        private final ModelPart pernaEsquerda;

        public Modelo(ModelPart raiz) {
            super(raiz);
            this.cabeça = raiz.getChild("head");
            this.braçoDireito = raiz.getChild("rightarm");
            this.braçoEsquerdo = raiz.getChild("leftarm");
            this.pernaDireita = raiz.getChild("rightleg");
            this.pernaEsquerda = raiz.getChild("leftleg");
        }

        @Override
        public void setupAnim(LivingEntityRenderState estado) {
            super.setupAnim(estado);
            this.cabeça.yRot = estado.yRot * Mth.DEG_TO_RAD;
            this.cabeça.xRot = estado.xRot * Mth.DEG_TO_RAD;

            float passo = estado.walkAnimationPos * COMPASSO;
            float quanto = estado.walkAnimationSpeed;
            // o braço direito anda, mas sempre com a foice ao ombro: um quarto de volta para cima
            this.braçoDireito.xRot = Mth.cos(passo + Mth.PI) * BRAÇO_ABRE * quanto * 0.5f - Mth.HALF_PI;
            this.braçoEsquerdo.xRot = Mth.cos(passo) * BRAÇO_ABRE * quanto * 0.5f;
            this.pernaDireita.xRot = Mth.cos(passo) * PERNA_ABRE * quanto;
            this.pernaEsquerda.xRot = Mth.cos(passo + Mth.PI) * PERNA_ABRE * quanto;

            float idade = estado.ageInTicks;
            this.braçoDireito.zRot = Mth.cos(idade * BALANÇO_OMBRO) * BALANÇO + BALANÇO;
            this.braçoEsquerdo.zRot = -(Mth.cos(idade * BALANÇO_OMBRO) * BALANÇO + BALANÇO);
            this.braçoDireito.xRot += Mth.sin(idade * BALANÇO_BRAÇO) * BALANÇO;
            this.braçoEsquerdo.xRot -= Mth.sin(idade * BALANÇO_BRAÇO) * BALANÇO;
        }
    }

    /** E o desenhista. */
    public static class Morte extends MobRenderer<DeathEntity, LivingEntityRenderState, Modelo> {
        public Morte(EntityRendererProvider.Context contexto) {
            super(contexto, new Modelo(contexto.bakeLayer(LAYER)), 0.5f);
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState estado) {
            return FOLHA;
        }
    }
}
